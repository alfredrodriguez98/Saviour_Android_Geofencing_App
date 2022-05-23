package com.e.alfroid;



import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class WhatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats);
        getSupportActionBar().setTitle("What's New..");

    }
}
