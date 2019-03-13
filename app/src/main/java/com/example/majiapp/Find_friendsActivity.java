package com.example.majiapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Find_friendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText  SearchInpuText;
    private ImageView SearchButton;
    private RecyclerView SearchResultList;
    private DatabaseReference allUserDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find friends ");

        SearchInpuText = (EditText) findViewById(R.id.search_here_input);
        SearchButton = (ImageView) findViewById(R.id.search_icon);

        SearchResultList = (RecyclerView) findViewById(R.id.search_friends_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));


        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String searchBoxInput = SearchInpuText.getText().toString();
                SearchPeopleAndFriends(searchBoxInput);
            }
        });



    }

    private void SearchPeopleAndFriends(String searchBoxInput)
    {

        Toast.makeText(this,"Searching....",Toast.LENGTH_SHORT).show();

        Query searchpeopleandFriendsQuery = allUserDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff" );

        FirebaseRecyclerOptions<FindFriends> list =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(searchpeopleandFriendsQuery,FindFriends.class)
                .build();
        //retrieve all the user using the recycler view adapter....you need two parameters  <module class, static clas>
        FirebaseRecyclerAdapter<FindFriends,FindFiendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter <FindFriends, FindFiendsViewHolder>(list){
            @Override
            protected void onBindViewHolder(@NonNull FindFiendsViewHolder holder, int position, @NonNull FindFriends model)
            {
                holder.setFullname(model.getFullname());
               holder.setStatus(model.getStatus());
               holder.setProfileimage(model.getProfileimage());

            }

            @NonNull
            @Override
            public FindFiendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.all_users_display_layout, parent, false);
                FindFiendsViewHolder viewHolder = new FindFiendsViewHolder(view);
                return viewHolder;

            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class FindFiendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        //constructor
        public FindFiendsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
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

        public void setStatus(String status) {
            TextView mystatus =  mView.findViewById(R.id.all_users_status);
            mystatus.setText(status);
        }
    }
}
