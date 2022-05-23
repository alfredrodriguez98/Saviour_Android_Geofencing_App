package com.e.alfroid;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutusActivity extends AppCompatActivity {
    private TextView adress, phoneno, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        getSupportActionBar().setTitle("About Us");
        TextView text=(TextView) findViewById(R.id.web);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        adress = (TextView) findViewById(R.id.adress);
        phoneno = (TextView) findViewById(R.id.phoneno);
        email = (TextView) findViewById(R.id.eemail);
    }
}