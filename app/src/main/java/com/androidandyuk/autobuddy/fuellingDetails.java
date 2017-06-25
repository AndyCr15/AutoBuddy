package com.androidandyuk.autobuddy;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.util.Date;

import static com.androidandyuk.autobuddy.MainActivity.sdf;

/**
 * Created by AndyCr15 on 08/05/2017.
 */

public class fuellingDetails implements Comparable<fuellingDetails> {
    String date;
    double miles;
    double price;
    double litres;
    double mpg;
    double mileage = 0;

    public fuellingDetails(double miles, double price, double litres, Date date, double mileage) {
        this.miles = miles;
        this.price = price;
        this.litres = litres;
        mpg = miles / (litres / 4.54609);
        this.date = sdf.format(date);
        this.mileage = mileage;
    }

    public fuellingDetails(double miles, double price, double litres, String date, double mileage) {
        // adding a brand new fueling
        this.miles = miles;
        this.price = price;
        this.litres = litres;
        mpg = miles / (litres / 4.54609);
        this.date = date;
        this.mileage = mileage;
    }

    public String getDate() {
        return date;
    }

    public double getMiles() {
        return miles;
    }

    public double getPrice() {
        return price;
    }

    public double getLitres() {
        return litres;
    }

    public double getMpg() {
        return mpg;
    }

    public double getMileage() {
        return mileage;
    }

    @Override
    public String toString() {
        return "Date" + date + " Miles " + miles;
    }

    @Override
    public int compareTo(@NonNull fuellingDetails o) {
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
}
