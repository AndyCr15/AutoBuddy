package com.androidandyuk.autobuddy;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.androidandyuk.autobuddy.CarShows.loadShows;
import static com.androidandyuk.autobuddy.Fuelling.loadFuels;
import static com.androidandyuk.autobuddy.Fuelling.loadFuelsOld;
import static com.androidandyuk.autobuddy.Fuelling.saveFuels;
import static com.androidandyuk.autobuddy.Maintenance.loadLogs;
import static com.androidandyuk.autobuddy.Maintenance.loadLogsOld;
import static com.androidandyuk.autobuddy.Maintenance.saveLogs;
import static com.androidandyuk.autobuddy.ToDo.loadToDos;
import static com.androidandyuk.autobuddy.ToDo.loadToDosOld;
import static com.androidandyuk.autobuddy.ToDo.saveToDos;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor ed;

    public static ArrayList<Bike> bikes = new ArrayList<>();

    private FirebaseAnalytics mFirebaseAnalytics;

    public static LocationManager locationManager;
    public static LocationListener locationListener;

    public static RelativeLayout main;

    public static Boolean instructionsRead;

    public static LatLng userLatLng;
    public static JSONObject jsonObject;
    public static TextView weatherText;
    public static String currentForecast;
    public static int warningDays = 30;
    public static int notiHour = 10;
    public static int notiMinute = 0;
    public static String currencySetting;
    public static String milesSetting;

    public static boolean updateNeeded;

    public static SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");

    // to store if the user has given permission to storage and location
    public static boolean storageAccepted;
    public static boolean locationAccepted;

    static markedLocation user;
    static double conversion = 0.621;
    static Geocoder geocoder;

    public static int activeBike;

    public static int locationUpdatesTime;
    public static int lastHowManyFuels;
    public static boolean incCarEvents;
    public static boolean incBikeEvents;
    public static String jsonLocation = "http://www.androidandy.uk/json/";
    public static boolean backgroundsWanted;
    public static boolean notificationsWanted;

    public static SQLiteDatabase vehiclesDB;

    public static String userLocationForWeather;

    public static final DecimalFormat precision = new DecimalFormat("0.00");
    public static final DecimalFormat oneDecimal = new DecimalFormat("0.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("Main Activity", "onCreate");

        // until I implement landscape view, lock the orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sharedPreferences = this.getSharedPreferences("com.androidandyuk.autobuddy", Context.MODE_PRIVATE);
        ed = sharedPreferences.edit();


        vehiclesDB = this.openOrCreateDatabase("Vehicles", MODE_PRIVATE, null);

        vehiclesDB.execSQL("CREATE TABLE IF NOT EXISTS vehicles (make VARCHAR, model VARCHAR, reg VARCHAR, bikeId VARCHAR, VIN VARCHAR, serviceDue VARCHAR, MOTdue VARCHAR" +
                ", lastKnownService VARCHAR, lastKnownMOT VARCHAR, yearOfMan VARCHAR, notes VARCHAR, estMileage VARCHAR, MOTwarned VARCHAR, serviceWarned VARCHAR, taxDue VARCHAR)");

        loadSettings();

        // check if there are any bikes
        if (bikes.size() == 0) {
            activeBike = -1;
        }

        // requesting permissions to access storage and location
        Log.i("storageAccepted " + storageAccepted, "locationAccepted " + locationAccepted);
        if (!storageAccepted || !locationAccepted) {
            String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION"};
            int permsRequestCode = 200;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(perms, permsRequestCode);
            }
        }

        checkUpdate();

        Log.i("updateNeeded", "" + updateNeeded);
        if (updateNeeded) {
            loadBikesOld();
            loadFuelsOld();
            loadLogsOld();
            loadToDosOld();
            saveFuels();
            saveLogs();
            saveToDos();
            getApplicationContext().getSharedPreferences("CREDENTIALS", 0).edit().clear().commit();
            updateNeeded = false;
            ed.putBoolean("updateNeeded3", false).apply();
            saveBikes();
            loadShows();
        } else {
            loadBikes();
            loadFuels();
            loadLogs();
            loadToDos();
            loadShows();
        }
        checkMOTwarning();
        checkServiceWarning();

