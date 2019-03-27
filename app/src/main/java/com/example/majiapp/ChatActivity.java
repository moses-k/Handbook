package com.example.majiapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar chartToombar;
    private ImageButton SendMessageButton, SenderImageButton;
    private EditText UserMessageInput;
    private RecyclerView UserMessageList;
    private final List<Messages> messagesList = new ArrayList <>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;

    private DatabaseReference rootRef,usersRef;
    private FirebaseAuth mAuth;

    String messageReceiverID,messageReceiverName, messageSenderId, saveCurrentDate,saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        rootRef  = FirebaseDatabase.getInstance().getReference();
        usersRef  = FirebaseDatabase.getInstance().getReference().child("Users");


        //retrieve the sent info
        messageReceiverID = getIntent().getExtras().get("visitUserId").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();



        InitialiseFields();

        DispalyReceiverInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();

            }
        });

        FetchMessages();


    }

    private void FetchMessages()
    {
        rootRef.child("Messages").child(messageSenderId).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        if(dataSnapshot.exists())
                        {

                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            //whenever new msg is add it will deplay
                            messageAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    // if started
    @Override
    protected void onStart()
    {
        super.onStart();
        updateUserStatus("online");
    }

    //if user quite the app
    @Override
    protected void onStop()
    {
        super.onStop();
        updateUserStatus("offline");

    }
    //if app crashes
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        updateUserStatus("offline");

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");

    }
    private void SendMessage()
    {

        updateUserStatus("online ");
        String messageText = UserMessageInput.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please write a message...",Toast.LENGTH_SHORT).show();
        }else
        {
            String message_sender_ref = "Messages/" + messageSenderId + "/" +messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" +messageSenderId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            //capture the date and time
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calFordTime.getTime());


            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageTextBody );
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageTextBody );

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this,"Message send successfully..",Toast.LENGTH_SHORT).show();
                        //whenever A TEXT is send it should empty the text box
                        UserMessageInput.setText("");

                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this,"Error" + message,Toast.LENGTH_SHORT).show();
                        UserMessageInput.setText("");


                    }
                }
            });
        }
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

        usersRef.child(messageSenderId).child("userState").updateChildren(currentstateMap);

    }

    private void DispalyReceiverInfo()
    {

        receiverName.setText(messageReceiverName );
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //retieve the state of the user
                    final  String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(receiverProfileImage);


                    // show online status on chats
                    if(dataSnapshot.hasChild("userState"))
                    {
                        final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                        final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();

                        final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                        if (type.equals("online")) {
                            userLastSeen.setText("online");

                        } else {
                            userLastSeen.setText("last seen :" + lastDate + "  " + lastTime);
                        }
                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void InitialiseFields()
    {
        chartToombar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chartToombar);

        ActionBar actionBar  = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        //connect to the chat activity
        actionBar.setCustomView(action_bar_view);



        receiverName =  (TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);


        SendMessageButton = (ImageButton) findViewById(R.id.chat_send_message_button);
        UserMessageInput = (EditText) findViewById(R.id.chat_input_message);


        messageAdapter = new MessagesAdapter(messagesList);
        UserMessageList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        UserMessageList.setHasFixedSize(true);
        UserMessageList.setLayoutManager(linearLayoutManager);
        UserMessageList.setAdapter(messageAdapter);


    }
}
