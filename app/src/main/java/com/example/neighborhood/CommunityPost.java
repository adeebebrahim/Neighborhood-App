package com.example.neighborhood;

import android.os.Parcel;
import android.os.Parcelable;

public class CommunityPost implements Parcelable {
    private String userId;
    private String topic;
    private String description;
    private String topicId;
    private long timestamp;

    public CommunityPost() {
        // Empty constructor for Firebase
    }

    public CommunityPost(String userId, String topic, String description, String topicId, long timestamp) {
        this.userId = userId;
        this.topic = topic;
        this.description = description;
        this.topicId = topicId;
        this.timestamp = timestamp;
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

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Parcelable implementation
    protected CommunityPost(Parcel in) {
        userId = in.readString();
        topic = in.readString();
        description = in.readString();
        topicId = in.readString();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(topic);
        dest.writeString(description);
        dest.writeString(topicId);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommunityPost> CREATOR = new Creator<CommunityPost>() {
        @Override
        public CommunityPost createFromParcel(Parcel in) {
            return new CommunityPost(in);
        }

        @Override
        public CommunityPost[] newArray(int size) {
            return new CommunityPost[size];
        }
    };

    // Add this method to get the user
    public String getUser() {
        return userId;
    }
}
