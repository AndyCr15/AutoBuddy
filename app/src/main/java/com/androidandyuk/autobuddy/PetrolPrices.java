package com.androidandyuk.autobuddy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PetrolPrices extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.androidandyuk.autobuddy.R.layout.activity_petrol_prices);

        WebView myWebView = (WebView) findViewById(com.androidandyuk.autobuddy.R.id.petrolWebView);


        myWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals("https://www.petrolprices.com")) {
                    // Designate Urls that you want to load in WebView still.
                    return false;
                }
                // Otherwise, give the default behavior (open in browser)
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        myWebView.loadUrl("https://www.petrolprices.com");

    }
}
