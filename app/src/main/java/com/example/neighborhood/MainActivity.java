package com.example.neighborhood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
}
