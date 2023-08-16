package com.example.neighborhood.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Message;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String loggedInUserId;
    private DatabaseReference messagesRef;
    private ChildEventListener childEventListener;

    public MessageAdapter(Context context, List<Message> messageList, String loggedInUserId) {
        this.context = context;
        this.messageList = messageList;
        this.loggedInUserId = loggedInUserId;
        this.messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Message newMessage = dataSnapshot.getValue(Message.class);

                if (newMessage != null) {
                    messageList.add(newMessage);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle child changed event if needed
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Handle child removed event if needed
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Handle child moved event if needed
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error if needed
            }
        };

        messagesRef.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.messageTextView.setText(message.getMessageText());

        String senderUserId = message.getSenderUserId();
        if (senderUserId != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(senderUserId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User senderUser = dataSnapshot.getValue(User.class);
                    if (senderUser != null && senderUser.getImage() != null && !senderUser.getImage().isEmpty()) {
                        // Load and display the sender's profile image using Glide
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(context)
                                .load(senderUser.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    } else {
                        // If profile image URL is empty, load a default image
                        Glide.with(context).load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors if any
                }
            });

            // Customize the appearance based on the sender of the message
            if (senderUserId.equals(loggedInUserId)) {
                // This message was sent by the logged-in user
                // You can customize the appearance accordingly, e.g., change the background color
            } else {
                // This message was received from another user
                // You can customize the appearance accordingly, e.g., change the background color
            }
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView profileImageView; // Add this line

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view); // Initialize the ImageView
        }
    }
}

