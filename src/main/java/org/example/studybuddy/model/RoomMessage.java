package org.example.studybuddy.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RoomMessage {
    private int id;
    private int roomId;
    private int userId;
    private String username;
    private String message;
    private String messageType; // 'text', 'system', 'file'
    private LocalDateTime sentAt;

    // Constructors
    public RoomMessage() {}

    public RoomMessage(int roomId, int userId, String username, String message, String messageType) {
        this.roomId = roomId;
        this.userId = userId;
        this.username = username;
        this.message = message;
        this.messageType = messageType;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getFormattedTime() {
        return sentAt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getDisplayText() {
        if ("system".equals(messageType)) {
            return String.format("[%s] %s", getFormattedTime(), message);
        } else {
            return String.format("[%s] %s: %s", getFormattedTime(), username, message);
        }
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
}
