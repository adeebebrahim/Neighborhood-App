package com.example.neighborhood;

public class Event {
    private String title;
    private String date;
    private String time;
    private String description;
    private String userId; // User ID who posted the event

    public Event() {
        // Default constructor required for Firebase
    }

    public Event(String title, String date, String time, String description, String userId) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.description = description;
        this.userId = userId;
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
}
