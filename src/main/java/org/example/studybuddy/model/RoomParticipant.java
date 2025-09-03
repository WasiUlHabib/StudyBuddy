package org.example.studybuddy.model;

import java.time.LocalDateTime;

public class RoomParticipant {
    private int id;
    private int roomId;
    private int userId;
    private String username;
    private String role; // 'admin', 'moderator', 'member'
    private LocalDateTime joinedAt;
    private boolean isActive;

    // Constructors
    public RoomParticipant() {}

    public RoomParticipant(int roomId, int userId, String role) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
        this.isActive = true;
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isAdmin() { return "admin".equals(role); }
    public boolean isModerator() { return "moderator".equals(role); }
    public boolean isMember() { return "member".equals(role); }
}
