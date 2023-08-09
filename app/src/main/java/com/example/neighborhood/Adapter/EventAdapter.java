package com.example.neighborhood.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborhood.Event;
import com.example.neighborhood.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
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
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(event.getDate());
        holder.timeTextView.setText(event.getTime());
        holder.descriptionTextView.setText(event.getDescription());
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
        TextView titleTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView descriptionTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
        }
    }
}
