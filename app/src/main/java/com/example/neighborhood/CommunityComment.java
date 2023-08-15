package com.example.neighborhood;

import java.util.List;

public class CommunityComment {
    private String userId;
    private String topicId;
    private String commentId;
    private long timestamp;
    private String text;

    public CommunityComment() {
        // Empty constructor required for Firebase Realtime Database
    }

    public CommunityComment(String userId, String topicId, String commentId, long timestamp, String text) {
        this.userId = userId;
        this.topicId = topicId;
        this.commentId = commentId;
        this.timestamp = timestamp;
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return topicId;
    }

    public void setPostId(String postId) {
        this.topicId = postId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
