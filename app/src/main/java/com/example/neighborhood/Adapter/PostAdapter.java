package com.example.neighborhood.Adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Post;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private DatabaseReference usersRef; // Reference to the "Users" node in Firebase
    private UserProfileClickListener userProfileClickListener;

    public PostAdapter(List<Post> postList, UserProfileClickListener listener) {
        this.postList = postList;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        this.userProfileClickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Fetch the user information from Firebase based on userId
        String userId = post.getUserId();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user profile picture, name, and username
                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_profile).into(holder.profileImageView);
                    } else {
                        // If the profile image URL is empty or null, load a default image or show an error image
                        Picasso.get().load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                    holder.nameTextView.setText(user.getName());
                    holder.usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

        // Set post text
        holder.postTextView.setText(post.getPostText());

        // Set the timestamp
        CharSequence timestampFormatted = DateUtils.getRelativeTimeSpanString(post.getTimestamp(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestampTextView.setText(timestampFormatted);

        // Check if the post has an image and show/hide the ImageView accordingly
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            // Load the post image using Picasso or any other image loading library
            Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.ic_addimage).into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userProfileClickListener.onItemClick(userId);
            }
        });
        holder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userProfileClickListener.onItemClick(userId);
            }
        });
        holder.usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userProfileClickListener.onItemClick(userId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    public interface UserProfileClickListener {
        void onItemClick(String userId);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView usernameTextView;
        TextView postTextView;
        TextView timestampTextView;
        ImageView postImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            postTextView = itemView.findViewById(R.id.post_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            postImageView = itemView.findViewById(R.id.post_image_view);
        }
    }
}
