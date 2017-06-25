package com.androidandyuk.autobuddy;

import android.app.DatePickerDialog;
import android.content.Context;
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

    Double mileage;

    String editDate = "";

    private DatePickerDialog.OnDateSetListener logDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_maintenance_log);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAdView = (AdView) findViewById(com.androidandyuk.autobuddy.R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        logString = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.logString);
        logCost = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.logCost);
        logMilage = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.logMileage);
        setLogDate = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.setLogDate);
        brakePads = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.brakePads);
        brakeDiscs = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.brakeDiscs);
        frontTyre = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.frontTyre);
        rearTyre = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.rearTyre);
        oilChange = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.oilChange);
        newBattery = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.newBattery);
        coolantChange = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.coolantChange);
        sparkPlugs = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.sparkPlugs);
        airFilter = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.airFilter);
        brakeFluid = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.brakeFluid);
        wasService = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.fullService);
        wasMOT = (ToggleButton) findViewById(com.androidandyuk.autobuddy.R.id.fullMOT);

        // set the date for a new log to today
        Calendar date = Calendar.getInstance();
        String today = sdf.format(date.getTime());
        setLogDate.setText(today);

        if (Maintenance.itemLongPressed != null) {

            // we're editing an old log, so load in the old logs settings

            setLogDate.setText(bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getDate());
            logString.setText(bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getLog());
            logCost.setText(Double.toString(bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getPrice()));
            Double miles = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).getMileage();
            // check what setting the user has, Miles or Km
            // if Km, convert to Miles for display
            if(milesSetting.equals("Km")){
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

        mileage = 1.0;

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


            // check if this is a log that has been edited
            // if so, carry over the old date
            // if not, create without a date, which will set it to today
            String date = setLogDate.getText().toString();
            Log.i("Log Date is", date);

            maintenanceLogDetails today;
            Log.i("itemLongPressed", " is " + Maintenance.itemLongPressed);

            // it's a new log, set mileage to 9999999 so we can find it after to verify the user set mileage
            Double marker = 9999999.0;

            if (Maintenance.itemLongPressed != null) {
                // adding back in an edited log
                bikes.get(activeBike).maintenanceLogs.remove(Maintenance.itemLongPressed);
                today = new maintenanceLogDetails(date, logInfo, cost, marker, isAService, isAMOT, theseBrakePads, theseBrakeDiscs, theseFrontTyre, theseRearTyre,
                        theseOilChange, theseNewBattery, theseCoolantChange, theseSparkPlugs, theseAirFilter, theseBrakeFluid);
            } else {
                today = new maintenanceLogDetails(date, logInfo, cost, marker, isAService, isAMOT, theseBrakePads, theseBrakeDiscs, theseFrontTyre, theseRearTyre,
                        theseOilChange, theseNewBattery, theseCoolantChange, theseSparkPlugs, theseAirFilter, theseBrakeFluid);
            }
            bikes.get(activeBike).maintenanceLogs.add(today);
            Collections.sort(bikes.get(activeBike).maintenanceLogs);
            Maintenance.myAdapter.notifyDataSetChanged();

            // now check if it was a new log, does the mileage fit the correct range
            for (int i = 0; i < bikes.get(activeBike).maintenanceLogs.size(); i++) {
                if (bikes.get(activeBike).maintenanceLogs.get(i).mileage == 9999999.0) {
                    Maintenance.itemLongPressed = bikes.get(activeBike).maintenanceLogs.get(i);
                    Maintenance.itemLongPressedPosition = i;
                    bikes.get(activeBike).maintenanceLogs.get(i).mileage = verifyMileage();
                }
            }

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
                com.androidandyuk.autobuddy.R.style.datepicker,
                logDateSetListener,
                year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        dialog.show();
    }

    public Double verifyMileage() {
        double thisMileage = 1d;

        Log.i("Item Pos", "" + Maintenance.itemLongPressedPosition);
        Log.i("Item", "" + Maintenance.itemLongPressed);


        if (logMilage.getText().toString().isEmpty()) {
            if (Maintenance.itemLongPressed == null) {
                // we're not editing so it must be a new entry, but empty
                thisMileage = bikes.get(activeBike).estMileage;
            } else {
                // we're editing but the box is now empty, so set it what it was before the edit began
                if (bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).mileage != 9999999.0) {
                    thisMileage = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition).mileage;
                }
            }
        } else {
            // the box is not empty
            Log.i("The Box is", " Not empty");
            thisMileage = Double.parseDouble(logMilage.getText().toString());
            Log.i("Mileage read at ", "" + thisMileage);
            // check what setting the user has, Miles or Km
            // if Km, convert to Miles for storage
            if(milesSetting.equals("Km")){
                thisMileage = thisMileage * conversion;
            }
            if (Maintenance.itemLongPressed != null) {
                // editing or coming through having had marker set, with a number in the box
                int numLogs = (bikes.get(activeBike).maintenanceLogs.size() - 1);
                if (Maintenance.itemLongPressedPosition == 0) {
                    // this is the last log entry, check it's after a previous
                    if (numLogs > 0) {
                        // there is more than one entry, check it's after the last one
                        if (thisMileage < bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition + 1).mileage) {
                            // it's lower than the previous item, so set it to the previous item
                            thisMileage = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition + 1).mileage;
                        } else {
                            // theres more than one, it's above the last, so set it as is
                            Log.i("thisMileage ", "" + thisMileage);
                            bikes.get(activeBike).estMileage = thisMileage;
                            return thisMileage;
                        }
                    } else {
//                        // it's the only entry
//                        thisMileage = Double.parseDouble(logMilage.getText().toString());
//                        // check what setting the user has, Miles or Km
//                        // if Km, convert to Miles for storage
//                        if(milesSetting.equals("Km")){
//                            thisMileage = thisMileage * conversion;
//                        }
                        bikes.get(activeBike).estMileage = thisMileage;
                        Log.i("thisMileage ", "" + thisMileage);
                        return thisMileage;
                    }
                } else if (Maintenance.itemLongPressedPosition < numLogs) {
                    // it's not the last item, but it's not the first item
                    if (thisMileage > bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition - 1).mileage) {
                        // it's higher than the next item, so set it to the next item
                        thisMileage = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition - 1).mileage;
                    }
                    if (thisMileage < bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition + 1).mileage) {
                        // it's lower than the previous item, so set it to the previous item
                        thisMileage = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition + 1).mileage;
                    }
                    // if we made it here, it's within the range of previous and last, so can be left as is
                } else {
                    // it's the first log
                    if (thisMileage > bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition - 1).mileage) {
                        // it's higher than the next item, so set it to the next item
                        thisMileage = bikes.get(activeBike).maintenanceLogs.get(Maintenance.itemLongPressedPosition - 1).mileage;
                    }
                }
            }
        }
        Log.i("thisMileage ", "" + thisMileage);
        return thisMileage;
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            MaintenanceLog.main.setBackground(drawablePic);
        } else {
            MaintenanceLog.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
        }
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Maintenance.itemLongPressed != null) {
                addLog();
            }
            finish();
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
