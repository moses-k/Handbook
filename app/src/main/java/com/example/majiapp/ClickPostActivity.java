package com.example.majiapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Button EditTextButton, DeleteTextButton;
    private FirebaseAuth mAuth;
    private DatabaseReference clickPostRef;

    private  String Postkey, currentUserId, databaseUserId, description,image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        mAuth = FirebaseAuth.getInstance();
        //id for the user who is online
        currentUserId = mAuth.getCurrentUser().getUid();

        PostImage = (ImageView) findViewById(R.id.click_post_image);
        PostDescription = (TextView) findViewById(R.id.click_post_description);
        EditTextButton = (Button) findViewById(R.id.edit_post_buton);
        DeleteTextButton = (Button) findViewById(R.id.delete_post_button);

        Postkey = getIntent().getExtras().get("Postkey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Postkey);

        EditTextButton.setVisibility(View.INVISIBLE);
        DeleteTextButton.setVisibility(View.INVISIBLE);


        //retrieve the image and description in the clickActivity
        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //add this validation else the app wshall crash
                if(dataSnapshot.exists())
                {
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    //get the id for the user of the post
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    PostDescription.setText(description);
                    Picasso.get().load(image).into(PostImage);

                    //display the two buttons only if the loged in user is the one that the post belongs
                    if(currentUserId.equals(databaseUserId))
                    {
                        EditTextButton.setVisibility(View.VISIBLE);
                        DeleteTextButton.setVisibility(View.VISIBLE);
                    }
                    EditTextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EdiCurrentPost(description);
                        }
                    });


                }
//
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeleteTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                DeleteCurrentPost();

            }
        });

    }

    private void EdiCurrentPost(final String description) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
         builder.setTitle("Edit Post");

         final EditText inputField = new EditText(ClickPostActivity.this);
         inputField.setText(description);
         builder.setView(inputField);

         builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {

                 clickPostRef.child("description").setValue(inputField.getText().toString());
                 Toast.makeText(ClickPostActivity.this,"Post has been updated successfully...",Toast.LENGTH_SHORT).show();
             }
         });

         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {

                 dialog.cancel();
             }
         });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_light);
    }



    private void DeleteCurrentPost()
    {
        clickPostRef.removeValue();
        Toast.makeText(ClickPostActivity.this, "Post has been deleted..", Toast.LENGTH_SHORT).show();

        sendUserToDashbordActivity();


    }

    private void sendUserToDashbordActivity() {
        Intent dashboardIntent = new Intent(ClickPostActivity.this,dashboard.class);
      dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboardIntent);
      finish();
    }
}
