package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.neighborhood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView bioTextView;
    private TextView followersTextView;
    private TextView followingTextView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTextView = rootView.findViewById(R.id.name_text);
        usernameTextView = rootView.findViewById(R.id.username_text);
        bioTextView = rootView.findViewById(R.id.bio_text);
        followersTextView = rootView.findViewById(R.id.followers_text);
        followingTextView = rootView.findViewById(R.id.following_text);

        // Get the logged-in user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Get a reference to the "users" node in the Firebase database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

            // Read the user's data from the database
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Retrieve user information
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String bio = dataSnapshot.child("bio").getValue(String.class);
                    long followersCount = dataSnapshot.child("followerCount").getValue(Long.class);
                    long followingCount = dataSnapshot.child("followingCount").getValue(Long.class);

                    // Update the TextView elements with the retrieved user information
                    nameTextView.setText(name);
                    usernameTextView.setText("@" + username);
                    bioTextView.setText(bio);
                    followersTextView.setText("Followers: " + followersCount);
                    followingTextView.setText("Following: " + followingCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }

        return rootView;
    }
}
