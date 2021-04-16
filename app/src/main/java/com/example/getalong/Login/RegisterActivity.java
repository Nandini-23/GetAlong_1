package com.example.getalong.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.getalong.R;
import com.example.getalong.Utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button  btnRegister;
    private ProgressBar  mProgressBar;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseMethods = new FirebaseMethods(mContext);
        mContext = RegisterActivity.this;
        Log.d(TAG, "onCreate started.");

        initWidgets();
        setupFirebaseAuth();
        init();
    }


    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if(checkInput(email, username, password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    firebaseMethods.registerNewEmail(email, password, username);

                }
            }
        });
    }

    private boolean checkInput(String email, String username, String password){
        Log.d(TAG, "checkInputs for null values .");
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(mContext, "All field must be filled out.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /*
    iniatialize the activuty widget
     */
    private void  initWidgets(){
        Log.d(TAG,"isStringNull: checking string if null.");
        mEmail = (EditText) findViewById(R.id.input_email);
        mUsername = (EditText)  findViewById(R.id.input_username);
        btnRegister = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.ProgressBar);
        loadingPleaseWait = (TextView) findViewById(R.id.loadingleasewait);
        mPassword = (EditText) findViewById(R.id.input_password);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStingNull : checking string is null.");

        if (string.equals("")) {
            return true;
        } else {
            return false;
        }
    }


    /* *********** firebase *******/
    //setup the firebase auth object
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebase : setting up firebase  auth. ");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            //1st check : make sure that username is not already in use
                                if(firebaseMethods.checkIfUsernameExists(username, datasnapshot)){
                                    append = myRef.push().getKey().substring(3,10);
                                    Log.d(TAG, "onDataChange: username already exixts . Appending random string to name: \" + append");
                                }
                                username = username + append;

                            //add user to the database
                            firebaseMethods.addNewUser(email, username, "", "", "");
                            Toast.makeText(mContext, "Signup successful. sending verification mail.", Toast.LENGTH_SHORT).show();

                            mAuth.signOut();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    finish();

                } else {
                    //user is signed out
                    Log.d(TAG, "authStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
