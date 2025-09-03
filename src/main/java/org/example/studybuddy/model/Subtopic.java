package org.example.studybuddy.model;

import java.time.LocalDateTime;

public class Subtopic {
    private int id;
    private String name;
    private int topicId;
    private String description;
    private int createdBy;
    private LocalDateTime createdAt;

    // Constructors
    public Subtopic() {}

    public Subtopic(String name, int topicId, String description, int createdBy) {
        this.name = name;
        this.topicId = topicId;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public Subtopic(int id, String name, int topicId, String description, int createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.topicId = topicId;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name; // For ComboBox display
    }
}
