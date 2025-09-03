package org.example.studybuddy.model;

public class SubjectStats {
    private String subjectName;
    private int attempts;
    private double averageScore;
    private double bestScore;
    private double accuracy;

    // Constructors, getters, setters
    public SubjectStats() {}

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getBestScore() { return bestScore; }
    public void setBestScore(double bestScore) { this.bestScore = bestScore; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
}
