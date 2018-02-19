package com.example.ranjan.databaseexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private AutoCompleteTextView user;
    private AutoCompleteTextView pass;
    private FirebaseAuth mAuth;
    private Button uregister;
    private Button ulogin;
    private ProgressDialog mLoginProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        user = (AutoCompleteTextView) findViewById(R.id.user_id);
        pass = (AutoCompleteTextView) findViewById(R.id.password);
        uregister = (Button) findViewById(R.id.register_button);
        ulogin = (Button) findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();
        mLoginProgress = new ProgressDialog(this);
        ulogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = user.getText().toString();
                String password = pass.getText().toString();
                if (!TextUtils.isEmpty(email)||!TextUtils.isEmpty(password)){

                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials!");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    login_UserId(email,password);
                }
                else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    user.setError("Field required");
                    pass.setError("Field required");
                } else {
                    Toast.makeText(Login.this, "Some error occure. Check you information and try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

        });
        uregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(Login.this,Register.class);
                startActivity(startIntent);
            }
        });
    }

    private void login_UserId(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    mLoginProgress.dismiss();
                    Intent startIntent = new Intent(Login.this, Home.class);
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(startIntent);
                    finish();

                } else {
                    // If sign in fails, display a message to the user.
                    mLoginProgress.hide();
                    Toast.makeText(Login.this, "Cannot sign In .Incorrect Username or Password",
                            Toast.LENGTH_SHORT).show();



                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent startintent = new Intent (Login.this, Home.class);
            startActivity(startintent);
            finish();
        }
    }


}