//        //      download the weather
//        weatherText = (TextView) findViewById(R.id.weatherView);
//        DownloadTask task = new DownloadTask();
//
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//
//        locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//
//                //centerMapOnLocation(location, "Your location");
//
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String s) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String s) {
//
//            }
//        };
//
//        userLatLng = new LatLng(51.5412794, -0.2799549);  //  default to Ace Cafe until location is overwritten
//
//        user = new markedLocation("You", "", userLatLng, "");
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10800000, 1000, locationListener);
//
//            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//            Log.i("lastKnownLocation", "" + lastKnownLocation);
//            if (lastKnownLocation != null) {
//                user.setLocation(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
//            }
//        }
//
//        if (user.location != null) {
//            //change this to be users location
//            double userLat = user.location.latitude;
//            double userLon = user.location.longitude;
//            String userLocation = "lat=" + userLat + "&lon=" + userLon;
//            userLocationForWeather = "http://api.openweathermap.org/data/2.5/weather?" + userLocation + "&APPID=81e5e0ca31ad432ee9153dd761ed3b27";
//            Log.i("Getting Weather", userLocationForWeather);
//            task.execute(userLocationForWeather);
//
//        }

        Favourites.loadFavs();
        Favourites.sortMyList();

        instructionsRead = sharedPreferences.getBoolean("instructionsRead", false);
        ImageView instructions;
        if (!instructionsRead) {
            instructions = (ImageView) findViewById(R.id.instructions);
            instructions.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent(getApplicationContext(), Garage.class);
            startActivity(intent);
        }
    }

    public void hideInstructions(View view) {
        ImageView instructions;
        ed.putBoolean("instructionsRead", true).apply();
        instructions = (ImageView) findViewById(R.id.instructions);
        instructions.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(getApplicationContext(), Garage.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case 200:

                storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                locationAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                Log.i("PERMS storageAccepted " + storageAccepted, "locationAccepted " + locationAccepted);
                ed.putBoolean("locationAccepted", locationAccepted).apply();
                ed.putBoolean("storageAccepted", storageAccepted).apply();

                break;

        }

    }

    public static boolean checkInRange(String due, Calendar testDate) {
        // establish what date we're testing against
        testDate.add(Calendar.DAY_OF_YEAR, warningDays);
        // get the date this bikes MOT is due
        Calendar dueDate = new GregorianCalendar();
        try {
            dueDate.setTime(sdf.parse(due));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("MOT Check", "Date conversion failed");
        }

        Log.i("dueDate", "" + dueDate);
        Log.i("testDate", "" + testDate);
        if (dueDate.before(testDate)) {
            return true;
        }
        return false;
    }

    public void checkMOTwarning() {
        for (Bike thisBike : bikes) {
            Calendar testDate = new GregorianCalendar();
            if (checkInRange(thisBike.MOTdue, testDate)) {
                // this bike is within limits for a warning
                Toast.makeText(MainActivity.this, "MOT Due for " + thisBike, Toast.LENGTH_LONG).show();
                // give a notification if not had one before
            } else {
                thisBike.MOTwarned = false;
            }
        }
    }

    public void checkServiceWarning() {
        for (Bike thisBike : bikes) {
            Calendar testDate = new GregorianCalendar();
            if (checkInRange(thisBike.serviceDue, testDate)) {
                // this bike is within limits for a warning
                Toast.makeText(MainActivity.this, "Service Due for " + thisBike, Toast.LENGTH_LONG).show();
                // give a notification if not had one before
            } else {
                thisBike.serviceWarned = false;
            }

        }
    }

    public void nextNotification(Context context) {
        Log.i(TAG, "nextNotification");

        SharedPreferences sharedPreferences = context.getSharedPreferences("com.androidandyuk.autobuddy", Context.MODE_PRIVATE);
        int warningDays = sharedPreferences.getInt("warningDays", 30);
        int notiHour = sharedPreferences.getInt("notiHour", 10);
        int notiMinute = sharedPreferences.getInt("notiMinute", 0);

        // make versions of the array and database that can be used by the service
        ArrayList<Bike> theseBikes = new ArrayList<>();

        SQLiteDatabase vehiclesDB = context.openOrCreateDatabase("Vehicles", MODE_PRIVATE, null);

        vehiclesDB.execSQL("CREATE TABLE IF NOT EXISTS vehicles (make VARCHAR, model VARCHAR, reg VARCHAR, bikeId VARCHAR, VIN VARCHAR, serviceDue VARCHAR, MOTdue VARCHAR" +
                ", lastKnownService VARCHAR, lastKnownMOT VARCHAR, yearOfMan VARCHAR, notes VARCHAR, estMileage VARCHAR, MOTwarned VARCHAR, serviceWarned VARCHAR, taxDue VARCHAR)");

        try {

            Cursor c = vehiclesDB.rawQuery("SELECT * FROM vehicles", null);

            int makeIndex = c.getColumnIndex("make");
            int modelIndex = c.getColumnIndex("model");
            int regIndex = c.getColumnIndex("reg");
            int bikeIdIndex = c.getColumnIndex("bikeId");
            int VINIndex = c.getColumnIndex("VIN");
            int serviceDueIndex = c.getColumnIndex("serviceDue");
            int MOTdueIndex = c.getColumnIndex("MOTdue");
            int lastKnownServiceIndex = c.getColumnIndex("lastKnownService");
            int lastKnownMOTIndex = c.getColumnIndex("lastKnownMOT");
            int yearOfManIndex = c.getColumnIndex("yearOfMan");
            int notesIndex = c.getColumnIndex("notes");
            int estMileageIndex = c.getColumnIndex("estMileage");
            int MOTwarnedIndex = c.getColumnIndex("MOTwarned");
            int serviceWarnedIndex = c.getColumnIndex("serviceWarned");
            int taxDueIndex = c.getColumnIndex("taxDue");

            c.moveToFirst();

            do {

                ArrayList<String> make = new ArrayList<>();
                ArrayList<String> model = new ArrayList<>();
                ArrayList<String> reg = new ArrayList<>();
                ArrayList<String> bikeId = new ArrayList<>();
                ArrayList<String> VIN = new ArrayList<>();
                ArrayList<String> serviceDue = new ArrayList<>();
                ArrayList<String> MOTdue = new ArrayList<>();
                ArrayList<String> lastKnownService = new ArrayList<>();
                ArrayList<String> lastKnownMOT = new ArrayList<>();
                ArrayList<String> yearOfMan = new ArrayList<>();
                ArrayList<String> notes = new ArrayList<>();
                ArrayList<String> estMileage = new ArrayList<>();
                ArrayList<String> MOTwarned = new ArrayList<>();
                ArrayList<String> serviceWarned = new ArrayList<>();
                ArrayList<String> taxDue = new ArrayList<>();

                try {

                    make = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(makeIndex));
                    model = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(modelIndex));
                    reg = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(regIndex));
                    bikeId = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(bikeIdIndex));
                    VIN = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(VINIndex));
                    serviceDue = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(serviceDueIndex));
                    MOTdue = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(MOTdueIndex));
                    lastKnownService = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(lastKnownServiceIndex));
                    lastKnownMOT = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(lastKnownMOTIndex));
                    yearOfMan = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(yearOfManIndex));
                    notes = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(notesIndex));
                    estMileage = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(estMileageIndex));
                    MOTwarned = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(MOTwarnedIndex));
                    serviceWarned = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(serviceWarnedIndex));
                    taxDue = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(taxDueIndex));

                    Log.i("Bikes Restored ", "Count :" + make.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("Loading Bikes", "Failed attempt");
                }

                Log.i("Retrieved info", "Log count :" + make.size());
                if (make.size() > 0 && model.size() > 0 && bikeId.size() > 0) {
                    // we've checked there is some info
                    if (make.size() == model.size() && model.size() == bikeId.size()) {
                        // we've checked each item has the same amount of info, nothing is missing
                        for (int x = 0; x < make.size(); x++) {
                            int thisId = Integer.parseInt(bikeId.get(x));
                            double thisEstMileage = Double.parseDouble(estMileage.get(x));
                            boolean thisMOTwarned = Boolean.parseBoolean(MOTwarned.get(x));
                            boolean thisServiceWarned = Boolean.parseBoolean(serviceWarned.get(x));
                            Bike newBike = new Bike(thisId, make.get(x), model.get(x), reg.get(x), VIN.get(x), serviceDue.get(x), MOTdue.get(x), lastKnownService.get(x), lastKnownMOT.get(x),
                                    yearOfMan.get(x), notes.get(x), thisEstMileage, thisMOTwarned, thisServiceWarned, taxDue.get(x));
                            Log.i("Adding", " " + x + " " + newBike);
                            theseBikes.add(newBike);
                        }
                    }
                }
            } while (c.moveToNext());

        } catch (Exception e) {

            Log.i("LoadingDB", "Caught Error");
            e.printStackTrace();

        }

        Calendar now = Calendar.getInstance();
        // shouldn't matter what time of day it's checked, so set to notiTime
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);

        Calendar nextNoti = Calendar.getInstance();
        nextNoti.set(Calendar.YEAR, 9999);
        String notiMessage = "";

        for (Bike thisBike : theseBikes) {

            Calendar MOTDue = new GregorianCalendar();
            try {
                MOTDue.setTime(sdf.parse(thisBike.MOTdue));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar serviceDue = new GregorianCalendar();
            try {
                serviceDue.setTime(sdf.parse(thisBike.serviceDue));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar taxDue = new GregorianCalendar();
            TaxDue month = TaxDue.valueOf(thisBike.taxDue);
            int taxMonth = month.ordinal();
            taxDue.set(Calendar.MONTH, taxMonth);
            taxDue.set(Calendar.DAY_OF_MONTH, 1);
            if (taxDue.get(Calendar.MONTH) < now.get(Calendar.MONTH)) {
                taxDue.add(Calendar.YEAR, 1);
            }

            //remove the warningDays
            MOTDue.add(Calendar.DAY_OF_YEAR, - (warningDays));
            serviceDue.add(Calendar.DAY_OF_YEAR, - (warningDays));
            taxDue.add(Calendar.DAY_OF_YEAR, - (warningDays));

            // now check which is due next
            if (MOTDue.compareTo(nextNoti) < 0 && MOTDue.compareTo(now) >= 0) {
//            if (MOTDue.compareTo(nextNoti) < 0) {
                nextNoti = MOTDue;
                notiMessage = thisBike + " MOT due in " + warningDays + " days.";
            }

            if (serviceDue.compareTo(nextNoti) < 0 && serviceDue.compareTo(now) >= 0) {
                nextNoti = serviceDue;
                notiMessage = thisBike + " Service due in " + warningDays + " days.";
            }

            if (taxDue.compareTo(nextNoti) < 0 && taxDue.compareTo(now) >= 0) {
                nextNoti = taxDue;
                notiMessage = thisBike + " Tax due in " + warningDays + " days.";
            }

            // now set alarm for the next thing due
            nextNoti.set(Calendar.HOUR_OF_DAY, notiHour);
            nextNoti.set(Calendar.MINUTE, notiMinute);
            nextNoti.set(Calendar.SECOND, 0);
            setAlarm(nextNoti, notiMessage, context);

        }


    }

    public void setAlarm(Calendar myAlarmDate, String message, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent _myIntent = new Intent(context, NotificationReceiver.class);
        _myIntent.putExtra("MyMessage", message);
        PendingIntent _myPendingIntent = PendingIntent.getBroadcast(context, 123, _myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, myAlarmDate.getTimeInMillis(), _myPendingIntent);
        Log.i("setAlarm", "For : " + myAlarmDate);
        Log.i("setAlarm", "Message : " + message);
    }

    public void checkNotifications(Context context, String message) {
        Log.i(TAG, "checkNotifications");

        int notificationID = 100;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The id of the channel.
            String id = "my_channel_01";
            // The user-visible name of the channel.
            CharSequence name = "Channel Name";
            // The user-visible description of the channel.
            String description = "Channel Desc";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(mChannel);


            // Create a notification and set the notification channel.
            Notification.Builder notification = new Notification.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Be Aware!")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.icon)
                    .setChannelId(id)
                    .setAutoCancel(true);

            // Issue the notification.
            notificationManager.notify(notificationID, notification.build());

        } else {

            Notification.Builder notification = new Notification.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Be Aware!")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true);

            // Issue the notification.
            notificationManager.notify(notificationID, notification.build());
        }
    }

