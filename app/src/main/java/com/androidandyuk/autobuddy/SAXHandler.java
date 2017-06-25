package com.androidandyuk.autobuddy;

import com.google.android.gms.maps.model.LatLng;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by AndyCr15 on 16/05/2017.
 */

public class SAXHandler extends DefaultHandler {

    private TrafficEvent newTrafficEvent;
    private String content;
    private String latitude;
    private String longitude;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (qName.equals("title")) {
            newTrafficEvent = new TrafficEvent();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "title":
                newTrafficEvent.title = content;
                break;
            case "road":
                newTrafficEvent.road = content;
                break;
            case "category":
                newTrafficEvent.delay = content;
                break;

            case "latitude":
                latitude = content;
                break;
            case "longitude":
                longitude = content;
                newTrafficEvent.location = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                Traffic.trafficEvents.add(newTrafficEvent);
                break;
//            case "description":
////                if (newTrafficEvent.description != null) {
////                    newTrafficEvent.description = content;
////                }
//                Traffic.trafficEvents.add(newTrafficEvent);
//                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content = String.copyValueOf(ch, start, length).trim();
    }
}
