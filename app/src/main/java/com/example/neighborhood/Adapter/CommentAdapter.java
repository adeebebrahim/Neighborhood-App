package com.example.neighborhood.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Comment;
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
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private Context context; // Add this field

    public CommentAdapter(List<Comment> commentList, Context context) { // Modify the constructor
        this.commentList = commentList;
        this.context = context;
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
                    // Show a confirmation dialog for deleting the comment
                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                    deleteBuilder.setMessage("Delete this comment?");
                    deleteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete the comment
                            deleteComment(comment);
                        }
                    });
                    deleteBuilder.setNegativeButton("No", null);
                    deleteBuilder.show();
                } else {
                    // Show a report confirmation dialog
                    AlertDialog.Builder reportBuilder = new AlertDialog.Builder(context);
                    reportBuilder.setMessage("Report this comment?");
                    reportBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Show a report reason dialog
                            showCommentReportReasonDialog(comment);
                        }
                    });
                    reportBuilder.setNegativeButton("No", null);
                    reportBuilder.show();
                }

                return true; // Consume the click event
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

    private void showCommentReportReasonDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Report Comment");

        // Inflate a custom layout for the report reasons
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
                    showCommentReportConfirmationDialog(comment, selectedReason);
                } else {
                    // No reason selected
                    // You can show a message to the user if desired
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCommentReportConfirmationDialog(Comment comment, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Report");
        builder.setMessage("Are you sure you want to report this comment for the following reason?\n\n" + reason);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call a method to save the report to Firebase for comments
                saveCommentReportToFirebase(comment, reason);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveCommentReportToFirebase(Comment comment, String reason) {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("CommentReports");
        String reportId = reportsRef.push().getKey();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Report report = new Report(reportId, currentUser.getUid(), comment.getPostId(), comment.getCommentId(), reason, System.currentTimeMillis());
            reportsRef.child(reportId).setValue(report)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Comment reported successfully", Toast.LENGTH_SHORT).show();
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
}
