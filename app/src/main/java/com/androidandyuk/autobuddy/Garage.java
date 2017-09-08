package com.androidandyuk.autobuddy;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static com.androidandyuk.autobuddy.Fuelling.loadFuels;
import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.currencySetting;
import static com.androidandyuk.autobuddy.MainActivity.currentForecast;
import static com.androidandyuk.autobuddy.MainActivity.jsonObject;
import static com.androidandyuk.autobuddy.MainActivity.locationListener;
import static com.androidandyuk.autobuddy.MainActivity.locationManager;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.precision;
import static com.androidandyuk.autobuddy.MainActivity.saveBikes;
import static com.androidandyuk.autobuddy.MainActivity.saveSettings;
import static com.androidandyuk.autobuddy.MainActivity.sdf;
import static com.androidandyuk.autobuddy.MainActivity.user;
import static com.androidandyuk.autobuddy.MainActivity.userLatLng;
import static com.androidandyuk.autobuddy.MainActivity.userLocationForWeather;
import static com.androidandyuk.autobuddy.Maintenance.loadLogs;

public class Garage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String TAG = "MainActivity";

    private AdView mAdView;

    public static RelativeLayout main;

    public static EditText bikeMake;
    public static EditText bikeModel;
    public static EditText bikeYear;
    TextView aveMPG;
    TextView milesDone;
    TextView bikeEstMileage;
    TextView costPerMile;
    TextView myRegView;
    TextView MOTdue;
    TextView serviceDue;
    Spinner taxDue;

    public static ImageView shield;

    public static View addingBikeInfo;
    public static View getDetailsView;
    public static Boolean editingBike = false;

    //    TextView bikeTitle;
    EditText bikeNotes;
    TextView toolbarTitle;

    String detail;

    private DatePickerDialog.OnDateSetListener MOTDateSetListener;
    private DatePickerDialog.OnDateSetListener serviceDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        if (bikes.size() == 0) {
//            // for testing
//            Bike newBike = new Bike("KTM", "Superduke R", "2016");
//            bikes.add(newBike);
//            Bike newBike2 = new Bike("Honda", "CB1000R", "2011");
//            bikes.add(newBike2);
//        }

        // for adding a new bike
        bikeMake = (EditText) findViewById(R.id.bikeMake);
        bikeModel = (EditText) findViewById(R.id.bikeModel);
        bikeYear = (EditText) findViewById(R.id.bikeYear);
        bikeEstMileage = (TextView) findViewById(R.id.estMileage);
        costPerMile = (TextView) findViewById(R.id.costPerMile);
        myRegView = (TextView) findViewById(R.id.clickable_reg_view);
        MOTdue = (TextView) findViewById(R.id.MOTdue);
        serviceDue = (TextView) findViewById(R.id.serviceDue);
        taxDue = (Spinner) findViewById(R.id.taxSpinner);

        getDetailsView = findViewById(R.id.getDetails);
        addingBikeInfo = findViewById(R.id.addingBikeInfo);

        shield = (ImageView) findViewById(R.id.shield);

        taxDue.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item_regular, TaxDue.values()));

        if (activeBike > -1) {
            int thisTax = getEnumPos(bikes.get(activeBike).taxDue);
            Log.i("onCreate taxDue", bikes.get(activeBike).taxDue + " " + thisTax);
            taxDue.setSelection(thisTax - 1);
        }

        Log.i("Active Bike", "" + activeBike);

        loadLogs();
        garageSetup();
        setListeners();

