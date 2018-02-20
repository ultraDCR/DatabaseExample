package com.example.ranjan.databaseexample;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUserList;

    private DatabaseReference mUserDatabase;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("user");
        mUserDatabase.keepSynced(true);
        query = FirebaseDatabase.getInstance().getReference().child("user");

        mUserList = (RecyclerView) findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));




    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDatabase, Users.class)
                        .build();
        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                   holder.setName(model.getName());
                   holder.setStatus(model.getStatus());
                   holder.setImage(model.getThumb_image(),getApplicationContext());

                   final String user_id = getRef(position).getKey();
                   holder.mView.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {

                           Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                           profileIntent.putExtra("userid",user_id);
                           startActivity(profileIntent);

                       }
                   });

            }

            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new UsersViewHolder(view);
            }
        };

        mUserList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setName(String name){
            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        public void setStatus(String status){
            TextView userNameView = mView.findViewById(R.id.user_single_status);
            userNameView.setText(status);
        }
        public void setImage(final String thumb_image , final Context ctx){
            final CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.avatar1).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.avatar1).into(userImageView);
                }
            });
        }
    }

}
