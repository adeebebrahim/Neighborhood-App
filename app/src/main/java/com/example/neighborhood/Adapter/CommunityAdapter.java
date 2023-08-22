package com.example.neighborhood.Adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.Fragment.CommunityCommentsFragment;
import com.example.neighborhood.Fragment.PostCommentsFragment;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.google.android.material.button.MaterialButton; // Import MaterialButton

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder> {

    private List<CommunityPost> communityPosts;
    private DatabaseReference usersRef; // Reference to the "Users" node in Firebase
//    private AppCompatActivity context;
    private FragmentActivity context;

    public CommunityAdapter(List<CommunityPost> communityPosts, FragmentActivity context) {
        this.communityPosts = communityPosts;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        this.context = context;
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
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(holder.itemView.getContext())
                                .load(user.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    } else {
                        Glide.with(holder.itemView.getContext()).load(R.drawable.ic_profile).into(holder.profileImageView);
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
        CharSequence timestampFormatted = DateUtils.getRelativeTimeSpanString(post.getTimestamp(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestampTextView.setText(timestampFormatted);

        // Set click listener for comment button
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a bundle to pass post data to the fragment
                Bundle bundle = new Bundle();
                bundle.putParcelable("post", post);

                // Create the fragment instance and pass the bundle
                CommunityCommentsFragment commentsFragment = new CommunityCommentsFragment();
                commentsFragment.setArguments(bundle);

                // Navigate to the fragment
                context.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, commentsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments").child(post.getTopicId());
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the number of comments
                int numComments = (int) dataSnapshot.getChildrenCount();
                holder.commentTextView.setText("Comments " + numComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Add a long click listener to the itemView
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (post.getUserId().equals(getCurrentUserId())) {
                    // Show a confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setMessage("Delete this post?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete the post and related comments
                            deleteCommunityPostAndComments(post);
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();

                    return true; // Consume the click event
                } else {
                    // Display an error message or take other action
                    return true; // Consume the click event
                }
            }
        });

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
        TextView timestampTextView;
        MaterialButton commentButton;
        TextView commentTextView;

        public CommunityViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view); // Assuming you have these TextViews in your item_community_post layout
            usernameTextView = itemView.findViewById(R.id.username_text_view); // Assuming you have these TextViews in your item_community_post layout
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            commentButton = itemView.findViewById(R.id.comment_button); // Initialize comment button as MaterialButton
            commentTextView = itemView.findViewById(R.id.comment_text_view);
        }
    }

    private void navigateToCommunityCommentsFragment(CommunityPost communityPost) {
        // Create a bundle to pass the selected post to the fragment
        Bundle bundle = new Bundle();
        bundle.putParcelable("communitypost", communityPost); // Use "communitypost" key

        // Create the fragment instance
        PostCommentsFragment commentsFragment = new PostCommentsFragment();
        commentsFragment.setArguments(bundle);

        // Navigate to the fragment
        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, commentsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deleteCommunityPostAndComments(CommunityPost post) {
        // Remove the post from the communityPosts list and update the RecyclerView
        communityPosts.remove(post);
        notifyDataSetChanged();

        // Delete the post data from the Firebase Realtime Database
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts").child(post.getTopicId());
        postsRef.removeValue();

        // Delete related comments from the Firebase Realtime Database
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments").child(post.getTopicId());
        commentsRef.removeValue();
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

}
