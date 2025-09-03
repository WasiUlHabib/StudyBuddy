package org.example.studybuddy.model;

public class UserStats {
    private int userId;
    private int totalExamsCompleted;
    private int totalQuestionsAttempted;
    private int totalCorrectAnswers;
    private int totalWrongAnswers;
    private int totalUnanswered;
    private double averageScore;
    private double highestScore;
    private int averageTimePerExam;
    private int personalExams;
    private int roomExams;

    // Default constructor with zeros
    public UserStats() {
        this.totalExamsCompleted = 0;
        this.totalQuestionsAttempted = 0;
        this.totalCorrectAnswers = 0;
        this.totalWrongAnswers = 0;
        this.totalUnanswered = 0;
        this.averageScore = 0.0;
        this.highestScore = 0.0;
        this.averageTimePerExam = 0;
        this.personalExams = 0;
        this.roomExams = 0;
    }

    // All getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTotalExamsCompleted() { return totalExamsCompleted; }
    public void setTotalExamsCompleted(int totalExamsCompleted) { this.totalExamsCompleted = totalExamsCompleted; }

    public int getTotalQuestionsAttempted() { return totalQuestionsAttempted; }
    public void setTotalQuestionsAttempted(int totalQuestionsAttempted) { this.totalQuestionsAttempted = totalQuestionsAttempted; }

    public int getTotalCorrectAnswers() { return totalCorrectAnswers; }
    public void setTotalCorrectAnswers(int totalCorrectAnswers) { this.totalCorrectAnswers = totalCorrectAnswers; }

    public int getTotalWrongAnswers() { return totalWrongAnswers; }
    public void setTotalWrongAnswers(int totalWrongAnswers) { this.totalWrongAnswers = totalWrongAnswers; }

    public int getTotalUnanswered() { return totalUnanswered; }
    public void setTotalUnanswered(int totalUnanswered) { this.totalUnanswered = totalUnanswered; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getHighestScore() { return highestScore; }
    public void setHighestScore(double highestScore) { this.highestScore = highestScore; }

    public int getAverageTimePerExam() { return averageTimePerExam; }
    public void setAverageTimePerExam(int averageTimePerExam) { this.averageTimePerExam = averageTimePerExam; }

    public int getPersonalExams() { return personalExams; }
    public void setPersonalExams(int personalExams) { this.personalExams = personalExams; }

    public int getRoomExams() { return roomExams; }
    public void setRoomExams(int roomExams) { this.roomExams = roomExams; }

    // Calculated properties
    public double getOverallAccuracy() {
        return totalQuestionsAttempted > 0 ?
                (double) totalCorrectAnswers / totalQuestionsAttempted * 100 : 0.0;
    }

    public String getFormattedAverageTime() {
        int minutes = averageTimePerExam / 60;
        int seconds = averageTimePerExam % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean hasData() {
        return totalExamsCompleted > 0;
    }
}
