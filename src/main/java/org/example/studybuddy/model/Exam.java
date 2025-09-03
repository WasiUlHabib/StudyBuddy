package org.example.studybuddy.model;

import java.time.LocalDateTime;
import java.util.List;

public class Exam {
    private int id;
    private int userId;
    private String name;
    private int timeLimit; // in minutes
    private boolean negativeMarks;
    private int totalQuestions;
    private LocalDateTime createdAt;
    private List<Question> questions;  // Constructors
    public Exam() {}

    public Exam(int userId, String name, int timeLimit, boolean negativeMarks, int totalQuestions) {
        this.userId = userId;
        this.name = name;
        this.timeLimit = timeLimit;
        this.negativeMarks = negativeMarks;
        this.totalQuestions = totalQuestions;      this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }

    public boolean isNegativeMarks() { return negativeMarks; }
    public void setNegativeMarks(boolean negativeMarks) { this.negativeMarks = negativeMarks; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    @Override
    public String toString() {
        return "Exam{" +
                "name='" + name + '\'' +
                ", timeLimit=" + timeLimit +
                ", totalQuestions=" + totalQuestions + '}';
    }
}
