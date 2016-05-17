package com.piyush.foodiebay.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by piyush on 13/05/16.
 */
public class Location {

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("coordinates")
    @Expose
    private List<Double> coordinates;
}
