package com.androidandyuk.autobuddy;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.checkUpdate;
import static com.androidandyuk.autobuddy.MainActivity.currencySetting;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.incBikeEvents;
import static com.androidandyuk.autobuddy.MainActivity.incCarEvents;
import static com.androidandyuk.autobuddy.MainActivity.lastHowManyFuels;
import static com.androidandyuk.autobuddy.MainActivity.locationUpdatesTime;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.notiHour;
import static com.androidandyuk.autobuddy.MainActivity.notiMinute;
import static com.androidandyuk.autobuddy.MainActivity.notificationsWanted;
import static com.androidandyuk.autobuddy.MainActivity.saveSettings;
import static com.androidandyuk.autobuddy.MainActivity.sdf;
import static com.androidandyuk.autobuddy.MainActivity.warningDays;

public class Settings extends AppCompatActivity {

    public static RelativeLayout main;

    TextView locationUpdatesTimeTV;
    TextView lastHowManyFuelsTV;
    TextView warningDaysTV;
    TextView notiHourTV;

    Switch incCarShows;
    Switch incBikeShows;
    Switch backgroundsWantedSW;
    Switch notificationsWantedSW;

    Spinner currencySpinner;
    Spinner milesSpinner;
    View settings;

    public static ImageView shield;

    View getDetails;
    public static String tag;
    public static String details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_settings);

        currencySpinner = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.currencySpinner);
        currencySpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, Currency.values()));

        switch (currencySetting) {
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


        milesSpinner = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.milesSpinner);
        milesSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, MilesKM.values()));

        switch (milesSetting) {
            case "Miles":
                milesSpinner.setSelection(0);
                break;
            case "Km":
                milesSpinner.setSelection(1);
                break;
        }

        lastHowManyFuelsTV = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.numberFuels);
        lastHowManyFuelsTV.setText(Integer.toString(lastHowManyFuels));

        warningDaysTV = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.warningDaysTV);
        warningDaysTV.setText(Integer.toString(warningDays));

        setNotiTV();

        locationUpdatesTimeTV = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.minutesBetween);
        locationUpdatesTimeTV.setText(Integer.toString(locationUpdatesTime / 60000));

        incCarShows = (Switch) findViewById(com.androidandyuk.autobuddy.R.id.incCarShows);
        incCarShows.setChecked(incCarEvents);

        incBikeShows = (Switch) findViewById(com.androidandyuk.autobuddy.R.id.incBikeShows);
        incBikeShows.setChecked(incBikeEvents);

        backgroundsWantedSW = (Switch) findViewById(com.androidandyuk.autobuddy.R.id.backgroundsWanted);
        backgroundsWantedSW.setChecked(backgroundsWanted);

        notificationsWantedSW = (Switch) findViewById(com.androidandyuk.autobuddy.R.id.notificationsWanted);
        notificationsWantedSW.setChecked(notificationsWanted);

        shield = (ImageView) findViewById(R.id.shield);
    }

    private void setNotiTV() {
        notiHourTV = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.notiHourTV);
        String mins = Integer.toString(notiMinute);
        if (notiMinute < 10) {
            mins = "0" + mins;
        }
        notiHourTV.setText(notiHour + ":" + mins);
    }

    public void getDetailsClicked(View view) {
        tag = view.getTag().toString();
        getDetails(tag);
    }

    public void checkDetails() {
        Log.i("Checking Details", details);
        switch (tag) {
            case "fuels":
                lastHowManyFuels = Integer.parseInt(details);
                lastHowManyFuelsTV = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.numberFuels);
                lastHowManyFuelsTV.setText(details);
                ed.putInt("lastHowManyFuels", Integer.parseInt(details)).apply();
                break;
            case "minutes":
                locationUpdatesTime = Integer.parseInt(details) * 60000;
                locationUpdatesTimeTV = (TextView) findViewById(R.id.minutesBetween);
                locationUpdatesTimeTV.setText(details);
                ed.putInt("locationUpdatesTime", Integer.parseInt(details)).apply();
                break;
            case "How many days":
                int days = Integer.parseInt(details);
                if (days > 0 && days < 40) {
                    warningDays = days;
                    warningDaysTV = (TextView) findViewById(R.id.warningDaysTV);
                    warningDaysTV.setText(details);
                    ed.putInt("locationUpdatesTime", days).apply();
                } else {
                    Toast.makeText(this, "Not a valid entry", Toast.LENGTH_SHORT).show();
                }
                break;
            case "fuels url":
                new MyAsyncTaskFuels().execute(details);
                break;
            case "maintenance url":
                new MyAsyncTaskMaint().execute(details);
                break;
        }
    }

    public void setNotificationTime(View view) {
//        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);

        TimePickerDialog timePicker = new TimePickerDialog(
                Settings.this,
                R.style.datepicker,
                timePickerListener,
                notiHour, notiMinute, true);
        timePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        timePicker.show();
    }

    TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay,
                                      int selectedMinute) {
                    notiHour = hourOfDay;
                    notiMinute = selectedMinute;
                    ed.putInt("notiHour", notiHour).apply();
                    ed.putInt("notiMinute", notiMinute).apply();
                    setNotiTV();
                }
            };

    public void getDetails(String hint) {
        Log.i("Get Details", hint);
        getDetails = findViewById(R.id.getDetails);
        getDetails.setVisibility(View.VISIBLE);
        shield.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        final EditText thisET = (EditText) findViewById(R.id.getDetailsText);
        thisET.setHint(hint);

        thisET.setFocusableInTouchMode(true);
        thisET.requestFocus();

        thisET.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    details = thisET.getText().toString();
                    Log.i("Details", details);
                    getDetails.setVisibility(View.INVISIBLE);
                    shield.setVisibility(View.INVISIBLE);
                    thisET.setText(null);
                    checkDetails();
                    hideDetails();
                    return true;
                }
                return false;
            }
        });
    }

    public void hideDetails() {
        View thisView = this.getCurrentFocus();
        if (thisView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(thisView.getWindowToken(), 0);
        }
        getDetails.setVisibility(View.INVISIBLE);
        shield.setVisibility(View.INVISIBLE);
    }

    public void submitPressed(View view) {
        Log.i("submitPressed", "Started");
        getDetails = findViewById(R.id.getDetails);
        EditText thisET = (EditText) findViewById(R.id.getDetailsText);
        details = thisET.getText().toString();
        Log.i("Details", details);
        thisET.setText(null);
        checkDetails();
        hideDetails();
    }

    public void shieldClicked(View view) {
        if (getDetails.isShown()) {
            getDetails.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);
        }
    }


    public static void exportDBorig() {
        Log.i("exportDB", "Starting");
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "AutoBuddy");
        Log.i("dir is ", "" + dir);
        //dir.mkdir();
        try {
            if (dir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Creating Dir Error", "" + e);
        }

        String currentDBPath = "/data/com.androidandyuk.autobuddy/databases/Vehicles";
        String backupDBPath = "AutoBuddy/Vehicles.db";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        Context context = App.getContext();
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(context, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.i("Export Failed", "Error");
            e.printStackTrace();
            Toast.makeText(context, "Export Failed!", Toast.LENGTH_LONG).show();
        }
    }































    public void goToAbout(View view) {
        // go to about me
        Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
        startActivity(intent);
    }

    public void resetInstructions(View view){
        ed.putBoolean("instructionsRead", false).apply();
    }

    // get fuels from fuelio
    public class MyAsyncTaskFuels extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.i("Import Fuels", "doInBackground");
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
                Log.i("Exception Caught ", "" + ex);
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            try {
                Log.i("Car Shows", "Getting JSON");
                JSONArray json = new JSONArray(progress[0]);
                Log.i("JSON size", "" + json.length());

                bikes.get(activeBike).fuelings.clear();

                for (int i = json.length() - 2; i >= 0; i--) {
                    JSONObject thisFuel = json.getJSONObject(i);
                    JSONObject lastFuel = json.getJSONObject(i + 1);

                    String startDate = thisFuel.getString("Data");
                    String[] parts = startDate.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int day = Integer.parseInt(parts[2]);
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    Date theDate = cal.getTime();

                    String date = sdf.format(theDate);

                    Double mileage = Double.parseDouble(thisFuel.getString("Odo (mi)"));
                    Double miles = mileage - Double.parseDouble(lastFuel.getString("Odo (mi)"));
                    Double litres = Double.parseDouble(lastFuel.getString("Fuel (litres)"));
                    Double price = Double.parseDouble(thisFuel.getString("VolumePrice"));
                    Log.i("Adding notes ", date + " " + miles + " " + mileage + " " + litres + " " + price);
                    fuellingDetails theseDetails = new fuellingDetails(miles, price, litres, date, mileage);
                    bikes.get(activeBike).fuelings.add(theseDetails);
                }
            } catch (Exception ex) {
                Log.i("JSON failed", "" + ex);
            }

            Collections.sort(bikes.get(activeBike).fuelings);
            Fuelling.saveFuels();
        }

        protected void onPostExecute(String result2) {
            checkUpdate();
        }

    }

    // get fuels from fuelio
    public class MyAsyncTaskMaint extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.i("Import Fuels", "doInBackground");
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
                Log.i("Exception Caught ", "" + ex);
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            try {
                Log.i("Importing Maintenance", "Getting JSON");
                JSONArray json = new JSONArray(progress[0]);
                Log.i("JSON size", "" + json.length());

                bikes.get(activeBike).maintenanceLogs.clear();

                for (int i = 0; i < json.length(); i++) {
                    JSONObject thisCost = json.getJSONObject(i);

                    String startDate = thisCost.getString("Date");
                    String[] parts = startDate.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int day = Integer.parseInt(parts[2]);
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    Date theDate = cal.getTime();
                    String date = sdf.format(theDate);

                    Double mileage = Double.parseDouble(thisCost.getString("Odo"));
                    String title = thisCost.getString("CostTitle");
                    String notes = thisCost.getString("Notes");
                    Double cost = Double.parseDouble(thisCost.getString("Cost"));

                    String fullNotes = "** " + title + " ** : " + notes;
                    Log.i("Adding maintenance ", date + " " + fullNotes + " " + mileage + " " + cost);
                    maintenanceLogDetails theseDetails = new maintenanceLogDetails(date, fullNotes, cost, mileage);
                    bikes.get(activeBike).maintenanceLogs.add(theseDetails);
                }
            } catch (Exception ex) {
                Log.i("JSON failed", "" + ex);
            }
            Collections.sort(bikes.get(activeBike).maintenanceLogs);
            Maintenance.saveLogs();
        }

        protected void onPostExecute(String result2) {
            checkUpdate();
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

    public void checkBackground() {
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Settings.main.setBackground(drawablePic);
            settings = findViewById(R.id.settings);
            settings.setBackground(getResources().getDrawable(R.drawable.rounded_corners_drkgrey_orange));
        } else {
            Settings.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
            settings = findViewById(R.id.settings);
            settings.setBackground(null);
        }
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            getDetails = findViewById(R.id.getDetails);
            if (getDetails.isShown()) {
                hideDetails();
            } else {
                finish();
                return true;
            }
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
        notificationsWanted = notificationsWantedSW.isChecked();

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
