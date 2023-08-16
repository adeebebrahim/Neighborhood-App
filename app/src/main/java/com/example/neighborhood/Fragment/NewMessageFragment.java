package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.bumptech.glide.Glide; // Import Glide library for image loading
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewMessageFragment extends Fragment {

    private TextInputEditText messageEditText;
    private ImageView sendButton;
    private ImageView profileImageView;
    private TextView recepientNameTextView;

    private String recipientUserId; // Store the recipient user's ID here

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_message, container, false);

        messageEditText = view.findViewById(R.id.message_edit_text);
        sendButton = view.findViewById(R.id.send_button);
        profileImageView = view.findViewById(R.id.profile_image_view);
        recepientNameTextView = view.findViewById(R.id.recepient_name);

        // Retrieve the recipient user's ID from the arguments
        recipientUserId = getArguments().getString("recipientUserId");

        // Retrieve the recipient user's details from Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(recipientUserId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User recipientUser = dataSnapshot.getValue(User.class);
                if (recipientUser != null) {
                    // Update the TextView elements with the recipient user's information
                    recepientNameTextView.setText(recipientUser.getName());

                    // Load the recipient user's profile image using Glide
                    if (recipientUser.getImage() != null && !recipientUser.getImage().isEmpty()) {
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(requireContext())
                                .load(recipientUser.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImageView);
                    } else {
                        // If profile image URL is empty, load a default image
                        Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

        // Handle the send button click
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEditText.getText().toString();
                // Implement sending logic to the recipient's user here using recipientUserId
            }
        });

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }
}
