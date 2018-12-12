package com.androidandyuk.autobuddy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.currencySetting;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.precision;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;
import static com.androidandyuk.autobuddy.MainActivity.vehiclesDB;


public class ToDo extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;

    private static final String TAG = "ToDo";
    private AdView mAdView;

    static MyToDoAdapter myAdapter;
    ListView toDoList;

    public static RelativeLayout main;

    EditText toDoDetails;
    EditText toDoURL;
    EditText toDoCost;
    Spinner prioritySpinner;
    TextView currentVehicle;

    View toDoDetailsLayout;
    public static View shield;

    //used for checking the url is valid
    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    Pattern urlCheck = Pattern.compile(URL_REGEX);

    // used to store what item might be being edited or deleted
    static int itemLongPressedPosition = 0;
    static ToDoDetails itemLongPressed = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sharedPreferences = this.getSharedPreferences("com.androidandyuk.autobuddy", Context.MODE_PRIVATE);
        ed = sharedPreferences.edit();

        toDoDetailsLayout = findViewById(R.id.toDoDetailsLayout);
        shield = findViewById(R.id.shield);

        currentVehicle = findViewById(R.id.currentVehicle);
        currentVehicle.setText(bikes.get(activeBike).yearOfMan + " " + bikes.get(activeBike).make + " " + bikes.get(activeBike).model);

        toDoDetails = (EditText) findViewById(R.id.toDoDetails);
        toDoURL = (EditText) findViewById(R.id.toDoURL);
        toDoCost = (EditText) findViewById(R.id.toDoCost);
        prioritySpinner = (Spinner) findViewById(R.id.prioritySpinner);

        prioritySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ToDoPriority.values()));
        // load the to Do list

        Log.i("Fuelling", "Loading ToDos");
        loadToDos();

        initiateList();

        toDoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                itemLongPressedPosition = position;
                itemLongPressed = bikes.get(activeBike).toDoList.get(position);
                Log.i("ToDo List", "Tapped " + position);

                // show the log editing view
                toDoDetails.setText(bikes.get(activeBike).toDoList.get(position).getLog());
                toDoURL.setText(bikes.get(activeBike).toDoList.get(position).getUrl());
                toDoCost.setText(Double.toString(bikes.get(activeBike).toDoList.get(position).getPrice()));
                prioritySpinner.setSelection(bikes.get(activeBike).toDoList.get(position).getPriority().getValue() - 1);
                toDoDetailsLayout.setVisibility(View.VISIBLE);
                shield.setVisibility(View.VISIBLE);
            }
        });

        toDoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final int logPosition = position;
                final Context context = App.getContext();

                new AlertDialog.Builder(ToDo.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("You're about to delete this To Do item forever...")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("Removing", "ToDo " + logPosition);
                                bikes.get(activeBike).toDoList.remove(logPosition);
                                initiateList();
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            }


        });


    }

    private void initiateList() {
        toDoList = (ListView) findViewById(R.id.toDoList);

        myAdapter = new MyToDoAdapter(bikes.get(activeBike).toDoList);

        toDoList.setAdapter(myAdapter);

        setTitle("To Do List: " + bikes.get(activeBike));
    }

    public void showToDoClicked(View view) {
        showToDo();
    }

    public void showToDo() {
        // opens the add fueling dialog
        Log.i("ToDo", "Show To Do layout");

        resetToDo();

        toDoDetailsLayout.setVisibility(View.VISIBLE);
        shield.setVisibility(View.VISIBLE);
    }

    public void resetToDo() {
        toDoDetails.setText(null);
        toDoURL.setText(null);
        toDoCost.setText(null);
    }

    public void addToDoClicked(View view) {
        addToDo();
    }

    public void addToDo() {

        if (toDoDetails.getText().toString().isEmpty()) {

            Toast.makeText(ToDo.this, "Please complete all necessary details", Toast.LENGTH_LONG).show();

        } else {
            // there is at least a To Do item, so make a new to do and add it

            String details = toDoDetails.getText().toString();
            String url = toDoURL.getText().toString();

            Matcher m = urlCheck.matcher(url);
            Log.i("URL " + url, "m " + m);
            // todo need to check if it's empty, then don't toast
            if (!toDoURL.getText().toString().isEmpty() && !m.find()) {
                Toast.makeText(getApplicationContext(), "Not a valid URL", Toast.LENGTH_SHORT).show();
                url = "";
            }


            double cost = 0;
            try {
                cost = Double.parseDouble(toDoCost.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            ToDoPriority priority = (ToDoPriority) prioritySpinner.getSelectedItem();

            if (itemLongPressed != null) {
                // adding back in an edited log, so remove the old one
                bikes.get(activeBike).toDoList.remove(itemLongPressed);
            }

            ToDoDetails newToDo = new ToDoDetails(details, cost, url, priority);
            bikes.get(activeBike).toDoList.add(newToDo);
            Collections.sort(bikes.get(activeBike).toDoList);
            myAdapter.notifyDataSetChanged();
            toDoDetailsLayout.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);

            // clear previous entries
            toDoDetails.setText(null);
            toDoDetails.clearFocus();
            toDoCost.setText(null);
            toDoCost.clearFocus();
            toDoURL.setText(null);
            toDoURL.clearFocus();

        }
        Log.i("Reset", "itemLongPressed");
        itemLongPressed = null;
        itemLongPressedPosition = -1;
    }

    public void shieldClicked(View view){
        if (!toDoDetails.getText().toString().equals("") || !toDoURL.getText().toString().equals("") || !toDoCost.getText().toString().equals("")) {

            new AlertDialog.Builder(ToDo.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Discard Current Details?")
                    .setMessage("Would you like to discard the current information?")
                    .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // editing or adding a To Do, so hide the box
                            toDoDetailsLayout.setVisibility(View.INVISIBLE);
                            shield.setVisibility(View.INVISIBLE);
                            Log.i("Reset", "itemLongPressed");
                            itemLongPressed = null;
                            itemLongPressedPosition = -1;
                        }
                    })
                    .setNegativeButton("Keep", null)
                    .show();
        } else {
            toDoDetailsLayout.setVisibility(View.INVISIBLE);
            shield.setVisibility(View.INVISIBLE);
        }
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            ToDo.main.setBackground(drawablePic);
            toDoList.setBackground(getResources().getDrawable(R.drawable.rounded_corners_drkgrey_orange));
        } else {
            ToDo.main.setBackgroundColor(getResources().getColor(R.color.background));
            toDoList.setBackground(null);
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


    public class MyToDoAdapter extends BaseAdapter {
        public ArrayList<ToDoDetails> toDoDataAdapter;

        public MyToDoAdapter(ArrayList<ToDoDetails> toDoDataAdapter) {
            this.toDoDataAdapter = toDoDataAdapter;
        }

        @Override
        public int getCount() {
            return toDoDataAdapter.size();
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

            final ToDoDetails s = toDoDataAdapter.get(position);

            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(R.layout.todo_listview, null);

            TextView toDoListURL = (TextView) myView.findViewById(R.id.toDoURL);
            String thisURL = s.url;
            if (thisURL.length() > 80) {
                thisURL = thisURL.substring(0, 80);
            }
            toDoListURL.setText(thisURL);
            toDoListURL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Opening in Browser", Toast.LENGTH_SHORT).show();
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s.url));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            TextView toDoListLog = (TextView) myView.findViewById(R.id.toDoLog);
            toDoListLog.setText(s.log);

            TextView toDoListCost = (TextView) myView.findViewById(R.id.toDoCost);
            String text = currencySetting + precision.format(s.price);
            if (s.price == 0) {
                text = "";
            }
            toDoListCost.setText(text);

            return myView;
        }
    }

    public static void saveToDos() {

        for (Bike thisBike : bikes) {

            Log.i("Saving ToDos DB", "" + thisBike);
            ArrayList<String> tdDetails = new ArrayList<>();
            ArrayList<String> tdPrices = new ArrayList<>();
            ArrayList<String> tdUrls = new ArrayList<>();
            ArrayList<String> tdPriority = new ArrayList<>();

            for (ToDoDetails thisTD : thisBike.toDoList) {

                tdDetails.add(thisTD.log);
                tdPrices.add(Double.toString(thisTD.price));
                tdUrls.add(thisTD.url);
                tdPriority.add(Integer.toString(thisTD.priority.getValue()));

            }

            Log.i("Saving ToDos DB", "Size :" + tdDetails.size());
            try {
                String dbname = "todo" + thisBike.bikeId;

                vehiclesDB.execSQL("CREATE TABLE IF NOT EXISTS '" + dbname + "' (tdDetails VARCHAR, tdPrices VARCHAR, tdUrls VARCHAR, tdPriority VARCHAR)");

                vehiclesDB.delete(dbname, null, null);

                vehiclesDB.execSQL("INSERT INTO '" + dbname + "' (tdDetails, tdPrices, tdUrls, tdPriority) VALUES ('" + ObjectSerializer.serialize(tdDetails) + "' , '" +
                        ObjectSerializer.serialize(tdPrices) + "' , '" + ObjectSerializer.serialize(tdUrls) + "' , '" +ObjectSerializer.serialize(tdPriority) + "')");

                Log.i("Saving ToDos DB", "Saved");

            } catch (Exception e) {

                e.printStackTrace();
                Log.i("Saving ToDos DB", "Caught Error :" + e);

            }


        }
    }

    public static void loadToDosOld() {

        for (Bike thisBike : bikes) {
            thisBike.toDoList.clear();

            Log.i("Loading ToDos Old", "" + thisBike);

            ArrayList<String> tdDetails = new ArrayList<>();
            ArrayList<String> tdPrices = new ArrayList<>();
            ArrayList<String> tdUrls = new ArrayList<>();
            ArrayList<String> tdPriority = new ArrayList<>();

            try {

                tdDetails = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("tdDetails" + thisBike.bikeId, ""));
                tdPrices = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("tdPrices" + thisBike.bikeId, "0"));
                tdUrls = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("tdUrls" + thisBike.bikeId, ""));
                tdPriority = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("tdPriority" + thisBike.bikeId, "1"));
                Log.i("tdDetails for " + thisBike, "Count :" + tdDetails.size());


                Log.i("Retrieved info" + thisBike, "ToDo count :" + tdDetails.size());
                if (tdDetails.size() > 0 && tdPrices.size() > 0 && tdUrls.size() > 0 && tdPriority.size() > 0) {
                    // we've checked there is some info
                    if (tdDetails.size() == tdPrices.size() && tdPrices.size() == tdUrls.size() && tdUrls.size() == tdPriority.size()) {
                        // we've checked each item has the same amount of info, nothing is missing
                        for (int x = 0; x < tdDetails.size(); x++) {

                            ToDoDetails newToDo = new ToDoDetails(tdDetails.get(x), Double.parseDouble(tdPrices.get(x)), tdUrls.get(x), Integer.parseInt(tdPriority.get(x)));
                            Log.i("Adding", "" + x + "" + newToDo);
                            thisBike.toDoList.add(newToDo);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Loading ToDo details", "Failed attempt");
            }
        }
    }

    public static void loadToDos() {

        for (Bike thisBike : bikes) {
            thisBike.toDoList.clear();

            Log.i("Loading ToDos", "" + thisBike);
            try {
                String dbname = "todo" + thisBike.bikeId;

                Cursor c = vehiclesDB.rawQuery("SELECT * FROM " + dbname, null);

                int tdDetailsIndex = c.getColumnIndex("tdDetails");
                int tdPricesIndex = c.getColumnIndex("tdPrices");
                int tdUrlsIndex = c.getColumnIndex("tdUrls");
                int tdPriorityIndex = c.getColumnIndex("tdPriority");

                c.moveToFirst();

                do {

                    ArrayList<String> tdDetails = new ArrayList<>();
                    ArrayList<String> tdPrices = new ArrayList<>();
                    ArrayList<String> tdUrls = new ArrayList<>();
                    ArrayList<String> tdPriority = new ArrayList<>();

                    try {

                        tdDetails = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(tdDetailsIndex));
                        tdPrices = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(tdPricesIndex));
                        tdUrls = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(tdUrlsIndex));
                        tdPriority = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(tdPriorityIndex));

                        Log.i("Fuelings Restored ", "Count :" + tdDetails.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Loading Fuel", "Failed attempt");
                    }

                    Log.i("Retrieved info", "Log count :" + tdDetails.size());
                    if (tdDetails.size() > 0 && tdPrices.size() > 0 && tdUrls.size() > 0) {
                        // we've checked there is some info
                        if (tdDetails.size() == tdPrices.size() && tdPrices.size() == tdUrls.size()) {
                            // we've checked each item has the same amount of info, nothing is missing
                            for (int x = 0; x < tdDetails.size(); x++) {

                                ToDoDetails newToDo = new ToDoDetails(tdDetails.get(x), Double.parseDouble(tdPrices.get(x)), tdUrls.get(x), Integer.parseInt(tdPriority.get(x)));
                                Log.i("Adding", "" + x + "" + newToDo);
                                thisBike.toDoList.add(newToDo);

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
            if (toDoDetailsLayout.isShown()) {

                // make a check, if they're all empty dont ask the alert
                if (!toDoDetails.getText().toString().equals("") || !toDoURL.getText().toString().equals("") || !toDoCost.getText().toString().equals("")) {

                    new AlertDialog.Builder(ToDo.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Discard Current Details?")
                            .setMessage("Would you like to discard the current information?")
                            .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // editing or adding a To Do, so hide the box
                                    toDoDetailsLayout.setVisibility(View.INVISIBLE);
                                    shield.setVisibility(View.INVISIBLE);
                                    Log.i("Reset", "itemLongPressed");
                                    itemLongPressed = null;
                                    itemLongPressedPosition = -1;
                                }
                            })
                            .setNegativeButton("Keep", null)
                            .show();
                } else {
                    toDoDetailsLayout.setVisibility(View.INVISIBLE);
                    shield.setVisibility(View.INVISIBLE);
                    return true;
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
        Log.i("ToDo Activity", "On Pause");
        saveToDos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackground();
    }
}
