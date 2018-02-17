package com.androidandyuk.autobuddy;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static com.androidandyuk.autobuddy.CarShows.carShows;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;

/**
 * Created by AndyCr15 on 17/02/2018.
 */

public class LoadShows extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objects) {
        carShows.clear();

        Log.i("Loading", "Shows");

        ArrayList<String> csName = new ArrayList<>();
        ArrayList<String> csComment = new ArrayList<>();
        ArrayList<String> csAddress = new ArrayList<>();
        ArrayList<String> csUrl = new ArrayList<>();
        ArrayList<String> csStart = new ArrayList<>();
        ArrayList<String> csEnd = new ArrayList<>();
        ArrayList<String> csLat = new ArrayList<>();
        ArrayList<String> csLon = new ArrayList<>();

        // I think these are new variables, so likely don't need clearing?
        csName.clear();
        csComment.clear();
        csAddress.clear();
        csUrl.clear();
        csStart.clear();
        csEnd.clear();
        csLat.clear();
        csLon.clear();

        try {

            csName = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csName", ObjectSerializer.serialize(new ArrayList<String>())));
            csComment = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csComment", ObjectSerializer.serialize(new ArrayList<String>())));
            csAddress = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csAddress", ObjectSerializer.serialize(new ArrayList<String>())));
            csUrl = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csUrl", ObjectSerializer.serialize(new ArrayList<String>())));
            csStart = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csStart", ObjectSerializer.serialize(new ArrayList<String>())));
            csEnd = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csEnd", ObjectSerializer.serialize(new ArrayList<String>())));
            csLat = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csLat", ObjectSerializer.serialize(new ArrayList<String>())));
            csLon = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("csLon", ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Loading Shows", "Failed attempt");
        }

        if (csName.size() > 0 && csComment.size() > 0 && csAddress.size() > 0 && csUrl.size() > 0) {
            // we've checked there is some info
            if (csName.size() == csComment.size() && csComment.size() == csAddress.size() && csAddress.size() == csUrl.size()) {
                // we've checked each item has the same amount of info, nothing is missing
                for (int x = 0; x < csName.size(); x++) {
                    Double lat = Double.parseDouble(csLat.get(x));
                    Double lon = Double.parseDouble(csLon.get(x));
                    LatLng thisLocation = new LatLng(lat, lon);
                    carShows.add(new markedLocation(csName.get(x), thisLocation, csAddress.get(x), csComment.get(x), csStart.get(x), csEnd.get(x), csUrl.get(x)));
                }
            }
        }

        return null;
    }
}
