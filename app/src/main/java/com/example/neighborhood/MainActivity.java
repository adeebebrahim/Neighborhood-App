package com.example.neighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.neighborhood.Fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button buttonLogout, buttonForum, buttonEventManagement, buttonPostFeed;
    ImageButton buttonMessaging, buttonProfile;
    TextView textView;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        buttonLogout = findViewById(R.id.btn_logout);
        buttonForum = findViewById(R.id.btn_forum);
        buttonEventManagement = findViewById(R.id.btn_event_management);
        buttonPostFeed = findViewById(R.id.btn_post_feed);
        buttonMessaging = findViewById(R.id.btn_messaging);
        buttonProfile = findViewById(R.id.btn_profile);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        buttonProfile = findViewById(R.id.btn_profile);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            textView.setText("Logged in as: " + user.getEmail());
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationListener(this));

        // Set the initial selected item to Home
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonMessaging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle messaging button click
                // Add your code here
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle profile button click
                // Add your code here
            }
        });

        buttonForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle forum button click
                // Add your code here
            }
        });

        buttonEventManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle event management button click
                // Add your code here
            }
        });

        buttonPostFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle post feed button click
                // Add your code here
            }
        });
    }

////    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        Fragment selectedFragment = null;
//
//        switch (item.getItemId()) {
//            case R.id.navigation_home:
//                selectedFragment = new HomeFragment();
//                break;
//            case R.id.navigation_community:
//                // Handle click for Community icon
//                // Add your code here for the Community screen or fragment
//                break;
//            case R.id.navigation_add:
//                // Handle click for Add icon
//                // Add your code here for the Add screen or fragment
//                break;
//            case R.id.navigation_event:
//                // Handle click for Event icon
//                // Add your code here for the Event screen or fragment
//                break;
//            case R.id.navigation_profile:
//                // Handle click for Profile icon
//                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
//                startActivity(intent);
//                finish();
//                break;
//        }
//
//        if (selectedFragment != null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, selectedFragment)
//                    .commit();
//        }
//
//        return true;
//    }
}
