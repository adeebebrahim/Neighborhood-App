package com.example.neighborhood.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.neighborhood.R;
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
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 400;

    private EditText editTextName;
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextMobileno;
    private EditText editTextBio;
    private Button btnSave;
    private ImageView profileImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseUsers;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri imageUri;

    //arrays of permissions to be requested
    String[] cameraPermissions;
    String[] storagePermissions;

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

        //init arrays of permissions
        cameraPermissions = new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        editTextName = rootView.findViewById(R.id.editTextName);
        editTextUsername = rootView.findViewById(R.id.editTextUsername);
        editTextBio = rootView.findViewById(R.id.editTextBio);
        editTextEmail = rootView.findViewById(R.id.editTextEmail);
        editTextMobileno = rootView.findViewById(R.id.editTextPhone);
        btnSave = rootView.findViewById(R.id.btnSave);
        profileImageView = rootView.findViewById(R.id.profileImageView);

        // Fetch the user data from Firebase and populate the EditText fields
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

                        // Load the profile image using Glide
                        Glide.with(requireContext())
                                .load(profileImage)
                                .into(profileImageView);
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
                showImagePicDialog();
            }
        });

        return rootView;
    }

    private void updateProfile() {
        // Get the updated values from the EditText fields
        String name = editTextName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String mobile = editTextMobileno.getText().toString().trim();

        // Update the user data in the Firebase Realtime Database
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

    private void showImagePicDialog() {
        // Show dialog containing options Camera and Gallery to pick the image
        String options[] = {"Camera", "Gallery"};

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle dialog item clicks
                if (which == 0) {
                    // Camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    // Gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        // Show the dialog
        builder.create().show();
    }

    private boolean checkCameraPermission() {
        // Check if camera permission is granted or not
        int cameraPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        // Request camera permission
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        // Check if storage permission is granted or not
        int storagePermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        // Request storage permission
        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }

    private void pickFromCamera() {
        // Pick image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery() {
        // Pick image from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() throws IOException {
        if (imageUri != null) {
            StorageReference imageRef = storageReference.child("profile_images").child(currentUser.getUid() + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    // Update the user's profile image URL in the Firebase Realtime Database
                                    if (currentUser != null) {
                                        String userId = currentUser.getUid();
                                        HashMap<String, Object> updates = new HashMap<>();
                                        updates.put("image", imageUrl);

                                        databaseUsers.child(userId).updateChildren(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Glide.with(requireContext())
                                                                .load(imageUrl)
                                                                .into(profileImageView);
                                                        Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(requireContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle image upload failure
                        }
                    });
        }
    }
}
