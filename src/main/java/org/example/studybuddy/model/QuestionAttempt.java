package org.example.studybuddy.model;

import java.time.LocalDateTime;

public class QuestionAttempt {
    private int id;
    private int userId;
    private int questionId;
    private String selectedAnswer;
    private boolean isCorrect;
    private int timeTaken; // in seconds
    private LocalDateTime attemptedAt;
    private Integer examId; // nullable

    // Constructors
    public QuestionAttempt() {}

    public QuestionAttempt(int userId, int questionId, String selectedAnswer, boolean isCorrect, int timeTaken) {
        this.userId = userId;
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
        this.timeTaken = timeTaken;
        this.attemptedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public int getTimeTaken() { return timeTaken; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }

    public Integer getExamId() { return examId; }
    public void setExamId(Integer examId) { this.examId = examId; }
}
