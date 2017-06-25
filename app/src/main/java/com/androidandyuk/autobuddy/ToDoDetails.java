package com.androidandyuk.autobuddy;

import android.support.annotation.NonNull;

import static com.androidandyuk.autobuddy.ToDoPriority.HIGH;
import static com.androidandyuk.autobuddy.ToDoPriority.LOW;
import static com.androidandyuk.autobuddy.ToDoPriority.MEDIUM;

/**
 * Created by AndyCr15 on 14/06/2017.
 */

enum ToDoPriority {
    LOW(1), MEDIUM(2), HIGH(3);
    private final int value;

    private ToDoPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class ToDoDetails implements Comparable<ToDoDetails> {
    String log;
    double price;
    String url;
    ToDoPriority priority;

    public ToDoDetails(String log, double price, String url, ToDoPriority priority) {
        this.log = log;
        this.price = price;
        this.url = url;
        this.priority = priority;
    }

    public ToDoDetails(String log, double price, String url, int priority) {
        this.log = log;
        this.price = price;
        this.url = url;
        switch(priority){
            case 1: this.priority = LOW;
                break;
            case 2: this.priority = MEDIUM;
                break;
            case 3: this.priority = HIGH;
                break;
        }
    }

    public String getLog() {
        return log;
    }

    public double getPrice() {
        return price;
    }

    public String getUrl() {
        return url;
    }

    public ToDoPriority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(@NonNull ToDoDetails o) {
        int compareValue = o.priority.getValue() - this.priority.getValue();

        if (compareValue == 0) {
            return (int) o.getPrice() - (int) this.getPrice();
        } else {
            return compareValue;
        }
    }
}
