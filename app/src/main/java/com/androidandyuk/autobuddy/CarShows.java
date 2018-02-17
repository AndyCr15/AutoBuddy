package com.androidandyuk.autobuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.incBikeEvents;
import static com.androidandyuk.autobuddy.MainActivity.incCarEvents;
import static com.androidandyuk.autobuddy.MainActivity.jsonLocation;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.sdf;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;

public class CarShows extends AppCompatActivity {
    static List<markedLocation> carShows = new ArrayList<>();
    static MyLocationAdapter myAdapter;

    boolean updatedRecently = false;
    // Sort Order - True = done by nearest
    public static boolean sortShowsOrder = true;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static LinearLayout main;

    private SwipeRefreshLayout swipeContainer;

    private ProgressDialog pDialog;

    ListView listView;

    String carUrl;
    String bikeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_shows);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Log.i("Car Shows", "onCreate");

        pDialog = new ProgressDialog(CarShows.this);

        listView = (ListView) findViewById(R.id.listShows);

        myAdapter = new MyLocationAdapter(carShows);

        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), LocationInfoActivity.class);
                intent.putExtra("placeNumber", i);
                intent.putExtra("Type", "Show");

                startActivity(intent);
            }

        });

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
                updateShows();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        // the location of the shows xml file
        carUrl = jsonLocation + "carshows.json";
        bikeUrl = jsonLocation + "bikeshows.json";

        // check if shows have been loaded already
        if (!updatedRecently) {
            updateShows();
        }

        checkBackground();
    }

    public void toggleSortOrder(View view) {
        sortShowsOrder = !sortShowsOrder;
        if (sortShowsOrder) {
            Toast.makeText(this, "Sorting by locality", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sorting soonest first", Toast.LENGTH_SHORT).show();
        }
        sortMyList();
    }

    public void updateShows() {
        updatedRecently = true;
        carShows.clear();
        // get the data
        if (incCarEvents) {
            new MyAsyncTaskgetNews().execute(carUrl);
        }
        if (incBikeEvents) {
            new MyAsyncTaskgetNews().execute(bikeUrl);
        }
        saveShows();

    }

    public void checkBackground() {
        main = (LinearLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            CarShows.main.setBackground(drawablePic);
            listView.setBackground(getResources().getDrawable(R.drawable.rounded_corners_drkgrey_orange));
        } else {
            CarShows.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    // get news from server
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
            if (!pDialog.isShowing()) {
                startLoading();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.i("Car Shows", "doInBackground");
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
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            try {
                Log.i("Car Shows", "Getting JSON");
                JSONArray json = new JSONArray(progress[0]);
                Log.i("JSON size", "" + json.length());

                for (int i = 0; i < json.length(); i++) {
                    JSONObject thisShow = json.getJSONObject(i);
                    String name = thisShow.getString("name");
                    String comment = thisShow.getString("comment");
                    String address = thisShow.getString("address");
                    String url = thisShow.getString("url");
                    String start = thisShow.getString("start");
                    String end = thisShow.getString("end");
                    LatLng location = new LatLng(thisShow.getDouble("lat"), thisShow.getDouble("lon"));
                    carShows.add(new markedLocation(name, location, address, comment, start, end, url));
                    Log.i("Adding show ", name);
                }
            } catch (Exception ex) {
                Log.i("JSON failed", "" + ex);
            }
        }

        protected void onPostExecute(String result2) {
            sortMyList();
            stopLoading();
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

            TextView milesKM = myView.findViewById(R.id.milesKM);
            milesKM.setText(milesSetting);

            TextView locationListDistance = myView.findViewById(R.id.locationListDistance);
            locationListDistance.setText(oneDecimal.format(s.distance));

            TextView locationListName = myView.findViewById(R.id.locationListName);
            locationListName.setText(s.name);

            return myView;
        }

    }

    public void suggestShow(View view) {

        // save logcat in file
        File outputFile = new File(Environment.getExternalStorageDirectory(),
                "logcat.txt");
        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // Set type to "email"
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"AndyCr15@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, outputFile.getAbsolutePath());
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BBF Suggest a show");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public void viewShows(View view) {
        Log.i("View Shows", "called");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        // 9998 tells the Maps activity to show all the markers
        intent.putExtra("placeNumber", 9998);
        intent.putExtra("Type", "Shows");
        startActivity(intent);
    }


    public void sortMyList() {
        Log.i("Sort Shows", "" + carShows.size());
        if (carShows.size() > 0) {

            // remove any events that have passed
            Date today = new Date();
            Date thisShowEnd = new Date();
            for (int i = 0; i < carShows.size(); i++) {

                try {
                    thisShowEnd = sdf.parse(carShows.get(i).end);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (today.after(thisShowEnd)) {
                    carShows.remove(i);
                    // A show has been removed, so go back one on the counter
                    i--;
                }
            }

            if (sortShowsOrder) {
                Collections.sort(carShows);
            } else {
                Collections.sort(carShows, new CustomComparator());
            }


            myAdapter.notifyDataSetChanged();
        }
    }

    public void saveShows() {
        Log.i("Saving", "Shows");
        try {

            ArrayList<String> csName = new ArrayList<>();
            ArrayList<String> csComment = new ArrayList<>();
            ArrayList<String> csAddress = new ArrayList<>();
            ArrayList<String> csUrl = new ArrayList<>();
            ArrayList<String> csStart = new ArrayList<>();
            ArrayList<String> csEnd = new ArrayList<>();
            ArrayList<String> csLat = new ArrayList<>();
            ArrayList<String> csLon = new ArrayList<>();

            for (markedLocation thisShow : carShows) {

                csName.add(thisShow.name);
                csComment.add(thisShow.comment);
                csAddress.add(thisShow.address);
                csUrl.add(thisShow.url);
                csStart.add(thisShow.start);
                csEnd.add(thisShow.end);
                csLat.add(Double.toString(thisShow.location.latitude));
                csLon.add(Double.toString(thisShow.location.longitude));

            }

            Log.i("Saving Shows", "Size :" + csName.size());
            ed.putString("csName", ObjectSerializer.serialize(csName)).apply();
            ed.putString("csComment", ObjectSerializer.serialize(csComment)).apply();
            ed.putString("csAddress", ObjectSerializer.serialize(csAddress)).apply();
            ed.putString("csUrl", ObjectSerializer.serialize(csUrl)).apply();
            ed.putString("csStart", ObjectSerializer.serialize(csStart)).apply();
            ed.putString("csEnd", ObjectSerializer.serialize(csEnd)).apply();
            ed.putString("csLat", ObjectSerializer.serialize(csLat)).apply();
            ed.putString("csLon", ObjectSerializer.serialize(csLon)).apply();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Adding Shows", "Failed attempt");
        }
    }

    public static void loadShows() {

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
    }

    public void startLoading() {

        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void stopLoading() {
        // Dismiss the progress dialog
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackground();
    }
}
