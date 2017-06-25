package com.androidandyuk.autobuddy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;

import static com.androidandyuk.autobuddy.MainActivity.activeBike;
import static com.androidandyuk.autobuddy.MainActivity.bikes;
import static com.androidandyuk.autobuddy.MainActivity.sdf;

/**
 * Created by AndyCr15 on 12/05/2017.
 */

public class maintenanceLogDetails implements Comparable<maintenanceLogDetails> {
    String date;
    String log;
    double price;
    double mileage;
    Boolean wasService;
    Boolean wasMOT;
    Boolean brakePads;
    Boolean brakeDiscs;
    Boolean frontTyre;
    Boolean rearTyre;
    Boolean oilChange;
    Boolean newBattery;
    Boolean coolantChange;
    Boolean sparkPlugs;
    Boolean airFilter;
    Boolean brakeFluid;

    public maintenanceLogDetails(String date, String log, double price, double mileage, Boolean wasService, Boolean wasMOT, Boolean brakePads, Boolean brakeDiscs,
                                 Boolean frontTyre, Boolean rearTyre, Boolean oilChange, Boolean newBattery, Boolean coolantChange, Boolean sparkPlugs, Boolean airFilter, Boolean brakeFluid) {
        // only used when editing a maintenance log

        this.date = date;
        this.log = log;
        this.price = price;
        this.mileage = mileage;
        this.wasService = wasService;
        this.wasMOT = wasMOT;
        this.brakePads = brakePads;
        this.brakeDiscs = brakeDiscs;
        this.frontTyre = frontTyre;
        this.rearTyre = rearTyre;
        this.oilChange = oilChange;
        this.newBattery = newBattery;
        this.coolantChange = coolantChange;
        this.sparkPlugs = sparkPlugs;
        this.airFilter = airFilter;
        this.brakeFluid = brakeFluid;
    }

    public maintenanceLogDetails(Date logDate, String log, double price, double mileage, Boolean wasService, Boolean wasMOT, Boolean brakePads, Boolean brakeDiscs,
                                 Boolean frontTyre, Boolean rearTyre, Boolean oilChange, Boolean newBattery, Boolean coolantChange, Boolean sparkPlugs, Boolean airFilter, Boolean brakeFluid) {
        // this one is only used in loading logs after a save

        this.date = sdf.format(logDate);
        this.log = log;
        this.price = price;
        this.wasService = wasService;
        this.wasMOT = wasMOT;
        this.brakePads = brakePads;
        this.brakeDiscs = brakeDiscs;
        this.frontTyre = frontTyre;
        this.rearTyre = rearTyre;
        this.oilChange = oilChange;
        this.newBattery = newBattery;
        this.coolantChange = coolantChange;
        this.sparkPlugs = sparkPlugs;
        this.airFilter = airFilter;
        this.brakeFluid = brakeFluid;
        this.mileage = mileage;


    }

    public maintenanceLogDetails(String log, double price, double mileage, Boolean wasService, Boolean wasMOT, Boolean brakePads, Boolean brakeDiscs, Boolean frontTyre,
                                 Boolean rearTyre, Boolean oilChange, Boolean newBattery, Boolean coolantChange, Boolean sparkPlugs, Boolean airFilter, Boolean brakeFluid) {
        // used for brand new maintenance log entries

        Date logDate = new Date();
        this.date = sdf.format(logDate);
        this.log = log;
        this.price = price;
        this.wasService = wasService;
        this.wasMOT = wasMOT;
        this.brakePads = brakePads;
        this.brakeDiscs = brakeDiscs;
        this.frontTyre = frontTyre;
        this.rearTyre = rearTyre;
        this.oilChange = oilChange;
        this.newBattery = newBattery;
        this.coolantChange = coolantChange;
        this.sparkPlugs = sparkPlugs;
        this.airFilter = airFilter;
        this.brakeFluid = brakeFluid;

        if (mileage != 0 && mileage >= bikes.get(activeBike).estMileage) {
            bikes.get(activeBike).estMileage = mileage;
            this.mileage = mileage;
        } else if (mileage != 0) {
            Context context = App.getContext();
            Toast.makeText(context, "The mileage appears to be lower than current est mileage. Not applied", Toast.LENGTH_LONG).show();
            this.mileage = bikes.get(activeBike).estMileage;
        } else {
        }
    }

    public String getDate() {
        return date;
    }

    public String getLog() {
        return log;
    }

    public double getPrice() {
        return price;
    }

    public double getMileage() {
        return mileage;
    }

    public Boolean getWasService() {
        return wasService;
    }

    public Boolean getWasMOT() {
        return wasMOT;
    }

    public Boolean getBrakePads() {
        return brakePads;
    }

    public Boolean getBrakeDiscs() {
        return brakeDiscs;
    }

    public Boolean getFrontTyre() {
        return frontTyre;
    }

    public Boolean getRearTyre() {
        return rearTyre;
    }

    public Boolean getOilChange() {
        return oilChange;
    }

    public Boolean getNewBattery() {
        return newBattery;
    }

    public Boolean getCoolantChange() {
        return coolantChange;
    }

    public Boolean getSparkPlugs() {
        return sparkPlugs;
    }

    public Boolean getAirFilter() {
        return airFilter;
    }

    public Boolean getBrakeFluid() {
        return brakeFluid;
    }

    @Override
    public int compareTo(@NonNull maintenanceLogDetails o) {

        Date thisDate = new Date();
        Date oDate = new Date();

        try {
            thisDate = sdf.parse(this.date);
            oDate = sdf.parse(o.date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (thisDate.after(oDate)) {
            return -1;
        }

        if (oDate.after(thisDate)) {
            return 1;
        }

        // if we're here, they're the same date, so use mileage to decide
        return (int) o.mileage - (int) this.mileage;
    }

    @Override
    public String toString() {

        String message = "";

        if (this.wasMOT) {
            message += "[MOT]";
        }

        if (this.wasService) {
            message += "[Service]";
        }


        java.text.DecimalFormat df = new java.text.DecimalFormat();
        df.setMaximumFractionDigits(2);

        String tempLog = log;
        if (log.length() > 60) {
            tempLog = log.substring(0, 59) + "...";
        }
        return this.date + " : " + message + tempLog;
    }
}
