package com.example.getalong.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.getalong.Login.LoginActivity;
import com.example.getalong.Models.UserAccountSettings;
import com.example.getalong.Models.UserSettings;
import com.example.getalong.R;
import com.example.getalong.Utils.BottomNavigationViewHelper;
import com.example.getalong.Utils.FirebaseMethods;
import com.example.getalong.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "profileFragment";
    private static final int ACTIVITY_NUM = 4;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private TextView mPosts, mfolowers, mfollowing, mDisplayName, mUsername, mWebsite, mDatabase, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx buttomNavigationView;

    private Context mContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mfolowers = (TextView) view.findViewById(R.id.tvFollowers);
        mfollowing = (TextView) view.findViewById(R.id.textFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        buttomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());


        Log.d(TAG, "onCreateView: started");

        setupBottomNavigationView();
        setupToolBar();

        setupFirebaseAuth();

        TextView editProfile = (TextView)  view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile_fragment));
                Intent intent =  new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
                startActivity(intent);

            }
        });
        
        return view;
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase" + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieved from firebase database " + userSettings.getSettings().getUsername());

        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String.valueOf(settings.getPosts()));
        mfollowing.setText(String.valueOf(settings.getFollowing()));
        mfolowers.setText(String.valueOf(settings.getFollowers()));
        mProgressBar.setVisibility(View.GONE);


    }

    private void setupToolBar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Navigating to Account Settings.");
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    //Bottom Navigation View
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up Bottom Navigation View");
        BottomNavigationViewHelper.setupBottomNavigationView(buttomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, buttomNavigationView);
        Menu menu = buttomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }



/* ****************Firebase ******************/
    //setup the firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG,"setupFirebase : setting up firebase  auth. ");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase =  FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =  firebaseAuth.getCurrentUser();
                if (user != null){
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                }else{
                    //user is signed out
                    Log.d(TAG,"authStateChanged:signed_out");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
                //retrieve image for the user in question

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }
    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
