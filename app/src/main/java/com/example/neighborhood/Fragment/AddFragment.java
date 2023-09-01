package com.example.neighborhood.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddFragment extends Fragment {

    private EditText editText;
    private ImageView addImageIcon;
    private ImageView profileImageView;
    private ImageView selectedImage;
    private Button btnCancel, btnPost;
    private LinearLayout buttonsLayout;

    private Uri imageUri;
    private ProgressDialog progressDialog;
    private String currentUserId;

    public AddFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add, container, false);

        editText = rootView.findViewById(R.id.edit_text);
        addImageIcon = rootView.findViewById(R.id.add_image_icon);
        profileImageView = rootView.findViewById(R.id.profile_picture);
        selectedImage = rootView.findViewById(R.id.selected_image);
        btnCancel = rootView.findViewById(R.id.btn_cancel);
        btnPost = rootView.findViewById(R.id.btn_post);
        buttonsLayout = rootView.findViewById(R.id.buttons_layout);

        progressDialog = new ProgressDialog(getActivity());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postText = editText.getText().toString().trim();

                if (postText.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter something", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (violatesRules(postText)) {
                    showGuidelinesViolationDialog();
                    return;
                }

                progressDialog.setMessage("Posting...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                if (imageUri != null) {
                    uploadImageAndSavePost(postText);
                } else {
                    savePostToFirebase(postText, null);
                }
            }
        });

        addImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        loadUserProfileImage();
        return rootView;
    }

    private void loadUserProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            userRef.addValueEventListener(new ValueEventListener() {
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

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            imageUri = data.getData();
            addImageIcon.setVisibility(View.VISIBLE);
            selectedImage.setVisibility(View.VISIBLE);
            selectedImage.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSavePost(final String postText) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("post_images");
        final String postId = FirebaseDatabase.getInstance().getReference("posts").push().getKey();
        final StorageReference imageFilePath = storageReference.child(postId + ".jpg");

        UploadTask uploadTask = imageFilePath.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageFilePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    savePostToFirebase(postText, downloadUri.toString());
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void savePostToFirebase(String postText, String imageUrl) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        String postId = postsRef.push().getKey();

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("postText", postText);
        postMap.put("imageUrl", imageUrl);
        postMap.put("userId", currentUserId);
        postMap.put("timestamp", System.currentTimeMillis());

        postsRef.child(postId).setValue(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Post added successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                } else {
                    Toast.makeText(getActivity(), "Failed to add post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean violatesRules(String text) {
        String[] ruleKeywords = {"fuck","bitch","motherfucker","ass", "nigga",
                "asshole","twat","cunt"};
        text = text.toLowerCase();
        for (String keyword : ruleKeywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void showGuidelinesViolationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Guidelines Violation");
        builder.setMessage("Your post violates our Community Guidelines. We are committed to maintaining a respectful and inclusive environment. " +
                "\n• Bullying\n• Harassment\n• Use of inappropriate language \nare not tolerated here. Please ensure that your posts are respectful and adhere to our guidelines. Thank you for helping us create a positive community.");
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
