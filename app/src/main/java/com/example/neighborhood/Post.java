package com.example.neighborhood;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Post implements Parcelable {
    private String userName;
    private String userUsername;
    private String postText;
    private String postId;
    private String imageUrl;
    private long timestamp;
    private String userId;
    private List<String> likedByUsers;

    public Post() {

        likedByUsers = new ArrayList<>();
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


    public List<String> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<String> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }


    protected Post(Parcel in) {
        userName = in.readString();
        userUsername = in.readString();
        postText = in.readString();
        postId = in.readString();
        imageUrl = in.readString();
        timestamp = in.readLong();
        userId = in.readString();
        likedByUsers = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(userUsername);
        dest.writeString(postText);
        dest.writeString(postId);
        dest.writeString(imageUrl);
        dest.writeLong(timestamp);
        dest.writeString(userId);
        dest.writeStringList(likedByUsers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };


}
