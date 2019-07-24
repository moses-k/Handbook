package com.example.majiapp;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private TextView navusername;
    private NavigationView navigationView;
    private DrawerLayout mDrawerLayout;
    private Button Add_post_Button;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;
    boolean status = false;
    private RecyclerView postList;
    private WebView webView;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, postsRef,LikesRef;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private CircleImageView NavProfileImage;
    public String currentUserID;
    private View navView;
    private Boolean LikeCheker = false;

    //private ActionBar actionBar;
    private ActionBar actionbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mAuth = FirebaseAuth.getInstance();
        UserRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserID = mAuth.getCurrentUser().getUid();

        postList = (RecyclerView) findViewById(R.id.all_users_post_view);
        postList.setLayoutManager(new LinearLayoutManager(this));
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //display new post at the top and old at the bottom
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
       postList.setLayoutManager(linearLayoutManager);
       Add_post_Button = (Button) findViewById(R.id.post_button);

       //  View navView = navigationView.inflateHeaderView(R.layout.header);
       //  navView.findViewById(R.id.naigation_view);

        mToolbar = (Toolbar) findViewById(R.id.nav_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        navigationView = findViewById(R.id.nav_view);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

      //enable mToogle icon visibility
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //inflate the layout
        navView = navigationView.inflateHeaderView(R.layout.header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navusername = (TextView) navView.findViewById(R.id.nav_username);


        navigationView.setNavigationItemSelectedListener(this);

        //add onclick listener
        //navigationView.setNavigationItemSelectedListener(this);
        DisplAllUsersPost();

        //Load home fragment on the empty containeri dashboaed
      //  HomeFragment homeFragment= new HomeFragment();
      //  FragmentManager manager = getSupportFragmentManager();
       // manager.beginTransaction().replace(R.id.container,homeFragment,homeFragment.getTag()).commit();
        //load profile IMAGE and USERNAME on the navigation drawer

     UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                         navusername.setText(fullname);
                    }else
                    {
                        Toast.makeText(dashboard.this, "User has no username in the database",Toast.LENGTH_SHORT).show();
                    }

                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image =    dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(dashboard.this, "Profile name do not exist",Toast.LENGTH_SHORT).show();

                    }
                }
            }
