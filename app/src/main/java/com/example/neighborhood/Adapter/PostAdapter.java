package com.example.neighborhood.Adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Fragment.PostCommentsFragment;
import com.example.neighborhood.Post;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.google.android.material.button.MaterialButton; // Import MaterialButton
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private DatabaseReference usersRef;
    private AppCompatActivity context;
    private FirebaseUser currentUser; // Add currentUser
    private UserProfileClickListener userProfileClickListener;

    public PostAdapter(List<Post> postList, AppCompatActivity context, UserProfileClickListener listener) {
        this.postList = postList;
        this.context = context;
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
        String userId = post.getUserId();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Fetch user information
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user profile data
                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(context)
                                .load(user.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    } else {
                        Glide.with(context)
                                .load(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    }
                    holder.nameTextView.setText(user.getName());
                    holder.usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Set post data
        holder.postTextView.setText(post.getPostText());

        // Set timestamp
        CharSequence timestampFormatted = DateUtils.getRelativeTimeSpanString(post.getTimestamp(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestampTextView.setText(timestampFormatted);

        // Set post image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.ic_addimage).into(holder.postImageView);
        } else {
            holder.postImageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (userId.equals(currentUser.getUid())) {
                    // Show a confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Delete this post?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete the post and associated data
                            deletePost(post);
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();

                    return true; // Consume the click event
                } else {
                    // Display an error message

                    return true; // Consume the click event
                }
            }
        });

        if (post.getLikedByUsers().contains(currentUser.getUid())) {
            holder.likeButton.setText("Liked");
        } else {
            holder.likeButton.setText("Like");
        }
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user has already liked the post
                if (post.getLikedByUsers().contains(currentUser.getUid())) {
                    // Remove user's like
                    post.getLikedByUsers().remove(currentUser.getUid());
                    holder.likeButton.setText("Like");
                } else {
                    // Add user's like
                    post.getLikedByUsers().add(currentUser.getUid());
                    holder.likeButton.setText("Liked");
                }

                // Update the post's likedByUsers field in the database
                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostId());
                postsRef.child("likedByUsers").setValue(post.getLikedByUsers());
            }
        });

        // Set click listeners
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPostCommentsFragment(post);
            }
        });

        // Set other click listeners
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

        int numLikes = post.getLikedByUsers().size();
        holder.likeTextView.setText("Likes " + numLikes);

        // Query the database to count the number of comments
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostId());
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
        MaterialButton commentButton; // Use MaterialButton for comment button
        MaterialButton likeButton;
        TextView likeTextView;
        TextView commentTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            postTextView = itemView.findViewById(R.id.post_text_view);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
            postImageView = itemView.findViewById(R.id.post_image_view);
            commentButton = itemView.findViewById(R.id.comment_button); // Initialize comment button as MaterialButton
            likeButton = itemView.findViewById(R.id.like_button);
            likeTextView = itemView.findViewById(R.id.like_text_view);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
        }
    }

    private void navigateToPostCommentsFragment(Post post) {
        // Create a bundle to pass the selected post to the fragment
        Bundle bundle = new Bundle();
        bundle.putParcelable("post", post); // Use "post" key instead of "selected_post"

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

    private void deletePost(Post post) {
        // Delete post from the postList and update the RecyclerView
        postList.remove(post);
        notifyDataSetChanged();

        // Delete post data from the Firebase Realtime Database
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.child(post.getPostId()).removeValue();

        // Delete post image from storage (if available)
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(post.getImageUrl());
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Image deleted successfully
                }
            });
        }

        // Delete comments associated with the post from the Firebase Realtime Database
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostId());
        commentsRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Comments associated with the post deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure
            }
        });
    }

}
