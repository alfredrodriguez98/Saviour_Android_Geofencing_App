package com.e.alfroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private TextView locatoin,terms,privacy,faq;

    FirebaseAuth mAuth;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.signOutMenuItd)
        {
            FirebaseAuth.getInstance().signOut();
            finish();

            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Settings");


        locatoin = (TextView) findViewById(R.id.location);
        terms = (TextView) findViewById(R.id.terms);
        privacy = (TextView) findViewById(R.id.privacy);
        faq = (TextView) findViewById(R.id.faq);


        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity2();
            }
        });

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity3();
            }
        });

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity4();

            }
        });

        mAuth=FirebaseAuth.getInstance();

    }


    private void activity2() {
        Intent i1=new Intent (this,TermsActivity.class);
        startActivity(i1);
    }
    private void activity3() {
        Intent i1=new Intent (this,PrivacyActivity.class);
        startActivity(i1);
    }
    private void activity4() {
        Intent i1=new Intent (this,FaqActivity.class);
        startActivity(i1);
    }

}
