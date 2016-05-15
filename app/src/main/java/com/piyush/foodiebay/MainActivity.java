package com.piyush.foodiebay;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.piyush.foodiebay.network.ApiService;
import com.piyush.foodiebay.network.HttpServiceGenerator;
import com.piyush.foodiebay.network.models.FoodFacility;
import com.piyush.foodiebay.network.models.MyLocationItem;
import com.piyush.foodiebay.utils.Constants;
import com.piyush.foodiebay.utils.CustomMapsClusterRendering;
import com.piyush.foodiebay.utils.ErrorMessages;
import com.piyush.foodiebay.utils.NetworkUtils;
import com.piyush.foodiebay.utils.Notify;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 13/05/16.
 */
public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private String TAG = "MainActivity";

    private GoogleMap mGoogleMap;
    private ClusterManager<MyLocationItem> mClusterManager;

    private Toolbar toolbar;
    private ImageView filterIcon;
    private Spinner filterSpinner;

    private ApiService apiService;
    private Call<ArrayList<FoodFacility>> getFoodFacilitiesCall;
    private ArrayList<FoodFacility> foodFacilities = new ArrayList<>();
    private List<String> foodFacilityTypes = new ArrayList<>();

    private ArrayAdapter filterSpinnerAdapter;

    private String selectedFacilityType = Constants.FACILITY_TYPE_ALL;

    public static final int NETWORK_CALL_DATA_MAX_LIMIT = 1000;
    private boolean isRefreshIconVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the toolbar with title specified
        initToolbar("FoodieBay");

        //Initialize Layout views
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        filterSpinner = (Spinner) findViewById(R.id.main_filter_spinner);
        filterIcon = (ImageView) findViewById(R.id.main_filter_icon);
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSpinner.performClick();
            }
        });

        // Add a default Filter "All types"
        foodFacilityTypes.add(Constants.FACILITY_TYPE_ALL);

        //Initialize Filter Layout Spinner Adapter
        filterSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, foodFacilityTypes);
        filterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterSpinnerAdapter);

        // Get Map object
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);

        //Fetch food facilities data from Server
        fetchFoodFacilitiesData();
    }

    public void fetchFoodFacilitiesData() {
        displayLoader(true);
        isRefreshIconVisible = false;
        invalidateOptionsMenu();

        apiService = HttpServiceGenerator.generate(this, ApiService.class);
        getFoodFacilitiesCall = apiService.getFoodFacilities(NETWORK_CALL_DATA_MAX_LIMIT, null);

        getFoodFacilitiesCall.enqueue(new Callback<ArrayList<FoodFacility>>() {
            @Override
            public void onResponse(Call<ArrayList<FoodFacility>> call,
                                   Response<ArrayList<FoodFacility>> response) {

                if (NetworkUtils.isCallSuccess(MainActivity.this, response)) {

                    foodFacilities = response.body();
                    //Process the retrieved data to be shown on map
                    displayFetchedFoodFacilities();
                }

                // No Data available error message
                if (foodFacilities == null || foodFacilities.size() == 0) {
                    Notify.error(MainActivity.this, Notify.TYPE_DIALOG_NO_TITLE,
                            ErrorMessages.NO_DATA_AVAILABLE_MSG);

                    // Hide loader in case no data was fetched from server
                    displayLoader(false);
                    isRefreshIconVisible = true;
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<FoodFacility>> call, Throwable t) {
                Log.e("MainActivity", t.getMessage());
                displayLoader(false);
                isRefreshIconVisible = true;
                invalidateOptionsMenu();

                //Handle Network Call Failure
                NetworkUtils.handleCallFailure(MainActivity.this, t);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Enable MyLocationButton on Map only if Location Permission has been granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            mGoogleMap.setMyLocationEnabled(true);
        }

        // Initiate the map with a default position
        LatLng defaultLatLng = new LatLng(Constants.MAP_DEFAULT_LATITUDE_SAN_FRANCISCO,
                Constants.MAP_DEFAULT_LONGITUDE_SAN_FRANCISCO);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 12.0f));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(this, mGoogleMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mClusterManager.onCameraChange(cameraPosition);

                //LatLng Bounds of Current Visible Screen on Map Change
                LatLngBounds currScreen = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                Log.d(TAG, "Northeast lat, lng: " + currScreen.northeast.latitude + ", " + currScreen.northeast.longitude);
                Log.d(TAG, "Southwest lat, lng: " + currScreen.southwest.latitude + ", " + currScreen.southwest.longitude);
            }
        });
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        //Set Custom Rendering
        mClusterManager.setRenderer(new CustomMapsClusterRendering(getApplicationContext(),
                mGoogleMap, mClusterManager));

        //LatLng Bounds of Current Visible Screen
        LatLngBounds currScreen = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
        Log.d(TAG, "Northeast lat, lng: " + currScreen.northeast.latitude + ", " + currScreen.northeast.longitude);
        Log.d(TAG, "Southwest lat, lng: " + currScreen.southwest.latitude + ", " + currScreen.southwest.longitude);

        // Add cluster items (markers) to the cluster manager.
        displayFetchedFoodFacilities();
    }

    private void displayFetchedFoodFacilities() {
        if (mGoogleMap != null) {

            if (foodFacilities != null && foodFacilities.size() > 0) {

                // Clear the already present Clusters
                mClusterManager.clearItems();

                //Parse dataset to retrieve Cluster MyLocationItem List
                List<MyLocationItem> itemList = parseFoodFacilitiesList(foodFacilities, selectedFacilityType, true);
                mClusterManager.addItems(itemList);
                mClusterManager.cluster();

                //Initialize Filter Layout Spinner Adapter with Updated List
                filterSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, foodFacilityTypes);
                filterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                filterSpinner.setAdapter(filterSpinnerAdapter);

                filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        //Filter the dataset, in case filter type selected is different
                        if (!selectedFacilityType.equalsIgnoreCase(foodFacilityTypes.get(position))) {
                            selectedFacilityType = foodFacilityTypes.get(position);
                            filterFoodFacilities(selectedFacilityType);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                // Hide loader after both map is Ready and data has been fetched
                displayLoader(false);
                isRefreshIconVisible = true;
                invalidateOptionsMenu();
            }
        }
    }

    /**
     * Method to filter the dataset on the basis of selectedFacilityType.
     * @param selectedFacilityType The Type of Facility selected to be filtered.
     */
    private void filterFoodFacilities(String selectedFacilityType) {
        displayLoader(true);
        isRefreshIconVisible = false;
        invalidateOptionsMenu();

        // Clear the already present Clusters
        mClusterManager.clearItems();

        // Repopulate the ClusterManager with filtered data
        List<MyLocationItem> itemList = parseFoodFacilitiesList(foodFacilities, selectedFacilityType, false);
        mClusterManager.addItems(itemList);
        mClusterManager.cluster();

        displayLoader(false);
        isRefreshIconVisible = true;
        invalidateOptionsMenu();
    }

    /**
     * Method to parse the dataset fetched from server in order to retrieve MyLocationItem List reqd
     * to populate clusters on map.
     * @param foodFacilities The raw dataset fetched from Server.
     * @param foodFacilityType The type of Food Facility to be used as filter during parsing.
     * @param getFoodFacilitiesType Flag to repopulate the FoodFacilityTypes List.
     * @return
     */
    private List<MyLocationItem> parseFoodFacilitiesList(List<FoodFacility> foodFacilities, String foodFacilityType,
                                                         boolean getFoodFacilitiesType) {

        List<MyLocationItem> itemList = new ArrayList<>();

        // Loop over the entire dataset last fetched from server
        for (FoodFacility facility : foodFacilities) {

            // Ignore facilities without Location Data (cannot be represented on Map)
            if (facility.getLatitude() != 0.0 && facility.getLongitude() != 0.0) {

                // Parse and populate the entire dataset as No Filter applied
                if (Constants.FACILITY_TYPE_ALL.equalsIgnoreCase(foodFacilityType)) {

                    itemList.add(new MyLocationItem(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin),
                            facility.getMarkerTitle(), facility.getAddress(), facility.getLatitude(),
                            facility.getLongitude()));

                    if (getFoodFacilitiesType) {
                        // Add facilityType to the foodFacilityTypes List, if not already present
                        if (facility.getFacilityType() != null && !foodFacilityTypes.contains(facility.getFacilityType())) {
                            foodFacilityTypes.add(facility.getFacilityType());
                        }
                    }

                    // Parse and populate only the specific data according to the current Filter applied.
                } else if (foodFacilityType.equalsIgnoreCase(facility.getFacilityType())) {

                    itemList.add(new MyLocationItem(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin),
                            facility.getMarkerTitle(), facility.getAddress(), facility.getLatitude(),
                            facility.getLongitude()));
                }
            }
        }

        return itemList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.menu_main);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_data:

                //Fetch food facilities data from Server
                fetchFoodFacilitiesData();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.getItem(0).setVisible(isRefreshIconVisible);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Cancel the Network Call, if not yet executed
        if (getFoodFacilitiesCall != null) {
            getFoodFacilitiesCall.cancel();
        }
    }
}
