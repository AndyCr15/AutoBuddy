package com.androidandyuk.autobuddy;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import static com.androidandyuk.autobuddy.MainActivity.sdf;

/**
 * Created by AndyCr15 on 09/05/2017.
 */

enum TaxDue {
    JAN(1), FEB(2), MAR(3), APR(4), MAY(5), JUN(6), JUL(7),AUG(8),SEP(9),OCT(10),NOV(11),DEC(12);
    private final int value;

    private TaxDue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

enum MilesKM {
    Miles(1), Km(2);
    private final int value;

    private MilesKM(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

enum Currency {
    £(1), $(2),€(3);
    private final int value;

    private Currency(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}


public class Bike {
    static int bikeCount;
    int bikeId;
    String make;
    String model;
    String registration;
    String VIN;
    String serviceDue;
    String MOTdue;
    String lastKnownService;
    String lastKnownMOT;
    String yearOfMan;
    String notes;
    String taxDue;

    double estMileage;
    boolean MOTwarned;
    boolean serviceWarned;
    ArrayList<fuellingDetails> fuelings = new ArrayList<>();
    ArrayList<maintenanceLogDetails> maintenanceLogs = new ArrayList<>();
    ArrayList<ToDoDetails> toDoList = new ArrayList<>();


    public Bike(int bikeId, String make, String model, String registration, String VIN, String serviceDue, String MOTdue,
                String lastKnownService, String lastKnownMOT, String yearOfMan, String notes, double estMileage,
                boolean MOTwarned, boolean serviceWarned, String taxDue) {
        this.bikeId = bikeId;
        this.make = make;
        this.model = model;
        this.registration = registration;
        this.VIN = VIN;
        this.serviceDue = serviceDue;
        this.MOTdue = MOTdue;
        this.lastKnownService = lastKnownService;
        this.lastKnownMOT = lastKnownMOT;
        this.yearOfMan = yearOfMan;
        this.notes = notes;
        this.estMileage = estMileage;
        this.MOTwarned = MOTwarned;
        this.serviceWarned = serviceWarned;
        this.taxDue = taxDue;
        // no bikeCount increment as this is only used by loading bikes, which restores the old bikeCount anyway
    }

    public Bike(String make, String model, String year) {

        // any new bike has it's MOT and service set to today
        Calendar cal = Calendar.getInstance();
//        int yearNow = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH);
//        int day = cal.get(Calendar.DAY_OF_MONTH);
        String todaysDate = sdf.format(cal.getTime());

        this.make = make;
        this.model = model;
        this.registration = "unknown";
        this.bikeId = bikeCount;
        this.VIN = "";
        this.serviceDue = todaysDate;
        this.MOTdue = todaysDate;
        this.lastKnownService = "";
        this.lastKnownMOT = "";
        this.yearOfMan = year;
        this.estMileage = 1;
        this.taxDue = "JAN";
        bikeCount++;
    }

    public static double annualMiles(Bike o, int year){
        int miles = 0;
        for(fuellingDetails thisFuel : o.fuelings){
            // check what year this fuel happened in
            String thisStr[] = thisFuel.date.split("/");
            int thisYear = Integer.parseInt(thisStr[2]);
            if(thisYear == year){
                // in correct year, so add figures on
                miles += thisFuel.miles;
            }
        }
        Log.i("annualMiles ",year + " : " + miles);
        return miles;
    }

    @Override
    public String toString() {
        return yearOfMan + " " + model;
    }
}
