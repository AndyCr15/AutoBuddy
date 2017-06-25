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

//        // being sent in with a date means it's from an edit or a save
//        Date todaysDate = new Date();
//        String formattedDate = sdf.format(todaysDate);
//        // check if it's still the same day, allow mileage to be changed
//        if (date.equals(formattedDate)) {
//            if (mileage != 0 && mileage > MainActivity.bikes.get(activeBike).estMileage) {
//                MainActivity.bikes.get(activeBike).estMileage = mileage;
//                this.mileage = mileage;
//            } else if (mileage != 0) {
//                Context context = App.getContext();
//                Toast.makeText(context, "The mileage appears to be lower than current est mileage. Not applied", Toast.LENGTH_LONG).show();
//            } else {
//                // mileage has been left blank
//                MainActivity.bikes.get(activeBike).estMileage += miles;
//            }
//        } else {
//            Context context = App.getContext();
//            Toast.makeText(context, "You can't change mileage from previous days entries. It will update the next time you provide the mileage", Toast.LENGTH_LONG).show();
//        }
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

//    @Override
//    public String toString() {
//        DecimalFormat df = new DecimalFormat();
//        df.setMaximumFractionDigits(2);
//        return this.date + " - Miles : " + miles + " - mpg = " + df.format(mpg);
//    }

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
