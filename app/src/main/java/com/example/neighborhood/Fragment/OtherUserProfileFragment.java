package com.example.neighborhood.Fragment;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Adapter.PostAdapter;
import com.example.neighborhood.Post;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OtherUserProfileFragment extends Fragment implements PostAdapter.UserProfileClickListener {

    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView bioTextView;
    private TextView followersTextView;
    private TextView followingTextView;
    private ImageView profileImageView;
    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private String otherUserId; // User ID of the other user
    private String currentUserId; // User ID of the current user


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        nameTextView = rootView.findViewById(R.id.name_text);
        usernameTextView = rootView.findViewById(R.id.username_text);
        bioTextView = rootView.findViewById(R.id.bio_text);
        followersTextView = rootView.findViewById(R.id.followers_text);
        followingTextView = rootView.findViewById(R.id.following_text);
        profileImageView = rootView.findViewById(R.id.profile_image);
        postRecyclerView = rootView.findViewById(R.id.posts_recycler_view);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : null;


        // Retrieve the other user's ID from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            otherUserId = bundle.getString("userId");
            if (otherUserId != null) {
                // Fetch other user's information using otherUserId
                fetchOtherUserInfo();
            }
        }

        // Initialize RecyclerView and PostAdapter
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, (AppCompatActivity) getActivity(), this);
        postRecyclerView.setAdapter(postAdapter);

        // Load other user's posts
        loadOtherUserPosts();

        // Inside onCreateView method of OtherUserProfileFragment
        ImageView backButton = rootView.findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });

        return rootView;
    }

    private void fetchOtherUserInfo() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User otherUser = dataSnapshot.getValue(User.class);
                if (otherUser != null) {
                    // Update UI elements with other user's information
                    nameTextView.setText(otherUser.getName());
                    usernameTextView.setText("@" + otherUser.getUsername());
                    bioTextView.setText(otherUser.getBio());
                    followersTextView.setText("Followers: " + otherUser.getFollowerCount());
                    followingTextView.setText("Following: " + otherUser.getFollowingCount());

                    // Load the other user's profile image using Glide and apply circular cropping
                    if (otherUser.getImage() != null && !otherUser.getImage().isEmpty()) {
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(requireContext())
                                .load(otherUser.getImage())
                                .apply(requestOptions)
                                .error(R.drawable.ic_profile) // Set the default image on error
                                .into(profileImageView);
                    } else {
                        // If profile image URL is empty, load a default image
                        Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                    }

                    // Check if the current user is following the other user
                    boolean isFollowing = false; // Initialize as false
                    if (currentUserId != null && otherUser.getFollowers() != null) {
                        isFollowing = otherUser.getFollowers().containsKey(currentUserId);
                    }

                    // Update the follow button text and behavior
                    updateFollowButton(isFollowing);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }


    private void updateFollowButton(boolean isFollowing) {
        Button followButton = getView().findViewById(R.id.follow_button);
        followButton.setText(isFollowing ? "Following" : "Follow");

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollowing) {
                    // Unfollow the user
                    unfollowUser();
                } else {
                    // Follow the user
                    followUser();
                }
            }
        });
    }

    private void followUser() {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserId);

        // Add the other user's ID to the following list of the current user
        currentUserRef.child("following").child(otherUserId).setValue(true);

        // Add the current user's ID to the followers list of the other user
        otherUserRef.child("followers").child(currentUserId).setValue(true);

        // Increment the followerCount of the other user and the followingCount of the current user
        otherUserRef.child("followerCount").setValue(ServerValue.increment(1));
        currentUserRef.child("followingCount").setValue(ServerValue.increment(1));

        // Update UI and button text
        updateFollowButton(true);
    }

    private void unfollowUser() {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserId);

        // Remove the other user's ID from the following list of the current user
        currentUserRef.child("following").child(otherUserId).removeValue();

        // Remove the current user's ID from the followers list of the other user
        otherUserRef.child("followers").child(currentUserId).removeValue();

        // Decrement the followerCount of the other user and the followingCount of the current user
        otherUserRef.child("followerCount").setValue(ServerValue.increment(-1));
        currentUserRef.child("followingCount").setValue(ServerValue.increment(-1));

        // Update UI and button text
        updateFollowButton(false);
    }


    private void loadOtherUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.orderByChild("userId").equalTo(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing post list before adding new posts
                postList.clear();

                // Loop through the dataSnapshot to get all other user posts and add them to the postList
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur while fetching data
            }
        });
    }

    @Override
    public void onItemClick(String userId) {
        navigateToOtherUserProfile(userId);
    }

    private void navigateToOtherUserProfile(String userId) {
        OtherUserProfileFragment fragment = new OtherUserProfileFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Allow navigating back
        transaction.commit();
    }
}
