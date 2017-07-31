package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.currentForecast;
import static com.androidandyuk.autobuddy.MainActivity.weatherText;

public class Locations extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String TAG = "MainActivity";

    public static RelativeLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_locations);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);

        weatherText = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.weatherView);
        // take weather found in MainActivity
        weatherText.setText("Today's forecast: " + currentForecast);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.androidandyuk.autobuddy.R.menu.bike_choice, menu);
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Settings").setShortcut('3', 'c');

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {
            case 0:
                Log.i("Option", "0");
                intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "Settings not yet implemented", Toast.LENGTH_LONG).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToFavourites(View view) {
        Intent intent = new Intent(getApplicationContext(), Favourites.class);
        startActivity(intent);
    }

    public void goToTraffic(View view) {
        Intent intent = new Intent(getApplicationContext(), Traffic.class);
        startActivity(intent);
    }

    public void goToPetrol(View view) {
        Intent intent = new Intent(getApplicationContext(), PetrolPrices.class);
        startActivity(intent);
    }

    public void goToTracks(View view) {
        View loading = findViewById(com.androidandyuk.autobuddy.R.id.loading);
        loading.setVisibility(View.VISIBLE);
        Intent intent = new Intent(getApplicationContext(), RaceTracks.class);
        startActivity(intent);
    }

    public void goToHotSpots(View view) {
        View loading = findViewById(com.androidandyuk.autobuddy.R.id.loading);
        loading.setVisibility(View.VISIBLE);
        Intent intent = new Intent(getApplicationContext(), HotSpots.class);
        startActivity(intent);
    }

    public void goToCarShows(View view) {
        Intent intent = new Intent(getApplicationContext(), CarShows.class);
        startActivity(intent);
    }


    public void checkBackground() {
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Locations.main.setBackground(drawablePic);
        } else {
            Locations.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
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
    protected void onResume() {
        super.onResume();
        // removing the loading sign on coming back to this page
        View loading = findViewById(com.androidandyuk.autobuddy.R.id.loading);
        loading.setVisibility(View.INVISIBLE);

        checkBackground();

    }
}
