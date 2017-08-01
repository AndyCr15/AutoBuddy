package com.androidandyuk.autobuddy;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.lastHowManyFuels;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.sdf;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;
import static com.androidandyuk.autobuddy.MainActivity.vehiclesDB;

public class Fuelling extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AdView mAdView;

    public static RelativeLayout main;

    static MyFuelAdapter myAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private DatePickerDialog.OnDateSetListener fuelDateSetListener;

    String fuelingDate;

    ListView listView;
    EditText milesDone;
    EditText petrolPrice;
    EditText litresUsed;
    EditText mileageText;
    TextView setFuelDate;
    TextView milesDoneTV;

    public static ImageView shield;

    View fuelingDetailsLayout;
    View fuelSummary;

    // used to store what item might be being edited or deleted
    int itemLongPressedPosition = -1;
    fuellingDetails itemLongPressed = null;
    String editDate = "";

    double miles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fueling);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        fuelingDetailsLayout = findViewById(R.id.fuelingDetailsLayout);
        shield = (ImageView) findViewById(R.id.shield);
        fuelSummary = findViewById(R.id.fuelSummary);

        main = (RelativeLayout) findViewById(R.id.main);

        setFuelDate = (TextView) findViewById(R.id.setFuelDate);
        milesDone = (EditText) findViewById(R.id.milesDone);
        petrolPrice = (EditText) findViewById(R.id.petrolPrice);
        litresUsed = (EditText) findViewById(R.id.litresUsed);
        mileageText = (EditText) findViewById(R.id.mileageET);

        milesDone.setHint(milesSetting + " done");

        Log.i("Fuelling", "Loading Fuels");
        loadFuels();

        initiateList();

        //needed for editing a fueling

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // this is for editing a fueling, it stores the info in itemLongPressed
                itemLongPressedPosition = position;
                itemLongPressed = bikes.get(activeBike).fuelings.get(position);
                Log.i("Fuel List", "Tapped " + position);
                Double thisMileage = bikes.get(activeBike).fuelings.get(position).getMileage();
                Double thisDone = bikes.get(activeBike).fuelings.get(position).getMiles();
                // check what setting the user has, Miles or Km
                // if Km, convert to Miles for display
                if (milesSetting.equals("Km")) {
                    thisMileage = thisMileage / conversion;
                    thisDone = thisDone / conversion;
                }
                milesDone.setText(oneDecimal.format(thisDone));
                petrolPrice.setText(Double.toString(bikes.get(activeBike).fuelings.get(position).getPrice()));
                litresUsed.setText(Double.toString(bikes.get(activeBike).fuelings.get(position).getLitres()));
                mileageText.setText(oneDecimal.format(thisMileage));
                editDate = bikes.get(activeBike).fuelings.get(position).getDate();
                setFuelDate.setText(editDate);
                fuelingDetailsLayout.setVisibility(View.VISIBLE);
                shield.setVisibility(View.VISIBLE);

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final int fuelPosition = position;
                final Context context = App.getContext();

                new AlertDialog.Builder(Fuelling.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("You're about to delete this log forever...")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("Removing", "Log " + fuelPosition);
                                bikes.get(activeBike).fuelings.remove(fuelPosition);
                                initiateList();
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }


        });


        fuelDateSetListener = new DatePickerDialog.OnDateSetListener()

        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar date = Calendar.getInstance();
                date.set(year, month, day);
                String sdfDate = sdf.format(date.getTime());
                Log.i("Chosen Date", sdfDate);
                setFuelDate.setText(sdfDate);
            }
        };
    }

    public void setFuelDate(View view) {
        String thisDateString = "";
        // this sets what date will show when the date picker shows
        // first check if we're editing a current fueling
        if (itemLongPressed != null) {
            thisDateString = bikes.get(activeBike).fuelings.get(itemLongPressedPosition).getDate();
        }
        Log.i("thisDateString", thisDateString);
        Date thisDate = new Date();
        try {
            thisDate = sdf.parse(thisDateString);
            Log.i("Parsed date", "" + thisDate);
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
                Fuelling.this,
                R.style.datepicker,
                fuelDateSetListener,
                year, month, day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));
        dialog.show();
    }

    private class MyFuelAdapter extends BaseAdapter {
        public ArrayList<fuellingDetails> fuelDataAdapter;

        public MyFuelAdapter(ArrayList<fuellingDetails> fuelDataAdapter) {
            this.fuelDataAdapter = fuelDataAdapter;
        }

        @Override
        public int getCount() {
            return fuelDataAdapter.size();
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
            View myView = mInflater.inflate(R.layout.fuel_listview, null);

            final fuellingDetails s = fuelDataAdapter.get(position);

            TextView fuelDate = (TextView) myView.findViewById(R.id.fuelDate);
            fuelDate.setText(s.date.substring(0, s.date.length() - 5));

            TextView milesDone = (TextView) myView.findViewById(R.id.milesDone);
            Double units = s.miles;
            if (milesSetting.equals("Km")) {
                units = units / conversion;
            }
            milesDone.setText(oneDecimal.format(units) + " " + milesSetting);

            TextView fuelMPG = (TextView) myView.findViewById(R.id.fuelMPG);
            fuelMPG.setText(oneDecimal.format(s.mpg) + "mpg");

            return myView;
        }

    }

    private void initiateList() {
        Log.i("Fuelling", "Initiating List");
        listView = (ListView) findViewById(R.id.fuelList);

        Log.i("Fuelling", "Setting myAdapter");
        myAdapter = new MyFuelAdapter(bikes.get(activeBike).fuelings);

        listView.setAdapter(myAdapter);

        updateAveMPG();

        setTitle("Fuelling: " + bikes.get(activeBike).model);

    }

    public void showFueling(View view) {
        // opens the add fueling dialog
        Log.i("Fuelling", "Adding fuel up");
        fuelingDetailsLayout.setVisibility(View.VISIBLE);
        shield.setVisibility(View.VISIBLE);

        // set the date for a new fueling to today
        Calendar date = Calendar.getInstance();
        String today = sdf.format(date.getTime());
        setFuelDate.setText(today);

//        // reset all info in the box. If it's an edit, this will be overwritten with info anyway
        milesDone.setText(null);
        milesDone.clearFocus();
        petrolPrice.setText(null);
        petrolPrice.clearFocus();
        litresUsed.setText(null);
        litresUsed.clearFocus();
        mileageText.setText(null);
        mileageText.clearFocus();
    }

    public static String aveMPG(int bikeID, int numberOfFuelings) {
        double totalMiles = 0;
        double totalLitres = 0;
        int count = 0;
        Bike thisBike = MainActivity.bikes.get(bikeID);
        if (numberOfFuelings > thisBike.fuelings.size()) {
            numberOfFuelings = thisBike.fuelings.size();
        }


        for (int i = 0; i < numberOfFuelings; i++) {
            count++;
            totalMiles += thisBike.fuelings.get(i).miles;
            totalLitres += thisBike.fuelings.get(i).litres;

        }
        Log.i("Calculating MPG", "" + count);
        if (totalLitres > 0) {
            double mpg = totalMiles / (totalLitres / 4.54609);
            return oneDecimal.format(mpg);
        } else return "No Fuels";
    }

    public void addFuelingClicked(View view) {
        addFueling();
    }

    public void addFueling() {

        // this ensures the current bike has a correct estimated mileage not including this trip
        new Garage().calcEstMileage();

        Double mileage = 0.0;
        fuellingDetails today;

        // only add the details if all three important details have information in
        if (petrolPrice.getText().toString().isEmpty() || litresUsed.getText().toString().isEmpty() || (milesDone.getText().toString().isEmpty() && mileageText.getText().toString().isEmpty())) {

            Toast.makeText(Fuelling.this, "Please complete all necessary details", Toast.LENGTH_LONG).show();

        } else {

            if(milesDone.getText().toString().isEmpty()){
                miles = Double.parseDouble(mileageText.getText().toString()) - bikes.get(activeBike).estMileage;
            } else {
                miles = Double.parseDouble(milesDone.getText().toString());
            }

            if(mileageText.getText().toString().isEmpty()){
                mileage = bikes.get(activeBike).estMileage + Double.parseDouble(milesDone.getText().toString());
            } else {
                mileage = Double.parseDouble(mileageText.getText().toString());
            }

            String date = setFuelDate.getText().toString();

            // check what setting the user has, Miles or Km
            // if Km, convert to Miles for storage
            if (milesSetting.equals("Km")) {
                miles = miles * conversion;
                mileage = mileage * conversion;
            }
            double price = Double.parseDouble(petrolPrice.getText().toString());
            double litres = Double.parseDouble(litresUsed.getText().toString());


            // check if we're adding as it was being edited
            if (itemLongPressed != null) {
                // adding back in an edited log, so remove the old one
                bikes.get(activeBike).fuelings.remove(itemLongPressed);
            }


            today = new fuellingDetails(miles, price, litres, date, mileage);
            bikes.get(activeBike).fuelings.add(today);
            Collections.sort(bikes.get(activeBike).fuelings);
            myAdapter.notifyDataSetChanged();
            fuelingDetailsLayout.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);

            TextView mpgSummary = (TextView) findViewById(R.id.mpgSummary);
            double mpgSum = miles / (litres / 4.54609);
            mpgSummary.setText(oneDecimal.format(mpgSum));
            fuelSummary.setVisibility(View.VISIBLE);
            shield.setVisibility(View.VISIBLE);

            updateAveMPG();

            View thisView = this.getCurrentFocus();
            if (thisView != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(thisView.getWindowToken(), 0);
            }

            saveFuels();
            // clear previous entries
            milesDone.setText(null);
            milesDone.clearFocus();
            petrolPrice.setText(null);
            petrolPrice.clearFocus();
            litresUsed.setText(null);
            litresUsed.clearFocus();
        }
        Log.i("Reset", "itemLongPressed");
        itemLongPressed = null;
        itemLongPressedPosition = -1;
    }

    public void hideSummary(View view) {
        fuelSummary.setVisibility(View.INVISIBLE);
    }

    public void updateAveMPG() {

        TextView mpgView = (TextView) findViewById(R.id.mpgView);

        int fuelingsForAve = lastHowManyFuels;

        if (bikes.get(activeBike).fuelings.size() < fuelingsForAve) {
            fuelingsForAve = bikes.get(activeBike).fuelings.size();
        }
        String mpg = aveMPG(activeBike, fuelingsForAve);
        if (mpg.equals("No Fuels")) {
            mpgView.setText("Your average will appear here once you've recorded a refuel");
        } else {
            mpgView.setText("Ave MPG over the last " + fuelingsForAve + " stops is " + mpg + " mpg");
        }
    }

    public void shieldClicked(View view){
        if(fuelSummary.isShown()){
            fuelSummary.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);
        } else {
            if (!milesDone.getText().toString().equals("") || !petrolPrice.getText().toString().equals("") || !litresUsed.getText().toString().equals("") || !mileageText.getText().toString().equals("")) {
            // add warning
            new AlertDialog.Builder(Fuelling.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Discard Current Log?")
                    .setMessage("Would you like to discard the current information?")
                    .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fuelingDetailsLayout.setVisibility(View.INVISIBLE);
                            shield.setVisibility(View.INVISIBLE);
                            itemLongPressed = null;
                            itemLongPressedPosition = -1;
                            // reset the fuelling text boxes?
                        }
                    })
                    .setNegativeButton("Keep", null)
                    .show();
            } else {
                fuelingDetailsLayout.setVisibility(View.INVISIBLE);
                shield.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bike_choice, menu);

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

        switch (item.getItemId()) {
            case 0:
                Log.i("Option", "0");
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "Settings not yet implemented", Toast.LENGTH_LONG).show();
                return true;
            case 1:
                Log.i("Option", "1");
                activeBike = 0;
                initiateList();
                return true;
            case 2:
                Log.i("Option", "2");
                activeBike = 1;
                initiateList();
                return true;
            case 3:
                Log.i("Option", "3");
                activeBike = 2;
                initiateList();
                return true;
            case 4:
                Log.i("Option", "4");
                activeBike = 3;
                initiateList();
                return true;
            case 5:
                Log.i("Option", "5");
                activeBike = 4;
                initiateList();
                return true;
            case 6:
                Log.i("Option", "6");
                activeBike = 5;
                initiateList();
                return true;
            case 7:
                Log.i("Option", "7");
                activeBike = 6;
                initiateList();
                return true;
            case 8:
                Log.i("Option", "8");
                activeBike = 7;
                initiateList();
                return true;
            case 9:
                Log.i("Option", "9");
                activeBike = 8;
                initiateList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Fuelling.main.setBackground(drawablePic);
        } else {
            Fuelling.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    public static void saveFuelsOld() {

        for (Bike thisBike : bikes) {

            Log.i("Saving Fuelings", "" + thisBike);
            try {
                ArrayList<String> fdates = new ArrayList<>();
                ArrayList<String> miles = new ArrayList<>();
                ArrayList<String> prices = new ArrayList<>();
                ArrayList<String> litres = new ArrayList<>();
                ArrayList<String> fuMileage = new ArrayList<>();

                for (fuellingDetails thisLog : thisBike.fuelings) {

                    fdates.add(thisLog.date);
                    miles.add(Double.toString(thisLog.miles));
                    prices.add(Double.toString(thisLog.price));
                    litres.add(Double.toString(thisLog.litres));
                    fuMileage.add(Double.toString(thisLog.mileage));

                }

                Log.i("Saving Fuels", "Size :" + fdates.size());
                ed.putString("fdates" + thisBike.bikeId, ObjectSerializer.serialize(fdates)).apply();
                ed.putString("miles" + thisBike.bikeId, ObjectSerializer.serialize(miles)).apply();
                ed.putString("prices" + thisBike.bikeId, ObjectSerializer.serialize(prices)).apply();
                ed.putString("litres" + thisBike.bikeId, ObjectSerializer.serialize(litres)).apply();
                ed.putString("fuMileage" + thisBike.bikeId, ObjectSerializer.serialize(fuMileage)).apply();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Adding fuellings", "Failed attempt");
            }
        }
    }

    public static void saveFuels() {

        for (Bike thisBike : bikes) {

            Log.i("Saving Fuelings", "" + thisBike);

            ArrayList<String> fdates = new ArrayList<>();
            ArrayList<String> miles = new ArrayList<>();
            ArrayList<String> prices = new ArrayList<>();
            ArrayList<String> litres = new ArrayList<>();
            ArrayList<String> fuMileage = new ArrayList<>();

            for (fuellingDetails thisLog : thisBike.fuelings) {

                fdates.add(thisLog.date);
                miles.add(Double.toString(thisLog.miles));
                prices.add(Double.toString(thisLog.price));
                litres.add(Double.toString(thisLog.litres));
                fuMileage.add(Double.toString(thisLog.mileage));

            }
            Log.i("saveFuelDB", "Size :" + fdates.size());

            try {
                String dbname = "fuel" + thisBike.bikeId;

                vehiclesDB.execSQL("CREATE TABLE IF NOT EXISTS '" + dbname + "' (fdates VARCHAR, miles VARCHAR, prices VARCHAR, litres VARCHAR, fuMileage VARCHAR)");

                vehiclesDB.delete(dbname, null, null);

                vehiclesDB.execSQL("INSERT INTO '" + dbname + "' (fdates, miles, prices, litres, fuMileage) VALUES ('" +
                        ObjectSerializer.serialize(fdates) + "' , '" + ObjectSerializer.serialize(miles) + "' , '" + ObjectSerializer.serialize(prices) + "' , '" +
                        ObjectSerializer.serialize(litres) + "' , '" + ObjectSerializer.serialize(fuMileage) +  "')");

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    public static void loadFuelsOld() {

        for (Bike thisBike : bikes) {
            thisBike.fuelings.clear();

            Log.i("Loading Fuels", "" + thisBike);

            ArrayList<String> fdates = new ArrayList<>();
            ArrayList<String> miles = new ArrayList<>();
            ArrayList<String> prices = new ArrayList<>();
            ArrayList<String> litres = new ArrayList<>();
            ArrayList<String> fuMileage = new ArrayList<>();

            try {

                fdates = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("fdates" + thisBike.bikeId, ObjectSerializer.serialize(new ArrayList<String>())));
                miles = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("miles" + thisBike.bikeId, ObjectSerializer.serialize(new ArrayList<String>())));
                prices = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("prices" + thisBike.bikeId, ObjectSerializer.serialize(new ArrayList<String>())));
                litres = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("litres" + thisBike.bikeId, ObjectSerializer.serialize(new ArrayList<String>())));
                fuMileage = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("fuMileage" + thisBike.bikeId, ObjectSerializer.serialize(new ArrayList<String>())));
                Log.i("fDates for " + thisBike, "Count :" + fdates.size());
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Loading details", "Failed attempt");
            }

            Log.i("Retrieved info" + thisBike, "Log count :" + fdates.size());
            if (fdates.size() > 0 && miles.size() > 0 && prices.size() > 0 && litres.size() > 0) {
                // we've checked there is some info
                if (fdates.size() == miles.size() && miles.size() == prices.size() && prices.size() == litres.size()) {
                    // we've checked each item has the same amount of info, nothing is missing
                    for (int x = 0; x < fdates.size(); x++) {
                        Date thisDate = new Date();
                        try {
                            thisDate = sdf.parse(fdates.get(x));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        fuellingDetails newLog = new fuellingDetails(Double.parseDouble(miles.get(x)), Double.parseDouble(prices.get(x)), Double.parseDouble(litres.get(x)), thisDate, Double.parseDouble(fuMileage.get(x)));
                        Log.i("Adding", "" + x + "" + newLog);
                        thisBike.fuelings.add(newLog);
                    }

                }
            }
        }
    }

    public static void loadFuels() {

        for (Bike thisBike : bikes) {
            thisBike.fuelings.clear();

            try {
                String dbname = "fuel" + thisBike.bikeId;

                Cursor c = vehiclesDB.rawQuery("SELECT * FROM " + dbname, null);

                int fdatesIndex = c.getColumnIndex("fdates");
                int milesIndex = c.getColumnIndex("miles");
                int pricesIndex = c.getColumnIndex("prices");
                int litresIndex = c.getColumnIndex("litres");
                int fuMileageIndex = c.getColumnIndex("fuMileage");

                c.moveToFirst();

                do {

                    ArrayList<String> fdates = new ArrayList<>();
                    ArrayList<String> miles = new ArrayList<>();
                    ArrayList<String> prices = new ArrayList<>();
                    ArrayList<String> litres = new ArrayList<>();
                    ArrayList<String> fuMileage = new ArrayList<>();

                    try {

                        fdates = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(fdatesIndex));
                        miles = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(milesIndex));
                        prices = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(pricesIndex));
                        litres = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(litresIndex));
                        fuMileage = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(fuMileageIndex));

                        Log.i("Fuelings Restored ", "Count :" + fdates.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Loading Fuel", "Failed attempt");
                    }

                    Log.i("Retrieved info", "Log count :" + fdates.size());
                    if (fdates.size() > 0 && miles.size() > 0 && prices.size() > 0) {
                        // we've checked there is some info
                        if (fdates.size() == miles.size() && miles.size() == prices.size()) {
                            // we've checked each item has the same amount of info, nothing is missing
                            for (int x = 0; x < fdates.size(); x++) {
                                Date thisDate = new Date();
                                try {
                                    thisDate = sdf.parse(fdates.get(x));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                fuellingDetails newLog = new fuellingDetails(Double.parseDouble(miles.get(x)), Double.parseDouble(prices.get(x)), Double.parseDouble(litres.get(x)), thisDate, Double.parseDouble(fuMileage.get(x)));
                                Log.i("Adding", "" + x + "" + newLog);
                                thisBike.fuelings.add(newLog);
                            }
                        }
                    }
                } while (c.moveToNext());

            } catch (Exception e) {

                Log.i("LoadingDB", "Caught Error");
                e.printStackTrace();
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

            // check if the back button was pressed with the add item view showing
            // if it was, hide this view.  If not, carry on as normal.

            if (fuelSummary.isShown()) {
                // editing or adding a fueling, so hide the box
                fuelSummary.setVisibility(View.INVISIBLE);
                shield.setVisibility(View.INVISIBLE);
            } else if (fuelingDetailsLayout.isShown()) {

                if (!milesDone.getText().toString().equals("") || !petrolPrice.getText().toString().equals("") || !litresUsed.getText().toString().equals("")) {

                    // add warning
                    new AlertDialog.Builder(Fuelling.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Discard Current Log?")
                            .setMessage("Would you like to discard the current information?")
                            .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fuelingDetailsLayout.setVisibility(View.INVISIBLE);
                                    shield.setVisibility(View.INVISIBLE);
                                    itemLongPressed = null;
                                    itemLongPressedPosition = -1;
                                    // reset the fuelling text boxes?
                                }
                            })
                            .setNegativeButton("Keep", null)
                            .show();
                } else {
                    fuelingDetailsLayout.setVisibility(View.INVISIBLE);
                    shield.setVisibility(View.INVISIBLE);
                }
            } else {

                finish();
                return true;
            }
        }
        myAdapter.notifyDataSetChanged();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Fuelling Activity", "On Pause");
        saveFuels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // could be coming back from a settings change, so set these just in case they changed
        milesDone.setHint(milesSetting + " done");
        myAdapter.notifyDataSetChanged();

        checkBackground();
    }
}