//    public void loadWeather(View view) {
//        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bbc.co.uk/weather/"));
//        startActivity(browserIntent);
//    }
//
//    public class DownloadTask extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... urls) {
//
//            String result = "";
//            URL url;
//            HttpURLConnection urlConnection = null;
//
//            try {
//                url = new URL(urls[0]);
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//
//                InputStream in = urlConnection.getInputStream();
//
//                InputStreamReader reader = new InputStreamReader(in);
//
//                int data = reader.read();
//
//                while (data != -1) {
//
//                    char current = (char) data;
//
//                    result += current;
//
//                    data = reader.read();
//
//                }
//
//                return result;
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            if (result != null) {
//                try {
//
//                    jsonObject = new JSONObject(result);
//
//                    String weatherInfo = jsonObject.getString("weather");
//
//                    Log.i("Weather content", weatherInfo);
//
//                    JSONArray arr = new JSONArray(weatherInfo);
//
//                    for (int i = 0; i < arr.length(); i++) {
//
//                        JSONObject jsonPart = arr.getJSONObject(i);
//
//                        Log.i("main", jsonPart.getString("main"));
//                        Log.i("description", jsonPart.getString("description"));
//
//                        currentForecast = jsonPart.getString("main");
//                        weatherText.setText("Today's forecast: " + currentForecast);
//
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//        }
//    }

    public static void checkUpdate() {
        updateNeeded = sharedPreferences.getBoolean("updateNeeded3", true);
        Log.i("updateNeeded3", " " + updateNeeded);
    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(R.id.main);
        if (backgroundsWanted) {
            int resID = getResources().getIdentifier("background_portrait", "drawable", this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            MainActivity.main.setBackground(drawablePic);
        } else {
            MainActivity.main.setBackgroundColor(getResources().getColor(R.color.background));
        }
    }

    public static void saveBikes() {

        Log.i("Main Activity", "saveBikesDB");
        ed.putInt("bikeCount", Bike.bikeCount).apply();
        ed.putInt("bikesSize", bikes.size()).apply();

        ArrayList<String> make = new ArrayList<>();
        ArrayList<String> model = new ArrayList<>();
        ArrayList<String> reg = new ArrayList<>();
        ArrayList<String> bikeId = new ArrayList<>();
        ArrayList<String> VIN = new ArrayList<>();
        ArrayList<String> serviceDue = new ArrayList<>();
        ArrayList<String> MOTdue = new ArrayList<>();
        ArrayList<String> lastKnownService = new ArrayList<>();
        ArrayList<String> lastKnownMOT = new ArrayList<>();
        ArrayList<String> yearOfMan = new ArrayList<>();
        ArrayList<String> notes = new ArrayList<>();
        ArrayList<String> estMileage = new ArrayList<>();
        ArrayList<String> MOTwarned = new ArrayList<>();
        ArrayList<String> serviceWarned = new ArrayList<>();
        ArrayList<String> taxDue = new ArrayList<>();

        for (Bike thisBike : bikes) {
            Log.i("Saving BikesDB", "" + thisBike);

            make.add(thisBike.make);
            model.add(thisBike.model);
            reg.add(thisBike.registration);
            bikeId.add(Integer.toString(thisBike.bikeId));
            VIN.add(thisBike.VIN);
            serviceDue.add(thisBike.serviceDue);
            MOTdue.add(thisBike.MOTdue);
            lastKnownService.add(thisBike.lastKnownService);
            lastKnownMOT.add(thisBike.lastKnownMOT);
            yearOfMan.add(thisBike.yearOfMan);
            notes.add(thisBike.notes);
            estMileage.add(Double.toString(thisBike.estMileage));
            MOTwarned.add(String.valueOf(thisBike.MOTwarned));
            serviceWarned.add(String.valueOf(thisBike.serviceWarned));
            taxDue.add(thisBike.taxDue);
        }
        Log.i("saveBikesDB", "Size :" + bikes.size());

        try {

            vehiclesDB.delete("vehicles", null, null);

            vehiclesDB.execSQL("INSERT INTO vehicles (make, model, reg, bikeId, VIN, serviceDue, MOTdue, lastKnownService, lastKnownMOT, yearOfMan, notes, estMileage, MOTwarned, serviceWarned, taxDue) VALUES ('" +
                    ObjectSerializer.serialize(make) + "' , '" + ObjectSerializer.serialize(model) + "' , '" + ObjectSerializer.serialize(reg) + "' , '" +
                    ObjectSerializer.serialize(bikeId) + "' , '" + ObjectSerializer.serialize(VIN) + "' , '" + ObjectSerializer.serialize(serviceDue) + "' , '" + ObjectSerializer.serialize(MOTdue) + "' , '" +
                    ObjectSerializer.serialize(lastKnownService) + "' , '" + ObjectSerializer.serialize(lastKnownMOT) + "' , '" + ObjectSerializer.serialize(yearOfMan) + "' , '" + ObjectSerializer.serialize(notes) + "' , '" +
                    ObjectSerializer.serialize(estMileage) + "' , '" + ObjectSerializer.serialize(MOTwarned) + "' , '" + ObjectSerializer.serialize(serviceWarned) + "' , '" + ObjectSerializer.serialize(taxDue) + "')");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public static void loadBikesOld() {
        Log.i("Main Activity", "loadBikes Old");
        int bikesSize = sharedPreferences.getInt("bikesSize", 0);

        Log.i("Bikes Size", "" + bikesSize);
        bikes.clear();

        ArrayList<String> make = new ArrayList<>();
        ArrayList<String> model = new ArrayList<>();
        ArrayList<String> reg = new ArrayList<>();
        ArrayList<String> bikeId = new ArrayList<>();
        ArrayList<String> VIN = new ArrayList<>();
        ArrayList<String> serviceDue = new ArrayList<>();
        ArrayList<String> MOTdue = new ArrayList<>();
        ArrayList<String> lastKnownService = new ArrayList<>();
        ArrayList<String> lastKnownMOT = new ArrayList<>();
        ArrayList<String> yearOfMan = new ArrayList<>();
        ArrayList<String> notes = new ArrayList<>();
        ArrayList<String> estMileage = new ArrayList<>();
        ArrayList<String> MOTwarned = new ArrayList<>();
        ArrayList<String> serviceWarned = new ArrayList<>();
        ArrayList<String> taxDue = new ArrayList<>();

        try {

            make = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("make", ObjectSerializer.serialize(new ArrayList<String>())));
            model = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("model", ObjectSerializer.serialize(new ArrayList<String>())));
            reg = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("reg", ObjectSerializer.serialize(new ArrayList<String>())));
            bikeId = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("bikeId", ObjectSerializer.serialize(new ArrayList<String>())));
            VIN = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("VIN", ObjectSerializer.serialize(new ArrayList<String>())));
            serviceDue = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("serviceDue", ObjectSerializer.serialize(new ArrayList<String>())));
            MOTdue = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("MOTdue", ObjectSerializer.serialize(new ArrayList<String>())));
            lastKnownService = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lastKnownService", ObjectSerializer.serialize(new ArrayList<String>())));
            lastKnownMOT = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lastKnownMOT", ObjectSerializer.serialize(new ArrayList<String>())));
            yearOfMan = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("yearOfMan", ObjectSerializer.serialize(new ArrayList<String>())));
            notes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("notes", ObjectSerializer.serialize(new ArrayList<String>())));
            estMileage = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("estMileage", ObjectSerializer.serialize(new ArrayList<String>())));
            MOTwarned = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("MOTwarned", ObjectSerializer.serialize(new ArrayList<String>())));
            serviceWarned = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("serviceWarned", ObjectSerializer.serialize(new ArrayList<String>())));
            taxDue = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("taxDue", ObjectSerializer.serialize(new ArrayList<String>())));

            Log.i("Bikes Restored ", "Count :" + make.size());
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Loading Bikes", "Failed attempt");
        }

        Log.i("Retrieved info", "Log count :" + make.size());
        if (make.size() > 0 && model.size() > 0 && bikeId.size() > 0) {
            // we've checked there is some info
            if (make.size() == model.size() && model.size() == bikeId.size()) {
                // we've checked each item has the same amount of info, nothing is missing
                for (int x = 0; x < make.size(); x++) {
                    Log.i("Retrieving", "Log " + x);
                    int thisId = Integer.parseInt(bikeId.get(x));
                    Log.i("Est Mileage", estMileage.get(x));
                    double thisEstMileage = Double.parseDouble(estMileage.get(x));
                    boolean thisMOTwarned = Boolean.parseBoolean(MOTwarned.get(x));
                    boolean thisServiceWarned = Boolean.parseBoolean(serviceWarned.get(x));
                    Bike newBike = new Bike(thisId, make.get(x), model.get(x), reg.get(x), VIN.get(x), serviceDue.get(x), MOTdue.get(x), lastKnownService.get(x), lastKnownMOT.get(x),
                            yearOfMan.get(x), notes.get(x), thisEstMileage, thisMOTwarned, thisServiceWarned, taxDue.get(x));
                    Log.i("Adding", "" + x + "" + newBike);
                    bikes.add(newBike);
                }
            }
        }
        Bike.bikeCount = sharedPreferences.getInt("bikeCount", 0);
        loadLogs();
        loadFuels();
    }

    public static void loadBikes() {

        Log.i("Main Activity", "New Bikes Loading");
        int bikesSize = sharedPreferences.getInt("bikesSize", 0);

        Log.i("Bikes Size", "" + bikesSize);
        bikes.clear();

        try {

            Cursor c = vehiclesDB.rawQuery("SELECT * FROM vehicles", null);

            int makeIndex = c.getColumnIndex("make");
            int modelIndex = c.getColumnIndex("model");
            int regIndex = c.getColumnIndex("reg");
            int bikeIdIndex = c.getColumnIndex("bikeId");
            int VINIndex = c.getColumnIndex("VIN");
            int serviceDueIndex = c.getColumnIndex("serviceDue");
            int MOTdueIndex = c.getColumnIndex("MOTdue");
            int lastKnownServiceIndex = c.getColumnIndex("lastKnownService");
            int lastKnownMOTIndex = c.getColumnIndex("lastKnownMOT");
            int yearOfManIndex = c.getColumnIndex("yearOfMan");
            int notesIndex = c.getColumnIndex("notes");
            int estMileageIndex = c.getColumnIndex("estMileage");
            int MOTwarnedIndex = c.getColumnIndex("MOTwarned");
            int serviceWarnedIndex = c.getColumnIndex("serviceWarned");
            int taxDueIndex = c.getColumnIndex("taxDue");

            c.moveToFirst();

            do {

                ArrayList<String> make = new ArrayList<>();
                ArrayList<String> model = new ArrayList<>();
                ArrayList<String> reg = new ArrayList<>();
                ArrayList<String> bikeId = new ArrayList<>();
                ArrayList<String> VIN = new ArrayList<>();
                ArrayList<String> serviceDue = new ArrayList<>();
                ArrayList<String> MOTdue = new ArrayList<>();
                ArrayList<String> lastKnownService = new ArrayList<>();
                ArrayList<String> lastKnownMOT = new ArrayList<>();
                ArrayList<String> yearOfMan = new ArrayList<>();
                ArrayList<String> notes = new ArrayList<>();
                ArrayList<String> estMileage = new ArrayList<>();
                ArrayList<String> MOTwarned = new ArrayList<>();
                ArrayList<String> serviceWarned = new ArrayList<>();
                ArrayList<String> taxDue = new ArrayList<>();

                try {

                    make = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(makeIndex));
                    model = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(modelIndex));
                    reg = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(regIndex));
                    bikeId = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(bikeIdIndex));
                    VIN = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(VINIndex));
                    serviceDue = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(serviceDueIndex));
                    MOTdue = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(MOTdueIndex));
                    lastKnownService = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(lastKnownServiceIndex));
                    lastKnownMOT = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(lastKnownMOTIndex));
                    yearOfMan = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(yearOfManIndex));
                    notes = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(notesIndex));
                    estMileage = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(estMileageIndex));
                    MOTwarned = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(MOTwarnedIndex));
                    serviceWarned = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(serviceWarnedIndex));
                    taxDue = (ArrayList<String>) ObjectSerializer.deserialize(c.getString(taxDueIndex));

                    Log.i("Bikes Restored ", "Count :" + make.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("Loading Bikes", "Failed attempt");
                }

                Log.i("Retrieved info", "Log count :" + make.size());
                if (make.size() > 0 && model.size() > 0 && bikeId.size() > 0) {
                    // we've checked there is some info
                    if (make.size() == model.size() && model.size() == bikeId.size()) {
                        // we've checked each item has the same amount of info, nothing is missing
                        for (int x = 0; x < make.size(); x++) {
                            int thisId = Integer.parseInt(bikeId.get(x));
                            double thisEstMileage = Double.parseDouble(estMileage.get(x));
                            boolean thisMOTwarned = Boolean.parseBoolean(MOTwarned.get(x));
                            boolean thisServiceWarned = Boolean.parseBoolean(serviceWarned.get(x));
                            Bike newBike = new Bike(thisId, make.get(x), model.get(x), reg.get(x), VIN.get(x), serviceDue.get(x), MOTdue.get(x), lastKnownService.get(x), lastKnownMOT.get(x),
                                    yearOfMan.get(x), notes.get(x), thisEstMileage, thisMOTwarned, thisServiceWarned, taxDue.get(x));
                            Log.i("Adding", " " + x + " " + newBike);
                            bikes.add(newBike);
                        }
                    }
                }
            } while (c.moveToNext());

        } catch (Exception e) {

            Log.i("LoadingDB", "Caught Error");
            e.printStackTrace();

        }
        Bike.bikeCount = sharedPreferences.getInt("bikeCount", 0);
        loadLogs();
        loadFuels();
    }

    public static void loadSettings() {
        int bikesSize = sharedPreferences.getInt("bikesSize", 0);
        activeBike = sharedPreferences.getInt("activeBike", bikesSize - 1);
        lastHowManyFuels = sharedPreferences.getInt("lastHowManyFuels", 10);
        warningDays = sharedPreferences.getInt("warningDays", 30);
        notiHour = sharedPreferences.getInt("notiHour", 10);
        notiMinute = sharedPreferences.getInt("notiMinute", 0);
        locationUpdatesTime = sharedPreferences.getInt("locationUpdatesTime", 1200000);
        incCarEvents = sharedPreferences.getBoolean("incCarEvents", true);
        incBikeEvents = sharedPreferences.getBoolean("incBikeEvents", true);
        backgroundsWanted = sharedPreferences.getBoolean("backgroundsWanted", false);
        notificationsWanted = sharedPreferences.getBoolean("notificationsWanted", true);
        locationAccepted = sharedPreferences.getBoolean("locationAccepted", false);
        storageAccepted = sharedPreferences.getBoolean("storageAccepted", false);
        updateNeeded = sharedPreferences.getBoolean("updateNeeded3", true);
        currencySetting = sharedPreferences.getString("currencySetting", "£");
        milesSetting = sharedPreferences.getString("milesSetting", "Miles");
    }

    public static void saveSettings() {
        ed.putInt("activeBike", activeBike).apply();
        ed.putInt("lastHowManyFuels", lastHowManyFuels).apply();
        ed.putInt("warningDays", warningDays).apply();
        ed.putInt("notiHour", notiHour).apply();
        ed.putInt("notiMinute", notiMinute).apply();
        ed.putInt("locationUpdatesTime", locationUpdatesTime).apply();
        ed.putBoolean("incCarEvents", incCarEvents).apply();
        ed.putBoolean("incBikeEvents", incBikeEvents).apply();
        ed.putBoolean("backgroundsWanted", backgroundsWanted).apply();
        ed.putBoolean("notificationsWanted", notificationsWanted).apply();
        ed.putBoolean("locationAccepted", locationAccepted).apply();
        ed.putBoolean("storageAccepted", storageAccepted).apply();
        ed.putBoolean("updateNeeded3", updateNeeded).apply();
        ed.putString("currencySetting", currencySetting).apply();
        ed.putString("milesSetting", milesSetting).apply();
    }

    @Override
    protected void onDestroy() {
        nextNotification(this);
        super.onDestroy();
//        SendLogcatMail();
    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
        loadSettings();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = settings.getString("theme", "1");
        Log.i("Theme", theme);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveBikes();
        saveSettings();
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Logs Activity", "On Stop");
        saveBikes();
        saveSettings();
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }
}