//         Hides the soft keyboard
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }


        //      download the weather
        DownloadTask task = new DownloadTask();

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

        userLatLng = new LatLng(51.5412794, -0.2799549);  //  default to Ace Cafe until location is overwritten

        user = new markedLocation("You", "", userLatLng, "");


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10800000, 1000, locationListener);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Log.i("lastKnownLocation", "" + lastKnownLocation);
        if (lastKnownLocation != null) {
            user.setLocation(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
        }

        if (user.location != null) {
            //change this to be users location
            double userLat = user.location.latitude;
            double userLon = user.location.longitude;
            String userLocation = "lat=" + userLat + "&lon=" + userLon;
            userLocationForWeather = "http://api.openweathermap.org/data/2.5/weather?" + userLocation + "&APPID=81e5e0ca31ad432ee9153dd761ed3b27";
            Log.i("Getting Weather", userLocationForWeather);
            task.execute(userLocationForWeather);

        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DO SOMETHING HERE
                nextBike(view);
            }
        });




    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;

                    result += current;

                    data = reader.read();

                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {

                    jsonObject = new JSONObject(result);

                    String weatherInfo = jsonObject.getString("weather");

                    Log.i("Weather content", weatherInfo);

                    JSONArray arr = new JSONArray(weatherInfo);

                    for (int i = 0; i < arr.length(); i++) {

                        JSONObject jsonPart = arr.getJSONObject(i);

                        Log.i("main", jsonPart.getString("main"));
                        Log.i("description", jsonPart.getString("description"));

                        currentForecast = jsonPart.getString("main");
                        showWeather();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


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

    public void showWeather() {
        Snackbar.make(findViewById(R.id.main), "Today's Forecast is : " + currentForecast, Snackbar.LENGTH_LONG)
                .setAction("Open BBC Weather", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = "https://m.bbc.co.uk/weather";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                }).show();
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

            serviceDateSetListener = new DatePickerDialog.OnDateSetListener() {
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
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
//        bikeTitle = (TextView) findViewById(R.id.bikeTitle);
//        regSwitcher = (ViewSwitcher) findViewById(R.id.regSwitcher);
        aveMPG = (TextView) findViewById(R.id.aveMPG);
        bikeEstMileage = (TextView) findViewById(R.id.estMileage);
        costPerMile = (TextView) findViewById(R.id.costPerMile);
        bikeNotes = (EditText) findViewById(R.id.bikeNotes);
        bikeNotes.setSelected(false);
        myRegView = (TextView) findViewById(R.id.clickable_reg_view);
        milesDone = (TextView) findViewById(R.id.milesDoneTV);
        MOTdue = (TextView) findViewById(R.id.MOTdue);
        serviceDue = (TextView) findViewById(R.id.serviceDue);
        taxDue = (Spinner) findViewById(R.id.taxSpinner);


        setListeners();
        calcEstMileage();

        // check the user has a bike, then set all the views to it's current details
        if (bikes.size() > 0) {
            toolbarTitle.setText(bikes.get(activeBike).yearOfMan + " " + bikes.get(activeBike).model);
//            bikeTitle.setText(bikes.get(activeBike).yearOfMan + " " + bikes.get(activeBike).model);

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            TextView navMake = (TextView) headerView.findViewById(R.id.nav_tv_make);
            navMake.setText(bikes.get(activeBike).make);
            TextView navModel = (TextView) headerView.findViewById(R.id.nav_tv_model);
            navModel.setText(bikes.get(activeBike).model);
            TextView navYear = (TextView) headerView.findViewById(R.id.nav_tv_year);
            navYear.setText(bikes.get(activeBike).yearOfMan);

            Log.i("Active Bike Reg", " " + (bikes.get(activeBike).registration));

            myRegView.setText((bikes.get(activeBike).registration));

            Log.i("garageSetup taxDue", bikes.get(activeBike).taxDue);
            Log.i("position", "" + getEnumPos(bikes.get(activeBike).taxDue));

            taxDue.setSelection(getEnumPos(bikes.get(activeBike).taxDue) - 1);

            // show only 2 decimal places.  Precision is declared in MainActivity to 2 decimal places
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            cal.set(Calendar.YEAR, year - 1);
            Date lastYear = cal.getTime();

            TextView mDTV = (TextView) findViewById(R.id.milesDone);
            TextView cpm = (TextView) findViewById(R.id.spentText);

            String unit = " Mi";
            Double odometer = milesSince(lastYear);
            if (milesSetting.equals("Km")) {
                mDTV.setText("KM Done");
                cpm.setText("Cost Per KM");
                odometer = odometer / conversion;
                unit = " Km";
            } else {
                mDTV.setText("Miles Done");
                cpm.setText("Cost Per Mile");
                unit = " Mi";
            }
            String mD = oneDecimal.format(odometer) + unit;
            milesDone.setText(mD);

            Double distance = milesSince(lastYear);
            if (milesSetting.equals("Km")) {
                distance = distance / conversion;
            }
            costPerMile.setText(currencySetting + precision.format((maintSpentSince(lastYear) + petrolSpentSince(lastYear)) / distance));

            aveMPG.setText(precision.format(milesSince(lastYear) / (litresSince(lastYear) / 4.54609)));

            bikeEstMileage.setText("tbc");
            if (bikes.get(activeBike).estMileage > 0) {
                Double estMile = bikes.get(activeBike).estMileage;
                // check what setting the user has, Miles or Km
                // if Km, convert to Miles for display

                if (milesSetting.equals("Km")) {
                    estMile = estMile / conversion;
                }
                bikeEstMileage.setText(oneDecimal.format(estMile) + unit);
            }
            bikeNotes.setText(bikes.get(activeBike).notes);

            // check if an MOT date is set
            Log.i("MOT Due ", bikes.get(activeBike).MOTdue);
            MOTdue.setText(bikes.get(activeBike).MOTdue);
            Calendar testDate = new GregorianCalendar();
            if (MainActivity.checkInRange(bikes.get(activeBike).MOTdue, testDate)) {
                MOTdue.setBackground(getResources().getDrawable(R.drawable.rounded_corners_red));
            } else {
                MOTdue.setBackground(null);
            }
//            }

            // check if a Service date is set
            Log.i("Service Due ", bikes.get(activeBike).serviceDue);
//            if (bikes.get(activeBike).serviceDue != null) {
            serviceDue.setText(bikes.get(activeBike).serviceDue);
            testDate = new GregorianCalendar();
            if (MainActivity.checkInRange(bikes.get(activeBike).serviceDue, testDate)) {
                serviceDue.setBackground(getResources().getDrawable(R.drawable.rounded_corners_red));
            } else {
                serviceDue.setBackground(null);
            }

            // check Tax is due this month

            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int taxMonth = getEnumPos(bikes.get(activeBike).taxDue);
            Log.i("Current Month", "" + Calendar.getInstance().get(Calendar.MONTH));
            Log.i("Tax Month", "" + getEnumPos(bikes.get(activeBike).taxDue));
            if (currentMonth == taxMonth) {
                taxDue.setBackground(getResources().getDrawable(R.drawable.rounded_corners_red));
            } else {
                taxDue.setBackground(null);
            }
//            bikeTitle.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    editBike();
//                    return true;
//                }
//            });
        }
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Garage.main.setBackground(drawablePic);
        } else {
            Garage.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    public void shieldClicked(View view) {
        if (addingBikeInfo.isShown() || getDetailsView.isShown()) {
            addingBikeInfo.setVisibility(View.INVISIBLE);
            getDetailsView.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);
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
                    R.style.datepicker,
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

            // for some reason I can't getYear from thisDate, so will just use the current year
            Calendar cal = Calendar.getInstance();
            cal.setTime(thisDate);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    Garage.this,
                    R.style.datepicker,
                    serviceDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
            dialog.show();
        }
    }

    public static double calculateMaintSpend(Bike bike) {
        Log.i("Number of logs", "" + bike.maintenanceLogs.size());
        double spend = 0;
        for (maintenanceLogDetails log : bike.maintenanceLogs) {
            Log.i("Price", "" + log.price);
            spend += log.price;
        }
        return spend;
    }

    public static double maintSpentSince(Date date) {
        Double spendCount = 0d;
        for (int i = 0; i < bikes.get(activeBike).maintenanceLogs.size(); i++) {
            Date testDate = null;
            try {
                testDate = sdf.parse(bikes.get(activeBike).maintenanceLogs.get(i).date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (testDate.after(date)) {
                spendCount += bikes.get(activeBike).maintenanceLogs.get(i).getPrice();
            }
        }
        return spendCount;
    }

    public static double petrolSpentSince(Date date) {
        Double petrolCount = 0d;
        for (int i = 0; i < bikes.get(activeBike).fuelings.size(); i++) {
            Date testDate = null;
            try {
                testDate = sdf.parse(bikes.get(activeBike).fuelings.get(i).date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (testDate.after(date)) {
                petrolCount += (bikes.get(activeBike).fuelings.get(i).price * bikes.get(activeBike).fuelings.get(i).litres);
            }
        }
        return petrolCount;
    }

    public static double litresSince(Date date) {
        Double petrolCount = 0d;
        for (int i = 0; i < bikes.get(activeBike).fuelings.size(); i++) {
            Date testDate = null;
            try {
                testDate = sdf.parse(bikes.get(activeBike).fuelings.get(i).date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (testDate.after(date)) {
                petrolCount += (bikes.get(activeBike).fuelings.get(i).litres);
            }
        }
        return petrolCount;
    }

    public void calcEstMileage() {
        if (activeBike > -1) {
            loadLogs();
            loadFuels();
            Log.i("Garage", "calcEstMileage");
            Bike thisBike = bikes.get(activeBike);
            Date lastFuelDate = new Date(90, 1, 1);
//            double lastMaintMileage = 0;
            double lastFuelMileage = 0;

            int fuelLogs = thisBike.fuelings.size();

            //find the last fuel log with a mileage
            for (int i = fuelLogs - 1; i >= 0; i--) {
                if (thisBike.fuelings.get(i).mileage > 1) {
                    try {
                        lastFuelDate = sdf.parse(thisBike.fuelings.get(i).date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    lastFuelMileage = thisBike.fuelings.get(i).mileage;
                }
            }
            Log.i("LastFuel", "Mileage " + lastFuelMileage);

            // now add any miles from fuel ups that happened after the last date
            thisBike.estMileage = lastFuelMileage + milesSince(lastFuelDate);
        }
    }

    public Double milesSince(Date date) {
        Double countMileage = 0d;
        for (int i = 0; i < bikes.get(activeBike).fuelings.size(); i++) {
            Date testDate = null;
            try {
                testDate = sdf.parse(bikes.get(activeBike).fuelings.get(i).date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (testDate.after(date)) {
                countMileage += bikes.get(activeBike).fuelings.get(i).miles;
            }
        }
        return countMileage;
    }

    public void TextViewClicked(View view) {
        if (activeBike > -1) {
            getDetails("Enter Reg");
        }
    }

    public void addBike(View view) {
        Log.i("Bike", "Add bike");

        // clear out any previous details
        bikeMake.setText("");
        bikeModel.setText("");
        bikeYear.setText("");

        addingBikeInfo.setVisibility(View.VISIBLE);
        shield.setVisibility(View.VISIBLE);

    }

    public void nextBike(View view) {
        Log.i("Next Bike", "activeBike " + activeBike);
        activeBike++;
        if (activeBike > (bikes.size() - 1)) {
            activeBike = 0;
        }
        garageSetup();
    }

    public void editBike() {
        if (activeBike >= 0) {
            editingBike = true;
            addingBikeInfo.setVisibility(View.VISIBLE);
            shield.setVisibility(View.VISIBLE);
            bikeMake.setText(bikes.get(activeBike).make);
            bikeModel.setText(bikes.get(activeBike).model);
            bikeYear.setText(bikes.get(activeBike).yearOfMan);
        }

    }

    public void addNewBike(View view) {
        changingBikes();
        String make = bikeMake.getText().toString();
        String model = bikeModel.getText().toString();
        String year = bikeYear.getText().toString();

        // check enough details are entered
        if (make.isEmpty() || model.isEmpty() || year.isEmpty()) {

            Toast.makeText(Garage.this, "Please complete all necessary details", Toast.LENGTH_LONG).show();

        } else {
            // check the year looks correct
            if (Integer.parseInt(year) > 1900 && Integer.parseInt(year) < 2100) {
                if (editingBike) {

                    bikes.get(activeBike).make = make;
                    bikes.get(activeBike).model = model;
                    bikes.get(activeBike).yearOfMan = year;
                    editingBike = false;

                } else {
                    // we're not editing, so it must be a new bike
                    Bike newBike = new Bike(make, model, year);

                    bikes.add(newBike);

                    activeBike = bikes.size() - 1;

                }

                Log.i("Adding Bike", "Global Code");
                // hide keyboard
                View viewAddBike = this.getCurrentFocus();
                if (viewAddBike != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(viewAddBike.getWindowToken(), 0);
                }

                addingBikeInfo.setVisibility(View.INVISIBLE);
                shield.setVisibility(View.INVISIBLE);
                saveBikes();
                saveSettings();
                invalidateOptionsMenu();
                garageSetup();

            } else {

                Toast.makeText(Garage.this, "That year looks unlikely", Toast.LENGTH_LONG).show();

            }

        }
    }

    public void deleteBike() {

        if (activeBike > -1) {
            Log.i("Delete Bike", "" + bikes.get(activeBike));

            // add warning
            new AlertDialog.Builder(Garage.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure?")
                    .setMessage("You're about to remove this vehicle and all it's data forever...")
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
        getDetailsView.setVisibility(View.VISIBLE);
        shield.setVisibility(View.VISIBLE);
        final EditText reg = (EditText) findViewById(R.id.getDetailsText);
        reg.setHint(hint);
        if (!bikes.get(activeBike).registration.equals("unknown")) {
            reg.setText("");
            reg.append(bikes.get(activeBike).registration);
        }

        reg.setFocusableInTouchMode(true);
        reg.requestFocus();

        reg.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    detail = reg.getText().toString().toUpperCase();
                    getDetailsView.setVisibility(View.INVISIBLE);
                    shield.setVisibility(View.INVISIBLE);
                    bikes.get(activeBike).registration = detail;
                    myRegView = (TextView) findViewById(R.id.clickable_reg_view);
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

    public void goToFuelling(View view) {
        if (activeBike > -1) {
            Intent intent = new Intent(getApplicationContext(), Fuelling.class);
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

    public void changingBikes() {
        taxDue = (Spinner) findViewById(R.id.taxSpinner);
        String thisTaxDue = taxDue.getSelectedItem().toString();
        Log.i("Changing Bikes taxDue", thisTaxDue);
        bikeNotes = (EditText) findViewById(R.id.bikeNotes);
        // check there's actually a bike before saving the notes
        if (bikeNotes != null && bikes.size() > 0) {
            bikes.get(activeBike).notes = bikeNotes.getText().toString();
            bikes.get(activeBike).taxDue = thisTaxDue;
        }
        saveBikes();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.bike_choice, menu);
//        super.onCreateOptionsMenu(menu);
//
//        menu.add(0, 0, 0, "Settings").setShortcut('3', 'c');
//        int i;
//        for (i = 0; i < bikes.size(); i++) {
//            String bikeMakeMenu = bikes.get(i).model;
//            menu.add(0, i + 1, 0, bikeMakeMenu).setShortcut('3', 'c');
//        }
//        menu.add(0, 10, 0, "Annual Reports").setShortcut('3', 'c');
//        menu.add(0, 11, 0, "Delete Vehicle").setShortcut('3', 'c');
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        changingBikes();
//        if (activeBike > -1) {
//            // save any changes in Bike notes
//            bikeNotes = (EditText) findViewById(R.id.bikeNotes);
//            bikes.get(activeBike).notes = bikeNotes.getText().toString();
//        }
//        // change to bike selected
//        switch (item.getItemId()) {
//            case 0:
//                Log.i("Option", "0");
//                Intent intent = new Intent(getApplicationContext(), Settings.class);
//                startActivity(intent);
////                Toast.makeText(MainActivity.this, "Settings not yet implemented", Toast.LENGTH_LONG).show();
//                return true;
//            case 1:
//                Log.i("Option", "2");
//                activeBike = 0;
//                garageSetup();
//                return true;
//            case 2:
//                Log.i("Option", "2");
//                activeBike = 1;
//                garageSetup();
//                return true;
//            case 3:
//                Log.i("Option", "3");
//                activeBike = 2;
//                garageSetup();
//                return true;
//            case 4:
//                Log.i("Option", "4");
//                activeBike = 3;
//                garageSetup();
//                return true;
//            case 5:
//                Log.i("Option", "5");
//                activeBike = 4;
//                garageSetup();
//                return true;
//            case 6:
//                Log.i("Option", "6");
//                activeBike = 5;
//                garageSetup();
//                return true;
//            case 7:
//                Log.i("Option", "7");
//                activeBike = 6;
//                garageSetup();
//                return true;
//            case 8:
//                Log.i("Option", "8");
//                activeBike = 7;
//                garageSetup();
//                return true;
//            case 9:
//                Log.i("Option", "9");
//                activeBike = 8;
//                garageSetup();
//                return true;
//            case 10:
//                Log.i("Option", "10");
//                Intent thisIntent = new Intent(getApplicationContext(), AnnualReports.class);
//                startActivity(thisIntent);
//                return true;
//            case 11:
//                Log.i("Option", "11");
//                deleteBike();
//                garageSetup();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //assign the views that could be showing, to check if they are showing when back is pressed
            final View addingBikeInfo = findViewById(R.id.addingBikeInfo);
            final View getDetails = findViewById(R.id.getDetails);
            if (addingBikeInfo.isShown() || getDetails.isShown()) {

                // add warning
                new AlertDialog.Builder(Garage.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Discard Current Details?")
                        .setMessage("Would you like to discard the current information?")
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addingBikeInfo.setVisibility(View.INVISIBLE);
                                getDetails.setVisibility(View.INVISIBLE);
                                shield.setVisibility(View.INVISIBLE);
                            }
                        })
                        .setNegativeButton("Keep", null)
                        .show();


            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        changingBikes();
        saveSettings();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Garage", "onResume");
        garageSetup();
        checkBackground();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_garage) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_maint) {

            Intent intent = new Intent(getApplicationContext(), Maintenance.class);
            startActivity(intent);

        } else if (id == R.id.nav_fuel) {

            Intent intent = new Intent(getApplicationContext(), Fuelling.class);
            startActivity(intent);

        } else if (id == R.id.nav_todo) {

            Intent intent = new Intent(getApplicationContext(), ToDo.class);
            startActivity(intent);

        } else if (id == R.id.nav_parts) {

            Intent intent = new Intent(getApplicationContext(), PartsLog.class);
            startActivity(intent);

        } else if (id == R.id.nav_settings) {

            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);

        } else if (id == R.id.nav_reports) {

            Intent intent = new Intent(getApplicationContext(), AnnualReports.class);
            startActivity(intent);

        } else if (id == R.id.nav_favs) {

            Intent intent = new Intent(getApplicationContext(), Favourites.class);
            startActivity(intent);

        } else if (id == R.id.nav_track) {

            Intent intent = new Intent(getApplicationContext(), RaceTracks.class);
            startActivity(intent);

        } else if (id == R.id.nav_biker) {

            Intent intent = new Intent(getApplicationContext(), HotSpots.class);
            startActivity(intent);

        } else if (id == R.id.nav_shows) {

            Intent intent = new Intent(getApplicationContext(), CarShows.class);
            startActivity(intent);

        } else if (id == R.id.nav_traffic) {

            Intent intent = new Intent(getApplicationContext(), Traffic.class);
            startActivity(intent);

        } else if (id == R.id.nav_delete) {

            deleteBike();

        } else if (id == R.id.nav_backup) {

            Settings.exportDB();

        } else if (id == R.id.nav_restore) {

            Settings.importDB();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}