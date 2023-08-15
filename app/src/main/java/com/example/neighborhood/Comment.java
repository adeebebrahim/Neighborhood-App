package com.example.neighborhood;

public class Comment {
    private String userId;
    private String postId;
    private String commentId;
    private long timestamp;
    private String text;

    public Comment() {
        // Empty constructor required for Firebase Realtime Database
    }

    public Comment(String userId, String postId, String commentId, long timestamp, String text) {
        this.userId = userId;
        this.postId = postId;
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
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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
