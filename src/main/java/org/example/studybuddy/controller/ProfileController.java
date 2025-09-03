package org.example.studybuddy.controller;

import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    // Summary Cards
    @FXML private Label overallAccuracyLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalExamsLabel;
    @FXML private Label averageScoreLabel;

    // Performance Trend Chart
    @FXML private LineChart<String, Number> performanceTrendChart;
    @FXML private CategoryAxis dateAxis;
    @FXML private NumberAxis accuracyAxis;

    // Strengths and Weaknesses
    @FXML private VBox strengthsContainer;
    @FXML private VBox weaknessesContainer;

    // Topic Performance Table
    @FXML private TableView<TopicPerformance> topicPerformanceTable;
    @FXML private TableColumn<TopicPerformance, String> topicColumn;
    @FXML private TableColumn<TopicPerformance, Integer> attemptedColumn;
    @FXML private TableColumn<TopicPerformance, Integer> solvedColumn;
    @FXML private TableColumn<TopicPerformance, Double> accuracyColumn;
    @FXML private TableColumn<TopicPerformance, String> gradeColumn;

    // Exam History Table
    @FXML private TableView<ExamHistory> examHistoryTable;
    @FXML private TableColumn<ExamHistory, String> examDateColumn;
    @FXML private TableColumn<ExamHistory, Double> examScoreColumn;
    @FXML private TableColumn<ExamHistory, Double> examAccuracyColumn;
    @FXML private TableColumn<ExamHistory, Integer> examQuestionsColumn;
    @FXML private TableColumn<ExamHistory, String> examTimeColumn;

    // Action Buttons
    @FXML private Button refreshDataButton;
    @FXML private Button exportReportButton;

    private SessionManager sessionManager = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProfileController initialize called");
        setupTables();
        loadAnalyticsData();
    }

    private void setupTables() {
        // Setup Topic Performance Table
        topicColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTopicName()));
        attemptedColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getAttempted()).asObject());
        solvedColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSolved()).asObject());
        accuracyColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAccuracy()).asObject());
        gradeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGrade()));

        // Setup Exam History Table
        examDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate()));
        examScoreColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getScore()).asObject());
        examAccuracyColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAccuracy()).asObject());
        examQuestionsColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuestions()).asObject());
        examTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimeTaken()));
    }

    private void loadAnalyticsData() {
        // Load summary data (placeholder values)
        overallAccuracyLabel.setText("75%");
        totalQuestionsLabel.setText("150");
        totalExamsLabel.setText("12");
        averageScoreLabel.setText("8.2");

        // Load strengths
        strengthsContainer.getChildren().clear();
        strengthsContainer.getChildren().addAll(
                new Label("✓ Mathematics (85%)"),
                new Label("✓ Science (82%)"),
                new Label("✓ History (78%)")
        );

        // Load weaknesses
        weaknessesContainer.getChildren().clear();
        weaknessesContainer.getChildren().addAll(
                new Label("⚠ English (45%)"),
                new Label("⚠ Geography (52%)"),
                new Label("⚠ Literature (58%)")
        );

        // Load sample data for tables
        loadSampleTopicData();
        loadSampleExamData();
        loadPerformanceChart();
    }

    private void loadSampleTopicData() {
        ObservableList<TopicPerformance> data = FXCollections.observableArrayList();
        data.add(new TopicPerformance("Mathematics", 50, 42, 84.0, "A"));
        data.add(new TopicPerformance("Science", 30, 25, 83.3, "A"));
        data.add(new TopicPerformance("History", 25, 18, 72.0, "B"));
        data.add(new TopicPerformance("English", 20, 9, 45.0, "D"));

        topicPerformanceTable.setItems(data);
    }

    private void loadSampleExamData() {
        ObservableList<ExamHistory> data = FXCollections.observableArrayList();
        data.add(new ExamHistory("2025-08-25", 8.5, 85.0, 10, "15:30"));
        data.add(new ExamHistory("2025-08-20", 7.2, 72.0, 10, "18:45"));
        data.add(new ExamHistory("2025-08-15", 9.1, 91.0, 10, "12:20"));

        examHistoryTable.setItems(data);
    }

    private void loadPerformanceChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Accuracy Trend");

        series.getData().add(new XYChart.Data<>("Aug 15", 91));
        series.getData().add(new XYChart.Data<>("Aug 20", 72));
        series.getData().add(new XYChart.Data<>("Aug 25", 85));

        performanceTrendChart.getData().add(series);
    }

    @FXML
    private void refreshAnalytics() {
        System.out.println("Refreshing analytics data...");
        loadAnalyticsData();
    }

    @FXML
    private void exportReport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Report");
        alert.setHeaderText("Feature Coming Soon");
        alert.setContentText("PDF export functionality will be available in the next update!");
        alert.showAndWait();
    }

    @FXML
    private void exportData() {
        exportReport(); // Same functionality for now
    }

    @FXML
    private void generateReport() {
        exportReport(); // Same functionality for now
    }

    // Navigation methods
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
        // Already here
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        SceneManager.getInstance().switchToLogin();
    }

    // Data classes for tables
    public static class TopicPerformance {
        private String topicName;
        private int attempted;
        private int solved;
        private double accuracy;
        private String grade;

        public TopicPerformance(String topicName, int attempted, int solved, double accuracy, String grade) {
            this.topicName = topicName;
            this.attempted = attempted;
            this.solved = solved;
            this.accuracy = accuracy;
            this.grade = grade;
        }

        // Getters
        public String getTopicName() { return topicName; }
        public int getAttempted() { return attempted; }
        public int getSolved() { return solved; }
        public double getAccuracy() { return accuracy; }
        public String getGrade() { return grade; }
    }

    public static class ExamHistory {
        private String date;
        private double score;
        private double accuracy;
        private int questions;
        private String timeTaken;

        public ExamHistory(String date, double score, double accuracy, int questions, String timeTaken) {
            this.date = date;
            this.score = score;
            this.accuracy = accuracy;
            this.questions = questions;
            this.timeTaken = timeTaken;
        }

        // Getters
        public String getDate() { return date; }
        public double getScore() { return score; }
        public double getAccuracy() { return accuracy; }
        public int getQuestions() { return questions; }
        public String getTimeTaken() { return timeTaken; }
    }
}
