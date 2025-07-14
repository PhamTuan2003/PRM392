package com.example.messengerprm;

public class msgModelclass {
    String message;
    String senderId;
    long timestamp;
    String imageUrl;
    String type;
    String messageKey;

    public msgModelclass() {
    }

    public msgModelclass(String message, String senderId, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = "text";
    }

    public msgModelclass(String message, String senderId, long timestamp, String imageUrl, String type) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
        this.type = type;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
}
