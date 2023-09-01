package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddCommunityPostFragment extends Fragment {

    private EditText topicEditText, descriptionEditText;
    private ImageView profileImageView;
    private DatabaseReference communityPostsRef;
    private DatabaseReference usersRef;
    private Button btnCancel;

    public AddCommunityPostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_community_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        communityPostsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        profileImageView = view.findViewById(R.id.profile_picture);
        topicEditText = view.findViewById(R.id.topic_edit_text);
        descriptionEditText = view.findViewById(R.id.description_edit_text);
        Button postButton = view.findViewById(R.id.post_button);
        btnCancel = view.findViewById(R.id.btn_cancel);

        loadUserProfileImage();

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCommunityPost();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void loadUserProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null && user.getImage() != null && !user.getImage().isEmpty()) {
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
    }

    private void postCommunityPost() {
        String topic = topicEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String topicId = communityPostsRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            CommunityPost newPost = new CommunityPost(userId, topic, description, topicId, timestamp);
            communityPostsRef.child(topicId).setValue(newPost);
            topicEditText.getText().clear();
            descriptionEditText.getText().clear();
            getParentFragmentManager().popBackStack();
        }
    }
}
