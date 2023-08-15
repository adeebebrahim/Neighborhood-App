package com.example.neighborhood;

public class Message {
    private String userId;
    private String profileImage;
    private String name;
    private String username;

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String userId, String profileImage, String name, String username) {
        this.userId = userId;
        this.profileImage = profileImage;
        this.name = name;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
}
