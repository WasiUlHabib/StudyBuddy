package org.example.studybuddy.model;

import java.time.LocalDateTime;

public class ExamResult {
    private int id;
    private int examId;
    private int userId;
    private double score;
    private int totalQuestions;
    private int correctAnswers;
    private int wrongAnswers;
    private int unanswered;
    private int timeTaken; // in seconds
    private LocalDateTime completedAt;

    // Constructors
    public ExamResult() {}

    public ExamResult(int examId, int userId, double score,int totalQuestions,
                      int correctAnswers, int wrongAnswers, int unanswered,int timeTaken) {
        this.examId = examId;
        this.userId = userId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;     this.wrongAnswers = wrongAnswers;
        this.unanswered = unanswered;
        this.timeTaken = timeTaken;
        this.completedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getWrongAnswers() { return wrongAnswers; }
    public void setWrongAnswers(int wrongAnswers) { this.wrongAnswers = wrongAnswers; }

    public int getUnanswered() { return unanswered; }
    public void setUnanswered(int unanswered) { this.unanswered = unanswered; }

    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public double getAccuracyPercentage() {
        return totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0.0;
    }

    public String getFormattedTime() {
        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public double getPercentage() {
        if (totalQuestions == 0) {
            return 0.0;
        }
        double percentage = ((double) correctAnswers / totalQuestions) * 100;
        return Math.round(percentage * 100.0) / 100.0; // Round to 2 decimal places
    }

}
