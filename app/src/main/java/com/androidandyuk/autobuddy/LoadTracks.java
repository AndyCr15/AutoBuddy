package com.androidandyuk.autobuddy;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;
import static com.androidandyuk.autobuddy.RaceTracks.trackLocations;

/**
 * Created by AndyCr15 on 17/02/2018.
 */

public class LoadTracks extends AsyncTask {
    @Override
    protected Void doInBackground(Object... objects) {
        trackLocations.clear();

        Log.i("LoadTracks", "doInBackground");

        ArrayList<String> trName = new ArrayList<>();
        ArrayList<String> trComment = new ArrayList<>();
        ArrayList<String> trLat = new ArrayList<>();
        ArrayList<String> trLon = new ArrayList<>();

        try {

            trName = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("trName", ObjectSerializer.serialize(new ArrayList<String>())));
            trComment = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("trComment", ObjectSerializer.serialize(new ArrayList<String>())));
            trLat = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("trLat", ObjectSerializer.serialize(new ArrayList<String>())));
            trLon = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("trLon", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Loading Tracks", "Failed attempt");
        }

        if (trName.size() > 0 && trComment.size() > 0 && trLat.size() > 0) {
            // we've checked there is some info
            if (trName.size() == trComment.size() && trComment.size() == trLat.size()) {
                // we've checked each item has the same amount of info, nothing is missing
                for (int x = 0; x < trName.size(); x++) {
                    Double lat = Double.parseDouble(trLat.get(x));
                    Double lon = Double.parseDouble(trLon.get(x));
                    LatLng thisLocation = new LatLng(lat, lon);
                    trackLocations.add(new markedLocation(trName.get(x), thisLocation, trComment.get(x)));
                }
            }
        }
//        pDialog.dismiss();

        return null;
    }
}

