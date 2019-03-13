package com.example.majiapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private EditText userName, fullName, phoneNumber, Residence, userStatus;
    private Button UpdateAccountSettingsButon;
    private CircleImageView UserProfImage;
    private DatabaseReference SettingsuserRef;
    private FirebaseAuth mAth;
    final static int Gallery_pick = 1;
    ProgressDialog loadingBar;
    private StorageReference userProfileImageRef;
    private Uri resultUri;






    String currentuserId,downloadImageUrl,saveCurrentDate, saveCurrentTime, postRandomName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAth = FirebaseAuth.getInstance();

        currentuserId = mAth.getUid();
        SettingsuserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");



        mtoolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Account Settings");
        //display the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        userName = (EditText) findViewById(R.id.settings_username);
        fullName = (EditText) findViewById(R.id.settings_fullname);
        phoneNumber = (EditText) findViewById(R.id.settings_phonenumber);
        Residence = (EditText) findViewById(R.id.settings_residence);
        userStatus = (EditText) findViewById(R.id.settings_status);
        UpdateAccountSettingsButon = (Button) findViewById(R.id.update_account_settings_button);
        UserProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        loadingBar = new ProgressDialog(this);



        //retrieve data
        SettingsuserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //get data from the firebase and store into the strings
                    String mystatus  = dataSnapshot.child("Status").getValue().toString();
                    String myProfileImage  = dataSnapshot.child("profileimage").getValue().toString();
                    String myusername = dataSnapshot.child("Username").getValue().toString();
                    String myfullname = dataSnapshot.child("Fullname").getValue().toString();
                    String myPhonenumber = dataSnapshot.child("Phone Number").getValue().toString();
                    String myResidence= dataSnapshot.child("Residence").getValue().toString();

                    //load image stored in the string to the profile image and set data to the one i the database
                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile_pic).into(UserProfImage);

                    //display data
                    userStatus.setText(mystatus);
                    userName.setText(myusername);
                    fullName.setText(myfullname);
                    phoneNumber.setText(myPhonenumber);
                    Residence.setText(myResidence);



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


        UpdateAccountSettingsButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ValidateAccountInfo();

            }
        });

        //update profile image
        UserProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_pick);

            }
        });

        //display image of the profile image circular view
        SettingsuserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(UserProfImage);
                    } else {
                        Toast.makeText(SettingsActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void ValidateAccountInfo() {
        String status = userStatus.getText().toString();
        String username = userName.getText().toString();
        String fullname = fullName.getText().toString();
        String phonenumber = phoneNumber.getText().toString();
        String residence = Residence.getText().toString();

        if (TextUtils.isEmpty(status))
        {
            Toast.makeText(this,"Please write your Status... ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(username))
        {
            Toast.makeText(this,"Please write your Username... ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this,"Please write your Fullname... ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phonenumber))
        {
            Toast.makeText(this,"Please write your Phone number... ",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(residence))
        {
            Toast.makeText(this,"Please write your Residence... ",Toast.LENGTH_SHORT).show();
        }else
        {
            loadingBar.setTitle(" Profile Image");
            loadingBar.setMessage("Please wait while we are updating your profile image");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccountInfo(status,username,fullname,phonenumber,residence);
        }
    }

    private void UpdateAccountInfo(String status, String username, String fullname, String phonenumber, String residence)
    {
        HashMap userMap = new HashMap();
        userMap.put("Status", status);
        userMap.put("Username",username);
        userMap.put("Fullname",fullname);
        userMap.put("Phone Number",phonenumber);
        userMap.put("Residence",residence);

        SettingsuserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this,"Account settings updated successfully..",Toast.LENGTH_SHORT).show();;
                    loadingBar.dismiss();
                    sendUserToDashbordActivity();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error occured  " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }
        });
    }

    private void sendUserToDashbordActivity() {
        Intent dashboardIntent = new Intent(SettingsActivity.this,dashboard.class);
        startActivity(dashboardIntent);
    }

    //method to upload image to the firebase storage
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

                // Here is where you start to forcus USE Upload task to help you get the URi of the croped image
                final UploadTask uploadTask = filepath.putFile(resultUri);

                uploadTask.addOnFailureListener(new OnFailureListener()
                {

                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message = e.toString();
                        Toast.makeText(SettingsActivity.this, "Error occured:   " + message, Toast.LENGTH_SHORT).show();
                    }

                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {

                        Task <Uri> UriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task <Uri>>()
                        {
                            @Override
                            public Task <Uri> then(@NonNull Task <UploadTask.TaskSnapshot> task) throws Exception
                            {
                                if (!task.isSuccessful())
                                {
                                    throw task.getException();
                                }
                                //get the url...INITIALISE downloadImageUrl at the top ie....String downloadImageUrl;
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
                                    Toast.makeText(SettingsActivity.this, "Gooooood!! ", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }
                });




            }


        }
    } //END of the method

    //add link to the firebase database
    private void addLinkToFirebaseDatabase()
    {

        SettingsuserRef.child("profileimage").setValue(downloadImageUrl).addOnCompleteListener(new OnCompleteListener <Void>()
        {
            @Override
            public void onComplete(@NonNull Task <Void> task)
            {
                if (task.isSuccessful())
                {
                    Intent selfintent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    startActivity(selfintent);
                    loadingBar.dismiss();
                    Toast.makeText(SettingsActivity.this, "profile image uploaded successfully uploaded....woooow...   ", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error occured  " + message, Toast.LENGTH_SHORT).show();


                }
            }


        });


    }






}
