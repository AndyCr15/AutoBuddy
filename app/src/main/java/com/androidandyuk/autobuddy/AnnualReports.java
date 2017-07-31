package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.currencySetting;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.oneDecimal;
import static com.androidandyuk.autobuddy.MainActivity.precision;
import static com.androidandyuk.autobuddy.MainActivity.sdf;


public class AnnualReports extends AppCompatActivity {

    static MyAnnualFuelAdapter myAdapter;

    ListView listView;

    public static RelativeLayout main;
    int numberOfYears;
    public static ArrayList<AnnualDetails> fuelReports = new ArrayList<>();
    public static ArrayList<AnnualDetails> maintReports = new ArrayList<>();

    public static int listType;
    View myView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annual_reports);

        changeHeader();

        Spinner reportChoice = (Spinner) findViewById(R.id.reportChoice);

        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("Fuel");
        spinnerArray.add("Maintenance");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        reportChoice.setAdapter(adapter);

        reportChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Position", "" + position);
                listType = position;
                changeReports();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // by default, load the fuel reports
        loadFuelReports();
    }

    public void changeReports() {
        if (listType == 0) {
            // load fuel annual reports
            loadFuelReports();
        } else if (listType == 1) {
            // load maintenance reports
            loadMaintReports();
        }
    }

    public void loadFuelReports() {
        if (bikes.get(activeBike).fuelings.size() > 0) {
            fuelReports.clear();

            numberOfYears = countFuelYears();

            // find the latest year so we can work back from it
            String str[] = bikes.get(activeBike).fuelings.get(0).date.split("/");
            int currentYear = Integer.parseInt(str[2]);
            int i = 0;
            // make the first item, for current year
            fuelReports.add(new AnnualDetails(currentYear, 0, 0d, 0d, 0d));
            Log.i("FuelReportSize", "" + fuelReports.size());
            for (fuellingDetails thisFuel : bikes.get(activeBike).fuelings) {
                Log.i("i ", "" + i);
                // check what year this fuel happened in
                String thisStr[] = thisFuel.date.split("/");
                int thisYear = Integer.parseInt(thisStr[2]);
                if (thisYear == currentYear) {
                    // in same year, so add figures on
                    fuelReports.get(i).afCount++;
                    fuelReports.get(i).afCost += thisFuel.getPrice() * thisFuel.getLitres();
                    fuelReports.get(i).afMiles += thisFuel.getMiles();
                    fuelReports.get(i).afLitres += thisFuel.getLitres();
                } else {
                    // must be a new year, make new addition and decrease current year
                    currentYear = thisYear;
                    AnnualDetails newYear = new AnnualDetails(thisYear, 1, (thisFuel.getPrice() * thisFuel.getLitres()), thisFuel.getMiles(), thisFuel.getLitres());
                    fuelReports.add(newYear);
                    i++;
                }
            }
            Log.i("FuelReportSize", "" + fuelReports.size());
            initiateList();
        } else {
            Toast.makeText(this, "No Fuel Logs", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadMaintReports() {
        if (bikes.get(activeBike).maintenanceLogs.size() > 0) {
            maintReports.clear();

            numberOfYears = countMaintYears();

            // find the latest year so we can work back from it
            String str[] = bikes.get(activeBike).maintenanceLogs.get(0).date.split("/");
            int currentYear = Integer.parseInt(str[2]);
            int i = 0;
            // make the first item, for current year
            maintReports.add(new AnnualDetails(currentYear, 0, 0d, Bike.annualMiles(bikes.get(activeBike), currentYear), 0d));
            Log.i("MaintReportSize", "" + maintReports.size());
            for (maintenanceLogDetails thisLog : bikes.get(activeBike).maintenanceLogs) {
                // check what year this fuel happened in
                String thisStr[] = thisLog.date.split("/");
                int thisYear = Integer.parseInt(thisStr[2]);
                if (thisYear == currentYear) {
                    // in same year, so add figures on
                    maintReports.get(i).afCount++;
                    maintReports.get(i).afCost += thisLog.getPrice();
                } else {
                    // must be a new year, make new addition and decrease current year
                    currentYear = thisYear;
                    AnnualDetails newYear = new AnnualDetails(thisYear, 1, thisLog.getPrice(), Bike.annualMiles(bikes.get(activeBike), thisYear), 0d);
                    maintReports.add(newYear);
                    i++;
                }
            }
            Log.i("MaintReportSize", "" + maintReports.size());
            initiateList();
        } else {
            Toast.makeText(this, "No Maintenance Logs", Toast.LENGTH_SHORT).show();
        }
    }

    private int countFuelYears() {

        int latestYear = 0;
        int earliestYear = 3000;

        for (fuellingDetails thisFuelling : bikes.get(activeBike).fuelings) {
            Date thisDate = new Date();
            try {
                thisDate = sdf.parse(thisFuelling.date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = null;
            cal = Calendar.getInstance();
            cal.setTime(thisDate);

            int thisYear = cal.get(Calendar.YEAR);

            if (thisYear < earliestYear) {
                earliestYear = thisYear;
            }

            if (thisYear > latestYear) {
                latestYear = thisYear;
            }
        }
        return (latestYear - earliestYear) + 1;
    }

    private int countMaintYears() {

        int latestYear = 0;
        int earliestYear = 3000;

        for (maintenanceLogDetails thisMaint : bikes.get(activeBike).maintenanceLogs) {
            Date thisDate = new Date();
            try {
                thisDate = sdf.parse(thisMaint.date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = null;
            cal = Calendar.getInstance();
            cal.setTime(thisDate);

            int thisYear = cal.get(Calendar.YEAR);

            if (thisYear < earliestYear) {
                earliestYear = thisYear;
            }

            if (thisYear > latestYear) {
                latestYear = thisYear;
            }
        }
        return (latestYear - earliestYear) + 1;
    }

    public void changeHeader() {
        TextView selectedVehicle = (TextView) findViewById(R.id.selectedVehicle);
        if (activeBike > -1) {
            selectedVehicle.setText(bikes.get(activeBike).yearOfMan + " " + bikes.get(activeBike).model);
        }
        changeReports();
    }

    private void initiateList() {
        Log.i("initiateList", "Initiating List");
        listView = (ListView) findViewById(R.id.reportList);


        if (listType == 0) {
            setTitle("Annual Fuelling Report: " + bikes.get(activeBike).model);
            myAdapter = new MyAnnualFuelAdapter(fuelReports);
        } else {
            setTitle("Annual Maintenance Report: " + bikes.get(activeBike).model);
            myAdapter = new MyAnnualFuelAdapter(maintReports);
        }
        listView.setAdapter(myAdapter);
    }

    private class MyAnnualFuelAdapter extends BaseAdapter {
        public ArrayList<AnnualDetails> annualDataAdapter;

        public MyAnnualFuelAdapter(ArrayList<AnnualDetails> annualDataAdapter) {
            this.annualDataAdapter = annualDataAdapter;
        }

        @Override
        public int getCount() {
            return annualDataAdapter.size();
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
            myView = mInflater.inflate(R.layout.annual_fuel_report_listview, null);

            if (listType == 1) {
                myView = mInflater.inflate(R.layout.annual_maint_report_listview, null);
            }

            final AnnualDetails s = annualDataAdapter.get(position);

            TextView yearTV = (TextView) myView.findViewById(R.id.yearTV);
            String year = Integer.toString(s.afYear);
            yearTV.setText(year);

            TextView countTV = (TextView) myView.findViewById(R.id.countTV);
            String count = Integer.toString(s.afCount);
            countTV.setText("Count : " + count);

            // add check for kilometers
            TextView milesTV = (TextView) myView.findViewById(R.id.milesTV);
            Double thisMiles = s.afMiles;
            // check what setting the user has, Miles or Km
            // if Km, convert to Miles for display
            String unit = "";
            if (milesSetting.equals("Km")) {
                thisMiles = thisMiles / conversion;
                unit = " Km";
            } else {
                unit = " Mi";
            }
            milesTV.setText(unit + " : " + oneDecimal.format(thisMiles));

            TextView spendTV = (TextView) myView.findViewById(R.id.spendTV);
            Double spend = s.afCost;
            spendTV.setText("Spend : " + currencySetting + precision.format(spend));

            TextView costPerMile = (TextView) myView.findViewById(R.id.costMileTV);
            Double aveCost = s.afCost / s.afMiles;
            // check what setting the user has, Miles or Km
            // if Km, convert to Miles for display
            if (milesSetting.equals("Km")) {
                aveCost = aveCost * conversion;
            }
            costPerMile.setText("Per " + unit + " : " + currencySetting + precision.format(aveCost));

            if (listType == 0) {
                TextView litresTV = (TextView) myView.findViewById(R.id.litresTV);
                litresTV.setText("Litres : " + oneDecimal.format(s.afLitres));

                TextView mpgTV = (TextView) myView.findViewById(R.id.mpgTV);
                Double mpg = s.afMiles / (s.afLitres / 4.54609);
                mpgTV.setText("MPG : " + oneDecimal.format(mpg));

                TextView tankTV = (TextView) myView.findViewById(R.id.tankSpendTV);
                Double aveSpend = s.afCost / s.afCount;
                tankTV.setText("Ave Tank : " + currencySetting + precision.format(aveSpend));

                TextView milesTankTV = (TextView) myView.findViewById(R.id.milesTankTV);
                Double aveMilesTank = s.afMiles / s.afCount;
                // check what setting the user has, Miles or Km
                // if Km, convert to Miles for display
                if (milesSetting.equals("Km")) {
                    aveMilesTank = aveMilesTank / conversion;
                }
                milesTankTV.setText(unit + "/Tank : " + oneDecimal.format(aveMilesTank));

                TextView avePriceTV = (TextView) myView.findViewById(R.id.avePriceTV);
                Double avePrice = s.afCost / s.afLitres;
                // check what setting the user has, Miles or Km
                // if Km, convert to Miles for display
                if (milesSetting.equals("Km")) {
                    avePrice = avePrice * conversion;
                }
                avePriceTV.setText("Ave Price : " + currencySetting + precision.format(avePrice));
            }
            return myView;
        }
    }

    public void setAppTheme(int themeNum) {
        this.setTheme(R.style.AppTheme);
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            AnnualReports.main.setBackground(drawablePic);
        } else {
            AnnualReports.main.setBackgroundColor(getResources().getColor(R.color.background));
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
        Intent intent;
        switch (item.getItemId()) {
            case 0:
                Log.i("Option", "0");
                intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
//                Toast.makeText(MainActivity.this, "Settings not yet implemented", Toast.LENGTH_LONG).show();
                return true;
            case 1:
                Log.i("Option", "1");
                activeBike = 0;
                changeHeader();
                return true;
            case 2:
                Log.i("Option", "2");
                activeBike = 1;
                changeHeader();
                return true;
            case 3:
                Log.i("Option", "3");
                activeBike = 2;
                changeHeader();
                return true;
            case 4:
                Log.i("Option", "4");
                activeBike = 3;
                changeHeader();
                return true;
            case 5:
                Log.i("Option", "5");
                activeBike = 4;
                changeHeader();
                return true;
            case 6:
                Log.i("Option", "6");
                activeBike = 5;
                changeHeader();
                return true;
            case 7:
                Log.i("Option", "7");
                activeBike = 6;
                changeHeader();
                return true;
            case 8:
                Log.i("Option", "8");
                activeBike = 7;
                changeHeader();
                return true;
            case 9:
                Log.i("Option", "9");
                activeBike = 8;
                changeHeader();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        invalidateOptionsMenu();
        super.onResume();
        changeHeader();
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//        String theme = settings.getString("theme", "1");
//        Log.i("Theme", theme);
//        setAppTheme(Integer.parseInt(theme));
        checkBackground();
    }
}
