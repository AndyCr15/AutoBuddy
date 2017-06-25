package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.currencySetting;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.incBikeEvents;
import static com.androidandyuk.autobuddy.MainActivity.incCarEvents;
import static com.androidandyuk.autobuddy.MainActivity.lastHowManyFuels;
import static com.androidandyuk.autobuddy.MainActivity.locationUpdatesTime;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.saveSettings;

public class Settings extends AppCompatActivity {

    public static RelativeLayout main;

    TextView locationUpdatesTimeTV;
    TextView lastHowManyFuelsTV;

    Switch incCarShows;
    Switch incBikeShows;
    Switch backgroundsWantedSW;

    Spinner currencySpinner;
    Spinner milesSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_settings);

        currencySpinner = (Spinner)findViewById(com.androidandyuk.autobuddy.R.id.currencySpinner);
        currencySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Currency.values()));

        switch (currencySetting){
            case "£":
                currencySpinner.setSelection(0);
                break;
            case "$":
                currencySpinner.setSelection(1);
                break;
            case "€":
                currencySpinner.setSelection(2);
                break;
        }


        milesSpinner = (Spinner)findViewById(com.androidandyuk.autobuddy.R.id.milesSpinner);
        milesSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MilesKM.values()));

        switch (milesSetting){
            case "Miles":
                milesSpinner.setSelection(0);
                break;
            case "Km":
                milesSpinner.setSelection(1);
                break;
        }

        lastHowManyFuelsTV = (TextView)findViewById(com.androidandyuk.autobuddy.R.id.numberFuels);
        lastHowManyFuelsTV.setText(Integer.toString(lastHowManyFuels));

        locationUpdatesTimeTV = (TextView)findViewById(com.androidandyuk.autobuddy.R.id.minutesBetween);
        locationUpdatesTimeTV.setText(Integer.toString(locationUpdatesTime/60000));

        incCarShows = (Switch)findViewById(com.androidandyuk.autobuddy.R.id.incCarShows);
        incCarShows.setChecked(incCarEvents);

        incBikeShows = (Switch)findViewById(com.androidandyuk.autobuddy.R.id.incBikeShows);
        incBikeShows.setChecked(incBikeEvents);

        backgroundsWantedSW = (Switch)findViewById(com.androidandyuk.autobuddy.R.id.backgroundsWanted);
        backgroundsWantedSW.setChecked(backgroundsWanted);
    }

    public void setFuels (View view){
        getDetailsFuels("fuels");
    }

    public void setMins (View view){
        getDetailsMins("minutes");
    }

    public void getDetailsFuels(String hint) {
        Log.i("Get Details", hint);
//        final String[] detail = new String[1];
        final View getDetails = findViewById(com.androidandyuk.autobuddy.R.id.getDetails);
        getDetails.setVisibility(View.VISIBLE);
        final EditText thisET = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.getDetailsText);
        thisET.setHint(hint);

        thisET.setFocusableInTouchMode(true);
        thisET.requestFocus();

        thisET.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String details = thisET.getText().toString().toUpperCase();
                    Log.i("Details", details);
                    getDetails.setVisibility(View.INVISIBLE);

                    lastHowManyFuels = Integer.parseInt(details);
                    lastHowManyFuelsTV = (TextView)findViewById(com.androidandyuk.autobuddy.R.id.numberFuels);
                    lastHowManyFuelsTV.setText(details);
                    ed.putInt("lastHowManyFuels", Integer.parseInt(details)).apply();

                    return true;
                }
                return false;
            }
        });
    }

    public void getDetailsMins(String hint) {
        Log.i("Get Details", hint);
//        final String[] detail = new String[1];
        final View getDetails = findViewById(com.androidandyuk.autobuddy.R.id.getDetails);
        getDetails.setVisibility(View.VISIBLE);
        final EditText thisET = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.getDetailsText);
        thisET.setHint(hint);

        thisET.setFocusableInTouchMode(true);
        thisET.requestFocus();

        thisET.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String details = thisET.getText().toString().toUpperCase();
                    Log.i("Details", details);
                    getDetails.setVisibility(View.INVISIBLE);

                    locationUpdatesTime = Integer.parseInt(details)*60000;
                    locationUpdatesTimeTV = (TextView)findViewById(com.androidandyuk.autobuddy.R.id.minutesBetween);
                    locationUpdatesTimeTV.setText(details);
                    ed.putInt("locationUpdatesTime", Integer.parseInt(details)).apply();

                    return true;
                }
                return false;
            }
        });
    }

    public void goToAbout(View view){
        // go to about me
        Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
        startActivity(intent);
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Settings.main.setBackground(drawablePic);
        } else {
            Settings.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
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
        Log.i("Settings Activity", "On Pause");
        incCarEvents = incCarShows.isChecked();
        incBikeEvents = incBikeShows.isChecked();
        backgroundsWanted = backgroundsWantedSW.isChecked();

        currencySpinner = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.currencySpinner);
        currencySetting = currencySpinner.getSelectedItem().toString();
        milesSpinner = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.milesSpinner);
        milesSetting = milesSpinner.getSelectedItem().toString();
        saveSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Settings Activity", "On Stop");
//        saveLogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackground();
    }
}
