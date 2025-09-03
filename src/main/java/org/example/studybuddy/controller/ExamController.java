package org.example.studybuddy.controller;

import org.example.studybuddy.database.ExamDAO;
import org.example.studybuddy.model.*;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import org.example.studybuddy.database.ExamLogDAO;
import org.example.studybuddy.model.ExamLog;
import org.example.studybuddy.util.PDFExporter;
import javafx.stage.FileChooser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ArrayList;

public class ExamController implements Initializable {

    @FXML private Label examNameLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label questionTextLabel;
    @FXML private RadioButton optionA;
    @FXML private RadioButton optionB;
    @FXML private RadioButton optionC;
    @FXML private RadioButton optionD;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button submitButton;
    @FXML private GridPane questionGridPane;

    // Fields
    private ExamLogDAO examLogDAO = new ExamLogDAO();
    private static int currentExamId;
    private ExamDAO examDAO = new ExamDAO();
    private SessionManager sessionManager = SessionManager.getInstance();

    private Exam currentExam;
    private List<Question> examQuestions;
    private int currentQuestionIndex = 0;
    private String[] userAnswers;
    private Timeline examTimer;
    private int remainingTimeInSeconds;
    private ToggleGroup answerGroup = new ToggleGroup();
    private ExamLog currentExamLog; // To store exam results for PDF export

    // Static method to set exam ID from ExamSetup
    public static void setCurrentExamId(int examId) {
        currentExamId = examId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAnswerGroup();
        loadExam();
        setupQuestionGrid();
        startTimer();
        loadQuestion();
    }

