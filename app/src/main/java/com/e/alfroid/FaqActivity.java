package com.e.alfroid;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class FaqActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        getSupportActionBar().setTitle("FAQs");

    }
}