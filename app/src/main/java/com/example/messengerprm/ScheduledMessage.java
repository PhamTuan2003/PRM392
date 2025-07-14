package com.example.messengerprm;

public class ScheduledMessage {
    private String messageId;
    private String message;
    private String senderId;
    private String receiverId;
    private String roomId;
    private String type;
    private String imageUrl;
    private long scheduledTime;
    private long createdAt;
    private boolean isSent;
    private String status;

    public ScheduledMessage() {
        // Required empty constructor for Firebase
    }

    public ScheduledMessage(String message, String senderId, String receiverId, String roomId, 
                          String type, long scheduledTime) {
        // Validate input parameters
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender ID cannot be null or empty");
        }
        if (receiverId == null || receiverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver ID cannot be null or empty");
        }
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        if (scheduledTime <= 0) {
            throw new IllegalArgumentException("Scheduled time must be positive");
        }
        
        this.message = message.trim();
        this.senderId = senderId.trim();
        this.receiverId = receiverId.trim();
        this.roomId = roomId.trim();
        this.type = type.trim();
        this.scheduledTime = scheduledTime;
        this.createdAt = System.currentTimeMillis();
        this.isSent = false;
        this.status = "pending";
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 