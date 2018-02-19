package com.example.ranjan.databaseexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    private EditText femail;
    private EditText fpassword;
    private EditText fname;
    private Button register;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        femail = (EditText) findViewById(R.id.email_field);
        fpassword = (EditText) findViewById(R.id.password_field);
        fname = (EditText) findViewById(R.id.name_field);
         register = (Button) findViewById(R.id.register_button);

        mRegProgress =new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = femail.getText().toString();
                String password = fpassword.getText().toString();
                String displayname = fname.getText().toString();
                if (!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)) {

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_userId(displayname,email, password);
                }
            }
        });

    }

    private void register_userId(final String displayname, String firebaseemail, String firebasepassword) {
        mAuth.createUserWithEmailAndPassword(firebaseemail, firebasepassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid =current_user.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name",displayname);
                            userMap.put("status","Hi there,I'm using this app.");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mRegProgress.dismiss();

                                        Intent startintent = new Intent(Register.this, Home.class);
                                        startintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                                        startActivity(startintent);
                                        finish();
                                    }
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            mRegProgress.hide();
                            Toast.makeText(Register.this, "Cannot register check the information and try again",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

}
