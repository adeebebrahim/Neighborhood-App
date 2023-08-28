package com.example.neighborhood;

public class Report {
    private String reportId;
    private String userId;
    private String postId;
    private String commentId; // Add this field for comments
    private String reason;
    private long timestamp;

    public Report() {
        // Default constructor required for Firebase
    }

    // Constructor for reporting posts
    public Report(String reportId, String userId, String postId, String reason, long timestamp) {
        this.reportId = reportId;
        this.userId = userId;
        this.postId = postId;
        this.reason = reason;
        this.timestamp = timestamp;
        this.commentId = null; // Set commentId as null for reporting posts
    }

    // Constructor for reporting comments
    public Report(String reportId, String userId, String postId, String commentId, String reason, long timestamp) {
        this.reportId = reportId;
        this.userId = userId;
        this.postId = postId;
        this.commentId = commentId;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getuserId() {
        return userId;
    }

    public void setuserId(String reporterId) {
        this.userId = reporterId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getcommentId() {
        return commentId;
    }

    public void setcommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
