package com.example.neighborhood;

public class Event {
    private String eventId; // Unique identifier for the event
    private String title;
    private String date;
    private String time;
    private String description;
    private String userId; // User ID who posted the event
    private long timestamp; // Timestamp of when the event was created

    // Default constructor required for Firebase
    public Event() {
    }

    // Constructor with all fields
    public Event(String eventId, String title, String date, String time, String description, String userId, long timestamp) {
        this.eventId = eventId;
        this.title = title;
        this.date = date;
        this.time = time;
        this.description = description;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getter and Setter methods for each field
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