    private void setupAnswerGroup() {
        optionA.setToggleGroup(answerGroup);
        optionB.setToggleGroup(answerGroup);
        optionC.setToggleGroup(answerGroup);
        optionD.setToggleGroup(answerGroup);

        // Listen for answer changes
        answerGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selected = (RadioButton) newToggle;
                saveCurrentAnswer(selected.getText().substring(0, 1)); // Get A, B, C, or D
            }
        });
    }

    private void loadExam() {
        currentExam = examDAO.getExamById(currentExamId);
        examQuestions = examDAO.getExamQuestions(currentExamId);
        userAnswers = new String[examQuestions.size()];

        if (currentExam != null) {
            examNameLabel.setText(currentExam.getName());
            totalQuestionsLabel.setText(String.valueOf(examQuestions.size()));
            remainingTimeInSeconds = currentExam.getTimeLimit() * 60; // Convert minutes to seconds
        }
    }

    private void setupQuestionGrid() {
        int cols = 10;
        for (int i = 0; i < examQuestions.size(); i++) {
            Button questionButton = new Button(String.valueOf(i + 1));
            questionButton.setPrefSize(35, 35);
            questionButton.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7;");

            final int questionIndex = i;
            questionButton.setOnAction(e -> {
                saveCurrentAnswer();
                currentQuestionIndex = questionIndex;
                loadQuestion();
            });

            int row = i / cols;
            int col = i % cols;
            questionGridPane.add(questionButton, col, row);
        }

        updateQuestionGridColors();
    }

    private void updateQuestionGridColors() {
        questionGridPane.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                int index = Integer.parseInt(btn.getText()) - 1;

                if (index == currentQuestionIndex) {
                    btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-border-color: #2980b9;");
                } else if (userAnswers[index] != null) {
                    btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-color: #229954;");
                } else {
                    btn.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7;");
                }
            }
        });
    }

    private void startTimer() {
        examTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingTimeInSeconds--;
            updateTimerDisplay();

            if (remainingTimeInSeconds <= 0) {
                examTimer.stop();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Time Up!");
                    alert.setHeaderText("Exam time has finished");
                    alert.setContentText("Your exam will be automatically submitted.");
                    alert.showAndWait();
                    submitExam();
                });
            }
        }));
        examTimer.setCycleCount(Timeline.INDEFINITE);
        examTimer.play();
    }

    private void updateTimerDisplay() {
        int minutes = remainingTimeInSeconds / 60;
        int seconds = remainingTimeInSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        timerLabel.setText(timeText);

        // Change color when time is running out
        if (remainingTimeInSeconds <= 300) { // Last 5 minutes
            timerLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20; -fx-font-weight: bold;");
        }
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= 0 && currentQuestionIndex < examQuestions.size()) {
            Question question = examQuestions.get(currentQuestionIndex);

            questionNumberLabel.setText(String.valueOf(currentQuestionIndex + 1));
            questionTextLabel.setText(question.getQuestionText());

            optionA.setText("A) " + question.getOptionA());
            optionB.setText("B) " + question.getOptionB());
            optionC.setText("C) " + question.getOptionC());
            optionD.setText("D) " + question.getOptionD());

            // Load previous answer if exists
            answerGroup.selectToggle(null);
            if (userAnswers[currentQuestionIndex] != null) {
                switch (userAnswers[currentQuestionIndex]) {
                    case "A" -> optionA.setSelected(true);
                    case "B" -> optionB.setSelected(true);
                    case "C" -> optionC.setSelected(true);
                    case "D" -> optionD.setSelected(true);
                }
            }

            // Update navigation buttons
            previousButton.setDisable(currentQuestionIndex == 0);
            nextButton.setText(currentQuestionIndex == examQuestions.size() - 1 ? "Finish" : "Next →");

            updateQuestionGridColors();
        }
    }

    private void saveCurrentAnswer(String answer) {
        userAnswers[currentQuestionIndex] = answer;
        examDAO.saveUserAnswer(currentExamId, currentQuestionIndex + 1, answer);
        updateQuestionGridColors();
    }

    private void saveCurrentAnswer() {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        if (selected != null) {
            String answer = selected.getText().substring(0, 1); // Get A, B, C, or D
            saveCurrentAnswer(answer);
        }
    }

    @FXML
    private void handlePrevious() {
        if (currentQuestionIndex > 0) {
            saveCurrentAnswer();
            currentQuestionIndex--;
            loadQuestion();
        }
    }

    @FXML
    private void handleNext() {
        if (currentQuestionIndex < examQuestions.size() - 1) {
            saveCurrentAnswer();
            currentQuestionIndex++;
            loadQuestion();
        } else {
            // This is the last question, show submit confirmation
            handleSubmit();
        }
    }

    @FXML
    private void handleSubmit() {
        saveCurrentAnswer();

        // Count answered questions
        long answeredCount = Arrays.stream(userAnswers)
                .filter(answer -> answer != null)
                .count();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Submit Exam");
        alert.setHeaderText("Are you sure you want to submit your exam?");
        alert.setContentText(String.format("You have answered %d out of %d questions.\nThis action cannot be undone.",
                answeredCount, examQuestions.size()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            submitExam();
        }
    }

    // CORRECTED submitExam method
    private void submitExam() {
        if (examTimer != null) {
            examTimer.stop();
        }

        // Calculate results
        ExamResult result = examDAO.calculateExamResult(currentExamId,
                sessionManager.getCurrentUser().getId(), currentExam.isNegativeMarks());

        // Calculate stats for exam log
        int correctCount = 0;
        int wrongCount = 0;
        int unanswered = 0;

        for (int i = 0; i < examQuestions.size(); i++) {
            if (userAnswers[i] == null) {
                unanswered++;
            } else if (userAnswers[i].equals(examQuestions.get(i).getCorrectAnswer())) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }

        int timeTaken = (currentExam.getTimeLimit() * 60) - remainingTimeInSeconds;

        // Save exam log
        saveExamLog(currentExam.getName(), examQuestions, Arrays.asList(userAnswers),
                timeTaken, correctCount, wrongCount, unanswered);

        // Update user statistics - Fixed method call
        try {
            sessionManager.refreshUserStats();
        } catch (Exception e) {
            // If refreshUserStats doesn't exist, try alternative
            sessionManager = SessionManager.getInstance();
        }

        // Navigate to results - FIXED: Pass correct parameters to match ExamResultController
        ExamResultController.setExamResult(result,examQuestions, userAnswers);
        SceneManager.getInstance().switchToExamResult();
    }

    // Method to save exam log when exam completes
    private void saveExamLog(String examName, List<Question> questions, List<String> userAnswersList,
                             int timeTaken, int correctCount, int wrongCount, int unansweredCount) {
        try {
            ExamLog examLog = new ExamLog(
                    sessionManager.getCurrentUser().getId(),
                    examName,
                    questions.size(),
                    correctCount,
                    wrongCount,
                    unansweredCount,
                    correctCount, // score = correct answers for simple scoring
                    timeTaken
            );

            // Save to database
            boolean saved = examLogDAO.saveExamLog(examLog);
            if (saved) {
                currentExamLog = examLog; // Store for PDF export
                System.out.println("Exam log saved successfully");
            } else {
                System.err.println("Failed to save exam log");
            }
        } catch (Exception e) {
            System.err.println("Error saving exam log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Message display method
    private void showMessage(String message, boolean isError) {
        Platform.runLater(() -> {
            Alert.AlertType alertType = isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
            Alert alert = new Alert(alertType);
            alert.setTitle(isError ? "Error" : "Success");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // PDF export method (optional - mainly for testing)
    @FXML
    private void handleExportPDF() {
        if (currentExamLog == null) {
            showMessage("No exam data to export. Complete an exam first.", true);
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Exam Report as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("StudyBuddy_Exam_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".pdf");

            java.io.File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                List<String> userAnswersList = Arrays.asList(userAnswers);
                boolean success = PDFExporter.exportExamResult(currentExamLog, examQuestions, userAnswersList, file.getAbsolutePath());

                if (success) {
                    showMessage("PDF exported successfully to " + file.getName(), false);
                } else {
                    showMessage("Failed to export PDF. Please try again.", true);
                }
            }
        } catch (Exception e) {
            showMessage("Error exporting PDF: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }
}
