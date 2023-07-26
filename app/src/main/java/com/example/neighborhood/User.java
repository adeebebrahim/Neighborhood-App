package com.example.neighborhood;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.User;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText searchBar;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> allUsers; // A list to store all users (you can fetch this list from Firebase Realtime Database or any other data source)
    private List<User> filteredUsers; // A list to store filtered users based on the search query

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        // Find the views within the fragment
        searchBar = rootView.findViewById(R.id.search_bar);
        Button cancelButton = rootView.findViewById(R.id.cancel_button);

        // Initialize the RecyclerView and UserAdapter
        userRecyclerView = rootView.findViewById(R.id.user_list);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        allUsers = new ArrayList<>(); // Fetch all users from Firebase Realtime Database or any other data source
        filteredUsers = new ArrayList<>();
        userAdapter = new UserAdapter(filteredUsers);
        userRecyclerView.setAdapter(userAdapter);

        // Set click listener for the cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the HomeFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        // Implement search logic here (e.g., filtering users based on the search query)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterUsers(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // For demonstration purposes, let's add some dummy users to the allUsers list
        allUsers.add(new User("John Doe", "john@example.com", "1234567890", "1990-01-01", "Male", "I am John Doe", 100, 50, "john_doe"));
        allUsers.add(new User("Jane Smith", "jane@example.com", "9876543210", "1992-05-15", "Female", "I am Jane Smith", 200, 80, "jane_smith"));

        // Initially, display all users (before any search query)
        filteredUsers.addAll(allUsers);
        userAdapter.notifyDataSetChanged();

        return rootView;
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        for (User user : allUsers) {
            // Filter users based on the search query (matching either name or username)
            if (user.getName().toLowerCase().contains(query.toLowerCase()) || user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        userAdapter.notifyDataSetChanged();
    }
}
