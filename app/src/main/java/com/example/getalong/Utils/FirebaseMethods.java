package com.example.getalong.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.getalong.Models.User;
import com.example.getalong.Models.UserAccountSettings;
import com.example.getalong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;

    private Context mContext;

    public FirebaseMethods(Context context){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mContext = context;

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfusernameExixts: chechking if"+ username+" already exists.");

        User user = new User();

        for(DataSnapshot ds: dataSnapshot.getChildren()){
            Log.d(TAG, "checkFirebaseExixts: datasnapshot:"+ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkFirebaseExixts: username:"+ user.getUsername());

            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkFirebaseExixts: FOUND & MATCH:" + user.getUsername());
                return true;
            }
        }
        return false;
    }

    /*
    Register a new email and password to firebase authentication
     */

    public void  registerNewEmail(final String email , final String password , final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener( new  OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail : onComplete:"+ task.isSuccessful());

                        if(!task.isSuccessful()){
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();


                        }else if(task.isSuccessful()){

                            //send verification
                            sendVerificationEmail();
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authenticate changed:"+ userID);
                        }
                    }
                });

    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext, "couldnt send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /*
    Add a info to the user nodes
    Add info to the user_account_Setting
     */

    public void addNewUser(String email, String username , String description, String website, String profile_photo){
        User user = new User(userID,1,email,  StringManipulation.condenseUsername(username));

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                username,
                website

        );

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

}
