package com.example.neighborhood;

public class Event {
    private String eventId;
    private String title;
    private String date;
    private String time;
    private String description;
    private String userId;
    private long timestamp;

    public Event() {
    }

    public Event(String eventId, String title, String date, String time, String description, String userId, long timestamp) {
        this.eventId = eventId;
        this.title = title;
        this.date = date;
        this.time = time;
        this.description = description;
        this.userId = userId;
        this.timestamp = timestamp;
    }

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
