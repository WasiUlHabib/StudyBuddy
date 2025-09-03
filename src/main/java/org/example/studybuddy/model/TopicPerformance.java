package org.example.studybuddy.model;

import java.time.LocalDateTime;

public class TopicPerformance {
    private int id;
    private int userId;
    private int topicId;
    private String topicName;
    private int questionsAttempted;
    private int questionsSolved;
    private double accuracyPercentage;
    private LocalDateTime lastUpdated;

    // Constructors
    public TopicPerformance() {}

    public TopicPerformance(int userId, int topicId, String topicName) {
        this.userId = userId;
        this.topicId = topicId;
        this.topicName = topicName;
        this.questionsAttempted = 0;
        this.questionsSolved = 0;
        this.accuracyPercentage = 0.0;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTopicId() { return topicId; }
    public void setTopicId(int topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public int getQuestionsAttempted() { return questionsAttempted; }
    public void setQuestionsAttempted(int questionsAttempted) {
        this.questionsAttempted = questionsAttempted;
        updateAccuracy();
    }

    public int getQuestionsSolved() { return questionsSolved; }
    public void setQuestionsSolved(int questionsSolved) {
        this.questionsSolved = questionsSolved;
        updateAccuracy();
    }

    public double getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    private void updateAccuracy() {
        this.accuracyPercentage = questionsAttempted > 0 ?
                (double) questionsSolved / questionsAttempted * 100 : 0.0;
    }

    public String getGradeLevel() {
        if (accuracyPercentage >= 90) return "A+";
        else if (accuracyPercentage >= 80) return "A";
        else if (accuracyPercentage >= 70) return "B";
        else if (accuracyPercentage >= 60) return "C";
        else if (accuracyPercentage >= 50) return "D";
        else return "F";
    }
}
