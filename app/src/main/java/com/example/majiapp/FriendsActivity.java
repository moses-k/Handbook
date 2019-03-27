package com.example.majiapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFrindList;
    private DatabaseReference FriendsRef, UserRef;
    private FirebaseAuth mAuth;


    String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
         myFrindList = (RecyclerView) findViewById(R.id.friend_list);

        myFrindList.setLayoutManager(new LinearLayoutManager(this));
        myFrindList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFrindList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    //show online status
    public void  updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        //save this info to the database
        Map currentstateMap = new HashMap();
        currentstateMap.put("time", saveCurrentTime);
        currentstateMap.put("date", saveCurrentDate);
        currentstateMap.put("type", state);

        UserRef.child(online_user_id).child("userState").updateChildren(currentstateMap);

    }

    // if started
    @Override
    protected void onStart()
    {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");

    }

    private void DisplayAllFriends()
    {
        updateUserStatus("online");


        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef, Friends.class)
                .build();


        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter <Friends, FriendsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model)
            {

                final String usersID =getRef(position).getKey();
                UserRef.child(usersID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {

                        holder.setDate(model.getDate());
                        if (dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                            //show online icon in friends list
                            final String type;
                            if(dataSnapshot.hasChild("userState"))
                            {
                                type = dataSnapshot.child("userState").child("type").getValue().toString();

                                if(type.equals("online"))
                                {
                                    holder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    holder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }


                            holder.setFullname(userName);
                            holder.setProfileimage(profileImage);

                            holder.mView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    userName + "'s Profile",
                                                    "Send Message"
                                            };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if(which ==0)
                                            {
                                                Intent profileIntent = new Intent(FriendsActivity.this,PersonProfileActivity  .class);
                                                profileIntent.putExtra("visitUserId", usersID);
                                                startActivity(profileIntent);
                                            }
                                            if(which == 1)
                                            {

                                                Intent chatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                chatIntent.putExtra("visitUserId", usersID);
                                                chatIntent.putExtra("userName", userName);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });

                                    builder.show();

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

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.all_users_display_layout, parent, false);
                FriendsViewHolder viewHolder  =  new FriendsViewHolder(view);
                return viewHolder;
            }
        };


        myFrindList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;
        ImageView onlineStatusView;
        public FriendsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }


        private void setProfileimage(String profileimage)
        {
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile_image).into(myImage);
        }

        public void setFullname(String Fullname) {
            TextView myName =  mView.findViewById(R.id.all_users_fullname);
            myName.setText(Fullname);
        }

        public void setDate(String date) {
            TextView friendsdate =  mView.findViewById(R.id.all_users_status);
            friendsdate.setText("Friends since: " +date);
        }


    }



}
