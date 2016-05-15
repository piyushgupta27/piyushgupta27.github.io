package com.piyush.foodiebay.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 13/05/16.
 */
public class FoodFacility {

    @SerializedName("address")
    @Expose
    private String address;

    @SerializedName("applicant")
    @Expose
    private String applicant;

    @SerializedName("approved")
    @Expose
    private String approved;

    @SerializedName("block")
    @Expose
    private String block;

    @SerializedName("blocklot")
    @Expose
    private String blockLot;

    @SerializedName("cnn")
    @Expose
    private int cnn;

    @SerializedName("dayshours")
    @Expose
    private String daysHours;

    @SerializedName("expirationdate")
    @Expose
    private String expirationDate;

    @SerializedName("facilitytype")
    @Expose
    private String facilityType;

    @SerializedName("fooditems")
    @Expose
    private String foodItems;

    @SerializedName("latitude")
    @Expose
    private Double latitude;

    @SerializedName("longitude")
    @Expose
    private Double longitude;

    @SerializedName("location")
    @Expose
    private Location location;

    @SerializedName("locationdescription")
    @Expose
    private String locationDescription;

    @SerializedName("lot")
    @Expose
    private String lot;

    @SerializedName("objectid")
    @Expose
    private int objectId;

    @SerializedName("permit")
    @Expose
    private String permit;

    @SerializedName("priorpermit")
    @Expose
    private int priorPermit;

    @SerializedName("received")
    @Expose
    private String received;

    @SerializedName("schedule")
    @Expose
    private String schedule;

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("x")
    @Expose
    private Double x;

    @SerializedName("y")
    @Expose
    private Double y;

    public String getAddress() {
        return address;
    }

    public String getFacilityType() {
        return facilityType;
    }

    public String getMarkerTitle() {
        StringBuilder builder = new StringBuilder();

        if (facilityType != null && facilityType.length() > 0) {
            builder.append(facilityType)
                    .append(": ");
        }

        builder.append(applicant);

        return builder.toString();
    }

    public Double getLatitude() {
        return latitude == null ? 0.0 : latitude;
    }

    public Double getLongitude() {
        return longitude == null ? 0.0 : longitude;
    }
}