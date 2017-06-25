package com.androidandyuk.autobuddy;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import static com.androidandyuk.autobuddy.MainActivity.conversion;
import static com.androidandyuk.autobuddy.MainActivity.milesSetting;

/**
 * Created by AndyCr15 on 16/05/2017.
 */

public class TrafficEvent implements Comparable<TrafficEvent> {
    String title;
    String road;
    LatLng location;
    String delay;

    public TrafficEvent() {
        this.title = "";
        this.road = "";
        this.location = null;
        this.delay = "";
//        this.description = "";
    }

    public double getDistance(LatLng o) {

        double lat1 = this.location.latitude;
        double lng1 = this.location.longitude;
        double lat2 = o.latitude;
        double lng2 = o.longitude;

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
        return d;
    }

    @Override
    public String toString() {
        return this.title + " " + this.delay;
    }

    @Override
    public int compareTo(@NonNull TrafficEvent o) {
        return (int) this.getDistance(MainActivity.userLatLng) - (int) o.getDistance(MainActivity.userLatLng);
    }
}
