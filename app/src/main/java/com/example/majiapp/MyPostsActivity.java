package com.example.majiapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private RecyclerView MyPostList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostRef,UserRef,LikesRef;
    String currentUserID;
    private Boolean LikeCheker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        UserRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");



        currentUserID = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        MyPostList = (RecyclerView) findViewById(R.id.my_all_post_list);
        MyPostList.setLayoutManager(new LinearLayoutManager(this));
        MyPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        MyPostList.setLayoutManager(linearLayoutManager);






        DisplayMyAllPosts();

    }

    private void DisplayMyAllPosts()
    {
        //queryy to serach all the post of the current user
        Query myPostQuery = PostRef.orderByChild("uid").startAt(currentUserID)
                .endAt(currentUserID + "\uf8ff");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts >()
                        .setQuery(myPostQuery,Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, MyPostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter <Posts, MyPostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull MyPostViewHolder holder, int position, @NonNull Posts model)
                    {
                        //get the key of that post and store in postkey
                        final String Postkey = getRef(position).getKey();

                        holder.setFullname(model.getUserfullname());
                        holder.setTime(model.getTime());
                        holder.setDate(model.getDate());
                        holder.setDescription(model.getDescription());
                        holder.setProfileimage(getApplicationContext(),model.getProfileimage());
                        holder.setPostimage(getApplicationContext(), model.getPostimage());

                        holder.setLikeButtonStatus(Postkey);

                        //when post is clicked take the user to the click activity together with the postkey
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent clickpostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                                clickpostIntent.putExtra("Postkey", Postkey);
                                startActivity(clickpostIntent);
                            }
                        });

                        holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent commentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("Postkey", Postkey);
                                startActivity(commentsIntent);

                            }
                        });
                        //START OF LIKE POST
                        //listener for the onlike button
                        holder.LikePostButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                LikeCheker = true;
                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        if(LikeCheker.equals(true))
                                        {
                                            if(dataSnapshot.child(Postkey).hasChild(currentUserID))
                                            {
                                                LikesRef.child(Postkey).child(currentUserID).removeValue();
                                                LikeCheker = false;
                                            }else
                                            {
                                                LikesRef.child(Postkey).child(currentUserID).setValue(true);
                                                LikeCheker = false;
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });

                            }
                        });
                        //END OF LIKE POST





                    }

                    @NonNull
                    @Override
                    public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.all_post_layout, parent, false);
                        MyPostViewHolder viewHolder  =  new MyPostViewHolder(view);
                        return viewHolder;
                    }
                };



        // postList.setAdapter(firebaseRecyclerAdapter);
        MyPostList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }



    public static class MyPostViewHolder extends RecyclerView.ViewHolder
    {

        private ImageButton LikePostButton, CommentPostButton;
        private TextView DisplayNoOfLikes;
        private DatabaseReference likeRef;
        int countLikes;
        String currentuserId;


        View mView;

        public MyPostViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            LikePostButton= (ImageButton) mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) mView.findViewById(R.id.display_numberof_likes);


        }


        public void setLikeButtonStatus(final String PostKey)
        {
            likeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child(PostKey).hasChild(currentuserId))
                    {
                        //COUNTS THE number of likes
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)) + (" Likes"));
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)) + (" Likes"));

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


        //access our all_post_layout in the view
        public void setFullname(String userfullname)
        {
            TextView  username = (TextView) mView.findViewById(R.id.post_username);
            username.setText(userfullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

            Picasso.get().load(profileimage).into(image);

        }
        public void setTime(String time)
        {
            TextView postTime = (TextView) mView.findViewById(R.id.post_time);
            postTime.setText("  "+time);
        }

        public void setDate(String date)
        {
            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
            postDate.setText("  "+date);
        }
        public void setDescription(String description)
        {
            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }


        public void setPostimage(Context ctx, String postimage)
        {
            ImageView Postimage = (ImageView) mView.findViewById(R.id.click_post_image);

            Picasso.get().load(postimage).into(Postimage);
        }


    }
}
