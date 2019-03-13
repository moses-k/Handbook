package com.example.majiapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {
    ProgressDialog loadingBar;
    private EditText Username, Fullname, Phonenumber, Residence;
    Button SafesetupInfo;
    private CircleImageView ProfileImage;

    String currentUserID, downloadImageUrl;
    final static int Gallery_pick = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference userProfileImageRef;
    private FirebaseDatabase mFirebaseDatabase;
    //private StorageReference mStorageReference;
    private double mPhotoUploadProgress = 0;
    private Uri resultUri;
    private String saveCurrentDate, saveCurrentTime, postRandomName, current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //create profile images folder in the storage
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        //mStorageReference = FirebaseStorage.getInstance().getReference();
        // UsersRef = mFirebaseDatabase.getReference();
        loadingBar = new ProgressDialog(this);

        Username = (EditText) findViewById(R.id.setup_userName);
        Fullname = (EditText) findViewById(R.id.setup_fullName);
        Phonenumber = (EditText) findViewById(R.id.setup_phoneNumber);
        Residence = (EditText) findViewById(R.id.setup_residence);
        SafesetupInfo = findViewById(R.id.setup_saveinfo);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SafesetupInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SaveaccountInfo();
            }
        });

        //Imut user to gallary
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);
            }
        });

        //iporun pichait  en profile
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(ProfileImage);
                    } else {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    // allow to save picked photo from the gallary
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_pick && resultCode == RESULT_OK && data != null) {

            Uri ImageUri = data.getData();
            //crop image
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);


            // ProfileImage.setImageURI(ImageUri);
            //Picasso.with(this).load(ImageUri).into(ProfileImage)
        }

        //get the uri of the croped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            // if cropped successfully
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle(" Profile Image");
                loadingBar.setMessage("Please wait while we are updating your profile image");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                resultUri = result.getUri();

                //capture the date and  time of the post to have a unique name for each post
                java.util.Calendar calFordDate = java.util.Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
                saveCurrentDate = currentDate.format(calFordDate.getTime());

                java.util.Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calFordTime.getTime());

                postRandomName= saveCurrentDate+saveCurrentTime;

                //SAVE THE CROPED IMAGE IN THE FIREBASE STORAGE
                final StorageReference filepath = userProfileImageRef.child(resultUri.getLastPathSegment() + postRandomName + ".jpg");

                //START

                // Here is where you start to forcus USE Upload task to help you get the URi
                final UploadTask uploadTask = filepath.putFile(resultUri);

                uploadTask.addOnFailureListener(new OnFailureListener()
                {

                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message = e.toString();
                        Toast.makeText(SetupActivity.this, "Error occured:   " + message, Toast.LENGTH_SHORT).show();
                    }

                }).addOnSuccessListener(new OnSuccessListener <UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {

                        Task <Uri> UriTask = uploadTask.continueWithTask(new Continuation <UploadTask.TaskSnapshot, Task <Uri>>()
                        {
                            @Override
                            public Task <Uri> then(@NonNull Task <UploadTask.TaskSnapshot> task) throws Exception
                            {
                                if (!task.isSuccessful())
                                {
                                    throw task.getException();
                                }

                                //get the url...INITIALISE downloadImageUrl at the most to ie....String downloadImageUrl
                                downloadImageUrl = filepath.getDownloadUrl().toString();
                                return filepath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener <Uri>()
                        {
                            @Override
                            public void onComplete(@NonNull Task <Uri> task)
                            {
                                if (task.isSuccessful())
                                {

                                    //get the link
                                    downloadImageUrl = task.getResult().toString();
                                    addLinkToFirebaseDatabase();
                                    Toast.makeText(SetupActivity.this, "Gooooood!! ", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                });


                //END

            }


        }
    }



    private void addLinkToFirebaseDatabase()
    {

        UsersRef.child("profileimage").setValue(downloadImageUrl).addOnCompleteListener(new OnCompleteListener <Void>()
        {
            @Override
            public void onComplete(@NonNull Task <Void> task)
            {
                if (task.isSuccessful())
                {
                    Intent selfintent = new Intent(SetupActivity.this, SetupActivity.class);
                    startActivity(selfintent);
                    loadingBar.dismiss();
                    Toast.makeText(SetupActivity.this, "profile image uploaded successfully uploaded....woooow...   ", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error occured  " + message, Toast.LENGTH_SHORT).show();


                }
            }


        });


    }


    private void SaveaccountInfo ()
    {
        {
            String username = Username.getText().toString();
            String Fullname = this.Fullname.getText().toString();
            String phonenumber = Phonenumber.getText().toString();
            String residence = Residence.getText().toString();

            if (TextUtils.isEmpty(username))
            {
                Username.setError("Username required!");
            }

            if (TextUtils.isEmpty(Fullname))
            {
                this.Fullname.setError("Fullname required!");
            }

            if (TextUtils.isEmpty(phonenumber))
            {
                Phonenumber.setError("Phone number is reqiured!");
            }

            if (TextUtils.isEmpty(residence))
            {
                Residence.setError("Residentce cannot be empty!");
            } else {
                HashMap userMap = new HashMap();
                userMap.put("Username", username);
                userMap.put("fullname", Fullname);
                userMap.put("Id Number", "Null");
                userMap.put("Phone Number", phonenumber);
                userMap.put("Residence", residence);
                userMap.put("status", "Hey there I am using this app developed by Moses Kipyegon in 2007");

                loadingBar.setTitle("Saving Information");
                loadingBar.setMessage("Please wait while we are creating new account...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            sendToDashboard();
                            Toast.makeText(SetupActivity.this, "Your account is created successfully..", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            String messages = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error occured :  " + messages, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                });

            }

        }
    }

    private void sendToDashboard () {
        startActivity(new Intent(this, dashboard.class));

    }
}


