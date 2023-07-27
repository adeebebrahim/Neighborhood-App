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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddFragment extends Fragment {

    private EditText editText;
    private ImageView addImageIcon;
    private Button btnCancel, btnPost;

    private Uri imageUri;
    private ProgressDialog progressDialog;
    private String currentUserId;

    public AddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add, container, false);

        // Find views within the fragment
        editText = rootView.findViewById(R.id.edit_text);
        addImageIcon = rootView.findViewById(R.id.add_image_icon);
        btnCancel = rootView.findViewById(R.id.btn_cancel);
        btnPost = rootView.findViewById(R.id.btn_post);

        progressDialog = new ProgressDialog(getActivity());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // Set click listener for the cancel button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        // Set click listener for the post button
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postText = editText.getText().toString().trim();

                if (postText.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter something", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.setMessage("Posting...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Check if an image is selected
                if (imageUri != null) {
                    // Upload the image to Firebase Storage
                    uploadImageAndSavePost(postText);
                } else {
                    // No image selected, just save the post without the image
                    savePostToFirebase(postText, null);
                }
            }
        });

        // Set click listener for the addImageIcon to select an image from the gallery
        addImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        return rootView;
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
            addImageIcon.setImageURI(imageUri);
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
}
