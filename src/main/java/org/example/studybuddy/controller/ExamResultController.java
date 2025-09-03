package org.example.studybuddy.controller;

import org.example.studybuddy.model.*;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import org.example.studybuddy.util.PDFExporter;
import org.example.studybuddy.util.ExamSettingsDialog; // NEW: Added for room exam dialog
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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

    private static ExamResult examResult;
    private static List<Question> questions;
    private static String[] userAnswers;

    // NEW: Session manager for smart navigation
    private SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayResults();
        createPerformanceChart();
        createQuestionReview();

        // NEW: Update button text based on exam context
        updateButtonText();
    }

    // NEW: Update button text based on last exam type
    private void updateButtonText() {
        if (sessionManager.wasLastExamFromRoom()) {
            dashboardButton.setText("← Back to Room");
            takeAnotherButton.setText("Take Another Room Exam");
            System.out.println("DEBUG: Updated buttons for room exam context - Room: " +
                    sessionManager.getLastActiveRoom().getName());
        } else {
            dashboardButton.setText("← Back to Dashboard");
            takeAnotherButton.setText("Take Another Exam");
            System.out.println("DEBUG: Updated buttons for personal exam context");
        }
    }

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

            // NEW: Include exam type in filename
            String examType = sessionManager.wasLastExamFromRoom() ? "RoomExam" : "PersonalExam";
            fileChooser.setInitialFileName("StudyBuddy_" + examType + "_Report_" +
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

    // UPDATED: Enhanced ExamLog creation with room context
    private ExamLog createExamLogFromResult() {
        if (examResult == null) {
            return null;
        }

        ExamLog examLog = new ExamLog();

        // NEW: Set exam name based on context
        if (sessionManager.wasLastExamFromRoom()) {
            examLog.setExamName(sessionManager.getLastActiveRoom().getName() + " - Room Exam Results");
        } else {
            examLog.setExamName("Personal Exam Results");
        }

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

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void setExamResult(ExamResult result, List<Question> questionsList, String[] answers) {
        examResult = result;
        questions = questionsList;
        userAnswers = answers;
    }

    private void displayResults() {
        if (examResult != null) {
            // NEW: Display exam name based on context
            if (sessionManager.wasLastExamFromRoom()) {
                examNameLabel.setText(sessionManager.getLastActiveRoom().getName() + " - Room Exam Results");
            } else {
                examNameLabel.setText("Personal Exam Results");
            }

            // Display completion date
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

    // UPDATED: Smart navigation
    @FXML
    private void goToDashboard() {
        System.out.println("DEBUG: Back button clicked - Context: " + sessionManager.getExamContextDescription());

        if (sessionManager.wasLastExamFromRoom()) {
            // For room exams, go directly to Rooms scene
            System.out.println("DEBUG: Navigating directly to Rooms for room exam context");
            SceneManager.getInstance().switchToRooms();
        } else {
            // For personal exams, go to dashboard
            System.out.println("DEBUG: Using navigation history for personal exam context");
//            if (SceneManager.getInstance().canGoBack()) {
//                SceneManager.getInstance().goBack();
//            } else {
                SceneManager.getInstance().switchToDashboard();
            //}
        }
    }

    // UPDATED: Smart "Take Another Exam" logic
    @FXML
    private void takeAnotherExam() {
        System.out.println("DEBUG: Take Another Exam clicked - Context: " + sessionManager.getExamContextDescription());

        if (sessionManager.wasLastExamFromRoom()) {
            // Last exam was from a room - show room exam dialog
            handleRoomExamOption();
        } else {
            // Last exam was personal - go to exam setup
            sessionManager.setLastExamMode(SessionManager.ExamMode.PERSONAL);
            sessionManager.setLastActiveRoom(null);
            SceneManager.getInstance().switchToExamSetup();
        }
    }

    // NEW: Handle room exam option with smart navigation
    private void handleRoomExamOption() {
        Room lastRoom = sessionManager.getLastActiveRoom();

        if (lastRoom != null) {
            // Show room exam notification and navigate back to room
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Room Exam");
            info.setHeaderText("Return to " + lastRoom.getName());
            info.setContentText("Returning to your room to take another exam with shared questions.");

            Optional<ButtonType> result = info.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Navigate back to rooms scene
                SceneManager.getInstance().switchToRooms();

                // Schedule room exam dialog to show after scene loads
                Platform.runLater(() -> {
                    Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                        showRoomExamAutoDialog();
                    }));
                    timeline.play();
                });
            }
        } else {
            // Fallback: no room context, go to rooms scene
            showAlert("Room Context Lost",
                    "Room context has been lost. Please navigate to a room and take an exam from there.",
                    Alert.AlertType.WARNING);
            SceneManager.getInstance().switchToRooms();
        }
    }

    // NEW: Show room exam dialog automatically
    private void showRoomExamAutoDialog() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Ready for Another Room Exam");
        info.setHeaderText("Back in " + sessionManager.getLastActiveRoom().getName());
        info.setContentText("You're back in your room! Click 'Take Room Exam' to start another exam with shared questions.\n\n" +
                "The system will remember your room exam preferences for a seamless experience.");

        // Customize buttons
        info.getButtonTypes().clear();
        info.getButtonTypes().addAll(ButtonType.OK);

        info.showAndWait();
    }

    // UPDATED: Enhanced exam context display
    private void displayExamContext() {
        if (sessionManager.wasLastExamFromRoom()) {
            System.out.println("EXAM CONTEXT: Room exam from '" +
                    sessionManager.getLastActiveRoom().getName() +
                    "' (ID: " + sessionManager.getLastActiveRoom().getId() + ")");
        } else {
            System.out.println("EXAM CONTEXT: Personal exam");
        }
    }
}
