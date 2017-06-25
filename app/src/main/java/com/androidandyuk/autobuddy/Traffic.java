package com.androidandyuk.autobuddy;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;
import static com.androidandyuk.autobuddy.MainActivity.ed;
import static com.androidandyuk.autobuddy.MainActivity.sharedPreferences;

public class Traffic extends AppCompatActivity {

    public static ArrayList<TrafficEvent> trafficEvents = new ArrayList<>();
    static MyTrafficAdapter myAdapter;
    ListView trafficList;

    public static RelativeLayout main;

    private DownloadManager downloadManager;
    public static long downloadId;
    private Handler handlerUpdate;

    public int trafficUpdatesMaximumMinutes = 30;
    private static boolean updateTraffic = true;
    public static Calendar lastTrafficUpdate;
    public static int lastTrafficUpdateDay;
    public static int lastTrafficUpdateMins;
//    public long secondsSinceUpdate;

    private volatile boolean done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_traffic);

        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(new DownloadReceiver(), intentFilter);

        setTitle("Traffic");

        sharedPreferences = this.getSharedPreferences("com.androidandyuk.autobuddy", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();

        // decide if a new update is required
        updateTraffic = false;

        Calendar now = Calendar.getInstance();

        int currentMins = now.get(Calendar.MINUTE) + (now.get(Calendar.HOUR_OF_DAY) * 60);

        if (lastTrafficUpdateDay < now.get(Calendar.DAY_OF_YEAR)) {
            updateTraffic = true;
        } else if (lastTrafficUpdateMins + trafficUpdatesMaximumMinutes < currentMins) {
            updateTraffic = true;
        }
        Log.i("Mins since update " + (currentMins - lastTrafficUpdateMins), "Update = " + updateTraffic);


        if (updateTraffic && MainActivity.storageAccepted) {
            downloadTraffic();
//            updateTraffic = false;
        } else if (!MainActivity.storageAccepted) {
            Toast.makeText(this, "Permissions to save the data to your Downloads folder is needed to receive traffic information", Toast.LENGTH_LONG).show();
        }

    }

    private class MyTrafficAdapter extends BaseAdapter {
        public ArrayList<TrafficEvent> trafficDataAdapter;

        public MyTrafficAdapter(ArrayList<TrafficEvent> trafficDataAdapter) {
            this.trafficDataAdapter = trafficDataAdapter;
        }

        @Override
        public int getCount() {
            return trafficDataAdapter.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();
            View myView = mInflater.inflate(com.androidandyuk.autobuddy.R.layout.traffic_listview, null);

            final TrafficEvent s = trafficDataAdapter.get(position);

            TextView trafficListRoad = (TextView) myView.findViewById(com.androidandyuk.autobuddy.R.id.trafficListRoad);
            trafficListRoad.setText(s.road);

            TextView trafficListTitle = (TextView) myView.findViewById(com.androidandyuk.autobuddy.R.id.trafficListTitle);
            trafficListTitle.setText(s.title);

            TextView trafficListDelay = (TextView) myView.findViewById(com.androidandyuk.autobuddy.R.id.trafficListDelay);
            trafficListDelay.setText(s.delay);

            return myView;
        }

    }

    private void parseList() {
        Log.i("Traffic", "Parsing List");
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();

            SAXHandler handler = new SAXHandler();

            InputStream source = new FileInputStream("/storage/emulated/0/Download/allevents.xml");
            parser.parse(source, handler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initiateList();
    }

    private void initiateList() {
        Log.i("Traffic", "Initiating List");
        Boolean found = false;
        Collections.sort(trafficEvents);
        for (int i = 0; i < trafficEvents.size(); i++) {
            if (trafficEvents.get(i).delay.toLowerCase().contains("no delay")) {
//                Log.i("Removed Traffic Item",trafficEvents.get(i).delay);
                trafficEvents.remove(i);
                found = true;
            } else {
//                Log.i("Traffic Item Kept",trafficEvents.get(i).delay);
            }
            // if there's another entry, check if it's the same reason
            // if it is, remove it
            if (i + 1 < trafficEvents.size()) {
                if (trafficEvents.get(i).title.equals(trafficEvents.get(i + 1).title)) {
                    trafficEvents.remove(i);
                }
            }
        }

        // check if the last initialisation found any 'No Delays'
        // if it did, run it again
        if (found) {
            initiateList();
        }

        Log.i("Initiating List", "List size " + trafficEvents.size());
        trafficList = (ListView) findViewById(com.androidandyuk.autobuddy.R.id.trafficList);
        myAdapter = new MyTrafficAdapter(trafficEvents);
        trafficList.setAdapter(myAdapter);
    }

    private Runnable runnableUpdate = new Runnable() {
        @Override
        public void run() {
            if (done)
                return;
            handlerUpdate.postDelayed(this, 1000);
        }
    };

    public void viewTrafficOnMap(View view) {
        Log.i("Traffic", "View on map called");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        // 9998 tells the Maps activity to show all the markers
        intent.putExtra("placeNumber", 9998);
        intent.putExtra("Type", "Traffic");
        startActivity(intent);
    }

    public void forceUpdate(View view) {
        updateTraffic = true;
        if (updateTraffic && MainActivity.storageAccepted) {
            downloadTraffic();
//            updateTraffic = false;
        } else if (!MainActivity.storageAccepted) {
            Toast.makeText(this, "Permissions to save the data to your Downloads folder is needed to receive traffic information", Toast.LENGTH_LONG).show();
        }
    }

    public void downloadTraffic() {
        Log.i("Traffic", "downloadTraffic");
        done = false;
        // set when the last update happened
        Calendar now = Calendar.getInstance();
        lastTrafficUpdateDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        lastTrafficUpdateMins = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60) + Calendar.getInstance().get(Calendar.MINUTE);
        saveUpdateTime();
        Uri uriDownload = Uri.parse("http://m.highways.gov.uk/feeds/rss/AllEvents.xml");
        downloadFile(uriDownload);
    }

    private void downloadFile(Uri uri) {
        Log.i("Traffic", "downloadFile");

        // delete the old file before downloading a new one
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +
                "allevents.xml");
        file.delete();

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("allevents.xml");
        request.setDescription("Downloading Traffic Info");

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "allevents.xml");
        downloadId = downloadManager.enqueue(request);
        handlerUpdate = new Handler();
        handlerUpdate.post(runnableUpdate);
    }

    public class DownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long refId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (refId == downloadId) {
                Toast.makeText(context, "Traffic File Updated", Toast.LENGTH_SHORT).show();
                parseList();
//                initiateList();
            }
        }
    }

    public void saveUpdateTime() {
        Log.i("Traffic", "Saving Update Time");

        ed.putInt("minsSinceUpdate", lastTrafficUpdateMins).apply();
        ed.putInt("daysSinceUpdate", lastTrafficUpdateDay).apply();

    }

    public void loadUpdateTime() {
        Log.i("Traffic", "Loading Update Time");

        lastTrafficUpdateMins = sharedPreferences.getInt("minsSinceUpdate", 0);
        lastTrafficUpdateDay = sharedPreferences.getInt("daysSinceUpdate", 0);

    }

    public void checkBackground() {
        main = (RelativeLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Traffic.main.setBackground(drawablePic);
        } else {
            Traffic.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
        }
    }

    @Override
    public void onBackPressed() {
        // this must be empty as back is being dealt with in onKeyDown
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUpdateTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Traffic", "On Resume");
        parseList();
        initiateList();
        loadUpdateTime();
        checkBackground();
    }

    @Override
    protected void onStop() {
//        unregisterReceiver(DownloadReceiver);
        super.onStop();
    }
}
