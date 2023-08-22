package com.example.neighborhood.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Login;
import com.example.neighborhood.Post;
import com.example.neighborhood.Adapter.PostAdapter;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProfileFragment extends Fragment implements PostAdapter.UserProfileClickListener {

    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView bioTextView;
    private TextView followersTextView;
    private TextView followingTextView;
    private ImageView profileImageView;
    private Button btnlogout;
    private Button btnedit;
    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        btnlogout = rootView.findViewById(R.id.btn_logout);
        btnedit = rootView.findViewById(R.id.btn_edit);
        profileImageView = rootView.findViewById(R.id.profile_image);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        // Set up swipe-to-refresh action
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Handle the refresh action
                // For example, call loadUserPosts() to reload the user's posts
                loadUserPosts();
            }
        });

        // Set click listeners for the logout and edit buttons
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });

        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToEditProfile();
            }
        });

        // Initialize RecyclerView and PostAdapter
        postRecyclerView = rootView.findViewById(R.id.posts_recycler_view);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, (AppCompatActivity) getActivity(), this);
        postRecyclerView.setAdapter(postAdapter);

        // Get the logged-in user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Get a reference to the "users" node in the Firebase database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

            // Read the user's data from the database
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Retrieve user information
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Update the TextView elements with the retrieved user information
                        nameTextView.setText(user.getName());
                        usernameTextView.setText("@" + user.getUsername());
                        bioTextView.setText(user.getBio());
                        followersTextView.setText("Followers: " + user.getFollowerCount());
                        followingTextView.setText("Following: " + user.getFollowingCount());

                        // Load the profile image using Picasso
                        if (user.getImage() != null && !user.getImage().isEmpty()) {
                            RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                            Glide.with(requireContext()) // Use requireContext() instead of getActivity()
                                    .load(user.getImage())
                                    .apply(requestOptions)
                                    .error(R.drawable.ic_profile) // Set the default image on error
                                    .into(profileImageView);
                        } else {
                            // If profile image URL is empty, load a default image
                            Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                        }

                        // Load the user's posts
                        loadUserPosts();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });
        }

        return rootView;
    }

    private void loadUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.orderByChild("userId").equalTo(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing post list before adding new posts
                postList.clear();

                // Loop through the dataSnapshot to get all user posts and add them to the postList
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }

                // Sort the posts by timestamp (newest first)
                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post post1, Post post2) {
                        return Long.compare(post2.getTimestamp(), post1.getTimestamp());
                    }
                });

                // Notify the adapter that the data has changed
                postAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur while fetching data
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        // Start the Login activity
        Intent intent = new Intent(getActivity(), Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Finish the current activity (optional, if needed)
        getActivity().finish();
    }

    private void navigateToEditProfile() {
        // Replace the current fragment with the EditProfileFragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new EditProfileFragment());
        transaction.addToBackStack(null); // This line allows the user to navigate back to HomeFragment
        transaction.commit();
    }

    @Override
    public void onItemClick(String userId) {
        // Handle user profile item click if needed
    }
}
