package com.androidandyuk.autobuddy;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.currencySetting;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.precision;
import static com.androidandyuk.autobuddy.MainActivity.saveBikes;
import static com.androidandyuk.autobuddy.MainActivity.sdf;
import static com.androidandyuk.autobuddy.Maintenance.loadLogs;

public class Garage extends AppCompatActivity {

//    public static ArrayList<Bike> bikes = new ArrayList<>();

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String TAG = "MainActivity";

    private AdView mAdView;

    public static ConstraintLayout main;

    View addingBikeInfo;
    EditText bikeMake;
    EditText bikeModel;
    EditText bikeYear;
    TextView aveMPG;
    TextView bikeEstMileage;
    TextView amountSpent;
    TextView myRegView;
    TextView MOTdue;
    TextView serviceDue;
    Spinner taxDue;

    TextView bikeTitle;
    ViewSwitcher regSwitcher;
    EditText bikeNotes;

    String detail;

    private DatePickerDialog.OnDateSetListener MOTDateSetListener;
    private DatePickerDialog.OnDateSetListener serviceDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_garage);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        if (bikes.size() == 0) {
//            // for testing
//            Bike newBike = new Bike("KTM", "Superduke R", "2016");
//            bikes.add(newBike);
//            Bike newBike2 = new Bike("Honda", "CB1000R", "2011");
//            bikes.add(newBike2);
//        }

        // for adding a new bike
        bikeMake = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.bikeMake);
        bikeModel = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.bikeModel);
        bikeYear = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.bikeYear);
        bikeEstMileage = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.estMileage);
        amountSpent = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.amountSpent);
        myRegView = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.clickable_reg_view);
        MOTdue = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.MOTdue);
        serviceDue = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.serviceDue);
        taxDue = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.taxSpinner);


        taxDue.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, TaxDue.values()));

        if (activeBike > -1) {
            int thisTax = getEnumPos(bikes.get(activeBike).taxDue);
            Log.i("This Tax", bikes.get(activeBike).taxDue + " " + thisTax);
            taxDue.setSelection(thisTax - 1);
        }

        Log.i("Active Bike", "" + activeBike);

        loadLogs();
        garageSetup();
        setListeners();


        /**
         * Hides the soft keyboard
         */
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }


    }

    public int getEnumPos(String thisEnum) {
        switch (thisEnum) {
            case "JAN":
                return 1;
            case "FEB":
                return 2;
            case "MAR":
                return 3;
            case "APR":
                return 4;
            case "MAY":
                return 5;
            case "JUN":
                return 6;
            case "JUL":
                return 7;
            case "AUG":
                return 8;
            case "SEP":
                return 9;
            case "OCT":
                return 10;
            case "NOV":
                return 11;
            case "DEC":
                return 12;
        }
        return 1;
    }

    public void setListeners() {

        if (activeBike > -1)

        {
            MOTDateSetListener = new DatePickerDialog.OnDateSetListener()

            {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    Log.i("MOT Date was: ", bikes.get(activeBike).MOTdue);
                    Calendar date = Calendar.getInstance(TimeZone.getDefault());
                    date.set(year, month, day);
                    String sdfDate = sdf.format(date.getTime());
                    bikes.get(activeBike).MOTdue = sdfDate;
                    Log.i("MOT Date now: ", bikes.get(activeBike).MOTdue);
                    garageSetup();
                }
            };

            serviceDateSetListener = new DatePickerDialog.OnDateSetListener()

            {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    Log.i("Service Date was: ", bikes.get(activeBike).serviceDue);
                    Calendar date = new GregorianCalendar();
                    date.set(year, month, day);
                    String sdfDate = sdf.format(date.getTime());
                    bikes.get(activeBike).serviceDue = sdfDate;
                    Log.i("Service Date now: ", bikes.get(activeBike).serviceDue);
                    garageSetup();
                }
            };
        }
    }

    public void garageSetup() {
        bikeTitle = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.bikeTitle);
