package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.neighborhood.CommunityPost;
import com.example.neighborhood.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCommunityPostFragment extends Fragment {

    private EditText topicEditText, descriptionEditText;
    private DatabaseReference communityPostsRef;

    public AddCommunityPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_community_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        communityPostsRef = FirebaseDatabase.getInstance().getReference().child("CommunityPosts");

        topicEditText = view.findViewById(R.id.topic_edit_text);
        descriptionEditText = view.findViewById(R.id.description_edit_text);
        Button postButton = view.findViewById(R.id.post_button);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postCommunityPost();
            }
        });
    }

    private void postCommunityPost() {
        String topic = topicEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            CommunityPost newPost = new CommunityPost(userId, topic, description);
            communityPostsRef.push().setValue(newPost);

            // Clear input fields after posting
            topicEditText.getText().clear();
            descriptionEditText.getText().clear();

            // Navigate back to CommunityFragment after posting
            getParentFragmentManager().popBackStack();
        }
    }
}
