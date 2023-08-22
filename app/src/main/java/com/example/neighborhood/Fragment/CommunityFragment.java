package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.neighborhood.Adapter.CommunityAdapter;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    // Declare the communityPosts list as a member variable
    private List<CommunityPost> communityPosts = new ArrayList<>();

    private RecyclerView communityRecyclerView;
    private CommunityAdapter communityAdapter;
    private Button addButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    private DatabaseReference postsRef;
    private DatabaseReference usersRef;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);

        // Initialize Firebase references
        postsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Initialize RecyclerView and Adapter
        communityRecyclerView = rootView.findViewById(R.id.community_recycler_view);
        communityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        communityAdapter = new CommunityAdapter(communityPosts, requireActivity());
        communityRecyclerView.setAdapter(communityAdapter);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);

        // Set up swipe-to-refresh action
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Handle the refresh action
                // For example, call loadCommunityPosts() to reload the community posts
                loadCommunityPosts();
            }
        });

        // Initialize "Add" button and set click listener
        addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the fragment to add a new post
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new AddCommunityPostFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Load community posts from Firebase
        loadCommunityPosts();

        return rootView;
    }

    private void loadCommunityPosts() {
        // Listen for changes in the "CommunityPosts" node
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                communityPosts.clear(); // Clear the list before loading new posts
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CommunityPost post = postSnapshot.getValue(CommunityPost.class);
                    if (post != null) {
                        communityPosts.add(post);
                    }
                }
                // Notify the adapter that data has changed
                communityAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
