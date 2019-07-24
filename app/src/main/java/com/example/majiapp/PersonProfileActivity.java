  package com.example.majiapp;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

  public class PersonProfileActivity extends AppCompatActivity
{
    private TextView userName, fullName, phoneNumber, Residence, userStatus;
    private CircleImageView userProfileImage;
    private Button SendFriendReqbutton, DeclineFriendReqbutton;
    private DatabaseReference FriendRequestRef,UserRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE,saveCurrentDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();

        //RECEIVE the visitUserID
        receiverUserId = getIntent().getExtras().get("visitUserId").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendsRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        InitializeFields();

        //retrieve data from the database and display them
        UserRef.child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //get data from the firebase and store into the strings
                    String mystatus  = dataSnapshot.child("status").getValue().toString();
                    String myProfileImage  = dataSnapshot.child("profileimage").getValue().toString();
                    String myusername = dataSnapshot.child("Username").getValue().toString();
                    String myfullname = dataSnapshot.child("fullname").getValue().toString();
                    String myPhonenumber = dataSnapshot.child("Phone Number").getValue().toString();
                    String myResidence= dataSnapshot.child("Residence").getValue().toString();

                    //load image stored in the string to the profile image and set data to the one i the database
                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile_pic).into(userProfileImage);

                    //display data
                    userStatus.setText(mystatus);
                    userName.setText("@"+ myusername);
                    fullName.setText(myfullname);
                    phoneNumber.setText("Phone Number: "+ myPhonenumber);
                    Residence.setText("Residence: "+ myResidence);

                    MaintananceofButtons();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
        DeclineFriendReqbutton.setEnabled(false);


        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendReqbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SendFriendReqbutton.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();

                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {

                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();

                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        AlertDialog.Builder ab = new AlertDialog.Builder(PersonProfileActivity.this);

                        ab.setTitle("confirm");
                        ab.setIcon(R.drawable.ic_launch_black_24dp);
                        ab.setMessage("Are you sure you want to unfriend this person");

                        ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                Toast.makeText(PersonProfileActivity.this, "ok", Toast.LENGTH_SHORT).show();
                                UnfriendExistingFriend();


                            }
                        });

                        ab.setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                Toast.makeText(PersonProfileActivity.this, "cancle", Toast.LENGTH_SHORT).show();
                            }
                        });
                        ab.show();

                    }
                }
            });

        }
        else
        {
            DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
            SendFriendReqbutton.setVisibility(View.INVISIBLE);
        }

    }

    private void UnfriendExistingFriend()

    {
        FriendsRef .child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener <Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener <Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task <Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqbutton.setText("Send Friend Request");

                                                DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqbutton.setEnabled(false);


                                            }

                                        }
                                    });
                        }

                    }
                });





    }


    private void AcceptFriendRequest()
    {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener <Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task <Void> task)
                    {
                        if(task.isSuccessful())
                        {

                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener <Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task <Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                              // they are now friends then the friend request data should be removedin the database
                                                //START
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener <Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener <Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task <Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        SendFriendReqbutton.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        SendFriendReqbutton.setText("Unfriend this Person");

                                                                                        DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendReqbutton.setEnabled(false);


                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });// END

                                            }

                                        }
                                    });

                        }

                    }
                });

    }

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener <Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener <Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task <Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendReqbutton.setText("Send Friend Request");
                                                SendFriendReqbutton.setTextColor(Color.WHITE);

                                                DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqbutton.setEnabled(false);


                                            }

                                        }
                                    });
                        }

                    }
                });



    }

    private void MaintananceofButtons()
    {

        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendFriendReqbutton.setText("Cancel Friend Request");
                                SendFriendReqbutton.setTextColor(Color.RED);

                                DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
                                DeclineFriendReqbutton.setEnabled(false);
                            }
                            else if(request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                SendFriendReqbutton.setText("Accept Friend Request");
                                DeclineFriendReqbutton.setVisibility(View.VISIBLE);

                                DeclineFriendReqbutton.setEnabled(true);

                                DeclineFriendReqbutton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelFriendRequest();

                                    }
                                });


                            }

                        }
                        else
                        {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if(dataSnapshot.hasChild(receiverUserId))
                                            {

                                                CURRENT_STATE = "friends";
                                                SendFriendReqbutton.setText("Unfriend this Person");
                                                SendFriendReqbutton.setTextColor(Color.RED);

                                                DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqbutton.setEnabled(false);

                                            }


                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {

                                        }
                                    });


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }



    private void SendFriendRequestToaPerson()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener <Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener <Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task <Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendFriendReqbutton.setText("Cancel Friend Request");
                                                SendFriendReqbutton.setTextColor(Color.RED);

                                                DeclineFriendReqbutton.setVisibility(View.INVISIBLE);
                                                DeclineFriendReqbutton.setEnabled(false);


                                            }

                                        }
                                    });


                        }

                    }
                });



    }

    private void InitializeFields()
    {
        userName = (TextView) findViewById(R.id.person_profile_username);
        fullName = (TextView) findViewById(R.id.person_profile_full_name);
        phoneNumber = (TextView) findViewById(R.id.person_profile_phone_number);
        Residence = (TextView) findViewById(R.id.person_profile_residence);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic );
        SendFriendReqbutton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendReqbutton = (Button) findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not_friends";
    }


}
