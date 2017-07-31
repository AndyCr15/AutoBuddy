package com.androidandyuk.autobuddy;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.checkInRange;
import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.sdf;

public class MaintenanceLog extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String TAG = "Maintenance Log";
    private AdView mAdView;

    public static RelativeLayout main;

    EditText logString;
    EditText logCost;
    EditText logMilage;
    TextView setLogDate;

    ToggleButton brakePads;
    ToggleButton brakeDiscs;
    ToggleButton frontTyre;
    ToggleButton rearTyre;
    ToggleButton oilChange;
    ToggleButton newBattery;
    ToggleButton coolantChange;
    ToggleButton sparkPlugs;
    ToggleButton airFilter;
    ToggleButton brakeFluid;
    ToggleButton wasService;
    ToggleButton wasMOT;

    public static Date staticTodayDate;
    public static String staticTodayString;

    String editDate = "";

    private DatePickerDialog.OnDateSetListener logDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_log);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        staticTodayDate = new Date();
        staticTodayString = sdf.format(staticTodayDate);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        logString = (EditText) findViewById(R.id.logString);
        logCost = (EditText) findViewById(R.id.logCost);
        logMilage = (EditText) findViewById(R.id.logMileage);
        setLogDate = (TextView) findViewById(R.id.setLogDate);
        brakePads = (ToggleButton) findViewById(R.id.brakePads);
        brakeDiscs = (ToggleButton) findViewById(R.id.brakeDiscs);
        frontTyre = (ToggleButton) findViewById(R.id.frontTyre);
        rearTyre = (ToggleButton) findViewById(R.id.rearTyre);
        oilChange = (ToggleButton) findViewById(R.id.oilChange);
        newBattery = (ToggleButton) findViewById(R.id.newBattery);
        coolantChange = (ToggleButton) findViewById(R.id.coolantChange);
        sparkPlugs = (ToggleButton) findViewById(R.id.sparkPlugs);
        airFilter = (ToggleButton) findViewById(R.id.airFilter);
        brakeFluid = (ToggleButton) findViewById(R.id.brakeFluid);
        wasService = (ToggleButton) findViewById(R.id.fullService);
        wasMOT = (ToggleButton) findViewById(R.id.fullMOT);

        // set the date for a new log to today
        setLogDate.setText(staticTodayString);

        if (Maintenance.itemLongPressed != null) {

            // we're editing an old log, so load in the old logs settings

            setLogDate.setText(bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getDate());
            logString.setText(bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getLog());
            logCost.setText(Double.toString(bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getPrice()));
            Double miles = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getMileage();
            // check what setting the user has, Miles or Km
            // if Km, convert to Miles for display
            if (milesSetting.equals("Km")) {
                miles = miles / conversion;
            }
            logMilage.setText(oneDecimal.format(miles));

            // set all the toggles to correct state
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getWasService()) {
                wasService.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getWasMOT()) {
                wasMOT.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getBrakePads()) {
                brakePads.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getBrakeDiscs()) {
                brakeDiscs.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getFrontTyre()) {
                frontTyre.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getRearTyre()) {
                rearTyre.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getOilChange()) {
                oilChange.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getNewBattery()) {
                newBattery.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getCoolantChange()) {
                coolantChange.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getSparkPlugs()) {
                sparkPlugs.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getAirFilter()) {
                airFilter.setChecked(true);
            }
            if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getBrakeFluid()) {
                brakeFluid.setChecked(true);
            }

            editDate = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getDate();
        }


        logDateSetListener = new DatePickerDialog.OnDateSetListener()

        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar date = Calendar.getInstance();
                date.set(year, month, day);
                String sdfDate = sdf.format(date.getTime());
                Log.i("Chosen Date", sdfDate);
                setLogDate.setText(sdfDate);
            }
        };


    }

    public void addClicked(View view) {
        addLog();
    }

    public void addLog() {
        Log.i("Maintenance", "Taking details and adding");
        Double cost = 0d;

        try {
            cost = Double.parseDouble(logCost.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String logInfo = logString.getText().toString();
        Log.i("Log Info", logInfo);

        // check information has been entered
        if (logInfo.isEmpty() || logInfo == null) {

            Toast.makeText(MaintenanceLog.this, "Please complete all necessary details", Toast.LENGTH_LONG).show();

        } else {

            Boolean isAService = wasService.isChecked();
            Boolean isAMOT = wasMOT.isChecked();

            Boolean theseBrakePads = brakePads.isChecked();
            Boolean theseBrakeDiscs = brakeDiscs.isChecked();
            Boolean theseFrontTyre = frontTyre.isChecked();
            Boolean theseRearTyre = rearTyre.isChecked();
            Boolean theseOilChange = oilChange.isChecked();
            Boolean theseNewBattery = newBattery.isChecked();
            Boolean theseCoolantChange = coolantChange.isChecked();
            Boolean theseSparkPlugs = sparkPlugs.isChecked();
            Boolean theseAirFilter = airFilter.isChecked();
            Boolean theseBrakeFluid = brakeFluid.isChecked();

            double mileage = 0;
            try {
                mileage = Double.parseDouble(logMilage.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            if (milesSetting.equals("Km")) {
                mileage = mileage * conversion;
            }

            // check if this is a log that has been edited
            // if so, carry over the old date
            // if not, create without a date, which will set it to today
            String date = setLogDate.getText().toString();
            Log.i("Log Date is", date);

            maintenanceLogDetails today;
            Log.i("itemLongPressed", " is " + Maintenance.itemLongPressed);

            if (Maintenance.itemLongPressed != null) {
                // we're adding back in an edited log, so remove the old one
                bikes.get(activeBike).maintenanceLogs.remove(Maintenance.itemLongPressed);
            }

            today = new maintenanceLogDetails(date, logInfo, cost, mileage, isAService, isAMOT, theseBrakePads, theseBrakeDiscs, theseFrontTyre, theseRearTyre,
                    theseOilChange, theseNewBattery, theseCoolantChange, theseSparkPlugs, theseAirFilter, theseBrakeFluid);

            bikes.get(activeBike).maintenanceLogs.add(today);
            Collections.sort(bikes.get(activeBike).maintenanceLogs);
            Maintenance.myAdapter.notifyDataSetChanged();

            if (isAMOT) {
                // create a new calendar item and then apply this bikes MOTdue to it
                Calendar thisDate = Calendar.getInstance();
                Date thisTestDate = null;
                try {
                    thisTestDate = sdf.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                thisDate.setTime(thisTestDate);
                // check if the MOT date of the log is within range
                if (checkInRange(bikes.get(activeBike).MOTdue, thisDate)) {
                    try {
                        thisDate.setTime(sdf.parse(bikes.get(activeBike).MOTdue));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.i("Adding an MOT", "Date conversion failed");
                    }
                    Log.i("MOT Within Range", "New date" + thisDate);
                } else {
                    try {
                        thisDate.setTime(sdf.parse(date));
                        Log.i("MOT Outside Range", "New date" + thisDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                // if it was within MOT range, add a year to the MOTdue
                // if it was outside, add a year to the date of the log
                thisDate.add(Calendar.YEAR, 1);
                bikes.get(activeBike).MOTdue = sdf.format(thisDate.getTime());

            }

            if (isAService) {
                // if the log added is a service, add a year to the date of the log
                Calendar thisDate = new GregorianCalendar();
                try {
                    thisDate.setTime(sdf.parse(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                thisDate.add(Calendar.YEAR, 1);
                bikes.get(activeBike).serviceDue = sdf.format(thisDate.getTime());

            }

            logString.setText(null);
            logString.clearFocus();
            logCost.setText(null);
            logCost.clearFocus();
            Maintenance.itemLongPressed = null;
            Maintenance.itemLongPressedPosition = -1;

            if (wasService.isChecked()) {
                wasService.setChecked(false);
            }
            if (wasMOT.isChecked()) {
                wasMOT.setChecked(false);
            }

            // Check if no view has focus:
            View thisView = this.getCurrentFocus();
            if (thisView != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(thisView.getWindowToken(), 0);
            }
            finish();
        }

    }

    public void setLogDate(View view) {
        String thisDateString = "";
        // this sets what date will show when the date picker shows
        // first check if we're editing a current fueling
        if (Maintenance.itemLongPressed != null) {
            thisDateString = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getDate();
        }
        Date thisDate = new Date();
        try {
            thisDate = sdf.parse(thisDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // for some reason I can't getYear from thisDate, so will just use the current year
        Calendar cal = Calendar.getInstance();
        cal.setTime(thisDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                MaintenanceLog.this,
                R.style.datepicker,
                logDateSetListener,
                year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        dialog.show();
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            MaintenanceLog.main.setBackground(drawablePic);
        } else {
            MaintenanceLog.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (!logString.getText().toString().equals("") || !logCost.getText().toString().equals("") || !logMilage.getText().toString().equals("")) {

                // add warning
                new AlertDialog.Builder(MaintenanceLog.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Discard Current Details?")
                        .setMessage("Would you like to discard the current information?")
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("Keep", null)
                        .show();
            } else {
                finish();
            }

            return true;
        }
        Maintenance.myAdapter.notifyDataSetChanged();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Maintenance Activity", "On Pause");
        Maintenance.saveLogs();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Maintenance Activity", "On Stop");
        Maintenance.saveLogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackground();
    }
}
