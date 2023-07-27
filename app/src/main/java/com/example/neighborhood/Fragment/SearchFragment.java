package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Adapter.UserAdapter;
import com.example.neighborhood.User;
import com.example.neighborhood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements UserAdapter.OnFollowButtonClickListener {

    private EditText searchBar;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> allUsers;
    private List<User> filteredUsers;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Find the views within the fragment
        searchBar = rootView.findViewById(R.id.search_bar);
        Button cancelButton = rootView.findViewById(R.id.cancel_button);

        // Initialize RecyclerView and UserAdapter
        userRecyclerView = rootView.findViewById(R.id.user_list);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        userAdapter = new UserAdapter(filteredUsers, this);
        userRecyclerView.setAdapter(userAdapter);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsers.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    allUsers.add(user);
                }
                userAdapter.setUserList(allUsers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur while fetching data
            }
        });

        // Set click listener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        // Implement search logic here (e.g., filtering users based on the search query)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().trim(); // Remove leading/trailing white spaces

                if (query.isEmpty()) {
                    // If the search query is empty, clear the filtered user list to hide all users
                    filteredUsers.clear();
                } else {
                    // If the search query is not empty, filter the users based on the query
                    filterUsers(query);
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().trim(); // Remove leading/trailing white spaces

                if (query.isEmpty()) {
                    // If the search query is empty, clear the filtered user list to hide all users
                    filteredUsers.clear();
                } else {
                    // If the search query is not empty, filter the users based on the query
                    filterUsers(query);
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return rootView;
    }

    private void filterUsers(String query) {
        String lowerCaseQuery = query.toLowerCase();
        filteredUsers.clear();
        for (User user : allUsers) {
            if (user.getName() != null && user.getName().toLowerCase().contains(lowerCaseQuery)
                    || user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerCaseQuery)) {
                filteredUsers.add(user);
            }
        }
        userAdapter.setUserList(filteredUsers);
    }

    @Override
    public void onFollowButtonClick(int position, boolean newFollowStatus) {
        User user = filteredUsers.get(position);
//        updateFollowStatus(user.getUsername(), newFollowStatus, user);
    }

//    private void updateFollowStatus(String username, boolean newFollowStatus, User targetUser) {
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            String currentUserUid = currentUser.getUid();
//            DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserUid);
//            currentUserRef.child("following").child(username).setValue(newFollowStatus);
//
//            DatabaseReference targetUserRef = FirebaseDatabase.getInstance().getReference("users").child(targetUser.getUserId());
//            targetUserRef.child("followers").child(currentUserUid).setValue(newFollowStatus);
//
//            // Update follower count for the target user
//            targetUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    long followerCount = snapshot.child("followers").getChildrenCount();
//                    targetUser.setFollowerCount((int) followerCount);
//                    targetUserRef.child("followerCount").setValue((int) followerCount);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    // Handle the error if needed
//                }
//            });
//
//            // Update following count for the current user
//            currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    long followingCount = snapshot.child("following").getChildrenCount();
//                    currentUser.setFollowingCount((int) followingCount);
//                    currentUserRef.child("followingCount").setValue((int) followingCount);
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//                    // Handle the error if needed
//                }
//            });
//        }
//    }
}
