package com.androidandyuk.autobuddy;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.androidandyuk.autobuddy.MainActivity.hotspotLocations;

/**
 * Created by AndyCr15 on 17/02/2018.
 */

public class LoadHotSpots extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        try {
            Log.i("HotSpots", "doInBackground");
            hotspotLocations.clear();
            String NewsData;
            //define the url we have to connect with
            URL url = new URL(params[0]);
            //make connect with url and send request
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //waiting for 7000ms for response
            urlConnection.setConnectTimeout(15000);//set timeout to 15 seconds

            try {
                //getting the response data
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                //convert the stream to string
                NewsData = ConvertInputToStringNoChange(in);
                //send to display data
                publishProgress(NewsData);
            } finally {
                //end connection
                urlConnection.disconnect();
            }

        } catch (Exception ex) {
            Log.i("HotSpots", "doInBackground Exception");
        }
        return null;
    }

    protected void onProgressUpdate(String... progress) {
        try {
            Log.i("HotSpots", "Getting JSON");
            JSONArray json = new JSONArray(progress[0]);
            Log.i("JSON size", "" + json.length());

            for (int i = 0; i < json.length(); i++) {
                JSONObject thisShow = json.getJSONObject(i);
                String name = thisShow.getString("name");
                LatLng location = new LatLng(thisShow.getDouble("lat"), thisShow.getDouble("lon"));
                String comment = thisShow.getString("comment");
                hotspotLocations.add(new markedLocation(name, location, comment));
                Log.i("Adding HotSpot ", name);
            }
        } catch (Exception ex) {
            Log.i("JSON failed", "" + ex);
        }
    }


    // this method convert any stream to string
    public static String ConvertInputToStringNoChange(InputStream inputStream) {

        BufferedReader bureader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String linereultcal = "";

        try {
            while ((line = bureader.readLine()) != null) {

                linereultcal += line;

            }
            inputStream.close();


        } catch (Exception ex) {
        }

        return linereultcal;
    }
}
