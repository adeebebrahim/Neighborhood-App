package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Adapter.PostAdapter;
import com.example.neighborhood.Post;
import com.example.neighborhood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView postRecyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the RecyclerView within the fragment
        postRecyclerView = rootView.findViewById(R.id.post_list);

        // Initialize the RecyclerView, PostAdapter, and postList
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        postRecyclerView.setAdapter(postAdapter);

        // Fetch posts from Firebase and update the RecyclerView
        fetchPostsFromFirebase();

        return rootView;
    }

    private void fetchPostsFromFirebase() {
        // Get a reference to the "posts" node in the Firebase database
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");

        // Read the posts data from the database
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing post list before adding new posts
                postList.clear();

                // Loop through the dataSnapshot to get all posts and add them to the postList
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
}
