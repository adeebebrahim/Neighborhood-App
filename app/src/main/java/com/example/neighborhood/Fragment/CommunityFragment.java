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

import com.example.neighborhood.Adapter.CommunityAdapter;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView communityRecyclerView;
    private CommunityAdapter communityAdapter;
    private Button addButton;

    private DatabaseReference postsRef;
    private DatabaseReference usersRef;

    public CommunityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);

        postsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        communityRecyclerView = rootView.findViewById(R.id.community_recycler_view);
        communityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        communityAdapter = new CommunityAdapter(new ArrayList<>());
        communityRecyclerView.setAdapter(communityAdapter);

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

        loadCommunityPosts();

        return rootView;
    }

    private void loadCommunityPosts() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CommunityPost> communityPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CommunityPost post = postSnapshot.getValue(CommunityPost.class);
                    if (post != null) {
                        communityPosts.add(post);
                    }
                }
                // Set the loaded posts to the adapter
                communityAdapter.setCommunityPosts(communityPosts); // Use the correct method name
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
}
