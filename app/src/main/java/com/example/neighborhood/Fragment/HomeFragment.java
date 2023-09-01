package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.neighborhood.Adapter.PostAdapter;
import com.example.neighborhood.Post;
import com.example.neighborhood.R;
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

public class HomeFragment extends Fragment implements PostAdapter.UserProfileClickListener {

    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        postRecyclerView = rootView.findViewById(R.id.post_list);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, (AppCompatActivity) getActivity(), this);
        postRecyclerView.setAdapter(postAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            fetchPostsFromFirebase();
        }

        ImageView searchIcon = rootView.findViewById(R.id.search_icon);
        ImageView messageIcon = rootView.findViewById(R.id.message_icon);

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        messageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MessageFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                fetchPostsFromFirebase();
            }
        });

        return rootView;
    }

    private void fetchPostsFromFirebase() {
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(currentUser.getUid());

        currentUserRef.child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> followedUserIds = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    followedUserIds.add(userSnapshot.getKey());
                }

                followedUserIds.add(currentUser.getUid());

                fetchPostsFromUsers(followedUserIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchPostsFromUsers(List<String> userIds) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference()
                .child("posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && userIds.contains(post.getUserId())) {
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


    private void fetchPostsFromFollowedUsers(List<String> followedUserIds) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference()
                .child("posts");

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && followedUserIds.contains(post.getUserId())) {
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

    @Override
    public void onItemClick(String userId) {
        navigateToUserProfile(userId);
    }

    public void navigateToUserProfile(String userId) {
        if (currentUser != null && userId.equals(currentUser.getUid())) {

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        } else {

            OtherUserProfileFragment otherUserProfileFragment = new OtherUserProfileFragment();
            Bundle args = new Bundle();
            args.putString("userId", userId);
            otherUserProfileFragment.setArguments(args);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, otherUserProfileFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}