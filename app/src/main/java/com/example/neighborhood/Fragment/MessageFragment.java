package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Adapter.MessageUserAdapter;
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

public class MessageFragment extends Fragment implements MessageUserAdapter.OnUserItemClickListener {

    private RecyclerView recyclerView;
    private MessageUserAdapter messageUserAdapter;
    private List<User> allUsersList;
    private List<User> filteredUsersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        recyclerView = view.findViewById(R.id.message_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allUsersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();
        messageUserAdapter = new MessageUserAdapter(getContext(), filteredUsersList);
        recyclerView.setAdapter(messageUserAdapter);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsersList.clear();

                String loggedInUserId = getCurrentUserId();

                Log.d("User Debug", "User ID: " + getCurrentUserId());

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null && user.getUserId() != null && !user.getUserId().equals(loggedInUserId)) {
                        Log.d("User Debug", "User ID: " + user.getUserId());
                        allUsersList.add(user);
                    }
                }


                filteredUsersList.clear();
                filteredUsersList.addAll(allUsersList);
                messageUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageUserAdapter.setOnUserItemClickListener(this);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    @Override
    public void onUserItemClick(User user) {

        String recipientUserId = user.getUserId();

        NewMessageFragment newMessageFragment = new NewMessageFragment();
        Bundle args = new Bundle();
        args.putString("recipientUserId", recipientUserId);
        newMessageFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newMessageFragment)
                .addToBackStack(null)
                .commit();
    }
}