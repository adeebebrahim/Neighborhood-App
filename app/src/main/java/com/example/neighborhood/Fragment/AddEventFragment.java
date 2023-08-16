package com.example.neighborhood.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.neighborhood.Event;
import com.example.neighborhood.R;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEventFragment extends Fragment {

    private EditText eventTitleEditText, eventDateEditText, eventTimeEditText, eventDescriptionEditText;
    private ImageView profileImageView;
    private Button btnCancel;
    private DatabaseReference eventsRef;

    private final Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private String userId;
    private String userProfileImageUrl;

    public AddEventFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get a reference to the "Events" node in the Firebase Realtime Database
        eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");

        // Retrieve the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserProfileImage();
        }

        // Initialize UI components
        eventTitleEditText = view.findViewById(R.id.event_title_edit_text);
        eventDateEditText = view.findViewById(R.id.event_date_edit_text);
        eventTimeEditText = view.findViewById(R.id.event_time_edit_text);
        eventDescriptionEditText = view.findViewById(R.id.event_description_edit_text);
        Button postButton = view.findViewById(R.id.post_button);
        profileImageView = view.findViewById(R.id.profile_picture);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set up event listeners for date and time pickers
        eventDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        eventTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        // Set up click listeners for "Post" and "Cancel" buttons
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postEvent();
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
        // Get a reference to the user's profile image in the Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve user data from the snapshot
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.getImage() != null && !user.getImage().isEmpty()) {
                        // Load the user's profile image using Glide library
                        RequestOptions requestOptions = new RequestOptions().transform(new CircleCrop());
                        Glide.with(requireContext())
                                .load(user.getImage())
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImageView);
                    } else {
                        // Load default profile image if user has no image
                        Glide.with(requireContext()).load(R.drawable.ic_profile).into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void showDatePicker() {
        // Create and show a DatePickerDialog to pick a date
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        eventDateEditText.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        // Create and show a TimePickerDialog to pick a time
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        eventTimeEditText.setText(timeFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void postEvent() {
        // Get event details from UI components
        String eventTitle = eventTitleEditText.getText().toString();
        String eventDate = eventDateEditText.getText().toString();
        String eventTime = eventTimeEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();

        // Check if all fields are filled
        if (!eventTitle.isEmpty() && !eventDate.isEmpty() && !eventTime.isEmpty() && !eventDescription.isEmpty()) {
            // Generate a unique event ID and get current timestamp
            String eventId = eventsRef.push().getKey();
            long timestamp = System.currentTimeMillis();

            // Create a new Event object
            Event newEvent = new Event(eventId, eventTitle, eventDate, eventTime, eventDescription, userId, timestamp);

            // Add the event to the Firebase Realtime Database
            eventsRef.child(eventId).setValue(newEvent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Clear input fields
                            eventTitleEditText.getText().clear();
                            eventDateEditText.getText().clear();
                            eventTimeEditText.getText().clear();
                            eventDescriptionEditText.getText().clear();

                            // Navigate back to the EventFragment
                            getParentFragmentManager().popBackStack();

                            // Log successful event posting
                            Log.d("AddEventFragment", "Event posted successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Log event posting failure
                            Log.e("AddEventFragment", "Failed to post event: " + e.getMessage());
                        }
                    });
        }
    }
}
