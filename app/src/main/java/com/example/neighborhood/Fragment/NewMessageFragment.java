package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Adapter.MessageAdapter;
import com.example.neighborhood.Message;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewMessageFragment extends Fragment {

    private TextInputEditText messageEditText;
    private ImageView sendButton;
    private ImageView profileImageView;
    private TextView recipientNameTextView;

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private DatabaseReference messagesRef;
    private FirebaseUser currentUser;
    private String recipientUserId;
    private boolean isAdapterSet = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_message, container, false);

        messageEditText = view.findViewById(R.id.message_edit_text);
        sendButton = view.findViewById(R.id.send_button);
        profileImageView = view.findViewById(R.id.profile_image_view);
        recipientNameTextView = view.findViewById(R.id.recepient_name);

        recyclerView = view.findViewById(R.id.message_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(getContext(), messageList, getCurrentUserId());
        recyclerView.setAdapter(messageAdapter);

        recipientUserId = getArguments().getString("recipientUserId");
        if (recipientUserId == null) {
            Toast.makeText(getContext(), "Recipient user ID not provided", Toast.LENGTH_SHORT).show();
            return view;
        }

        messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadMessages();
        } else {
            // Handle the case when the user is not logged in
        }

        if (!isAdapterSet) {
            // Set up the MessageAdapter only if it's not already set

            isAdapterSet = true;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(recipientUserId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User recipientUser = dataSnapshot.getValue(User.class);
                if (recipientUser != null) {
                    // Update the TextView elements with the recipient user's information
                    recipientNameTextView.setText(recipientUser.getName());

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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
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

    private void loadMessages() {
        String chatId = getChatId();
        if (chatId != null) {
            messagesRef.child(chatId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                    // Handle child changed event if needed
                    messageList.clear(); // Clear existing messages before adding new ones
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    // Handle child removed event if needed
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                    // Handle child moved event if needed
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database error if needed
                }
            });
        }
    }


    // Inside the sendMessage method
    private void sendMessage() {
        String messageText = messageEditText.getText().toString();
        String senderUserId = getCurrentUserId();
        String chatId = getChatId();

        if (!messageText.isEmpty() && senderUserId != null && chatId != null) {
            DatabaseReference chatRef = messagesRef.child(chatId);
            String messageId = chatRef.push().getKey();

            if (messageId != null) {
                Message message = new Message(messageText, senderUserId, recipientUserId);
                chatRef.child(messageId).setValue(message);
                messageEditText.setText(""); // Clear the message text field after sending
            }
        }
    }


    private String getCurrentUserId() {
        return currentUser != null ? currentUser.getUid() : null;
    }

    private String getChatId() {
        if (currentUser != null && recipientUserId != null) {
            String currentUserUid = currentUser.getUid();
            String otherUserUid = recipientUserId;

            // Create a consistent chat ID by concatenating the user IDs
            // in a specific order (alphabetical)
            if (currentUserUid.compareTo(otherUserUid) < 0) {
                return currentUserUid + "_" + otherUserUid;
            } else {
                return otherUserUid + "_" + currentUserUid;
            }
        }
        return null;
    }
}
