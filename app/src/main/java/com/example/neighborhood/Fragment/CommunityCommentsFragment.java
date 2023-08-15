package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Adapter.CommunityCommentAdapter;
import com.example.neighborhood.Comment;
import com.example.neighborhood.CommunityComment;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.Post;
import com.example.neighborhood.R;
import com.example.neighborhood.Adapter.CommentAdapter;
import com.example.neighborhood.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommunityCommentsFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView topicTextView;
    private TextView postTextView;
    private TextView timestampTextView;
    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private Button submitCommentButton;

    private List<CommunityComment> commentList; // Use the correct class here
    private CommunityCommentAdapter commentAdapter; // Update the adapter name
    private CommunityPost communityPost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_comments, container, false);

        // Initialize views
        profileImageView = view.findViewById(R.id.profile_image_view);
        nameTextView = view.findViewById(R.id.name_text_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        topicTextView = view.findViewById(R.id.topic_text_view);
        postTextView = view.findViewById(R.id.post_text_view);
        timestampTextView = view.findViewById(R.id.timestamp_text_view);
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentEditText = view.findViewById(R.id.comment_edit_text);
        submitCommentButton = view.findViewById(R.id.submit_comment_button);

        // Retrieve post data from arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            communityPost = arguments.getParcelable("post");
            if (communityPost != null) {
                // Retrieve user data and populate views
                retrieveUserFromDatabase(communityPost.getUserId());

                // Populate other post-related views
                topicTextView.setText(communityPost.getTopic());
                postTextView.setText(communityPost.getDescription());

                // Set up RecyclerView for comments
                commentList = new ArrayList<>();
                commentAdapter = new CommunityCommentAdapter(commentList);
                commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                commentsRecyclerView.setAdapter(commentAdapter);

                // Retrieve and populate comments for the post
                retrieveCommentsForPost(communityPost.getTopicId());
            }
        }

        // Set up comment submission button
        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = commentEditText.getText().toString().trim();
                if (!text.isEmpty()) {
                    // Generate a unique commentId
                    String commentId = FirebaseDatabase.getInstance().getReference().push().getKey();

                    // Get the current user's userId
                    String userId = getCurrentUserId();

                    // Get the postId from the retrieved post
                    String topicId = communityPost.getTopicId();

                    // Get the current timestamp
                    long timestamp = System.currentTimeMillis();

                    // Create a new Comment object
                    CommunityComment newComment = new CommunityComment(userId, topicId, commentId, timestamp, text);

                    // Save the comment to the database
                    DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments");
                    commentsRef.child(topicId).child(commentId).setValue(newComment)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Clear the comment text
                                        commentEditText.setText("");
                                    } else {
                                        // Handle error
                                    }
                                }
                            });

                }
            }
        });

        ImageView backButton = view.findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to the previous fragment
                getParentFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void retrieveUserFromDatabase(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user data to the views
                    if (!TextUtils.isEmpty(user.getImage())) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_profile).into(profileImageView);
                    } else {
                        Picasso.get().load(R.drawable.ic_profile).into(profileImageView);
                    }
                    nameTextView.setText(user.getName());
                    usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void retrieveCommentsForPost(String topicId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments").child(topicId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear(); // Clear existing comments
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    CommunityComment communityComment = commentSnapshot.getValue(CommunityComment.class);
                    if (communityComment != null) {
                        commentList.add(communityComment); // Add the retrieved comment to the list
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}
