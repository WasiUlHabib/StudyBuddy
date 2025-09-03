package org.example.studybuddy.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExamResult {
    private int id;
    private int examId;
    private int userId;
    private String examName; // NEW: Added for room-based exams
    private double score;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int unanswered;
    private int timeTaken; // in seconds
    private LocalDateTime completedAt;
    private double percentage; // NEW: Added backing field for percentage

    // Constructors
    public ExamResult() {}

    public ExamResult(int examId, int userId, double score, int totalQuestions,
                      int correctAnswers, int wrongAnswers, int unanswered, int timeTaken) {
        this.examId = examId;
        this.userId = userId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unanswered = unanswered;
        this.timeTaken = timeTaken;
        this.completedAt = LocalDateTime.now();
        this.percentage = calculatePercentage(); // Auto-calculate percentage
    }

    // Full constructor
    public ExamResult(int id, int examId, int userId, String examName, double score,
                      int totalQuestions, int correctAnswers, int wrongAnswers,
                      int unanswered, int timeTaken, LocalDateTime completedAt, double percentage) {
        this.id = id;
        this.examId = examId;
        this.userId = userId;
        this.examName = examName;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unanswered = unanswered;
        this.timeTaken = timeTaken;
        this.completedAt = completedAt;
        this.percentage = percentage;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    // NEW: Exam name getter and setter
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        // Recalculate percentage when total questions change
        if (this.correctAnswers > 0) {
            this.percentage = calculatePercentage();
        }
    }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
        // Recalculate percentage when correct answers change
        if (this.totalQuestions > 0) {
            this.percentage = calculatePercentage();
        }
    }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getUnanswered() { return unanswered; }
    public void setUnanswered(int unanswered) { this.unanswered = unanswered; }

    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // FIXED: Now properly stores and retrieves percentage
    public double getPercentage() {
        // If percentage is not set, calculate it
        if (percentage == 0.0 && totalQuestions > 0) {
            percentage = calculatePercentage();
        }
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = Math.round(percentage * 100.0) / 100.0; // Round to 2 decimal places
    }

    // Calculate percentage from correct answers and total questions
    private double calculatePercentage() {
        if (totalQuestions == 0) {
            return 0.0;
        }
        double calculated = ((double) correctAnswers / totalQuestions) * 100;
        return Math.round(calculated * 100.0) / 100.0; // Round to 2 decimal places
    }

    // Alias for getPercentage() for backward compatibility
    public double getAccuracyPercentage() {
        return getPercentage();
    }

    // Format time taken as MM:SS
    public String getFormattedTime() {
        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Format completed date for display
    public String getFormattedDate() {
        if (completedAt != null) {
            return completedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"));
        }
        return "Unknown";
    }

    // Calculate accuracy as decimal (0.0 to 1.0)
    public double getAccuracy() {
        if (totalQuestions == 0) {
            return 0.0;
        }
        return (double) correctAnswers / totalQuestions;
    }

    // Get completion status
    public String getCompletionStatus() {
        if (correctAnswers + wrongAnswers + unanswered != totalQuestions) {
            return "Incomplete";
        } else if (unanswered > 0) {
            return "Partial";
        } else {
            return "Complete";
        }
    }

    // Get performance grade based on percentage
    public String getGrade() {
        double percent = getPercentage();
        if (percent >= 90) return "A+";
        else if (percent >= 85) return "A";
        else if (percent >= 80) return "A-";
        else if (percent >= 75) return "B+";
        else if (percent >= 70) return "B";
        else if (percent >= 65) return "B-";
        else if (percent >= 60) return "C+";
        else if (percent >= 55) return "C";
        else if (percent >= 50) return "C-";
        else if (percent >= 40) return "D";
        else return "F";
    }

    @Override
    public String toString() {
        return String.format("ExamResult{id=%d, examName='%s', score=%.1f, accuracy=%.1f%%, completed=%s}",
                id, examName, score, getPercentage(), getFormattedDate());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExamResult that = (ExamResult) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
