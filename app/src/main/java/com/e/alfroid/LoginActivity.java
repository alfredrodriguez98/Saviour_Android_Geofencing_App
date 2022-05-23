package com.e.alfroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private Button register,login;
    private EditText password,email;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login Form");
        register=(Button)findViewById(R.id.reg);
        login=(Button)findViewById(R.id.login);
        password=(EditText) findViewById(R.id.password);
        email=(EditText)findViewById(R.id.email);

        firebaseAuth=FirebaseAuth.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String em=email.getText().toString().trim();
                String pass=password.getText().toString().trim();

                if(TextUtils.isEmpty(em)){
                    Toast.makeText(LoginActivity.this,"Please Enter your valid email ID",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(pass)){
                    Toast.makeText(LoginActivity.this,"Please Enter your password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(pass.length()<6){
                    Toast.makeText(LoginActivity.this,"Oops! Your password is too short",Toast.LENGTH_SHORT).show();

                }

                firebaseAuth.signInWithEmailAndPassword(em, pass)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    startActivity(new Intent(getApplicationContext(),MajorActivity.class));

                                } else {
                                    Toast.makeText(LoginActivity.this, "Oops! Login Failed", Toast.LENGTH_SHORT).show();
                                }
                                // ...
                            }
                        });
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity1();
            }
        });
    }

    private void activity1(){
        Intent i1=new Intent (this,SignupActivity.class);
        startActivity(i1);
    }

}
