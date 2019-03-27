package com.example.majiapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity
{
    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentsInputText;
    private DatabaseReference UserRef,PostsRef;

    private String Post_Key,current_user_id,saveCurrentDate,saveCurrentTime;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        Post_Key = getIntent().getExtras().get("Postkey").toString();
        UserRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("comments");



        //the comment lists
        CommentsList = (RecyclerView) findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
         linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentsInputText = (EditText) findViewById(R.id.comments_input);
        PostCommentButton = (ImageButton) findViewById(R.id.comments_send_button);


        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            //retrieve username  and profileimage from the database
                            String userName = dataSnapshot.child("Username").getValue().toString();
                            String profile = dataSnapshot.child("profileimage").getValue().toString();

                            ValidateComment(userName, profile);

                            CommentsInputText.setText("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

Displaycomments();
    }

    private void Displaycomments()
    {

        FirebaseRecyclerOptions <Comments> options =
                new FirebaseRecyclerOptions.Builder <Comments>()
                        .setQuery(PostsRef,Comments.class)
                        .build();

        FirebaseRecyclerAdapter <Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter <Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model)

            {
                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setProfileimage(model.getProfileimage());

            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };


        CommentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView)
        {
            super(itemView);

            mView = (itemView);
        }


            public void setUsername(String username)
            {
                TextView myUSerName = (TextView) mView.findViewById(R.id.comment_username);
                myUSerName.setText("@" +username +" ");


            }

            public void setComment(String comment)
            {

                TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
                myComment.setText(comment);


            }

            public void setDate(String date)
            {

                TextView mydate = (TextView) mView.findViewById(R.id.comment_date);
                mydate.setText(" Date: "+date);

            }

            public void setTime(String time)
            {

                TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
                myTime.setText("Time "+time);
            }

          public void setProfileimage(String profileimage)
          {

            CircleImageView myImage = mView.findViewById(R.id.comment_profileimage);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile_image).into(myImage);
         }

    }






    private void ValidateComment(String userName, String profile)
    {
        String commentText = CommentsInputText.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this,"Please write text to comment",Toast.LENGTH_SHORT).show();
        }else
        {

            //capture the date and time of the post to have a unique name for each post
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            final  String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid", current_user_id);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", userName);
            commentsMap.put("profileimage", profile);



            PostsRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(CommentsActivity.this,"You have commented successfully",Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        Toast.makeText(CommentsActivity.this,"Error ocured...please try again",Toast.LENGTH_SHORT).show();


                    }

                }
            });







        }

    }
}
