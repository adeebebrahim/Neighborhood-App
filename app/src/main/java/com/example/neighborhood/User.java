package com.example.neighborhood;

import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String mobileNo;
    private String dateOfBirth;
    private String gender;
    private String bio;
    private int followerCount;
    private int followingCount;
    private String username;
    private boolean followStatus; // Add the followStatus field to track if the current user is following this user
    private String image; // Add the image field for the profile image URL
    private String phone; // Add the phone field for the phone number
    private Map<String, Boolean> followers; // To store followers as key-value pairs (userUid -> true)
    private Map<String, Boolean> following; // To store following users as key-value pairs (userUid -> true)

    public User() {
        // Empty constructor required for Firebase Realtime Database
    }

    public User(String userId, String name, String email, String mobileNo, String dateOfBirth, String gender, String bio, int followerCount, int followingCount, String username, String image, String phone, Map<String, Boolean> followers, Map<String, Boolean> following) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobileNo = mobileNo;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bio = bio;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.username = username;
        this.followStatus = false; // Initialize followStatus as false (not following) by default
        this.image = image;
        this.phone = phone;
        this.followers = followers;
        this.following = following;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(boolean followStatus) {
        this.followStatus = followStatus;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, Boolean> getFollowers() {
        return followers;
    }

    public void setFollowers(Map<String, Boolean> followers) {
        this.followers = followers;
    }

    public Map<String, Boolean> getFollowing() {
        return following;
    }

    public void setFollowing(Map<String, Boolean> following) {
        this.following = following;
    }
}
