package org.example.studybuddy.model;

import java.time.LocalDate;

public class DailyStat {
    private int id;
    private int userId;
    private LocalDate date;
    private int questionsAttempted;
    private int questionsSolved;
    private int examsTaken;
    private double accuracyPercentage;

    // Constructors
    public DailyStat() {}

    public DailyStat(int userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
        this.questionsAttempted = 0;
        this.questionsSolved = 0;
        this.examsTaken = 0;
        this.accuracyPercentage = 0.0;
    }

    public DailyStat(int userId, LocalDate date, int questionsAttempted, int questionsSolved, int examsTaken) {
        this.userId = userId;
        this.date = date;
        this.questionsAttempted = questionsAttempted;
        this.questionsSolved = questionsSolved;
        this.examsTaken = examsTaken;
        this.accuracyPercentage = questionsAttempted > 0 ? (double) questionsSolved / questionsAttempted * 100 : 0.0;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getQuestionsAttempted() {
        return questionsAttempted;
    }

    public void setQuestionsAttempted(int questionsAttempted) {
        this.questionsAttempted = questionsAttempted;
        updateAccuracy();
    }

    public int getQuestionsSolved() {
        return questionsSolved;
    }

    public void setQuestionsSolved(int questionsSolved) {
        this.questionsSolved = questionsSolved;
        updateAccuracy();
    }

    public int getExamsTaken() {
        return examsTaken;
    }

    public void setExamsTaken(int examsTaken) {
        this.examsTaken = examsTaken;
    }

    public double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    private void updateAccuracy() {
        this.accuracyPercentage = questionsAttempted > 0 ? (double) questionsSolved / questionsAttempted * 100 : 0.0;
    }

    @Override
    public String toString() {
        return "DailyStat{" +
                "date=" + date +
                ", questionsAttempted=" + questionsAttempted +
                ", questionsSolved=" + questionsSolved +
                ", accuracyPercentage=" + String.format("%.1f", accuracyPercentage) + "%" +
                '}';
    }
}
