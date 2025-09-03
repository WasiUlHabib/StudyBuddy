package org.example.studybuddy.controller;

import org.example.studybuddy.model.*;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ExamResultController implements Initializable {

    @FXML private Label examNameLabel;
    @FXML private Label completedDateLabel;
    @FXML private Label scoreLabel;
    @FXML private Label correctLabel;
    @FXML private Label wrongLabel;
    @FXML private Label unansweredLabel;
    @FXML private PieChart performanceChart;
    @FXML private VBox questionsReviewContainer;
    @FXML private Button dashboardButton;
    @FXML private Button takeAnotherButton;

    private static ExamResult examResult;
    private static Exam exam;
    private static List<Question> questions;
    private static String[] userAnswers;

    public static void setExamResult(ExamResult result, Exam examInfo, List<Question> examQuestions, String[] answers) {
        examResult = result;
        exam = examInfo;
        questions = examQuestions;
        userAnswers = answers;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayResults();
        createPerformanceChart();
        createQuestionReview();
    }

    private void displayResults() {
        if (examResult != null && exam != null) {
            examNameLabel.setText(exam.getName());
            completedDateLabel.setText("Completed: " + examResult.getCompletedAt().format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));

            scoreLabel.setText(String.format("%.1f", examResult.getScore()));
            correctLabel.setText(String.valueOf(examResult.getCorrectAnswers()));
            wrongLabel.setText(String.valueOf(examResult.getWrongAnswers()));
            unansweredLabel.setText(String.valueOf(examResult.getUnanswered()));
        }
    }

    private void createPerformanceChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Correct (" + examResult.getCorrectAnswers() + ")", examResult.getCorrectAnswers()),
                new PieChart.Data("Wrong (" + examResult.getWrongAnswers() + ")", examResult.getWrongAnswers()),
                new PieChart.Data("Unanswered (" + examResult.getUnanswered() + ")", examResult.getUnanswered())
        );

        performanceChart.setData(pieChartData);
        performanceChart.setTitle(String.format("Accuracy: %.1f%%", examResult.getAccuracyPercentage()));
    }

    private void createQuestionReview() {
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers[i];
            boolean isCorrect = userAnswer != null && userAnswer.equals(question.getCorrectAnswer());

            VBox questionBox = new VBox(10);
            questionBox.setStyle(isCorrect ?
                    "-fx-background-color: #d5f4e6; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #27ae60; -fx-border-radius: 8;" :
                    userAnswer == null ?
                            "-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #95a5a6; -fx-border-radius: 8;" :
                            "-fx-background-color: #fadbd8; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e74c3c; -fx-border-radius: 8;");

            // Question header
            HBox headerBox = new HBox(10);
            Label questionNumberLabel = new Label("Question " + (i + 1));
            questionNumberLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            Label statusLabel = new Label(isCorrect ? "✓ Correct" : userAnswer == null ? "- Unanswered" : "✗ Wrong");
            statusLabel.setStyle(isCorrect ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
                    userAnswer == null ? "-fx-text-fill: #95a5a6; -fx-font-weight: bold;" :
                            "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            headerBox.getChildren().addAll(questionNumberLabel, statusLabel);

            // Question text
            Label questionTextLabel = new Label(question.getQuestionText());
            questionTextLabel.setWrapText(true);
            questionTextLabel.setStyle("-fx-font-size: 14;");

            // Options
            VBox optionsBox = new VBox(5);
            String[] options = {question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD()};
            String[] optionLabels = {"A", "B", "C", "D"};

            for (int j = 0; j < options.length; j++) {
                Label optionLabel = new Label(optionLabels[j] + ") " + options[j]);

                if (optionLabels[j].equals(question.getCorrectAnswer())) {
                    optionLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                } else if (optionLabels[j].equals(userAnswer) && !isCorrect) {
                    optionLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }

                optionsBox.getChildren().add(optionLabel);
            }

            // Answer summary
            Label answerSummary = new Label();
            if (userAnswer == null) {
                answerSummary.setText("Not answered. Correct answer: " + question.getCorrectAnswer());
            } else if (isCorrect) {
                answerSummary.setText("Your answer: " + userAnswer + " ✓");
            } else {
                answerSummary.setText("Your answer: " + userAnswer + " ✗ (Correct: " + question.getCorrectAnswer() + ")");
            }
            answerSummary.setStyle("-fx-font-size: 12; -fx-font-style: italic;");

            // Explanation if available
            if (question.getExplanation() != null && !question.getExplanation().trim().isEmpty()) {
                Label explanationLabel = new Label("Explanation: " + question.getExplanation());
                explanationLabel.setWrapText(true);
                explanationLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 4;");
                questionBox.getChildren().addAll(headerBox, questionTextLabel, optionsBox, answerSummary, explanationLabel);
            } else {
                questionBox.getChildren().addAll(headerBox, questionTextLabel, optionsBox, answerSummary);
            }

            questionsReviewContainer.getChildren().add(questionBox);
        }
    }

    @FXML
    private void goToDashboard() {
        SceneManager.getInstance().switchToDashboard();
    }

    @FXML
    private void takeAnotherExam() {
        SceneManager.getInstance().switchToExamSetup();
    }
}
