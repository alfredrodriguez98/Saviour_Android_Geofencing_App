package com.e.alfroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    private EditText  password, confirmpassword, email;
    private Button register1;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().setTitle("Signup Form");

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirmpassword = (EditText) findViewById(R.id.confirmPassword);
        register1 = (Button) findViewById(R.id.reg);
        firebaseAuth=FirebaseAuth.getInstance();

        register1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String em=email.getText().toString().trim();
                String pass=password.getText().toString().trim();
                String confirmpass=confirmpassword.getText().toString().trim();

                if(TextUtils.isEmpty(em)){
                    Toast.makeText(SignupActivity.this,"Please Enter your valid email ID",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(pass)){
                    Toast.makeText(SignupActivity.this,"Please Enter your password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(confirmpass)){
                    Toast.makeText(SignupActivity.this,"Please Enter your password again",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(pass.length()<6){
                    Toast.makeText(SignupActivity.this,"Oops! Your password is too short",Toast.LENGTH_SHORT).show();

                }


                progressBar.setVisibility(View.VISIBLE);
                if(pass.equals(confirmpass)){
                    firebaseAuth.createUserWithEmailAndPassword(em, pass)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getApplicationContext(),MajorActivity.class));
                                        Toast.makeText(SignupActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignupActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

}