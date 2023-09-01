package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Adapter.CommunityCommentAdapter;
import com.example.neighborhood.CommunityComment;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
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
    private ImageView submitCommentButton;

    private List<CommunityComment> commentList;
    private CommunityCommentAdapter commentAdapter;
    private CommunityPost communityPost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_comments, container, false);

        profileImageView = view.findViewById(R.id.profile_image_view);
        nameTextView = view.findViewById(R.id.name_text_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        topicTextView = view.findViewById(R.id.topic_text_view);
        postTextView = view.findViewById(R.id.post_text_view);
        timestampTextView = view.findViewById(R.id.timestamp_text_view);
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentEditText = view.findViewById(R.id.comment_edit_text);
        submitCommentButton = view.findViewById(R.id.submit_comment_button);

        Bundle arguments = getArguments();
        if (arguments != null) {
            communityPost = arguments.getParcelable("post");
            if (communityPost != null) {

                retrieveUserFromDatabase(communityPost.getUserId());
                topicTextView.setText(communityPost.getTopic());
                postTextView.setText(communityPost.getDescription());

                commentList = new ArrayList<>();
                commentAdapter = new CommunityCommentAdapter(commentList, requireContext());
                commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                commentsRecyclerView.setAdapter(commentAdapter);

                retrieveCommentsForPost(communityPost.getTopicId());
            }
        }

        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = commentEditText.getText().toString().trim();
                if (!text.isEmpty()) {
                    String commentId = FirebaseDatabase.getInstance().getReference().push().getKey();
                    String userId = getCurrentUserId();
                    String topicId = communityPost.getTopicId();
                    long timestamp = System.currentTimeMillis();
                    CommunityComment newComment = new CommunityComment(userId, topicId, commentId, timestamp, text);

                    DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments");
                    commentsRef.child(topicId).child(commentId).setValue(newComment)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        commentEditText.setText("");
                                    } else {

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
                    if (!TextUtils.isEmpty(user.getImage())) {
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(requireContext())
                                .load(user.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImageView);
                    } else {
                        Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                    }
                    nameTextView.setText(user.getName());
                    usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void retrieveCommentsForPost(String topicId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("CommunityComments").child(topicId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    CommunityComment communityComment = commentSnapshot.getValue(CommunityComment.class);
                    if (communityComment != null) {
                        commentList.add(communityComment);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}
