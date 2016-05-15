package com.piyush.foodiebay.utils;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.piyush.foodiebay.network.models.MyLocationItem;

/**
 * Created by piyush on 14/05/16.
 */
public class CustomMapsClusterRendering extends DefaultClusterRenderer<MyLocationItem> {

    public CustomMapsClusterRendering(Context context, GoogleMap map,
                                      ClusterManager<MyLocationItem> clusterManager) {
        super(context, map, clusterManager);
    }

    protected void onBeforeClusterItemRendered(MyLocationItem item, MarkerOptions markerOptions) {

        markerOptions.icon(item.getIcon());
        markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
