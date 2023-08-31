package com.example.neighborhood;

public class Message {
    private String messageId; // New field for message ID
    private String messageText;
    private String senderUserId;
    private String recipientUserId;

    // Default constructor for Firebase
    public Message() {
    }

    public Message(String messageText, String senderUserId, String recipientUserId) {
        this.messageText = messageText;
        this.senderUserId = senderUserId;
        this.recipientUserId = recipientUserId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
}
