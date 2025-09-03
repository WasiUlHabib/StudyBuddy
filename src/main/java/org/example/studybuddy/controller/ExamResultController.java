package org.example.studybuddy.controller;

import org.example.studybuddy.model.*;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import org.example.studybuddy.util.PDFExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.net.URL;
import java.time.LocalDateTime;
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
    @FXML private Button downloadPdfButton;

    // CORRECTED: Fixed static variable names to match usage
    private static ExamResult examResult;
    private static List<Question> questions;
    private static String[] userAnswers;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayResults();
        createPerformanceChart();
        createQuestionReview();
    }

    // CORRECTED: Fixed handleDownloadPdf method with proper implementation
    @FXML
    private void handleDownloadPdf(ActionEvent event) {
        try {
            if (examResult == null) {
                showAlert("No Data", "No exam results available to export.", Alert.AlertType.WARNING);
                return;
            }

            // Create FileChooser for PDF save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Exam Report as PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("StudyBuddy_ExamReport_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".pdf");

            // Show save dialog
            java.io.File file = fileChooser.showSaveDialog(downloadPdfButton.getScene().getWindow());

            if (file != null) {
                // Create ExamLog from current result for PDF export
                ExamLog examLog = createExamLogFromResult();

                if (examLog != null) {
                    // Export PDF
                    boolean success = PDFExporter.exportExamResult(
                            examLog,
                            questions,
                            java.util.Arrays.asList(userAnswers),
                            file.getAbsolutePath()
                    );

                    if (success) {
                        showAlert("Success", "PDF exported successfully to " + file.getName(), Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "Failed to export PDF. Please try again.", Alert.AlertType.ERROR);
                    }
                } else {
                    showAlert("Error", "Unable to create exam log for PDF export.", Alert.AlertType.ERROR);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while exporting PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // CORRECTED: Helper method to create ExamLog from ExamResult
    private ExamLog createExamLogFromResult() {
        if (examResult == null) {
            return null;
        }

        ExamLog examLog = new ExamLog();
        examLog.setExamName("Exam Results");
        examLog.setTotalQuestions(examResult.getTotalQuestions());
        examLog.setCorrectAnswers(examResult.getCorrectAnswers());
        examLog.setWrongAnswers(examResult.getWrongAnswers());
        examLog.setUnanswered(examResult.getUnanswered());
        examLog.setScore(examResult.getScore());
        examLog.setPercentage(examResult.getPercentage());
        examLog.setTimeTaken(examResult.getTimeTaken());
        examLog.setCompletedAt(LocalDateTime.now());

        return examLog;
    }

    // CORRECTED: Helper method to show alerts
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // CORRECTED: Static method to set exam result data (matches ExamController call)
    public static void setExamResult(ExamResult result, List<Question> questionsList, String[] answers) {
        examResult = result;
        questions = questionsList;
        userAnswers = answers;
    }

    private void displayResults() {
        if (examResult != null) {
            // CORRECTED: Use proper method names that exist in ExamResult
            examNameLabel.setText("Exam Results"); // or examResult.getExamName() if method exists

            // CORRECTED: Use proper date handling
            if (examResult.getCompletedAt() != null) {
                completedDateLabel.setText("Completed: " + examResult.getCompletedAt().format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            } else {
                completedDateLabel.setText("Completed: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
            }

            scoreLabel.setText(String.format("%.1f", examResult.getScore()));
            correctLabel.setText(String.valueOf(examResult.getCorrectAnswers()));
            wrongLabel.setText(String.valueOf(examResult.getWrongAnswers()));
            unansweredLabel.setText(String.valueOf(examResult.getUnanswered()));
        }
    }

    private void createPerformanceChart() {
        if (examResult == null) return;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Correct (" + examResult.getCorrectAnswers() + ")", examResult.getCorrectAnswers()),
                new PieChart.Data("Wrong (" + examResult.getWrongAnswers() + ")", examResult.getWrongAnswers()),
                new PieChart.Data("Unanswered (" + examResult.getUnanswered() + ")", examResult.getUnanswered())
        );

        performanceChart.setData(pieChartData);

        // CORRECTED: Use proper percentage calculation
        double percentage = examResult.getTotalQuestions() > 0 ?
                (double) examResult.getCorrectAnswers() / examResult.getTotalQuestions() * 100 : 0;
        performanceChart.setTitle(String.format("Accuracy: %.1f%%", percentage));
    }

    private void createQuestionReview() {
        if (questions == null || userAnswers == null) return;

        questionsReviewContainer.getChildren().clear();

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = (i < userAnswers.length) ? userAnswers[i] : null;
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
