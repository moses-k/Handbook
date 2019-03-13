package com.example.majiapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;

public class Sign_up extends AppCompatActivity implements View.OnClickListener {
    EditText name, username, Email, phonenumber, residence, Password, confirmpassword;
    ProgressBar mprogressBar;
    Handler handler;
    Runnable runnable;
    Timer timer;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        Email = (EditText) findViewById(R.id.etEmail);
        Password = (EditText) findViewById(R.id.etPassword);
        confirmpassword = (EditText) findViewById(R.id.etconfirmPassword);
        mprogressBar = (ProgressBar) findViewById(R.id.progressBar);
        mprogressBar.setVisibility(View.GONE);
        loadingBar = new ProgressDialog(this);

        findViewById(R.id.btreg).setOnClickListener(this);
        findViewById(R.id.btLogin).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            sendUserTodashboard();
        }
    }

    private void sendUserTodashboard() {

        startActivity(new Intent(this,dashboard.class));
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btreg:
                Register(view);
                break;

            case  R.id.btLogin:
                startActivity(new Intent(this, Login.class));

        }
    }

        public void Register (View view){

            if (!isConnected(Sign_up.this)) buildDialog(Sign_up.this).show();
            else {

                String username = Email.getText().toString();
                String password = Password.getText().toString();
                String str_confirmpassword = confirmpassword.getText().toString();

                //check if username is empty
                if (username.isEmpty()) {
                    Email.setError("username is required");


                    return;
                }
                //validate username
                if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                    Email.setError("Please enter a valid username");
                    Email.requestFocus();
                    return;
                }
                //check if password is empty
                if (password.isEmpty()) {
                    Password.setError("Password is required");
                    Password.requestFocus();
                    return;
                }

                //validate  password if the match
                if (!str_confirmpassword.matches(password)) {
                    confirmpassword.setError("Password does not match");
                    confirmpassword.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    Password.setError("Minimun length of password should be 6");
                    Password.requestFocus();
                    return;

                }

               loadingBar.setTitle("Creating New Account");
                loadingBar.setMessage("Please wait while we register you");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                mAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(this, new OnCompleteListener <AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task <AuthResult> task) {
                                if (task.isSuccessful()) {
                                    GoToLogin();
                                    Toast.makeText(getApplicationContext(), "User registered successfull", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                } else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(getApplicationContext(), "Error occured: "+message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });

                 }
        }

    private void GoToLogin() {
        startActivity(new Intent(this, Login.class));
    }

    public boolean isConnected (Context context){

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netinfo = cm.getActiveNetworkInfo();

            if (netinfo != null && netinfo.isConnectedOrConnecting()) {
                android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                    return true;
                else return false;
            } else
                return false;
        }

        public AlertDialog.Builder buildDialog (Context c){

            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle("No Internet Connection");
            builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ;

                    // finish();
                }
            });

            return builder;


        }
    }







