package com.androidandyuk.autobuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.saveBikes;
import static com.androidandyuk.autobuddy.MainActivity.saveSettings;
import static com.androidandyuk.autobuddy.R.id.vehicleList;

public class ChooseVehicle extends AppCompatActivity {

    private static final String TAG = "ChooseVehicle";

    public static RelativeLayout main;

    static MyVehicleAdapter myAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    ListView listView;

    public static ImageView shield;

    public static EditText bikeMake;
    public static EditText bikeModel;
    public static EditText bikeYear;

    public static Boolean editingBike = false;
    public static View addingNewVehicle;

    // used to store what item might be being edited or deleted
    int itemLongPressedPosition = -1;
    Bike itemLongPressed = null;
    String editDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_vehicle);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        main = findViewById(R.id.main);

        bikeMake = findViewById(R.id.bikeMake);
        bikeModel = findViewById(R.id.bikeModel);
        bikeYear = findViewById(R.id.bikeYear);

        addingNewVehicle = findViewById(R.id.addingNewVehicle);

        shield = findViewById(R.id.shield);

        initiateList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Log.i(TAG, String.valueOf(position));

                activeBike = position;

                Intent intent = new Intent(getApplicationContext(), Garage.class);
                startActivity(intent);
                finish();

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final Context context = App.getContext();

                new AlertDialog.Builder(ChooseVehicle.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("You're about to delete this vehicle forever...")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("Removing", "Vehicle " + position);
                                bikes.remove(position);
                                saveBikes();
                                initiateList();
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                if (position == activeBike){
                                    activeBike = (bikes.size() - 1);
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();


                return true;
            }


        });


    }

    public void addVehicle(View view) {
        Log.i(TAG, "Add bike");

        // clear out any previous details
        bikeMake.setText("");
        bikeModel.setText("");
        bikeYear.setText("");

        addingNewVehicle.setVisibility(View.VISIBLE);
        shield.setVisibility(View.VISIBLE);

    }

    public void addNewBike(View view) {
        String make = bikeMake.getText().toString();
        String model = bikeModel.getText().toString();
        String year = bikeYear.getText().toString();

        // check enough details are entered
        if (make.isEmpty() || model.isEmpty() || year.isEmpty()) {

            Toast.makeText(ChooseVehicle.this, "Please complete all necessary details", Toast.LENGTH_LONG).show();

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

                addingNewVehicle.setVisibility(View.INVISIBLE);
                shield.setVisibility(View.INVISIBLE);
                saveBikes();
                saveSettings();
                invalidateOptionsMenu();

            } else {

                Toast.makeText(ChooseVehicle.this, "That year looks unlikely", Toast.LENGTH_LONG).show();

            }

        }
    }

    private void initiateList() {
        Log.i(TAG, "Initiating List");
        listView = findViewById(vehicleList);

        myAdapter = new MyVehicleAdapter(bikes);

        listView.setAdapter(myAdapter);

    }

    private class MyVehicleAdapter extends BaseAdapter {
        public ArrayList<Bike> vehicleAdapter;

        public MyVehicleAdapter(ArrayList<Bike> vehicleAdapter) {
            this.vehicleAdapter = vehicleAdapter;
        }

        @Override
        public int getCount() {
            return vehicleAdapter.size();
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
            View myView = mInflater.inflate(R.layout.vehicle_listview, null);

            final Bike b = vehicleAdapter.get(position);

            TextView vehicle = myView.findViewById(R.id.vehicle);
            vehicle.setText(b.yearOfMan + " " + b.make + " " + b.model);

            return myView;
        }

    }

    public void shieldClicked(View view) {
        if (addingNewVehicle.isShown()) {
            addingNewVehicle.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);
        }
    }

    public void checkBackground() {
        main = findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            ChooseVehicle.main.setBackground(drawablePic);
        } else {
            ChooseVehicle.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //assign the views that could be showing, to check if they are showing when back is pressed
            final View addingNewVehicle = findViewById(R.id.addingNewVehicle);

            if (addingNewVehicle.isShown()) {

                // add warning
                new AlertDialog.Builder(ChooseVehicle.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Discard Current Details?")
                        .setMessage("Would you like to discard the current information?")
                        .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addingNewVehicle.setVisibility(View.INVISIBLE);
                                shield.setVisibility(View.INVISIBLE);
                            }
                        })
                        .setNegativeButton("Keep", null)
                        .show();


            } else {
                Intent intent = new Intent(getApplicationContext(), Garage.class);
                startActivity(intent);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackground();
    }
}
