package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SendFeedback extends AppCompatActivity {


    EditText feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_send_feedback);

    }

    public void sendFeedback(View view){

        feedback = (EditText) findViewById(com.androidandyuk.autobuddy.R.id.feedback);
        String feedbackText = feedback.getText().toString();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"andycr15@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "BBF Feedback");
        i.putExtra(Intent.EXTRA_TEXT   , feedbackText);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(SendFeedback.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
