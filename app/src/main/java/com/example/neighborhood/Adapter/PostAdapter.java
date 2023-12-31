package com.example.neighborhood.Adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Fragment.PostCommentsFragment;
import com.example.neighborhood.Post;
import com.example.neighborhood.R;
import com.example.neighborhood.Report;
import com.example.neighborhood.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private DatabaseReference usersRef;
    private AppCompatActivity context;
    private FirebaseUser currentUser;
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

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
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

            }
        });

        holder.postTextView.setText(post.getPostText());

        CharSequence timestampFormatted = DateUtils.getRelativeTimeSpanString(post.getTimestamp(),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestampTextView.setText(timestampFormatted);

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Delete this post?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deletePost(post);
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();
                    return true;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Report this post?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showReportReasonDialog(post);
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.show();

                    return true;
                }
            }
        });

        if (post.getLikedByUsers().contains(currentUser.getUid())) {
            holder.likeButton.setImageResource(R.drawable.ic_like);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_likeoutlined);
        }
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.getLikedByUsers().contains(currentUser.getUid())) {
                    post.getLikedByUsers().remove(currentUser.getUid());
                    holder.likeButton.setImageResource(R.drawable.ic_likeoutlined);
                } else {
                    post.getLikedByUsers().add(currentUser.getUid());
                    holder.likeButton.setImageResource(R.drawable.ic_like);
                }
                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostId());
                postsRef.child("likedByUsers").setValue(post.getLikedByUsers());
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPostCommentsFragment(post);
            }
        });
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

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostId());
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int numComments = (int) dataSnapshot.getChildrenCount();
                holder.commentTextView.setText("Comments " + numComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        ImageView commentButton;
        ImageView likeButton;
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
            commentButton = itemView.findViewById(R.id.comment_button);
            likeButton = itemView.findViewById(R.id.like_button);
            likeTextView = itemView.findViewById(R.id.like_text_view);
            commentTextView = itemView.findViewById(R.id.comment_text_view);
        }
    }

    private void navigateToPostCommentsFragment(Post post) {

        Bundle bundle = new Bundle();
        bundle.putParcelable("post", post);

        PostCommentsFragment commentsFragment = new PostCommentsFragment();
        commentsFragment.setArguments(bundle);

        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, commentsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deletePost(Post post) {
        postList.remove(post);
        notifyDataSetChanged();

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        postsRef.child(post.getPostId()).removeValue();

        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(post.getImageUrl());
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostId());
        commentsRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void showReportReasonDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Report Post");

        View reportReasonsView = LayoutInflater.from(context).inflate(R.layout.dialog_report_reasons, null);
        String[] reportReasons = {"Inappropriate content", "Spam", "Harassment", "Other"};

        ListView reasonsListView = reportReasonsView.findViewById(R.id.reasons_list_view);
        ArrayAdapter<String> reasonsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, reportReasons);
        reasonsListView.setAdapter(reasonsAdapter);
        builder.setView(reportReasonsView);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = reasonsListView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    String selectedReason = reportReasons[selectedPosition];
                    showReportConfirmationDialog(post, selectedReason);
                } else {

                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReportConfirmationDialog(Post post, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Report");
        builder.setMessage("Are you sure you want to report this post for the following reason?\n\n" + reason);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveReportToFirebase(post, reason);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveReportToFirebase(Post post, String reason) {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("Reports");
        String reportId = reportsRef.push().getKey();

        Report report = new Report(reportId, currentUser.getUid(), post.getPostId(), reason, System.currentTimeMillis());
        reportsRef.child(reportId).setValue(report)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Post reported successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Report submission failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
