package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
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


    private List<CommunityPost> communityPosts = new ArrayList<>();

    private RecyclerView communityRecyclerView;
    private CommunityAdapter communityAdapter;
    private Button addButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    private DatabaseReference postsRef;
    private DatabaseReference usersRef;

    public CommunityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);


        postsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        communityRecyclerView = rootView.findViewById(R.id.community_recycler_view);
        communityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        communityAdapter = new CommunityAdapter(communityPosts, requireActivity());
        communityRecyclerView.setAdapter(communityAdapter);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                loadCommunityPosts();
            }
        });


        addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                communityPosts.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    CommunityPost post = postSnapshot.getValue(CommunityPost.class);
                    if (post != null) {
                        communityPosts.add(post);
                    }
                }

                communityAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
