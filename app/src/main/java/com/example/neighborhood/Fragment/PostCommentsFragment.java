package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Comment;
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

public class PostCommentsFragment extends Fragment {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView postTextView;
    private ImageView postImageView;
    private TextView timestampTextView;
    private ImageView likeButton;
    private ImageView commentButton;
    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private ImageView submitCommentButton;
    private TextView likeTextView;
    private TextView commentTextView;

    private List<Comment> commentList;
    private CommentAdapter commentAdapter;
    private Post post;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_comments, container, false);


        profileImageView = view.findViewById(R.id.profile_image_view);
        nameTextView = view.findViewById(R.id.name_text_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        postTextView = view.findViewById(R.id.post_text_view);
        postImageView = view.findViewById(R.id.post_image_view);
        timestampTextView = view.findViewById(R.id.timestamp_text_view);
        likeButton = view.findViewById(R.id.like_button);
        commentButton = view.findViewById(R.id.comment_button);
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentEditText = view.findViewById(R.id.comment_edit_text);
        submitCommentButton = view.findViewById(R.id.submit_comment_button);
        likeTextView = view.findViewById(R.id.like_text_view);
        commentTextView = view.findViewById(R.id.comment_text_view);


        Bundle arguments = getArguments();
        if (arguments != null) {
            post = arguments.getParcelable("post");
            if (post != null) {

                retrieveUserFromDatabase(post.getUserId());


                postTextView.setText(post.getPostText());


                if (!TextUtils.isEmpty(post.getImageUrl())) {
                    postImageView.setVisibility(View.VISIBLE);
                    Picasso.get().load(post.getImageUrl()).placeholder(R.drawable.ic_addimage).into(postImageView);
                } else {
                    postImageView.setVisibility(View.GONE);
                }


                commentList = new ArrayList<>();
                commentAdapter = new CommentAdapter(commentList, requireContext());
                commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                commentsRecyclerView.setAdapter(commentAdapter);


                retrieveCommentsForPost(post.getPostId());
            }
        }


        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = commentEditText.getText().toString().trim();
                if (!commentText.isEmpty()) {

                    String commentId = FirebaseDatabase.getInstance().getReference().push().getKey();


                    String userId = getCurrentUserId();


                    String postId = post.getPostId();


                    long timestamp = System.currentTimeMillis();


                    Comment newComment = new Comment(userId, postId, commentId, timestamp, commentText);


                    DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments");
                    commentsRef.child(postId).child(commentId).setValue(newComment)
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


        if (post.getLikedByUsers().contains(getCurrentUserId())) {
            likeButton.setImageResource(R.drawable.ic_like);
        } else {
            likeButton.setImageResource(R.drawable.ic_likeoutlined);
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("posts").child(post.getPostId());


                if (post.getLikedByUsers().contains(getCurrentUserId())) {

                    post.getLikedByUsers().remove(getCurrentUserId());
                    likeButton.setImageResource(R.drawable.ic_likeoutlined);
                } else {

                    post.getLikedByUsers().add(getCurrentUserId());
                    likeButton.setImageResource(R.drawable.ic_like);
                }


                postsRef.child("likedByUsers").setValue(post.getLikedByUsers());
            }
        });


        int numLikes = post.getLikedByUsers().size();
        likeTextView.setText("Likes " + numLikes);


        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(post.getPostId());
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int numComments = (int) dataSnapshot.getChildrenCount();
                commentTextView.setText("Comments " + numComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                    nameTextView.setText(user.getName());
                    usernameTextView.setText("@" + user.getUsername());

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void retrieveCommentsForPost(String postId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
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
