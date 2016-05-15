package com.piyush.foodiebay.network.models;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.piyush.foodiebay.R;

/**
 * Created by piyush on 14/05/16.
 */
public class MyLocationItem implements ClusterItem {

    private final LatLng mPosition;
    private BitmapDescriptor icon;
    private String title;
    private String snippet;

    public MyLocationItem(BitmapDescriptor icon, String title, String snippet, double lat, double lng) {
        mPosition = new LatLng(lat, lng);
        this.icon = icon;
        this.title = title;
        this.snippet = snippet;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
