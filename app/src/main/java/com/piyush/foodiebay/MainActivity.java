package com.piyush.foodiebay;

import android.os.Bundle;
import android.os.Handler;
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
    private LatLng northeast, southwest;

    private ImageView filterIcon;
    private Spinner filterSpinner;
    private ArrayAdapter filterSpinnerAdapter;

    private ApiService apiService;
    private Call<ArrayList<FoodFacility>> getFoodFacilitiesCall;
    private ArrayList<FoodFacility> foodFacilities = new ArrayList<>();
    private List<String> foodFacilityTypes = new ArrayList<>();
    private String selectedFacilityType = Constants.FACILITY_TYPE_ALL;

    public static final int NETWORK_CALL_DATA_MAX_LIMIT = 1000, NETWORK_CALL_INITIATE_DELAY = 1000;
    private boolean networkCallEnqueued = true;
    private Handler cameraChangeHandler;
    private Runnable cameraChangeRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the toolbar with title specified
        initToolbar();

        //Initialize Layout views
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
    }

    public void fetchFoodFacilitiesData(String whereClause) {
        displayLoader(true);
        displayRefreshIcon(false);

        apiService = HttpServiceGenerator.generate(this, ApiService.class);
        getFoodFacilitiesCall = apiService.getFoodFacilities(NETWORK_CALL_DATA_MAX_LIMIT, whereClause);

        getFoodFacilitiesCall.enqueue(new Callback<ArrayList<FoodFacility>>() {
            @Override
            public void onResponse(Call<ArrayList<FoodFacility>> call,
                                   Response<ArrayList<FoodFacility>> response) {

                if (NetworkUtils.isCallSuccess(response)) {

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
                    displayRefreshIcon(true);
                }
            }

            @Override
            public void onFailure(Call<ArrayList<FoodFacility>> call, Throwable t) {
                Log.e("MainActivity", t.getMessage());

                //Handle Network Call Failure
                NetworkUtils.handleCallFailure(MainActivity.this, t);

                displayLoader(false);
                displayRefreshIcon(true);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Initiate the map with a default position
        LatLng defaultLatLng = new LatLng(Constants.MAP_DEFAULT_LATITUDE_SAN_FRANCISCO,
                Constants.MAP_DEFAULT_LONGITUDE_SAN_FRANCISCO);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 14.0f));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(this, mGoogleMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mClusterManager.onCameraChange(cameraPosition);

                if (cameraChangeHandler != null && cameraChangeRunnable != null) {
                    cameraChangeHandler.removeCallbacks(cameraChangeRunnable);
                }

                cameraChangeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Get current visible screen LatLngBounds and food facility data for this area
                        getVisibleScreenBounds(mGoogleMap);
                    }
                };
                cameraChangeHandler = new Handler();
                cameraChangeHandler.postDelayed(cameraChangeRunnable, NETWORK_CALL_INITIATE_DELAY);
            }
        });
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        // Set Custom Rendering
        mClusterManager.setRenderer(new CustomMapsClusterRendering(getApplicationContext(),
                mGoogleMap, mClusterManager));
    }

    /**
     * Method to get current screen visible LatLngBounds from the map.
     *
     * @param mGoogleMap
     */
    private void getVisibleScreenBounds(GoogleMap mGoogleMap) {
        //LatLng Bounds of Current Visible Screen
        LatLngBounds currScreen = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
        if (currScreen != null) {

            northeast = currScreen.northeast;
            southwest = currScreen.southwest;

            if (northeast != null && southwest != null) {

                Log.d(TAG, "Northeast lat, lng: " + currScreen.northeast.latitude + ", " +
                        currScreen.northeast.longitude);
                Log.d(TAG, "Southwest lat, lng: " + currScreen.southwest.latitude + ", " +
                        currScreen.southwest.longitude);

                String whereClause = getQueryFromLatLng(currScreen.northeast, currScreen.southwest);

                //Fetch food facilities data from Server
                fetchFoodFacilitiesData(whereClause);
            }
        }
    }

    /**
     * Method to get query string from northeast and southwest LatLng coordinates of the map to fetch
     * data specific to the current visible screen.
     *
     * @param northeast LatLng coordinates of the NorthEast corner of the currently visible screen.
     * @param southwest LatLng coordinates of the SouthWest corner of the currently visible screen.
     * @return
     */
    private String getQueryFromLatLng(LatLng northeast, LatLng southwest) {

        StringBuilder builder = new StringBuilder();

        builder.append("latitude between \'")
                .append(southwest.latitude)
                .append("\' and \'")
                .append(northeast.latitude)
                .append("\' and longitude*-1 between \'")
                .append(northeast.longitude * -1)
                .append("\' and \'")
                .append(southwest.longitude * -1)
                .append("\'");

        return builder.toString();
    }

    /**
     * Method to display fetched data in form of Clusters on the map
     */
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
                displayRefreshIcon(true);
            }
        }
    }

    /**
     * Method to filter the dataset on the basis of selectedFacilityType.
     *
     * @param selectedFacilityType The Type of Facility selected to be filtered.
     */
    private void filterFoodFacilities(String selectedFacilityType) {
        displayLoader(true);
        displayRefreshIcon(false);

        // Clear the already present Clusters
        mClusterManager.clearItems();

        // Repopulate the ClusterManager with filtered data
        List<MyLocationItem> itemList = parseFoodFacilitiesList(foodFacilities, selectedFacilityType, false);
        mClusterManager.addItems(itemList);
        mClusterManager.cluster();

        // Hide loader after both map is Ready and data has been fetched
        displayLoader(false);
        displayRefreshIcon(true);
    }

    /**
     * Method to parse the dataset fetched from server in order to retrieve MyLocationItem List reqd
     * to populate clusters on map.
     *
     * @param foodFacilities        The raw dataset fetched from Server.
     * @param foodFacilityType      The type of Food Facility to be used as filter during parsing.
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

    /**
     * Method to show/hide RefreshIcon whenever network call has been completed
     *
     * @param showRefreshIcon Flag to indicate whether to show/hide the Refresh Icon
     */
    private void displayRefreshIcon(final boolean showRefreshIcon) {
        networkCallEnqueued = !showRefreshIcon;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getToolbar().inflateMenu(R.menu.menu_main);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_data:
                //Fetch food facilities data from Server
                fetchFoodFacilitiesData(null);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(!networkCallEnqueued);
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
