package com.example.neighborhood.Adapter;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Comment;
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

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.commentTextView.setText(comment.getText());

        // Retrieve user information for the comment
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(comment.getUserId());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user information to the views
                    holder.nameTextView.setText(user.getName());
                    holder.usernameTextView.setText("@" + user.getUsername());

                    if (!TextUtils.isEmpty(user.getImage())) {
                        // Load the user profile image with circular cropping
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(holder.itemView.getContext())
                                .load(user.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.profileImageView);
                    } else {
                        // Load default profile image if the image URL is empty
                        Glide.with(holder.itemView.getContext()).load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Add a long click listener to the itemView
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (comment.getUserId().equals(getCurrentUserId())) {
                    // Show a confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    builder.setMessage("Delete this comment?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete the comment
                            deleteComment(comment);
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
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView usernameTextView;
        private TextView commentTextView;
        private ImageView profileImageView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
        }
    }

    private void deleteComment(Comment comment) {
        // Remove the comment from the commentList and update the RecyclerView
        commentList.remove(comment);
        notifyDataSetChanged();

        // Delete comment data from the Firebase Realtime Database
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(comment.getPostId()).child(comment.getCommentId());
        commentsRef.removeValue();
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

}
