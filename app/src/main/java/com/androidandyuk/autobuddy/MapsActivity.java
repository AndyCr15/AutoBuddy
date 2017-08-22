package com.androidandyuk.autobuddy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.androidandyuk.autobuddy.CarShows.carShows;
import static com.androidandyuk.autobuddy.Favourites.favouriteLocations;
import static com.androidandyuk.autobuddy.HotSpots.hotspotLocations;
import static com.androidandyuk.autobuddy.MainActivity.geocoder;
import static com.androidandyuk.autobuddy.MainActivity.locationListener;
import static com.androidandyuk.autobuddy.MainActivity.locationManager;
import static com.androidandyuk.autobuddy.MainActivity.locationUpdatesTime;
import static com.androidandyuk.autobuddy.RaceTracks.trackLocations;
import static com.androidandyuk.autobuddy.Traffic.trafficEvents;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static GoogleMap mMap;
    private FirebaseAnalytics mFirebaseAnalytics;

    static String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Log.i("Maps Activity", "onCreate");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("Maps Activity", "On Map Ready");
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        // read in the reason the map has been called
        Intent intent = getIntent();

        if (intent.getStringExtra("Type") != null) {
            type = intent.getStringExtra("Type");
        }
        int favItem = intent.getIntExtra("placeNumber", 9999);

        if (type.equals("Fav")) {
            if (favItem < 9998) {
                // focus on favourite location
                markedLocation temp = favouriteLocations.get(favItem);
                centerMapOnLocation(temp.location, temp.name);
                Log.i("Fav selected", "" + temp.name);
            } else if (favItem == 9998) {
                showMarkers(favouriteLocations, 0);
            }
        }

        if (type.equals("Hot")) {
            if (favItem < 9998) {
                // focus on favourite location
                markedLocation temp = hotspotLocations.get(favItem);
                centerMapOnLocation(temp.location, temp.name);
                Log.i("Hot spot selected", "" + temp.name);
            } else if (favItem == 9998) {
                showMarkers(hotspotLocations, 0);
            }
        }


        if (type.equals("Track")) {
            if (favItem < 9998) {
                // focus on favourite location
                markedLocation temp = trackLocations.get(favItem);
                centerMapOnLocation(temp.location, temp.name);
                Log.i("Track selected", "" + temp.name);
            } else if (favItem == 9998) {
                showMarkers(trackLocations, 0);
            }
        }

        if (type.equals("Shows")) {
            if (favItem < 9998) {
                // focus on favourite location
                markedLocation temp = carShows.get(favItem);
                centerMapOnLocation(temp.location, temp.name);
                Log.i("Show selected", "" + temp.name);
            } else if (favItem == 9998) {
                showMarkers(carShows, 0);
            }
        }

        if (type.equals("Traffic")) {
            if (favItem < 9998) {
//                markedLocation temp = Traffic.trafficEvents.get(favItem);
//                centerMapOnLocation(temp.location, temp.name);
//                Log.i("Track selected", "" + temp.name);
            } else if (favItem == 9998) {
                showTrafficMarkers(trafficEvents, 0);
            }
        }


        // zoom in on user's location

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //centerMapOnLocation(location, "Your location");

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdatesTime, 1000, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdatesTime, 1000, locationListener);

//                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//                centerMapOnLocation(lastKnownLocation, "Your location");

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdatesTime, 1000, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Log.i("Last Known Lct updated", "" + lastKnownLocation);
                //centerMapOnLocation(lastKnownLocation, "Your location");
            }
        }
    }

    public static void showMarkers(List<markedLocation> markedLocations, int colour) {
        Log.i("Show Markers", "called");
        //mMap.clear();
        for (markedLocation location : markedLocations) {
            Log.i("Marking", "" + location.getLocation());
            if (type.equals("Shows")) {
                mMap.addMarker(new MarkerOptions()
                        .position(location.getLocation())
                        .title(location.name)
                        .snippet(location.start + " to " + location.end)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(location.getLocation())
                        .title(location.name));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.user.location, 8));
    }

    public static void showTrafficMarkers(List<TrafficEvent> trafficEvents, int colour) {
        Log.i("Show Traffic Markers", "called");
        mMap.clear();
        for (TrafficEvent location : trafficEvents) {
            if (location.delay.contains("road closure")) {
                mMap.addMarker(new MarkerOptions()
                        .position(location.location)
                        .title(location.title)
                        .snippet(location.delay)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(location.location)
                        .title(location.title)
                        .snippet(location.delay)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.user.location, 11));
    }

    public void centerMapOnLocation(LatLng latLng, String title) {

        Log.i("Maps Activity", "Center on map - latLng");

        mMap.clear();

        if (title != "Your location") {

            mMap.addMarker(new MarkerOptions().position(latLng).title(title));

        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

    }

    public void centerMapOnLocation(Location location, String title) {

        Log.i("Maps Activity", "Center on map - location");

        LatLng selectedLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //mMap.clear();

        if (title != "Your location") {

            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(title));

        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 14));

    }

    public void centerMapOnUser(View view) {
        Log.i("Center View on User", "called");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdatesTime, 1000, locationListener);

            Log.i("Center View on User", "LK Location updated");
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            MainActivity.user.setLocation(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));

            centerMapOnLocation(lastKnownLocation, "Your location");
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.i("Maps Activity", "On Long Click");

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "Unknown";
        String locality = "Unknown";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddresses != null && listAddresses.size() > 0) {
                Log.i("Feature name", "" + listAddresses.get(0).getFeatureName());
                if (listAddresses.get(0).getLocality() != null) {
                    locality = listAddresses.get(0).getLocality();
                    Log.i("Locality", "" + listAddresses.get(0).getLocality());
                }
                if (listAddresses.get(0).getThoroughfare() != null) {
                    Log.i("Thoroughfare", "" + listAddresses.get(0).getThoroughfare());
                    locality += ", " + listAddresses.get(0).getThoroughfare();
                    if (listAddresses.get(0).getSubThoroughfare() != null) {
                        Log.i("Subthoroughfare", "" + listAddresses.get(0).getSubThoroughfare());
                        address += listAddresses.get(0).getSubThoroughfare() + " ";

                    }

                    address += listAddresses.get(0).getThoroughfare();

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        markedLocation newFav = new markedLocation(locality, latLng, "");

        favouriteLocations.add(newFav);

        Favourites.saveFavs();

//        Favourites.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Location " + locality + " saved", Toast.LENGTH_SHORT).show();
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
        Log.i("Maps Activity", "On Pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Maps Activity", "On Resume");
    }
}
