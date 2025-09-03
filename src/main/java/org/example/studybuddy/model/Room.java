package org.example.studybuddy.model;

import java.time.LocalDateTime;
import java.util.List;

public class Room {
    private int id;
    private String name;
    private String description;
    private String roomCode;
    private int createdBy;
    private int maxParticipants;
    private boolean isActive;
    private LocalDateTime createdAt;
    private List<User> participants;
    private int participantCount;

    // Constructors
    public Room() {}

    public Room(String name, String description, String roomCode, int createdBy, int maxParticipants) {
        this.name = name;
        this.description = description;
        this.roomCode = roomCode;
        this.createdBy = createdBy;
        this.maxParticipants = maxParticipants;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<User> getParticipants() { return participants; }
    public void setParticipants(List<User> participants) { this.participants = participants; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public boolean isFull() {
        return participantCount >= maxParticipants;
    }

    @Override
    public String toString() {
        return name + " (" + roomCode + ")";
    }
}
