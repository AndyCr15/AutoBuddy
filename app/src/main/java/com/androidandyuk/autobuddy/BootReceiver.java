package com.androidandyuk.autobuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by AndyCr15 on 24/08/2017.
 */

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy");

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG ,"onReceive");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i(TAG ,"onReceive2");
            nextNotification(context);
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

        SQLiteDatabase vehiclesDB = context.openOrCreateDatabase("Vehicles", Context.MODE_PRIVATE, null);

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

}
