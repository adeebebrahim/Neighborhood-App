package com.example.neighborhood.Adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Event;
import com.example.neighborhood.R;
import com.example.neighborhood.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private DatabaseReference usersRef; // Reference to the "Users" node in Firebase

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Fetch the user information from Firebase based on userId
        String userId = event.getUserId();
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set user profile picture
                    if (user.getImage() != null && !user.getImage().isEmpty()) {
                        Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_profile).into(holder.profileImageView);
                    } else {
                        Picasso.get().load(R.drawable.ic_profile).into(holder.profileImageView);
                    }
                    // Set user name and username
                    holder.nameTextView.setText(user.getName());
                    holder.usernameTextView.setText("@" + user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });

        // Set event data such as title, date, time, and description
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText("Date: " + event.getDate());
        holder.timeTextView.setText("Time: " + event.getTime());
        holder.descriptionTextView.setText(event.getDescription());

        // Handle other event-related data if needed
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView usernameTextView;
        TextView titleTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView descriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view); // Assuming you have these TextViews in your item_event layout
            usernameTextView = itemView.findViewById(R.id.username_text_view); // Assuming you have these TextViews in your item_event layout
            titleTextView = itemView.findViewById(R.id.title_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
        }
    }
}
