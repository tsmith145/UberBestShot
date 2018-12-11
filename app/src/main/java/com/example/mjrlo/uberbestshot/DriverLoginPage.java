package com.example.mjrlo.uberbestshot;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginPage extends AppCompatActivity {
   private FirebaseAuth mAuth;
    private Button loginButton;
    private EditText userNameEditText;
    private EditText PasswordEditText;
    private String email;
    private String password;
    private Button registerButton;
    private FirebaseAuth.AuthStateListener firebaseAuthStatelistener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_page);




        mAuth = FirebaseAuth.getInstance();

        loginButton=(Button) findViewById(R.id.LoginButton);
        registerButton =(Button) findViewById(R.id.registerButton);
        userNameEditText =(EditText) findViewById(R.id.UserNameEditText);
        PasswordEditText =(EditText) findViewById(R.id.PasswordEditText);

        firebaseAuthStatelistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(DriverLoginPage.this, DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }
        };


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String password = PasswordEditText.getText().toString();
                final String email = userNameEditText.getText().toString();
                createAccount(email,password);

            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                final String password = PasswordEditText.getText().toString();
                final String email = userNameEditText.getText().toString();
                signInAccount(email,password);

            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //updateUI(currentUser);
        mAuth.addAuthStateListener(firebaseAuthStatelistener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.addAuthStateListener(firebaseAuthStatelistener);

    }

    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(DriverLoginPage.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
                            current_user_db.setValue(true);
                        }

                        // ...
                    }
                });
    }

    public void signInAccount(String email,String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(DriverLoginPage.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                           // updateUI(null);

                        }

                        // ...
                    }
                });

    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {
            findViewById(R.id.PasswordEditText).setVisibility(View.GONE);
            findViewById(R.id.UserNameEditText).setVisibility(View.GONE);
        }else{
            findViewById(R.id.PasswordEditText).setVisibility(View.VISIBLE);
            findViewById(R.id.UserNameEditText).setVisibility(View.VISIBLE);
        }
    }


    }

