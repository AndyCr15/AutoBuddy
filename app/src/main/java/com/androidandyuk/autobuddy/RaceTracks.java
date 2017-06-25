package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;

public class RaceTracks extends AppCompatActivity {
    static List<markedLocation> trackLocations = new ArrayList<>();
    static MyLocationAdapter myAdapter;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static RelativeLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_race_tracks);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Log.i("Race Tracks", "onCreate");

        ListView listView = (ListView) findViewById(com.androidandyuk.autobuddy.R.id.listTracks);

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

        initialiseTracks();
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
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            RaceTracks.main.setBackground(drawablePic);
        } else {
            RaceTracks.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
        }
    }

    public static void initialiseTracks() {

        if (RaceTracks.trackLocations.size() == 0) {
            Log.i("Initialising Tracks", "Started");
            RaceTracks.trackLocations.add(new markedLocation("Brands Hatch", new LatLng(51.3598711, 0.2586481), ""));
            RaceTracks.trackLocations.add(new markedLocation("Silverstone", new LatLng(52.0733006, -1.0168521), ""));
            RaceTracks.trackLocations.add(new markedLocation("Snetterton", new LatLng(52.4636482, 0.9436173), ""));
            RaceTracks.trackLocations.add(new markedLocation("Oulton Park", new LatLng(53.178469, -2.6189947), ""));
            RaceTracks.trackLocations.add(new markedLocation("Donington Park", new LatLng(52.8305468, -1.381029), ""));
            RaceTracks.trackLocations.add(new markedLocation("Anglesey", new LatLng(53.191994, -4.5038327), ""));
            RaceTracks.trackLocations.add(new markedLocation("Bedford Autodrome", new LatLng(52.2211337, -0.4819822), ""));
            RaceTracks.trackLocations.add(new markedLocation("Cadwell Park", new LatLng(53.3108261, -0.0737291), ""));
            RaceTracks.trackLocations.add(new markedLocation("Croft", new LatLng(54.4554809, -1.5580811), ""));
            RaceTracks.trackLocations.add(new markedLocation("Lydden Hill", new LatLng(51.1771493, 1.1987867), ""));
            RaceTracks.trackLocations.add(new markedLocation("Mallory Park", new LatLng(52.6006262, -1.3344846), ""));
            RaceTracks.trackLocations.add(new markedLocation("Rockingham", new LatLng(52.5156871, -0.6600846), ""));
            RaceTracks.trackLocations.add(new markedLocation("Thruxton", new LatLng(51.185835, -1.55265), ""));
            RaceTracks.trackLocations.add(new markedLocation("Knock Hill", new LatLng(56.1313905, -3.5111837), ""));
            RaceTracks.trackLocations.add(new markedLocation("Pembrey Race Circuit", new LatLng(51.7052918, -4.3258864), ""));
            RaceTracks.trackLocations.add(new markedLocation("Castle Combe", new LatLng(51.4935115, -2.2200441), ""));
            RaceTracks.trackLocations.add(new markedLocation("Goodwood", new LatLng(50.859426, -0.753909), ""));
            RaceTracks.trackLocations.add(new markedLocation("Santa Pod", new LatLng(52.23485, -0.6022797), ""));
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


