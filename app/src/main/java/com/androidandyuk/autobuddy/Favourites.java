package com.androidandyuk.autobuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;

public class Favourites extends AppCompatActivity {
    static ArrayList<markedLocation> favouriteLocations = new ArrayList<>();
    static MyLocationAdapter myAdapter;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static RelativeLayout main;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("Favourites", "onCreate");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sharedPreferences = this.getSharedPreferences("com.androidandyuk.autobuddy", Context.MODE_PRIVATE);

        loadFavs();

        setContentView(com.androidandyuk.autobuddy.R.layout.activity_favourites);

        listView = (ListView) findViewById(com.androidandyuk.autobuddy.R.id.favsList);

        Button seeFavsMap = (Button) findViewById(com.androidandyuk.autobuddy.R.id.seeFavsMap);

        seeFavsMap.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // add some kind of check (boolean that means two pressed?)
                Log.i("Favourites", "Reset List");
                // code to rest here

                // draw the list again
                return true;
            }
        });

        myAdapter = new MyLocationAdapter(favouriteLocations);

        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), LocationInfoActivity.class);
                intent.putExtra("placeNumber", i);
                intent.putExtra("Type", "Fav");

                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Removing", "" + favouriteLocations.get(position));


                final int favPosition = position;
                final Context context = App.getContext();

                new AlertDialog.Builder(Favourites.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("You're about to remove this favourite forever...")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("Removing", "Location");
                                favouriteLocations.remove(favPosition);
                                Favourites.myAdapter.notifyDataSetChanged();
                                Toast.makeText(context,"Removed!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }

        });

        checkBackground();
    }

    public void addFav(View view) {
        Log.i("Add Favs", "");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("Type", "Fav");
        startActivity(intent);
    }

    public void viewFavourites(View view) {
        Log.i("View Favs", "called");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        // 9998 tells the Maps activity to show all the markers
        intent.putExtra("placeNumber", 9998);
        intent.putExtra("Type", "Fav");
        startActivity(intent);
    }

    public static void sortMyList() {
        Log.i("Sort List", "" + favouriteLocations.size());
        if (favouriteLocations.size() > 0) {
            Collections.sort(favouriteLocations);
//            myAdapter.notifyDataSetChanged();
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
            View myView = mInflater.inflate(com.androidandyuk.autobuddy.R.layout.location_listview, null);

            final markedLocation s = locationDataAdapter.get(position);

            TextView milesKM = (TextView)myView.findViewById(com.androidandyuk.autobuddy.R.id.milesKM);
            milesKM.setText(milesSetting);

            TextView locationListDistance = (TextView) myView.findViewById(com.androidandyuk.autobuddy.R.id.locationListDistance);
            locationListDistance.setText(oneDecimal.format(s.distance));

            TextView locationListName = (TextView) myView.findViewById(com.androidandyuk.autobuddy.R.id.locationListName);
            locationListName.setText(s.name);

            return myView;
        }

    }

    public void checkBackground() {
        Log.i("backgroundsWanted","" + backgroundsWanted);
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Favourites.main.setBackground(drawablePic);
            listView.setBackground(getResources().getDrawable(R.drawable.rounded_corners_grey));
        } else {
            Favourites.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
            listView.setBackground(null);
        }
    }

    public static void saveFavs() {
        Log.i("Shared Prefs", "Saving Favs");
        try {

            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();
            ArrayList<String> addresses = new ArrayList<>();
            ArrayList<String> comments = new ArrayList<>();

            for (markedLocation location : favouriteLocations) {
                names.add(location.name);
                latitudes.add(Double.toString(location.location.latitude));
                longitudes.add(Double.toString(location.location.longitude));
                addresses.add(location.address);
                comments.add(location.comment);
            }

            sharedPreferences.edit().putString("names", ObjectSerializer.serialize(names)).apply();
            sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();
            sharedPreferences.edit().putString("addresses", ObjectSerializer.serialize(addresses)).apply();
            sharedPreferences.edit().putString("comments", ObjectSerializer.serialize(comments)).apply();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Adding Favs", "Failed attempt");
        }
    }

    public static void loadFavs() {
        Log.i("Shared Prefs", "Loading Favs");

        favouriteLocations.clear();

        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();
        ArrayList<String> addresses = new ArrayList<>();
        ArrayList<String> comments = new ArrayList<>();

        names.clear();
        latitudes.clear();
        longitudes.clear();
        addresses.clear();
        comments.clear();

        try {

            names = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("names", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<String>())));
            addresses = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("addresses", ObjectSerializer.serialize(new ArrayList<String>())));
            comments = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("comments", ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (names.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0) {
            // we've checked there is some info
            if (names.size() == latitudes.size() && latitudes.size() == longitudes.size()) {
                // we've checked each item has the same amout of info, nothing is missing

                for (int i = 0; i < names.size(); i++) {
                    LatLng pos = new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i)));
                    markedLocation newLoc = new markedLocation(names.get(i), addresses.get(i), pos, comments.get(i));
                    favouriteLocations.add(newLoc);
                }

            }
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
        Log.i("Favs Activity", "On Pause");

        saveFavs();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Favs Activity","On Stop");
        saveFavs();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Favs Activity", "On Resume");
        checkBackground();
        sortMyList();
        myAdapter.notifyDataSetChanged();
    }


}
