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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Timer;

public class Login extends AppCompatActivity implements View.OnClickListener {

    EditText email, editTextPassword;
    TextView Forget_password_link;
    ProgressBar mprogressBar;
    ProgressDialog loadingBar;
    Handler handler;
    Runnable runnable;
    private ImageView facebookLoginButton, twitterLogonButton,googleLoginButton;
    Timer timer;
    Button btlog;
    Button btreg;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference UserRef;

//    implementation 'de.hdoenhof:circleimageview:2.2.0'

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

       FirebaseApp.initializeApp(this);

        mAuth =  FirebaseAuth.getInstance();
        email = (EditText) findViewById(R.id.etEmail);
        editTextPassword = (EditText) findViewById(R.id.etPassword);
        Forget_password_link = (TextView) findViewById(R.id.forget);
        loadingBar = new ProgressDialog(this);

        facebookLoginButton = (ImageView) findViewById(R.id.facebook_signin_button);
        googleLoginButton = (ImageView) findViewById(R.id.google_signin_button);
        twitterLogonButton = (ImageView) findViewById(R.id.twitter_signin_button);

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnSignup).setOnClickListener(this);
        findViewById(R.id.forget).setOnClickListener(this);

        Forget_password_link.setOnClickListener(this);


    }


    //check user if already loged in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            sendUserToDashboard();
        }
    }

    private void sendUserToSetupActivity() {
        startActivity(new Intent(this, SetupActivity.class));
    }


    private void sendUserToDashboard() {
        startActivity(new Intent(this,dashboard.class));
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnLogin:
                //  Toast.makeText(getApplicationContext(),"User registered",Toast.LENGTH_SHORT).show();

                Login(view);
                break;

            case R.id.btnSignup:
                startActivity(new Intent(this, Sign_up.class));
                break;
            case R.id.forget:
                startActivity(new Intent(this, Reset_passwordActivity.class));
                break;



        }

    }

    private void SendUserTodashboard() {
        startActivity(new Intent(this, dashboard.class));


    }



    public void Login(View view) {
        if (!isConnected(Login.this)) buildDialog(Login.this).show();
        else {
            // Toast.makeText( Login.this, "Welcome user", Toast.LENGTH_SHORT ).show();
            // setContentView( R.layout.activity_main )


            String username = email.getText().toString();
            String password = editTextPassword.getText().toString();
            String result = "";
          // String type = "login";
           // BackgroundWorker backgroundWorker = new BackgroundWorker(this);
           // backgroundWorker.execute(type, username, password);

            //check if  is empty
            if (username.isEmpty()) {
                email.setError("username is required");
                email.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                email.setError("Please enter a valid email");
                email.requestFocus();
                return;

            }

            //check if password is empty
            if (password.isEmpty()) {
                editTextPassword.setError("Password is required");
                editTextPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                editTextPassword.setError("Minimun length of password should be 6");
                editTextPassword.requestFocus();
                return;
            }

            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait while loging in");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            //create register in firebase and add user
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                           if (task.isSuccessful())
                          {
                            SendUserTodashboard();
                            Toast.makeText(getApplicationContext(),"User Login successfull",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                           }else

                          {
                            String message = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(),"Error occured: "+message,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                          }
                         }

                    });

        }
    }



    //check internet connection
    public boolean isConnected(Context context) {

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

    //dialogbox


    public AlertDialog.Builder buildDialog(Context c) {

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


