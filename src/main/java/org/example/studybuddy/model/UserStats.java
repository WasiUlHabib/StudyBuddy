package org.example.studybuddy.model;

public class UserStats {
    private int userId;
    private int questionsAttempted;
    private int questionsSolved;
    private int examsTaken;

    // Constructors
    public UserStats() {}

    public UserStats(int userId) {
        this.userId = userId;
        this.questionsAttempted = 0;
        this.questionsSolved = 0;
        this.examsTaken = 0;
    }

    public UserStats(int userId, int questionsAttempted, int questionsSolved, int examsTaken) {
        this.userId = userId;
        this.questionsAttempted = questionsAttempted;
        this.questionsSolved = questionsSolved;
        this.examsTaken = examsTaken;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuestionsAttempted() {
        return questionsAttempted;
    }

    public void setQuestionsAttempted(int questionsAttempted) {
        this.questionsAttempted = questionsAttempted;
    }

    public int getQuestionsSolved() {
        return questionsSolved;
    }

    public void setQuestionsSolved(int questionsSolved) {
        this.questionsSolved = questionsSolved;
    }

    public int getExamsTaken() {
        return examsTaken;
    }

    public void setExamsTaken(int examsTaken) {
        this.examsTaken = examsTaken;
    }
}
