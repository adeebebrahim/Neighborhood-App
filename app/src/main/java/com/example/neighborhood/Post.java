package com.example.neighborhood;

public class Post {
    private String userName;
    private String userUsername;
    private String postText; // Add postText field
    private String postId; // Add postId field
    private String imageUrl;
    private long timestamp;
    private String userId;

    // Constructors, getters, and setters

    public Post() {
        // Empty constructor required for Firebase Realtime Database
    }

    public Post(String userName, String userUsername, String postText, String postId, String imageUrl, long timestamp, String userId) {
        this.userName = userName;
        this.userUsername = userUsername;
        this.postText = postText;
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
