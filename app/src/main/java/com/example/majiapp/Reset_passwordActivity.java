package com.example.majiapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Reset_passwordActivity extends AppCompatActivity {

    private TextView Reset_description;
    private EditText Reset_input_email;
    private Button Reset_button;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();


        Reset_description = (TextView) findViewById(R.id.resetpas_description);
        Reset_input_email = (EditText) findViewById(R.id.resetpass_enteremail);
        Reset_button = (Button) findViewById(R.id.reset_button);

        mToolbar = (Toolbar) findViewById(R.id.reset_password_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password ");


        Reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = Reset_input_email.getText().toString();

                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(Reset_passwordActivity.this,"Input the email address..",Toast.LENGTH_SHORT).show();
                }else
                {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener <Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(Reset_passwordActivity.this,"Please check your Email Account if you want to reset your password",Toast.LENGTH_SHORT).show();startActivity(new Intent(Reset_passwordActivity.this, Login.class));

                            }else
                            {
                                String mesasge = task.getException().getMessage();
                                Toast.makeText(Reset_passwordActivity.this,"Error occured:  "+ mesasge,Toast.LENGTH_SHORT).show();

                            }


                        }
                    });


                }

            }
        });



    }
}
