package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;

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
import java.util.List;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.jsonLocation;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;

public class RaceTracks extends AppCompatActivity {
    static List<markedLocation> trackLocations = new ArrayList<>();
    static MyLocationAdapter myAdapter;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static LinearLayout main;
    ListView listView;

    private SwipeRefreshLayout swipeContainer;

    //private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race_tracks);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Log.i("Race Tracks", "onCreate");

        listView = (ListView) findViewById(R.id.listTracks);

        myAdapter = new MyLocationAdapter(trackLocations);

        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), LocationInfoActivity.class);
                intent.putExtra("placeNumber", i);
                intent.putExtra("Type", "Track");

                startActivity(intent);
            }

        });

        Log.i("trackLocations","Size " + trackLocations.size());
        if(trackLocations.size()<1) {
            Toast.makeText(this, "Currently loading tracks. Come back in a few seconds.", Toast.LENGTH_LONG).show();
        }

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                //fetchTimelineAsync(0);
                new MyAsyncTaskgetNews().execute(jsonLocation + "racetracks.json");
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

//        Long lastUpdated = Long.parseLong(sharedPreferences.getString("tracksUpdated", "0"));
//        Long sinceUpdated = (System.currentTimeMillis() - lastUpdated)/1000;
//        if(trackLocations.size() == 0 || sinceUpdated > 5200000) {
//            new MyAsyncTaskgetNews().execute(jsonLocation + "racetracks.json");
//        }

    }

    public void viewTracks(View view) {
        Log.i("View Race Tracks", "called");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        // 9998 tells the Maps activity to show all the markers
        intent.putExtra("placeNumber", 9998);
        intent.putExtra("Type", "Track");
        startActivity(intent);
    }

    public void sortMyList() {
        Log.i("Sort List", "" + trackLocations.size());
        if (trackLocations.size() > 0) {
            Collections.sort(trackLocations);
            myAdapter.notifyDataSetChanged();
        }

    }

    public class MyLocationAdapter extends BaseAdapter {
        public List<markedLocation> locationDataAdapter;

        public MyLocationAdapter(List<markedLocation> locationDataAdapter) {
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

            TextView milesKM = (TextView)myView.findViewById(R.id.milesKM);
            milesKM.setText(milesSetting);

            TextView locationListDistance = (TextView) myView.findViewById(R.id.locationListDistance);
            locationListDistance.setText(oneDecimal.format(s.distance));

            TextView locationListName = (TextView) myView.findViewById(R.id.locationListName);
            locationListName.setText(s.name);

            return myView;
        }

    }

    public void checkBackground() {
        main = (LinearLayout) findViewById(R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            RaceTracks.main.setBackground(drawablePic);
            listView.setBackground(getResources().getDrawable(R.drawable.rounded_corners_drkgrey_orange));
        } else {
            RaceTracks.main.setBackgroundColor(getResources().getColor(R.color.background));
            listView.setBackground(null);
        }
    }

    // get news from server
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
            //startLoading();
//            ImageView loading = (ImageView) findViewById(R.id.loadingShield);
//            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.i("RaceTracks", "doInBackground");
                trackLocations.clear();
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
                Log.i("RaceTracks", "doInBackground Exception");
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            try {
                Log.i("RaceTracks", "Getting JSON");
                JSONArray json = new JSONArray(progress[0]);
                Log.i("JSON size", "" + json.length());

                for (int i = 0; i < json.length(); i++) {
                    JSONObject thisShow = json.getJSONObject(i);
                    String name = thisShow.getString("name");
                    LatLng location = new LatLng(thisShow.getDouble("lat"), thisShow.getDouble("lon"));
                    String comment = thisShow.getString("comment");
                    trackLocations.add(new markedLocation(name, location, comment));
                    Log.i("Adding HotSpot ", name);
                }
            } catch (Exception ex) {
                Log.i("JSON failed", "" + ex);
            }
        }

        protected void onPostExecute(String result2) {
            sortMyList();
//            ImageView loading = (ImageView) findViewById(R.id.loadingShield);
//            loading.setVisibility(View.INVISIBLE);
            saveTracks();
            ed.putString("tracksUpdated", Long.toString(System.currentTimeMillis())).apply();
            swipeContainer.setRefreshing(false);
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

    public void saveTracks() {
        Log.i("Saving", "Tracks");
        try {

            ArrayList<String> trName = new ArrayList<>();
            ArrayList<String> trComment = new ArrayList<>();
            ArrayList<String> trLat = new ArrayList<>();
            ArrayList<String> trLon = new ArrayList<>();

            for (markedLocation thisTrack : trackLocations) {

                trName.add(thisTrack.name);
                trComment.add(thisTrack.comment);
                trLat.add(Double.toString(thisTrack.location.latitude));
                trLon.add(Double.toString(thisTrack.location.longitude));

            }

            Log.i("Saving Tracks", "Size :" + trName.size());
            ed.putString("trName", ObjectSerializer.serialize(trName)).apply();
            ed.putString("trComment", ObjectSerializer.serialize(trComment)).apply();
            ed.putString("trLat", ObjectSerializer.serialize(trLat)).apply();
            ed.putString("trLon", ObjectSerializer.serialize(trLon)).apply();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Adding Shows", "Failed attempt");
        }
    }

//    public void startLoading() {
//        pDialog = new ProgressDialog(RaceTracks.this);
//        pDialog.setMessage("Please wait...");
//        pDialog.setCancelable(false);
//        pDialog.show();
//    }
//
//    public void stopLoading() {
//        // Dismiss the progress dialog
//        if (pDialog.isShowing())
//            pDialog.dismiss();
//    }
    
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
        Log.i("Race Tracks Activity", "On Pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Race Tracks Activity", "On Resume");
        sortMyList();
        checkBackground();
    }
}


