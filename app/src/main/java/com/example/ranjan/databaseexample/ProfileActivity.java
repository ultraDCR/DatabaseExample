package com.example.ranjan.databaseexample;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReqBtn,mProfileDeclineBtn;

    private DatabaseReference mUsersDatabase, mFriendRequestDatabase,mFriendDatabase,mNotificationDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private String mCurrent_status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("userid");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(user_id);
        mUsersDatabase.keepSynced(true);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendRequestDatabase.keepSynced(true);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        mNotificationDatabase.keepSynced(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus =(TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mProfileDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mCurrent_status ="Not Friends";

        mProgressDialog =new ProgressDialog(ProfileActivity.this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();





        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);


                Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.avatar2).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar2).into(mProfileImage);                    }
                });


                //------------------ FRIENDS LIST / REQUEST FEATURE ------------

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                             if(req_type.equals("received")){
                                 mCurrent_status="req_received";
                                 mProfileSendReqBtn.setText("ACCEPT FRIEND REQUEST");

                                 mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                 mProfileDeclineBtn.setEnabled(true);

                             }else if(req_type.equals("sent")){
                                 mCurrent_status ="req_sent";
                                 mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");

                                 mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                 mProfileDeclineBtn.setEnabled(false);

                             }
                            mProgressDialog.dismiss();

                        }else{

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){

                                        mCurrent_status = "friends";
                                        mProfileSendReqBtn.setText("UNFRIEND THIS PERSION");

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }mProgressDialog.dismiss();


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendReqBtn.setEnabled(false);

                // ----------------------------- Not Friend State --------------------------------

                if(mCurrent_status.equals("Not Friends")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String,String> notificationData = new HashMap<>();
                                                notificationData.put("from",mCurrentUser.getUid());
                                                notificationData.put("type","request");

                                                mNotificationDatabase.child(user_id).push().setValue(notificationData)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mCurrent_status = "req_sent";
                                                                mProfileSendReqBtn.setText("CANCEL FRIEND REQUEST");
                                                                mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

                                                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                                                mProfileDeclineBtn.setEnabled(false);
                                                            }
                                                        });



                                                //Toast.makeText(ProfileActivity.this, "Request Send Sucessfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }else{
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });
                }

                // -------------------------------- Cancel REQUEST STATE ------------------------
                if(mCurrent_status.equals("req_sent")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrent_status = "Not Friends";
                                            mProfileSendReqBtn.setText("SEND FRIEND REQUEST");
                                            mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                            mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                            mProfileDeclineBtn.setEnabled(false);

                                        }
                                    });

                                }
                    });

                }


                //------------------REQ RECEIVED STATUS STATE -------------

                if(mCurrent_status.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    mProfileSendReqBtn.setEnabled(true);
                                                                    mCurrent_status = "friends";
                                                                    mProfileSendReqBtn.setText("UNFRIEND THIS PERSION");
                                                                    mProfileSendReqBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                                                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                                                    mProfileDeclineBtn.setEnabled(false);
                                                                }
                                                            });

                                                        }
                                                    });

                                                }
                                            });
                                }
                            });

                }


            }
        });
    }
}
