package com.example.neighborhood.Adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<CommunityPost> communityPosts;
    private DatabaseReference usersRef; // Reference to the "Users" node in Firebase

    public CommunityAdapter(List<CommunityPost> communityPosts) {
        this.communityPosts = communityPosts;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public CommunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_post, parent, false);
        return new CommunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityViewHolder holder, int position) {
        CommunityPost post = communityPosts.get(position);

        // Fetch the user information from Firebase based on userId
        String userId = post.getUserId();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user profile picture
                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_profile).into(holder.profileImageView);
                    } else {
                        Picasso.get().load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                    // Set user name and username
                    holder.nameTextView.setText(user.getName());
                    holder.usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

        // Set post data such as title and description
        holder.titleTextView.setText(post.getTopic());
        holder.descriptionTextView.setText(post.getDescription());
    }

    @Override
    public int getItemCount() {
        return communityPosts.size();
    }

    public void setCommunityPosts(List<CommunityPost> posts) {
        communityPosts = posts;
        notifyDataSetChanged();
    }

    public static class CommunityViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView usernameTextView;
        TextView titleTextView;
        TextView descriptionTextView;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view); // Assuming you have these TextViews in your item_community_post layout
            usernameTextView = itemView.findViewById(R.id.username_text_view); // Assuming you have these TextViews in your item_community_post layout
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
        }
    }
}
