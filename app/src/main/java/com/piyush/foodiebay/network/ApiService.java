package com.piyush.foodiebay.network;

import com.piyush.foodiebay.network.models.FoodFacility;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by piyush on 13/05/16.
 */
public interface ApiService {

    @GET("https://data.sfgov.org/resource/6a9r-agq8.json/")
    Call<ArrayList<FoodFacility>> getFoodFacilities(@Query("$limit") final int limit,
                                                    @Query("$where") final String whereClause);
}
