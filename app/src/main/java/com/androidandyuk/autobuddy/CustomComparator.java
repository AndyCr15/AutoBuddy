package com.androidandyuk.autobuddy;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import static com.androidandyuk.autobuddy.MainActivity.sdf;

/**
 * Created by AndyCr15 on 15/02/2018.
 */

public class CustomComparator implements Comparator<markedLocation> {

    @Override
    public int compare(markedLocation o1, markedLocation o2) {

        o1.getDistance(MainActivity.user);
        o2.getDistance(MainActivity.user);

        Date o1start = Calendar.getInstance().getTime();
        Date o2start = Calendar.getInstance().getTime();
        try {
            o1start = sdf.parse(o1.start);
            o2start = sdf.parse(o2.start);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(o1start.after(o2start)){
            return 1;
        } else {
            return -1;
        }
    }
}