//        regSwitcher = (ViewSwitcher) findViewById(R.id.regSwitcher);
        aveMPG = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.aveMPG);
        bikeEstMileage = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.estMileage);
        amountSpent = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.amountSpent);
        bikeNotes = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.bikeNotes);
        bikeNotes.setSelected(false);
        myRegView = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.clickable_reg_view);
        MOTdue = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.MOTdue);
        serviceDue = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.serviceDue);
        taxDue = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.taxSpinner);

        setListeners();

        // check the user has a bike, then set all the views to it's current details
        if (bikes.size() > 0) {
            bikeTitle.setText(bikes.get(activeBike).yearOfMan + " " + bikes.get(activeBike).model);
            aveMPG.setText(Fuelling.aveMPG(activeBike, 10));

            Log.i("Active Bike Reg", " " + (bikes.get(activeBike).registration));

            myRegView.setText((bikes.get(activeBike).registration));

            // show only 2 decimal places.  Precision is declared in MainActivity to 2 decimal places
            String spend = currencySetting + precision.format(calculateMaintSpend(bikes.get(activeBike)));
            amountSpent.setText(spend);
            bikeEstMileage.setText("tbc");
            if (bikes.get(activeBike).estMileage > 0) {
                Double estMile = bikes.get(activeBike).estMileage;
                // check what setting the user has, Miles or Km
                // if Km, convert to Miles for display
                String unit = "";
                if (milesSetting.equals("Km")) {
                    estMile = estMile / conversion;
                    unit = " Km";
                } else {
                    unit = " Mi";
                }
                bikeEstMileage.setText(oneDecimal.format(estMile) + unit);
            }
            bikeNotes.setText(bikes.get(activeBike).notes);

            // check if an MOT date is set
            Log.i("MOT Due ", bikes.get(activeBike).MOTdue);
            MOTdue.setText(bikes.get(activeBike).MOTdue);
            Calendar testDate = new GregorianCalendar();
            if (MainActivity.checkInRange(bikes.get(activeBike).MOTdue, testDate)) {
                MOTdue.setBackground(getResources().getDrawable(com.androidandyuk.autobuddy.R.drawable.rounded_corners_red));
            } else {
                MOTdue.setBackground(getResources().getDrawable(com.androidandyuk.autobuddy.R.drawable.rounded_corners_grey_orange));
            }
//            }

            // check if a Service date is set
            Log.i("Service Due ", bikes.get(activeBike).serviceDue);
//            if (bikes.get(activeBike).serviceDue != null) {
            serviceDue.setText(bikes.get(activeBike).serviceDue);
            testDate = new GregorianCalendar();
            if (MainActivity.checkInRange(bikes.get(activeBike).serviceDue, testDate)) {
                serviceDue.setBackground(getResources().getDrawable(com.androidandyuk.autobuddy.R.drawable.rounded_corners_red));
            } else {
                serviceDue.setBackground(getResources().getDrawable(com.androidandyuk.autobuddy.R.drawable.rounded_corners_grey_orange));
            }

            // check Tax is due this month

            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int taxMonth = getEnumPos(bikes.get(activeBike).taxDue);
            Log.i("Current Month", "" + Calendar.getInstance().get(Calendar.MONTH));
            Log.i("Tax Month", "" + getEnumPos(bikes.get(activeBike).taxDue));
            if (currentMonth == taxMonth) {
                taxDue.setBackground(getResources().getDrawable(com.androidandyuk.autobuddy.R.drawable.rounded_corners_red));
            } else {
                taxDue.setBackground(getResources().getDrawable(com.androidandyuk.autobuddy.R.drawable.rounded_corners_grey_orange));
            }
        }
    }

    public void checkBackground() {
        main = (ConstraintLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Garage.main.setBackground(drawablePic);
        } else {
            Garage.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
        }
    }

    public void setMOTdue(View view) {
        // this sets what date will show when the date picker shows
        Date thisDate = new Date();
        if (activeBike > -1) {
            try {
                thisDate = sdf.parse(bikes.get(activeBike).MOTdue);
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
                    Garage.this,
                    com.androidandyuk.autobuddy.R.style.datepicker,
                    MOTDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
            dialog.show();
        }
    }

    public void setServiceDue(View view) {
        // this sets what date will show when the date picker shows
        Date thisDate = new Date();
        if (activeBike > -1) {
            try {
                thisDate = sdf.parse(bikes.get(activeBike).serviceDue);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // for some reasone I can't getYear from thisDate, so will just use the current year
            Calendar cal = Calendar.getInstance();
            cal.setTime(thisDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    Garage.this,
                    com.androidandyuk.autobuddy.R.style.datepicker,
                    serviceDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
            dialog.show();
        }
    }

    public static double calculateMaintSpend(Bike bike) {
        Log.i("Garage", "Calculating Spend on " + bike);
        Log.i("Number of logs", "" + bike.maintenanceLogs.size());
        double spend = 0;
        for (maintenanceLogDetails log : bike.maintenanceLogs) {
            Log.i("Price", "" + log.price);
            spend += log.price;
        }
        return spend;
    }

    public void TextViewClicked(View view) {
        if (activeBike > -1) {
            getDetails("Enter Reg");
        }
    }

    public void addBike(View view) {
        Log.i("Bike", "Add bike");
        View addingBikeInfo = findViewById(com.androidandyuk.autobuddy.R.id.addingBikeInfo);

        // clear out any previous details
        bikeMake.setText("");
        bikeModel.setText("");
        bikeYear.setText("");

        addingBikeInfo.setVisibility(View.VISIBLE);
    }

    public void addNewBike(View view) {

        String make = bikeMake.getText().toString();
        String model = bikeModel.getText().toString();
        String year = bikeYear.getText().toString();

        // check enough details are entered
        if (make.isEmpty() || model.isEmpty() || year.isEmpty()) {

            Toast.makeText(Garage.this, "Please complete all necessary details", Toast.LENGTH_LONG).show();

        } else {

            // check the year looks correct
            if (Integer.parseInt(year) > 1900 && Integer.parseInt(year) < 2050) {
                Bike newBike = new Bike(make, model, year);

                bikes.add(newBike);

                activeBike = bikes.size() - 1;
                saveBikes();
                garageSetup();

                // hide keyboard
                View viewAddBike = this.getCurrentFocus();
                if (viewAddBike != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewAddBike.getWindowToken(), 0);
                }
                View addingBikeInfo = findViewById(com.androidandyuk.autobuddy.R.id.addingBikeInfo);
                addingBikeInfo.setVisibility(View.INVISIBLE);
                invalidateOptionsMenu();

            } else {

                Toast.makeText(Garage.this, "That year looks unlikely", Toast.LENGTH_LONG).show();

            }

        }
    }

    public void deleteBike(View view) {

        if (activeBike > -1) {
            Log.i("Delete Bike", "" + bikes.get(activeBike));

            // add warning
            new AlertDialog.Builder(Garage.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure?")
                    .setMessage("You're about to remove this bike and all it's data forever...")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i("Removing", "Bike");
                            bikes.get(activeBike).fuelings.clear();
                            bikes.get(activeBike).maintenanceLogs.clear();
                            bikes.remove(activeBike);
                            MainActivity.saveBikes();
                            Maintenance.saveLogs();
                            Fuelling.saveFuels();
                            activeBike = bikes.size() - 1;
                            Toast.makeText(Garage.this, "Removed!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        }
    }

    public void getDetails(String hint) {
        Log.i("Get Details", hint);
//        final String[] detail = new String[1];
        final View getDetails = findViewById(com.androidandyuk.autobuddy.R.id.getDetails);
        getDetails.setVisibility(View.VISIBLE);
        final EditText reg = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.getDetailsText);
        reg.setHint(hint);

        reg.setFocusableInTouchMode(true);
        reg.requestFocus();

        reg.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    detail = reg.getText().toString().toUpperCase();
                    getDetails.setVisibility(View.INVISIBLE);
                    bikes.get(activeBike).registration = detail;
                    myRegView = (TextView) findViewById(com.androidandyuk.autobuddy.R.id.clickable_reg_view);
                    myRegView.setText(detail);
                    saveBikes();
                    return true;
                }
                return false;
            }
        });
    }

    public void goToMaintenanceLog(View view) {
        if (activeBike > -1) {
            Intent intent = new Intent(getApplicationContext(), Maintenance.class);
            startActivity(intent);
        }
    }

    public void goToPartsLog(View view) {
        if (activeBike > -1) {
            Intent intent = new Intent(getApplicationContext(), PartsLog.class);
            startActivity(intent);
        }
    }

    public void goToToDo(View view) {
        if (activeBike > -1) {
            Intent intent = new Intent(getApplicationContext(), ToDo.class);
            startActivity(intent);
        }
    }

    public void cantSetMileage(View view) {
        Toast.makeText(Garage.this, "Mileage is determined from entries in logs and fuel ups, not set here", Toast.LENGTH_LONG).show();
    }

    public void cantSetMPG(View view) {
        Toast.makeText(Garage.this, "MPG is calculated from your fuel logs, not set here", Toast.LENGTH_LONG).show();
    }

    public void cantSetSpent(View view) {
        Toast.makeText(Garage.this, "Costs are calculated from your maintenance logs, not set here", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //assign the views that could be showing, to check if they are showing when back is pressed
            View addingBikeInfo = findViewById(com.androidandyuk.autobuddy.R.id.addingBikeInfo);
            View getDetails = findViewById(com.androidandyuk.autobuddy.R.id.getDetails);
            if (addingBikeInfo.isShown() || getDetails.isShown()) {
                addingBikeInfo.setVisibility(View.INVISIBLE);
                getDetails.setVisibility(View.INVISIBLE);
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.androidandyuk.autobuddy.R.menu.bike_choice, menu);
        super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, "Settings").setShortcut('3', 'c');

        for (int i = 0; i < bikes.size(); i++) {
            String bikeMakeMenu = bikes.get(i).model;
            menu.add(0, i + 1, 0, bikeMakeMenu).setShortcut('3', 'c');
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (activeBike > -1) {
            // save any changes in Bike notes
            bikeNotes = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.bikeNotes);
            bikes.get(activeBike).notes = bikeNotes.getText().toString();
        }
        // change to bike selected
        switch (item.getItemId()) {
            case 0:
                Log.i("Option", "0");
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "Settings not yet implemented", Toast.LENGTH_LONG).show();
                return true;
            case 1:
                Log.i("Option", "2");
                activeBike = 0;
                garageSetup();
                return true;
            case 2:
                Log.i("Option", "2");
                activeBike = 1;
                garageSetup();
                return true;
            case 3:
                Log.i("Option", "3");
                activeBike = 2;
                garageSetup();
                return true;
            case 4:
                Log.i("Option", "4");
                activeBike = 3;
                garageSetup();
                return true;
            case 5:
                Log.i("Option", "5");
                activeBike = 4;
                garageSetup();
                return true;
            case 6:
                Log.i("Option", "6");
                activeBike = 5;
                garageSetup();
                return true;
            case 7:
                Log.i("Option", "7");
                activeBike = 6;
                garageSetup();
                return true;
            case 8:
                Log.i("Option", "8");
                activeBike = 7;
                garageSetup();
                return true;
            case 9:
                Log.i("Option", "9");
                activeBike = 8;
                garageSetup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        taxDue = (Spinner) findViewById(com.androidandyuk.autobuddy.R.id.taxSpinner);
        String thisTaxDue = taxDue.getSelectedItem().toString();
        Log.i("Tax is due", thisTaxDue);
        bikeNotes = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.bikeNotes);
        // check there's actually a bike before saving the notes
        if (bikeNotes != null && bikes.size() > 0) {
            bikes.get(activeBike).notes = bikeNotes.getText().toString();
            bikes.get(activeBike).taxDue = thisTaxDue;
        }

//        myRegView = (EditText) findViewById(R.id.hidden_reg_view);
//        // check there's actually a bike before saving the reg
//        if (!myRegView.getText().toString().equals("") && bikes.size() > 0) {
//            bikes.get(activeBike).registration = myRegView.getText().toString();
//            Log.i("Setting Reg", " to " + myRegView.getText().toString());
//        }
        saveBikes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        garageSetup();
        checkBackground();
    }
}