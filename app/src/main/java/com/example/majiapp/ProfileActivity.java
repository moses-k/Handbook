package com.example.majiapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, fullName, phoneNumber, Residence, userStatus;
    private CircleImageView userProfileImage;
    private DatabaseReference profileUserRef, FriendsRef, PostRef;
    private FirebaseAuth mAuth;
     private String currentuserId;
     private Button MyPosts, MyFriends;
     private int countFriends = 0, countposts = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
         mAuth = FirebaseAuth.getInstance();
         currentuserId = mAuth.getCurrentUser().getUid();
         profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserId);
         FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        userName = (TextView) findViewById(R.id.my_profile_username);
        fullName = (TextView) findViewById(R.id.my_profile_full_name);
        phoneNumber = (TextView) findViewById(R.id.my_profile_phone_number);
        Residence = (TextView) findViewById(R.id.my_profile_residence);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        MyFriends = (Button) findViewById(R.id.my_friends_button);
        MyPosts = (Button) findViewById(R.id.my_post_button);


        MyFriends.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendUserToProfileActivity();

            }
        });


        MyPosts.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendUserToMyPostActivity();


            }
        });


        //apply a querry to the post to search the posts that belong to the current user
        PostRef.orderByChild("uid").startAt(currentuserId)
                .endAt(currentuserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                          countposts = (int) dataSnapshot.getChildrenCount();
                          //display on post button
                            MyPosts.setText(Integer.toString(countposts) + "  Posts");

                        }
                        else
                        {
                            MyPosts.setText("0 Posts");


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });


        FriendsRef.child(currentuserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //get the number of friends
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    //display on the button
                    MyFriends.setText(Integer.toString(countFriends) + " Friends");



                }
                else
                {
                    MyFriends.setText("0 Friends");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        //retrieve data
        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String myProfileImage  = dataSnapshot.child("profileimage").getValue().toString();
                        //load image stored in the string to the profile image and set data to the one i the database
                        Picasso.get().load(myProfileImage).placeholder(R.drawable.profile_pic).into(userProfileImage);

                    }
                    //get data from the firebase and store into the strings
                    String mystatus = dataSnapshot.child("status").getValue().toString();
                    String myusername = dataSnapshot.child("Username").getValue().toString();
                    String myfullname = dataSnapshot.child("fullname").getValue().toString();
                    String myPhonenumber = dataSnapshot.child("Phone Number").getValue().toString();
                    String myResidence = dataSnapshot.child("Residence").getValue().toString();


                    //display data
                    userStatus.setText(mystatus);
                    userName.setText("@" + myusername);
                    fullName.setText(myfullname);
                    phoneNumber.setText("Phone Number: " + myPhonenumber);
                    Residence.setText("Residence: " + myResidence);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToProfileActivity()
    {
        Intent friendsIntent = new Intent(ProfileActivity.this,FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToMyPostActivity()
    {
        Intent mypostIntent = new Intent(ProfileActivity.this,MyPostsActivity.class);
        startActivity(mypostIntent);

    }
}
