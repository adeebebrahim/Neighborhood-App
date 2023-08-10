package com.example.neighborhood;

public class CommunityPost {
    private String userId;
    private String topic;
    private String description;

    // Empty constructor for Firebase
    public CommunityPost() {
    }

    public CommunityPost(String userId, String topic, String description) {
        this.userId = userId;
        this.topic = topic;
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Add this method to get the user
    public String getUser() {
        return userId;
    }
}
