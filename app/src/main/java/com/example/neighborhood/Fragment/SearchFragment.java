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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Adapter.UserAdapter;
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
import java.util.List;

public class SearchFragment extends Fragment implements UserAdapter.UserProfileClickListener {

    private EditText searchBar;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> allUsers;
    private List<User> filteredUsers;

    public SearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);


        searchBar = rootView.findViewById(R.id.search_bar);
        Button cancelButton = rootView.findViewById(R.id.cancel_button);


        userRecyclerView = rootView.findViewById(R.id.user_list);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        userAdapter = new UserAdapter(requireContext(), filteredUsers, this);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });


        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().trim();

                if (query.isEmpty()) {

                    filteredUsers.clear();
                } else {

                    filterUsers(query);
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().trim();

                if (query.isEmpty()) {

                    filteredUsers.clear();
                } else {

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
    public void onItemClick(String userId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(userId)) {
            navigateToProfileFragment();
        } else {
            navigateToOtherUserProfile(userId);
        }
    }

    private void navigateToProfileFragment() {

        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
