package com.example.neighborhood;

public class Report {
    private String reportId;
    private String reporterId;
    private String postId;
    private String reason;
    private long timestamp;

    public Report() {
        // Default constructor required for Firebase
    }

    public Report(String reportId, String reporterId, String postId, String reason, long timestamp) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.postId = postId;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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