//
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(dashboard.this, "Erooorrrrrrrrr",Toast.LENGTH_SHORT).show();

            }
        });


  }

    //check if user is authenticated

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            sendUserToLoginActivity();

        } else {
            updateUserStatus("online");

            checkUserExistence();
            //DisplAllUsersPost();
        }
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

    //check user existance in the realtime dbase
    private void checkUserExistence()
    {
       // final  String current_user_id = mAuth.getCurrentUser().getUid();

       UserRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(currentUserID))
                {
                    sendUserToSetupActivity();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

                Toast.makeText(getApplicationContext(),"Database........... Error ocured",Toast.LENGTH_SHORT).show();
            }
        });
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

        UserRef.child(currentUserID).child("userState").updateChildren(currentstateMap);
    }




    //display all posts
    private void DisplAllUsersPost()
    {

        Query SortPostsInDecendingOrder = postsRef.orderByChild("counter");

        //with the help of firebase recycler we will retrieve all the post
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts >()
                        .setQuery(SortPostsInDecendingOrder,Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter <Posts, PostViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Posts model)
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

                                 Intent clickpostIntent = new Intent(dashboard.this, ClickPostActivity.class);
                                 clickpostIntent.putExtra("Postkey", Postkey);
                                 startActivity(clickpostIntent);
                            }
                        });

                        holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent commentsIntent = new Intent(dashboard.this, CommentsActivity.class);
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
                    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.all_post_layout, parent, false);
                        PostViewHolder viewHolder  =  new PostViewHolder(view);
                        return viewHolder;
                    }
                };
                     // postList.setAdapter(firebaseRecyclerAdapter);
        postList.setAdapter(firebaseRecyclerAdapter);
           firebaseRecyclerAdapter.startListening();
           updateUserStatus("online");
    }

     public static class PostViewHolder extends RecyclerView.ViewHolder
     {

        private ImageButton LikePostButton, CommentPostButton;
         private TextView DisplayNoOfLikes;
         private DatabaseReference likeRef;
         int countLikes;
        String currentuserId;

        View mView;

         public PostViewHolder(View itemView) {
             super(itemView);
             mView = itemView;
             likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");

             currentuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
             LikePostButton = (ImageButton) mView.findViewById(R.id.like_button);
             CommentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);
             DisplayNoOfLikes = (TextView) mView.findViewById(R.id.display_numberof_likes);
/* username = (TextView) mView.findViewById(R.id.post_username);
               profileImage = (CircleImageView) mView.findViewById(R.id.post_profile_image);
             postTime = (TextView) mView.findViewById(R.id.post_time);
             postDate = (TextView) mView.findViewById(R.id.post_date);
             postDescription = (TextView) mView.findViewById(R.id.post_decription);
             Postimage = (ImageView) mView.findViewById(R.id.post_image);*/
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




    private void sendUserToLoginActivity() {
        startActivity(new Intent(this,Login.class));
    }


    private void sendUserToSetupActivity()
    {
        startActivity(new Intent(this,SetupActivity.class));
    }

    //allow mtoogle to display the drawer
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    //check for internet connectivity
    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
            return false;
    }
    //alert box
    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or wifi to access this. Press ok to Exit");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // finish();
            }
        });

        return builder;
    }

    //drawer item selected
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_post:
               startActivity(new Intent(this,Post.class));
                break;
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_friends:

                SendUsersToFriendsActivity();

                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();

                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();

                break;
            case R.id.nav_message:

                SendUserToMessageActivity();
                break;

            case R.id.nav_logout:
                AlertDialog.Builder ab = new AlertDialog.Builder(dashboard.this);

                ab.setTitle("confirm");
                ab.setIcon(R.drawable.ic_launch_black_24dp);
                ab.setMessage("Are you sure you want to logout?");

                ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(dashboard.this, "ok", Toast.LENGTH_SHORT).show();
                        updateUserStatus("offline");
                        mAuth.signOut();
                        Login();
                    }
                });

                ab.setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(dashboard.this, "cancle", Toast.LENGTH_SHORT).show();
                    }
                });
                ab.show();
                break;
            case R.id.share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
            case R.id.send:
                Toast.makeText(this, "Send successful", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_logout2:
                 ab = new AlertDialog.Builder(dashboard.this);

                ab.setTitle("confirm");
                ab.setIcon(R.drawable.ic_launch_black_24dp);
                ab.setMessage("Are you sure you want to logout?");

                ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(dashboard.this, "ok", Toast.LENGTH_SHORT).show();
                        updateUserStatus("offline");
                        mAuth.signOut();
                        Login();
                    }
                });

                ab.setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(dashboard.this, "cancle", Toast.LENGTH_SHORT).show();
                    }
                });
                ab.show();
                break;
                case R.id.menu_about2:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,
                            new AboutFragment()).commit();
                    break;
            case R.id.menu_settings2:
                getSupportFragmentManager().beginTransaction().replace(R.id.container,
                        new SettingsFragment()).commit();
                break;


        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
    private void SendUserToSettingsActivity()
    {
        Intent settingIntent = new Intent(dashboard.this,SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent find_friendsIntent = new Intent(dashboard.this,Find_friendsActivity.class);
        startActivity(find_friendsIntent);
    }

    private void SendUserToMessageActivity()
    {
        Intent chatIntent = new Intent(dashboard.this,FriendsActivity.class);
        startActivity(chatIntent);
    }

    private void SendUsersToFriendsActivity()
    {
        Intent friendsIntent = new Intent(dashboard.this,FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(dashboard.this,ProfileActivity.class);
        startActivity(profileIntent);

    }

    public void onOptionsItemselected (Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    public void Login () {
        startActivity(new Intent(this, Login.class));
    }

}
