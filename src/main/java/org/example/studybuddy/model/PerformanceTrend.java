package org.example.studybuddy.model;

public class PerformanceTrend {
    private String date;
    private int examsCount;
    private double averagePercentage;
    private int correctAnswers;
    private int totalQuestions;

    // Constructors
    public PerformanceTrend() {}

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getExamsCount() { return examsCount; }
    public void setExamsCount(int examsCount) { this.examsCount = examsCount; }

    public double getAveragePercentage() { return averagePercentage; }
    public void setAveragePercentage(double averagePercentage) { this.averagePercentage = averagePercentage; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public double getAccuracy() {
        return totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
    }
}
