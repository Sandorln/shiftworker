package com.kinggo.app.shiftworker.view.community.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author sangsik.kim
 */
public class CommunityApiService {
    private static final String API_BASE_URL = "https://shiftworker.xyz/";

    private static Retrofit retrofit;
    private static Gson gson;

    private CommunityApiService() {
    }

    public static CommunityApi getInstance() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setLenient()
                    .create();
        }

        if (retrofit == null) {
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(CommunityApi.class);
    }
}
