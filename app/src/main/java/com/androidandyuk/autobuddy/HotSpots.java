package com.androidandyuk.autobuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.jsonLocation;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;

public class HotSpots extends AppCompatActivity {
    static ArrayList<markedLocation> hotspotLocations = new ArrayList<>();
    static MyLocationAdapter myAdapter;

    public static RelativeLayout main;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Hot Spots", "onCreate");

        sharedPreferences = this.getSharedPreferences("com.androidandyuk.autobuddy", Context.MODE_PRIVATE);

        setContentView(R.layout.activity_hot_spots);
        listView = (ListView) findViewById(R.id.favsList);

        new MyAsyncTaskgetNews().execute(jsonLocation + "hotspots.json");

        myAdapter = new MyLocationAdapter(hotspotLocations);
        listView.setAdapter(myAdapter);

        sortMyList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), LocationInfoActivity.class);
                intent.putExtra("placeNumber", i);
                intent.putExtra("Type", "Hot");
                startActivity(intent);
            }
        });

        checkBackground();

    }

    public void addHotSpot(View view) {
        Log.i("Add Hot Spots", "");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("Type", "Hot");
        startActivity(intent);
    }

    public void viewHotSpots(View view) {
        Log.i("View Hot Spots", "called");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        // 9998 tells the Maps activity to show all the markers
        intent.putExtra("placeNumber", 9998);
        intent.putExtra("Type", "Hot");
        startActivity(intent);
    }

    public void sortMyList() {
        Log.i("Sort List", "" + hotspotLocations.size());
        if (hotspotLocations.size() > 0) {
            Collections.sort(hotspotLocations);
            myAdapter.notifyDataSetChanged();
        }

    }

    public class MyLocationAdapter extends BaseAdapter {
        public ArrayList<markedLocation> locationDataAdapter;

        public MyLocationAdapter(ArrayList<markedLocation> locationDataAdapter) {
            this.locationDataAdapter = locationDataAdapter;
        }

        @Override
        public int getCount() {
            return locationDataAdapter.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.location_listview, null);

            final markedLocation s = locationDataAdapter.get(position);

            TextView milesKM = (TextView) myView.findViewById(R.id.milesKM);
            milesKM.setText(milesSetting);

            TextView locationListDistance = (TextView) myView.findViewById(R.id.locationListDistance);
            locationListDistance.setText(oneDecimal.format(s.distance));

            TextView locationListName = (TextView) myView.findViewById(R.id.locationListName);
            locationListName.setText(s.name);

            return myView;
        }

    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            HotSpots.main.setBackground(drawablePic);
            listView.setBackground(getResources().getDrawable(R.drawable.rounded_corners_grey));
        } else {
            HotSpots.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    // get news from server
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
        }

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

        protected void onPostExecute(String result2) {
            sortMyList();
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

    public void initialiseLocations() {
        if (hotspotLocations.size() == 0) {
            hotspotLocations.add(new markedLocation("Ace Cafe", new LatLng(51.5412794, -0.2799549), "The world famous Ace Cafe. Food not the best though. Friday nights are always busy"));
            hotspotLocations.add(new markedLocation("High Beach", new LatLng(51.657176, 0.0349883), ""));
            hotspotLocations.add(new markedLocation("Ryka's Cafe", new LatLng(51.255562, -0.3243657), ""));
            hotspotLocations.add(new markedLocation("Loomies Cafe", new LatLng(51.030443, -1.0779103), "Great roads lead to it. Nice burger once you get there!"));
            hotspotLocations.add(new markedLocation("H Cafe", new LatLng(51.658486, -1.1781097), ""));
            hotspotLocations.add(new markedLocation("On Yer Bike", new LatLng(51.854932, -0.968651), ""));
            hotspotLocations.add(new markedLocation("Revved Up", new LatLng(51.8500038, 1.274296), ""));
            hotspotLocations.add(new markedLocation("The Midway Truck Stop", new LatLng(52.9373479, -2.6643152), ""));
            hotspotLocations.add(new markedLocation("Finchingfield", new LatLng(51.96829, 0.4480183), "Beautiful scenery. Surrounded by great rounds."));
            hotspotLocations.add(new markedLocation("Bike Shed", new LatLng(51.527171, -0.0805737), "Own parking, often with security. Food can be pricey."));
            hotspotLocations.add(new markedLocation("Hartside Cafe", new LatLng(54.6360254, -2.5316498), ""));
        }
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Hot Spot Activity", "On Pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackground();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Hot Spots", "On Stop");
    }
}
