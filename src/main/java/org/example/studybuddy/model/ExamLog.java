package org.example.studybuddy.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

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
    private double difficultyAvg;
    private String topicsCovered;
    private LocalDateTime completedAt;
    private String examType;
    private Integer roomId; // Nullable for personal exams

    // Default constructor
    public ExamLog() {
        this.difficultyAvg = 0.0;
        this.topicsCovered = "";
        this.roomId = null;
        this.completedAt = LocalDateTime.now();
        this.examType = "personal"; // Default to personal exam
    }

    // Constructor for personal exams (most commonly used)
    public ExamLog(int userId, String examName, int totalQuestions, int correctAnswers,
                   int wrongAnswers, int unanswered, double score, int timeTaken) {
        this();
        this.userId = userId;
        this.examName = examName;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.unanswered = unanswered;
        this.score = score;
        this.percentage = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
        this.timeTaken = timeTaken;
        this.examType = "personal";
    }

    // Constructor for room exams
    public ExamLog(int userId, String examName, int totalQuestions, int correctAnswers,
                   int wrongAnswers, int unanswered, double score, int timeTaken, int roomId) {
        this(userId, examName, totalQuestions, correctAnswers, wrongAnswers, unanswered, score, timeTaken);
        this.examType = "room";
        this.roomId = roomId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        // Recalculate percentage when total questions change
        if (totalQuestions > 0) {
            this.percentage = (double) correctAnswers / totalQuestions * 100;
        }
    }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
        // Recalculate percentage when correct answers change
        if (totalQuestions > 0) {
            this.percentage = (double) correctAnswers / totalQuestions * 100;
        }
    }

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

    public double getDifficultyAvg() { return difficultyAvg; }
    public void setDifficultyAvg(double difficultyAvg) { this.difficultyAvg = difficultyAvg; }

    public String getTopicsCovered() { return topicsCovered; }
    public void setTopicsCovered(String topicsCovered) {
        this.topicsCovered = topicsCovered != null ? topicsCovered : "";
    }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt != null ? completedAt : LocalDateTime.now();
    }

    public String getExamType() { return examType; }
    public void setExamType(String examType) {
        this.examType = examType != null ? examType : "personal";
    }

    // Helper methods for UI display
    public String getFormattedTime() {
        if (timeTaken <= 0) return "0:00";
        int minutes = timeTaken / 60;
        int seconds = timeTaken % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getFormattedDate() {
        if (completedAt == null) return "Unknown";
        return completedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    public String getShortFormattedDate() {
        if (completedAt == null) return "Unknown";
        return completedAt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    public String getGrade() {
        if (percentage >= 90) return "A+";
        else if (percentage >= 85) return "A";
        else if (percentage >= 80) return "A-";
        else if (percentage >= 75) return "B+";
        else if (percentage >= 70) return "B";
        else if (percentage >= 65) return "B-";
        else if (percentage >= 60) return "C+";
        else if (percentage >= 55) return "C";
        else if (percentage >= 50) return "C-";
        else if (percentage >= 40) return "D";
        else return "F";
    }

    public String getGradeColor() {
        double p = percentage;
        if (p >= 90) return "#27ae60";      // Green for A+/A
        else if (p >= 80) return "#2ecc71"; // Light green for A-/B+
        else if (p >= 70) return "#f39c12"; // Orange for B/B-
        else if (p >= 60) return "#e67e22"; // Dark orange for C+/C
        else if (p >= 50) return "#e74c3c"; // Red for C-/D
        else return "#c0392b";              // Dark red for F
    }

    // Methods for real-time data analysis
    public boolean isPersonalExam() {
        return "personal".equals(examType) || examType == null;
    }

    public boolean isRoomExam() {
        return "room".equals(examType);
    }

    public boolean isPassing() {
        return percentage >= 60.0; // Configurable passing grade
    }

    public boolean isExcellent() {
        return percentage >= 90.0;
    }

    public boolean isGood() {
        return percentage >= 80.0;
    }

    public String getPerformanceLevel() {
        if (percentage >= 90) return "Excellent";
        else if (percentage >= 80) return "Good";
        else if (percentage >= 70) return "Average";
        else if (percentage >= 60) return "Below Average";
        else return "Needs Improvement";
    }

    // For activity feed display
    public String getActivityDescription() {
        String typeIcon = isRoomExam() ? "🏠" : "📝";
        return String.format("%s %s - %.1f%% (%d/%d correct)",
                typeIcon, examName, percentage, correctAnswers, totalQuestions);
    }

    public String getDetailedActivityDescription() {
        String typeIcon = isRoomExam() ? "🏠 Room" : "📝 Personal";
        return String.format("%s: %s - %.1f%% (%d/%d correct) in %s",
                typeIcon, examName, percentage, correctAnswers,
                totalQuestions, getFormattedTime());
    }

    // Validation methods
    public boolean isValid() {
        return userId > 0 &&
                examName != null && !examName.trim().isEmpty() &&
                totalQuestions > 0 &&
                correctAnswers >= 0 && correctAnswers <= totalQuestions &&
                wrongAnswers >= 0 && wrongAnswers <= totalQuestions &&
                unanswered >= 0 && unanswered <= totalQuestions &&
                (correctAnswers + wrongAnswers + unanswered) == totalQuestions &&
                timeTaken >= 0 &&
                completedAt != null;
    }

    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();

        if (userId <= 0) errors.add("Invalid user ID");
        if (examName == null || examName.trim().isEmpty()) errors.add("Exam name is required");
        if (totalQuestions <= 0) errors.add("Total questions must be positive");
        if (correctAnswers < 0 || correctAnswers > totalQuestions)
            errors.add("Invalid number of correct answers");
        if (wrongAnswers < 0 || wrongAnswers > totalQuestions)
            errors.add("Invalid number of wrong answers");
        if (unanswered < 0 || unanswered > totalQuestions)
            errors.add("Invalid number of unanswered questions");
        if ((correctAnswers + wrongAnswers + unanswered) != totalQuestions)
            errors.add("Answer counts don't match total questions");
        if (timeTaken < 0) errors.add("Time taken cannot be negative");
        if (completedAt == null) errors.add("Completion date is required");

        return errors;
    }

    // For analytics and statistics
    public double getAccuracy() {
        return percentage;
    }

    public double getEfficiency() {
        // Questions per minute
        if (timeTaken <= 0) return 0;
        return (double) totalQuestions / (timeTaken / 60.0);
    }

    public int getMinutesSpent() {
        return timeTaken / 60;
    }

    public boolean wasCompletedToday() {
        if (completedAt == null) return false;
        return completedAt.toLocalDate().equals(java.time.LocalDate.now());
    }

    public boolean wasCompletedThisWeek() {
        if (completedAt == null) return false;
        java.time.LocalDate examDate = completedAt.toLocalDate();
        java.time.LocalDate now = java.time.LocalDate.now();
        return examDate.isAfter(now.minusDays(7));
    }

    // Comparison methods for sorting
    public int compareByDate(ExamLog other) {
        if (this.completedAt == null && other.completedAt == null) return 0;
        if (this.completedAt == null) return 1;
        if (other.completedAt == null) return -1;
        return other.completedAt.compareTo(this.completedAt); // Newest first
    }

    public int compareByScore(ExamLog other) {
        return Double.compare(other.percentage, this.percentage); // Highest first
    }

    @Override
    public String toString() {
        return String.format("ExamLog{id=%d, examName='%s', type=%s, score=%.1f%%, date=%s}",
                id, examName, examType, percentage,
                completedAt != null ? getShortFormattedDate() : "null");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExamLog examLog = (ExamLog) obj;
        return id == examLog.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Create a copy for data manipulation
    public ExamLog copy() {
        ExamLog copy = new ExamLog();
        copy.id = this.id;
        copy.userId = this.userId;
        copy.examName = this.examName;
        copy.totalQuestions = this.totalQuestions;
        copy.correctAnswers = this.correctAnswers;
        copy.wrongAnswers = this.wrongAnswers;
        copy.unanswered = this.unanswered;
        copy.score = this.score;
        copy.percentage = this.percentage;
        copy.timeTaken = this.timeTaken;
        copy.difficultyAvg = this.difficultyAvg;
        copy.topicsCovered = this.topicsCovered;
        copy.completedAt = this.completedAt;
        copy.examType = this.examType;
        copy.roomId = this.roomId;
        return copy;
    }
}
