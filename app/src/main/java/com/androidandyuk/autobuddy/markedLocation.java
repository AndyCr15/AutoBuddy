package com.androidandyuk.autobuddy;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;
import static com.androidandyuk.autobuddy.MainActivity.user;

/**
 * Created by AndyCr15 on 06/05/2017.
 */

public class markedLocation implements Comparable<markedLocation> {
    String name;
    LatLng location;
    String address;
    String comment;
    double distance;
    String start;
    String end;
    String url;

    public markedLocation(String name, LatLng location, String address, String comment, String start, String end, String url) {
        this.name = name;
        this.location = location;
        this.address = address;
        this.comment = comment;
        this.start = start;
        this.end = end;
        this.url = url;
    }

    public markedLocation(String name, String address, LatLng location, String comment) {
        Log.i("New markedLocation", name);
        this.name = name;
        this.address = address;
        this.location = location;
        this.comment = comment;
    }

    public markedLocation(String name, LatLng location, String comment) {
        Log.i("New markedLocation", name);
        this.name = name;
        this.location = location;
        this.comment = comment;
        this.address = getAddress();
    }

    public double getDistance(markedLocation o) {
        if (o != null && this != o) {
            double lat1 = this.location.latitude;
            double lng1 = this.location.longitude;
            double lat2 = o.location.latitude;
            double lng2 = o.location.longitude;

            int r = 6371; // average radius of the earth in km
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = r * c;
            if(milesSetting.equals("Miles")){
                d = d * conversion;
            }
            this.distance = d;
            return d;
        }
        return 0;
    }

    public String getAddress() {
        String strAdd = "";
        Context context = App.getContext();

        double LATITUDE = this.location.latitude;
        double LONGITUDE = this.location.longitude;

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.i("Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.i("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Current loction address", "Canont get Address!");
        }
        return strAdd;
    }


    public LatLng getLocation() {

        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " : " + (int) (getDistance(user) * MainActivity.conversion) + " miles";
    }

    @Override
    public int compareTo(@NonNull markedLocation o) {
        return (int) this.getDistance(MainActivity.user) - (int) o.getDistance(MainActivity.user);
    }
}
