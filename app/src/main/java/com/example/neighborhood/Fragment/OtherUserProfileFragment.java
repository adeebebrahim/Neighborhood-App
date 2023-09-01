package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    private String otherUserId;
    private String currentUserId;


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



        Bundle bundle = getArguments();
        if (bundle != null) {
            otherUserId = bundle.getString("userId");
            if (otherUserId != null) {

                fetchOtherUserInfo();
            }
        }


        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, (AppCompatActivity) getActivity(), this);
        postRecyclerView.setAdapter(postAdapter);


        loadOtherUserPosts();


        ImageView backButton = rootView.findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                    nameTextView.setText(otherUser.getName());
                    usernameTextView.setText("@" + otherUser.getUsername());
                    bioTextView.setText(otherUser.getBio());
                    followersTextView.setText("Followers: " + otherUser.getFollowerCount());
                    followingTextView.setText("Following: " + otherUser.getFollowingCount());


                    if (otherUser.getImage() != null && !otherUser.getImage().isEmpty()) {
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(requireContext())
                                .load(otherUser.getImage())
                                .apply(requestOptions)
                                .error(R.drawable.ic_profile)
                                .into(profileImageView);
                    } else {

                        Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                    }


                    boolean isFollowing = false;
                    if (currentUserId != null && otherUser.getFollowers() != null) {
                        isFollowing = otherUser.getFollowers().containsKey(currentUserId);
                    }


                    updateFollowButton(isFollowing);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                    unfollowUser();
                } else {

                    followUser();
                }
            }
        });
    }

    private void followUser() {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserId);


        currentUserRef.child("following").child(otherUserId).setValue(true);


        otherUserRef.child("followers").child(currentUserId).setValue(true);


        otherUserRef.child("followerCount").setValue(ServerValue.increment(1));
        currentUserRef.child("followingCount").setValue(ServerValue.increment(1));


        updateFollowButton(true);
    }

    private void unfollowUser() {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserId);


        currentUserRef.child("following").child(otherUserId).removeValue();


        otherUserRef.child("followers").child(currentUserId).removeValue();


        otherUserRef.child("followerCount").setValue(ServerValue.increment(-1));
        currentUserRef.child("followingCount").setValue(ServerValue.increment(-1));


        updateFollowButton(false);
    }


    private void loadOtherUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.orderByChild("userId").equalTo(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList.clear();


                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }


                Collections.sort(postList, new Comparator<Post>() {
                    @Override
                    public int compare(Post post1, Post post2) {
                        return Long.compare(post2.getTimestamp(), post1.getTimestamp());
                    }
                });


                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
