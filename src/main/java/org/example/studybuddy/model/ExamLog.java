package org.example.studybuddy.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExamLog {
    private int id;
    private int userId;
    private String examName;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int unanswered;
    private double score;
    private double percentage;
    private int timeTaken; // in seconds
    private double difficultyAvg;    // ADDED - Missing property
    private String topicsCovered;    // ADDED - Missing property
    private LocalDateTime completedAt;
    private String examType;
    private Integer roomId;          // ADDED - Missing property

    // Default constructor
    public ExamLog() {
        this.difficultyAvg = 0.0;
        this.topicsCovered = "";
        this.roomId = null;
    }

    // Simple constructor (most commonly used)
    public ExamLog(int userId, String examName, int totalQuestions, int correctAnswers,
                   int wrongAnswers, int unanswered, double score, int timeTaken) {
        this.userId = userId;
        this.examName = examName;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unanswered = unanswered;
        this.score = score;
        this.percentage = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
        this.timeTaken = timeTaken;
        this.completedAt = LocalDateTime.now();
        this.examType = "practice";
        this.difficultyAvg = 0.0;     // Default value
        this.topicsCovered = "";      // Default value
        this.roomId = null;           // Default value
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getUnanswered() { return unanswered; }
    public void setUnanswered(int unanswered) { this.unanswered = unanswered; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }

    // ADDED - Missing getter/setter methods
    public double getDifficultyAvg() { return difficultyAvg; }
    public void setDifficultyAvg(double difficultyAvg) { this.difficultyAvg = difficultyAvg; }

    public String getTopicsCovered() { return topicsCovered; }
    public void setTopicsCovered(String topicsCovered) { this.topicsCovered = topicsCovered; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    // Helper methods
    public String getFormattedTime() {
        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFormattedDate() {
        return completedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getGrade() {
        if (percentage >= 90) return "A+";
        else if (percentage >= 80) return "A";
        else if (percentage >= 70) return "B";
        else if (percentage >= 60) return "C";
        else if (percentage >= 50) return "D";
        else return "F";
    }

    @Override
    public String toString() {
        return String.format("ExamLog{examName='%s', score=%.1f, percentage=%.1f%%}",
                examName, score, percentage);
    }
}
