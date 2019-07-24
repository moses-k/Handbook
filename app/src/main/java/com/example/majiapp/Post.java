package com.example.majiapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class Post extends AppCompatActivity {

    ProgressDialog loadingBar;
    Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button PostButton;
    private EditText PostDescription;
    final static int Gallery_pick = 1;
    private Uri ImageUri;
   // String Description;
    private StorageReference PostImagesReference;
    private String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id;
    private String downloadUrl;
    private DatabaseReference userRef,PostsRef;
    private FirebaseAuth mAuth;

    private long countPosts;






    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = (Toolbar) findViewById(R.id.post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        PostImagesReference = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        SelectPostImage = (ImageButton) findViewById(R.id.post_image_button);
        PostDescription = (EditText) findViewById(R.id.post_description_text);
        PostButton = (Button) findViewById(R.id.post_button);
        loadingBar = new ProgressDialog(this);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();

            }
        });

    }

    private void ValidatePostInfo()
    {
        String Description = PostDescription.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this,"Please select post image...,",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this," Please write something about the post",Toast.LENGTH_SHORT).show();

        }else 
        {

            loadingBar.setTitle("Add new Post");
            loadingBar.setMessage("Please wait while we are updating your profile new post");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            storeImageToFirebaseStorage();
            
        }


    }

    private void storeImageToFirebaseStorage()
    {
     //capture the date and time of the post to have a unique name for each post
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName= saveCurrentDate+saveCurrentTime;

        //store image in Firebase storage

        final StorageReference filepath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        final UploadTask uploadTask = filepath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener()
        {

            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(Post.this, "Error occured:   " + message, Toast.LENGTH_SHORT).show();
            }

        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task <Uri> UriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task <Uri>>() {
                    @Override
                    public Task <Uri> then(@NonNull Task <UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        //get the url
                        downloadUrl = filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                    @Override
                    public void onComplete(@NonNull Task <Uri> task) {
                        if (task.isSuccessful()) {

                            //get the link
                            downloadUrl = task.getResult().toString();
                            savePostImageToFirebaseDatabase();
                            Toast.makeText(Post.this, "Gooooood!! ", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });

    }

    private void savePostImageToFirebaseDatabase()
    {


        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //count the number of posts and store it in the variable created
                    countPosts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts = 0;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //get current username and profile for the current user loged in
                    String userfullname  = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String Description = PostDescription.getText().toString();

                    //String postDescription = Description;
                    HashMap postMap = new HashMap();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", Description);
                    postMap.put("postimage", downloadUrl);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("userfullname", userfullname);
                    postMap.put("counter", countPosts);



                    PostsRef.child(userfullname + current_user_id + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(Post.this,"New post is updated successfully",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                //sendUserToHomeActivity();



                            }else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(Post.this,"Error occured: "+message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToHomeActivity()
    {
        Intent homeIntent = new Intent(this,dashboard.class);
        startActivity(homeIntent);

    }



    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_pick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_pick && resultCode == RESULT_OK && data  !=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }
}
