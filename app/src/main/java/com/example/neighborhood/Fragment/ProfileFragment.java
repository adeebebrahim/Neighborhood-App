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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.firebase.database.ValueEventListener;

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
    private Button btnedit;
    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        nameTextView = rootView.findViewById(R.id.name_text);
        usernameTextView = rootView.findViewById(R.id.username_text);
        bioTextView = rootView.findViewById(R.id.bio_text);
        followersTextView = rootView.findViewById(R.id.followers_text);
        followingTextView = rootView.findViewById(R.id.following_text);
        btnedit = rootView.findViewById(R.id.btn_edit);
        profileImageView = rootView.findViewById(R.id.profile_image);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                loadUserPosts();
            }
        });

        btnedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToEditProfile();
            }
        });


        postRecyclerView = rootView.findViewById(R.id.posts_recycler_view);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, (AppCompatActivity) getActivity(), this);
        postRecyclerView.setAdapter(postAdapter);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();


            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);


            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {

                        nameTextView.setText(user.getName());
                        usernameTextView.setText("@" + user.getUsername());
                        bioTextView.setText(user.getBio());
                        followersTextView.setText("Followers: " + user.getFollowerCount());
                        followingTextView.setText("Following: " + user.getFollowingCount());


                        if (user.getImage() != null && !user.getImage().isEmpty()) {
                            RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                            Glide.with(requireContext())
                                    .load(user.getImage())
                                    .apply(requestOptions)
                                    .error(R.drawable.ic_profile)
                                    .into(profileImageView);
                        } else {

                            Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                        }


                        loadUserPosts();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

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
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void navigateToEditProfile() {

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new EditProfileFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onItemClick(String userId) {

    }
}
