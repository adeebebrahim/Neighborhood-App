package com.example.neighborhood.Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neighborhood.Comment;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
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
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_profile).into(holder.profileImageView);
                    } else {
                        // Set a default placeholder image if the image URL is empty
                        Picasso.get().load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
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
}
