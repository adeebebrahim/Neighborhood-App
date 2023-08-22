package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.neighborhood.Adapter.EventAdapter;
import com.example.neighborhood.Event;
import com.example.neighborhood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventFragment extends Fragment {

    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private Button addButton;

    private DatabaseReference eventsRef;

    public EventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_event, container, false);

        // Get a reference to the "Events" node in the Firebase Realtime Database
        eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");

        // Initialize RecyclerView and Adapter
        eventRecyclerView = rootView.findViewById(R.id.event_recycler_view);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(new ArrayList<>(), requireContext());
        eventRecyclerView.setAdapter(eventAdapter);

        // Initialize SwipeRefreshLayout
        SwipeRefreshLayout swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh the events
                loadEventsFromFirebase();
                swipeRefreshLayout.setRefreshing(false); // Hide the refresh indicator
            }
        });

        // Load events from Firebase and populate the adapter
        loadEventsFromFirebase();

        // Initialize and set up the "Add" button
        addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with AddEventFragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new AddEventFragment());
                transaction.addToBackStack(null); // This line allows the user to navigate back to EventFragment
                transaction.commit();
            }
        });

        return rootView;
    }

    // Method to load events from Firebase and populate the adapter
    private void loadEventsFromFirebase() {
        long currentTimeMillis = System.currentTimeMillis();
        // Order events by timestamp in descending order (newest first)
        eventsRef.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> eventList = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

                // Loop through the dataSnapshot to get all events and add them to the eventList
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        try {
                            Date eventDateTime = dateFormat.parse(event.getDate() + " " + event.getTime());
                            // Check if the event's date and time are in the future
                            if (eventDateTime != null && eventDateTime.getTime() >= currentTimeMillis) {
                                eventList.add(event);
                            } else {
                                // Delete the event from the Firebase Realtime Database
                                eventsRef.child(event.getEventId()).removeValue();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Reverse the list to get the newest events first
                Collections.reverse(eventList);

                // Set the eventList to the adapter
                eventAdapter.setEventList(eventList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur while fetching data
            }
        });
    }
}
