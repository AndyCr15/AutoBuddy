package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.IOException;

import static com.androidandyuk.autobuddy.MainActivity.backgroundsWanted;

public class AboutActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    public static ConstraintLayout main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_about);
        checkBackground();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    public void sendFeedback(View view){
        SendFeedbackMail();
    }

    public void SendFeedbackMailView(View view) {
        SendFeedbackMail();
    }

    public void checkBackground() {
        main = (ConstraintLayout) findViewById(com.androidandyuk.autobuddy.R.id.main);
        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            AboutActivity.main.setBackground(drawablePic);
        } else {
            AboutActivity.main.setBackgroundColor(getResources().getColor(com.androidandyuk.autobuddy.R.color.background));
        }
    }

    public void SendFeedbackMail() {

        // save logcat in file
        File outputFile = new File(Environment.getExternalStorageDirectory(),
                "logcat.txt");
        try {
            Runtime.getRuntime().exec(
                    "logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // Set type to "email"
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"AndyCr15@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, outputFile.getAbsolutePath());
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BBF Feedback");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(backgroundsWanted){
            int resID = getResources().getIdentifier("background_portrait", "drawable",  this.getPackageName());
            Drawable drawablePic = getResources().getDrawable(resID);
            Settings.main.setBackground(drawablePic);
        } else {
            Settings.main.setBackgroundColor(Color.parseColor("#ffffff"));
        }

    }
}
