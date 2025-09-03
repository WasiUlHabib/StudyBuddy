package org.example.studybuddy.controller;

import org.example.studybuddy.database.ExamDAO;
import org.example.studybuddy.database.QuestionDAO;
import org.example.studybuddy.model.*;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

public class ExamSetupController implements Initializable {

    @FXML private TextField examNameField;
    @FXML private Slider timeLimitSlider;
    @FXML private Label timeLimitLabel;
    @FXML private CheckBox negativeMarkingCheckBox;
    @FXML private VBox topicsContainer;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label estimatedDurationLabel;
    @FXML private Button generateExamButton;
    @FXML private Button resetButton;
    @FXML private Label messageLabel;

    private SessionManager sessionManager = SessionManager.getInstance();
    private QuestionDAO questionDAO = new QuestionDAO();
    private ExamDAO examDAO = new ExamDAO();
    private List<Topic> availableTopics;
    private Map<Integer, Spinner<Integer>> subtopicSpinners = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTimeSlider();
        loadTopicsAndSubtopics();
        setupListeners();
    }

    private void setupTimeSlider() {
        timeLimitSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int minutes = newVal.intValue();
            timeLimitLabel.setText(minutes + " minutes");
            estimatedDurationLabel.setText(minutes + " minutes");
        });
    }

    private void loadTopicsAndSubtopics() {
        availableTopics = questionDAO.getAllTopics();

        for (Topic topic : availableTopics) {
            // Create topic checkbox
            CheckBox topicCheckBox = new CheckBox(topic.getName());
            topicCheckBox.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            // Create subtopics container
            VBox subtopicsBox = new VBox(5);
            subtopicsBox.setStyle("-fx-padding: 0 0 0 20;");

            List<Subtopic> subtopics = questionDAO.getSubtopicsByTopic(topic.getId());

            for (Subtopic subtopic : subtopics) {
                HBox subtopicBox = new HBox(10);
                subtopicBox.setStyle("-fx-alignment: center-left;");

                CheckBox subtopicCheckBox = new CheckBox(subtopic.getName());
                int questionCount = questionDAO.getQuestionCountBySubtopic(subtopic.getId());
                Label questionCountLabel = new Label("(" + questionCount + " questions available)");
                questionCountLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12;");

                Label questionsLabel = new Label("Questions:");
                Spinner<Integer> questionSpinner = new Spinner<>(0, questionCount, 0);
                questionSpinner.setMaxWidth(80);
                questionSpinner.setDisable(true);

                // Store spinner reference
                subtopicSpinners.put(subtopic.getId(), questionSpinner);

                // Enable spinner when subtopic is selected
                subtopicCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    questionSpinner.setDisable(!newVal);
                    if (!newVal) {
                        questionSpinner.getValueFactory().setValue(0);
                    }
                    updateTotalQuestions();
                });

                // Update total when spinner value changes
                questionSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalQuestions());

                subtopicBox.getChildren().addAll(subtopicCheckBox, questionCountLabel, questionsLabel, questionSpinner);
                subtopicsBox.getChildren().add(subtopicBox);
            }

            // Topic checkbox controls all subtopics
            topicCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                subtopicsBox.getChildren().forEach(node -> {
                    if (node instanceof HBox) {
                        HBox subtopicBox = (HBox) node;
                        CheckBox subtopicCB = (CheckBox) subtopicBox.getChildren().get(0);
                        subtopicCB.setSelected(newVal);
                    }
                });
            });

            VBox topicSection = new VBox(10);
            topicSection.getChildren().addAll(topicCheckBox, subtopicsBox);
            topicsContainer.getChildren().add(topicSection);
        }
    }

    private void setupListeners() {
        // Auto-generate exam name
        examNameField.setText("Exam - " + new Date().toString().substring(0, 16));
    }

    private void updateTotalQuestions() {
        int total = subtopicSpinners.values().stream()
                .mapToInt(spinner -> spinner.getValue())
                .sum();

        totalQuestionsLabel.setText(String.valueOf(total));

        // Enable/disable generate button
        generateExamButton.setDisable(total == 0);
    }

    @FXML
    private void handleGenerateExam() {
        try {
            // Validate input
            if (examNameField.getText().trim().isEmpty()) {
                showMessage("Please enter an exam name.", true);
                return;
            }

            int totalQuestions = Integer.parseInt(totalQuestionsLabel.getText());
            if (totalQuestions == 0) {
                showMessage("Please select at least one question.", true);
                return;
            }

            // Create exam
            Exam exam = new Exam(
                    sessionManager.getCurrentUser().getId(),
                    examNameField.getText().trim(),
                    (int) timeLimitSlider.getValue(),
                    negativeMarkingCheckBox.isSelected(),
                    totalQuestions
            );

            // Generate exam
            int examId = examDAO.createExam(exam);
            if (examId == -1) {
                showMessage("Failed to create exam.", true);
                return;
            }

            // Prepare subtopic-question mapping
            Map<Integer, Integer> subtopicQuestionCounts = new HashMap<>();
            for (Map.Entry<Integer, Spinner<Integer>> entry : subtopicSpinners.entrySet()) {
                int count = entry.getValue().getValue();
                if (count > 0) {
                    subtopicQuestionCounts.put(entry.getKey(), count);
                }
            }

            // Generate questions
            List<Question> examQuestions = examDAO.generateExamQuestions(subtopicQuestionCounts);
            examDAO.saveExamQuestions(examId, examQuestions);

            showMessage("Exam created successfully! Starting exam...", false);

            // Navigate to exam interface
            ExamController.setCurrentExamId(examId);
            SceneManager.getInstance().switchToExam();

        } catch (Exception e) {
            showMessage("Error creating exam: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        examNameField.setText("Exam - " + new Date().toString().substring(0, 16));
        timeLimitSlider.setValue(60);
        negativeMarkingCheckBox.setSelected(false);

        // Reset all checkboxes and spinners
        topicsContainer.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                VBox topicSection = (VBox) node;
                CheckBox topicCheckBox = (CheckBox) topicSection.getChildren().get(0);
                topicCheckBox.setSelected(false);
            }
        });

        subtopicSpinners.values().forEach(spinner -> spinner.getValueFactory().setValue(0));
        updateTotalQuestions();
        messageLabel.setText("");
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    @FXML
    private void goToDashboard() {
        SceneManager.getInstance().switchToDashboard();
    }

    @FXML
    private void goToQuestionBank() {
        SceneManager.getInstance().switchToQuestionBank();
    }

    @FXML
    private void goToProfile() {
        SceneManager.getInstance().switchToProfile();
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        SceneManager.getInstance().switchToLogin();
    }

}
