package com.example.neighborhood.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Login;
import com.example.neighborhood.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProfileFragment extends Fragment {

    private EditText editTextName;
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextMobileno;
    private EditText editTextBio;
    private Button btnSave;
    private Button btnChangePassword;
    private ImageView profileImageView;
    private Button btnDeleteAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseUsers;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        editTextName = rootView.findViewById(R.id.editTextName);
        editTextUsername = rootView.findViewById(R.id.editTextUsername);
        editTextBio = rootView.findViewById(R.id.editTextBio);
        editTextEmail = rootView.findViewById(R.id.editTextEmail);
        editTextMobileno = rootView.findViewById(R.id.editTextPhone);
        btnSave = rootView.findViewById(R.id.btnSave);
        btnChangePassword = rootView.findViewById(R.id.btnChangePassword);
        profileImageView = rootView.findViewById(R.id.profileImageView);
        btnDeleteAccount = rootView.findViewById(R.id.btnDeleteAccount);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String bio = dataSnapshot.child("bio").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String mobileNo = dataSnapshot.child("mobileNo").getValue(String.class);
                        String profileImage = dataSnapshot.child("image").getValue(String.class);

                        editTextName.setText(name);
                        editTextUsername.setText(username);
                        editTextBio.setText(bio);
                        editTextEmail.setText(email);
                        editTextMobileno.setText(mobileNo);

//                        Glide.with(requireContext())
//                                .load(profileImage)
//                                .into(profileImageView);

                        // Load the profile image using Glide and apply circular cropping
                        if (profileImage != null && !profileImage.isEmpty()) {
                            RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                            Glide.with(requireContext())
                                    .load(profileImage)
                                    .apply(requestOptions)
                                    .into(profileImageView);
                        } else {
                            Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle database read error if needed
                }
            });
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSelectionDialog();
            }
        });

        btnChangePassword = rootView.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountConfirmationDialog();
            }
        });

        return rootView;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Change Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText oldPasswordEditText = view.findViewById(R.id.oldPasswordEditText);
        EditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        CheckBox showPasswordCheckBox = view.findViewById(R.id.showPasswordCheckBox);

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int inputType = isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                newPasswordEditText.setInputType(inputType);
            }
        });

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String oldPassword = oldPasswordEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();

                if (currentUser != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
                    currentUser.reauthenticate(credential)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    currentUser.updatePassword(newPassword)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), "Authentication failed. Check your old password.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose an option");
        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openCamera();
                } else if (which == 1) {
                    openGallery();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                selectedImageUri = data.getData();
                updateProfilePicture();
            } else if (requestCode == REQUEST_GALLERY) {
                selectedImageUri = data.getData();
                updateProfilePicture();
            }
        }
    }

    private void updateProfilePicture() {
        if (selectedImageUri != null) {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setTitle("Uploading profile picture...");
            progressDialog.show();

            StorageReference storageRef = storage.getReference();
            StorageReference profileImageRef = storageRef.child("profile_pictures").child(currentUser.getUid() + ".jpg");

            profileImageRef.putFile(selectedImageUri)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return profileImageRef.getDownloadUrl();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                if (downloadUri != null) {
                                    DatabaseReference userRef = databaseUsers.child(currentUser.getUid());
                                    userRef.child("image").setValue(downloadUri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Load the updated profile picture with circular cropping
                                                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                                                        Glide.with(requireContext())
                                                                .load(downloadUri)
                                                                .apply(requestOptions)
                                                                .into(profileImageView);

                                                        Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                                    }

                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(requireContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                    if (progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                            }
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateProfile() {
        String name = editTextName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String mobile = editTextMobileno.getText().toString().trim();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("username", username);
            updates.put("bio", bio);
            updates.put("email", email);
            updates.put("mobileNo", mobile);

            databaseUsers.child(userId).updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(requireContext());
        confirmDialogBuilder.setTitle("Delete Account");
        confirmDialogBuilder.setMessage("Are you sure you want to delete your account?");
        confirmDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPasswordVerificationDialog();
            }
        });
        confirmDialogBuilder.setNegativeButton("No", null);
        confirmDialogBuilder.show();
    }

    private void showPasswordVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Verify Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_verify_password, null);
        builder.setView(view);

        EditText passwordEditText = view.findViewById(R.id.passwordEditText);

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = passwordEditText.getText().toString().trim();

                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), enteredPassword);
                currentUser.reauthenticate(credential)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                deleteUserAccount();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(requireContext(), "Password verification failed.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void deleteUserAccount() {
        DatabaseReference userRef = databaseUsers.child(currentUser.getUid());
        // Delete user's profile data
        userRef.removeValue();

        // Delete user's posts and its comments and image
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        Query userPostsQuery = postsRef.orderByChild("userId").equalTo(currentUser.getUid());
        userPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    DatabaseReference postRef = postsRef.child(postId);

                    // Delete post comments
                    DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
                    commentsRef.removeValue();

                    // Delete post image from storage if it exists
                    String imageUrl = postSnapshot.child("imageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Image deleted successfully
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure to delete image
                            }
                        });
                    }

                    // Delete post
                    postSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });

        // Delete user's events
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        Query userEventsQuery = eventsRef.orderByChild("userId").equalTo(currentUser.getUid());
        userEventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    String eventId = eventSnapshot.getKey();
                    DatabaseReference eventRef = eventsRef.child(eventId);
                    // Delete event
                    eventRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });

        // Delete user's comments in other users' posts
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments");
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    for (DataSnapshot commentSnapshot : postSnapshot.getChildren()) {
                        String commentId = commentSnapshot.getKey();
                        String userId = commentSnapshot.child("userId").getValue(String.class);

                        // Check if the comment belongs to the user being deleted
                        if (userId != null && userId.equals(currentUser.getUid())) {
                            // Delete the comment
                            commentSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });

        // Delete user's comments in other users' community posts
        DatabaseReference communityCommentsRef = FirebaseDatabase.getInstance().getReference("CommunityComments");
        communityCommentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String postId = postSnapshot.getKey();
                    for (DataSnapshot commentSnapshot : postSnapshot.getChildren()) {
                        String commentId = commentSnapshot.getKey();
                        String userId = commentSnapshot.child("userId").getValue(String.class);

                        // Check if the comment belongs to the user being deleted
                        if (userId != null && userId.equals(currentUser.getUid())) {
                            // Delete the comment
                            commentSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });


        // Delete user's community posts and associated comments
        DatabaseReference communityPostsRef = FirebaseDatabase.getInstance().getReference("CommunityPosts");
        Query userCommunityPostsQuery = communityPostsRef.orderByChild("userId").equalTo(currentUser.getUid());
        userCommunityPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot communityPostSnapshot : dataSnapshot.getChildren()) {
                    String postId = communityPostSnapshot.getKey();
                    DatabaseReference postRef = communityPostsRef.child(postId);

                    // Delete post comments
                    DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("CommunityComments").child(postId);
                    commentsRef.removeValue();

                    // Delete post
                    postRef.removeValue();

                    // Delete user's comments on other community posts
                    DatabaseReference userCommentsRef = FirebaseDatabase.getInstance().getReference("CommunityComments");
                    Query userCommentsQuery = userCommentsRef.orderByChild("userId").equalTo(currentUser.getUid());
                    userCommentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                                String commentId = commentSnapshot.getKey();
                                String commentPostId = commentSnapshot.child("postId").getValue(String.class);
                                if (!commentPostId.equals(postId)) {
                                    DatabaseReference commentRef = userCommentsRef.child(commentId);
                                    commentRef.removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error if needed
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });


        // Delete user's messages
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userIds = userSnapshot.getKey();
                    for (DataSnapshot messageSnapshot : userSnapshot.getChildren()) {
                        String messageId = messageSnapshot.getKey();
                        String senderUserId = messageSnapshot.child("senderUserId").getValue(String.class);
                        String recipientUserId = messageSnapshot.child("recipientUserId").getValue(String.class);

                        // Check if the sender or recipient is the user being deleted
                        if (senderUserId.equals(currentUser.getUid()) || recipientUserId.equals(currentUser.getUid())) {
                            // Delete the message
                            messageSnapshot.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });


        currentUser.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Account deleted successfully
                        // Redirect the user to the login screen or perform any other necessary action
                        Intent intent = new Intent(requireContext(), Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
