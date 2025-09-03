package org.example.studybuddy.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.example.studybuddy.database.ExamLogDAO;
import org.example.studybuddy.database.UserDAO;
import org.example.studybuddy.model.DailyStat;
import org.example.studybuddy.model.ExamLog;
import org.example.studybuddy.model.User;
import org.example.studybuddy.model.UserStats;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class ProfileController implements Initializable {

    // Summary Cards
    @FXML private Label overallAccuracyLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalExamsLabel;
    @FXML private Label averageScoreLabel;

    // NEW: Enhanced statistics labels
    @FXML private Label personalExamsLabel;
    @FXML private Label roomExamsLabel;
    @FXML private Label highestScoreLabel;
    @FXML private Label studyStreakLabel;

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
    @FXML private TableColumn<ExamHistory, String> examNameColumn;
    @FXML private TableColumn<ExamHistory, Double> examScoreColumn;
    @FXML private TableColumn<ExamHistory, Double> examAccuracyColumn;
    @FXML private TableColumn<ExamHistory, Integer> examQuestionsColumn;
    @FXML private TableColumn<ExamHistory, String> examTimeColumn;
    @FXML private TableColumn<ExamHistory, String> examTypeColumn;
    @FXML private TableColumn<ExamHistory, Void> actionColumn;

    // Action Buttons
    @FXML private Button refreshDataButton;
    @FXML private Button exportReportButton;

    // Inner class for exam history table
    public static class ExamHistory {
        private int id;
        private String date;
        private String examName;
        private double score;
        private double accuracy;
        private int questions;
        private String timeTaken;
        private String examType;

        public ExamHistory(int id, String date, String examName, double score, double accuracy,
                           int questions, String timeTaken, String examType) {
            this.id = id;
            this.date = date;
            this.examName = examName;
            this.score = score;
            this.accuracy = accuracy;
            this.questions = questions;
            this.timeTaken = timeTaken;
            this.examType = examType;
        }

        // Getters
        public int getId() { return id; }
        public String getDate() { return date; }
        public String getExamName() { return examName; }
        public double getScore() { return score; }
        public double getAccuracy() { return accuracy; }
        public int getQuestions() { return questions; }
        public String getTimeTaken() { return timeTaken; }
        public String getExamType() { return examType; }
    }

    // NEW: UI components for better user experience
    @FXML private VBox profileContainer;
    @FXML private VBox welcomeContainer;
    @FXML private ListView<String> recentActivityList;
    @FXML private ListView<String> achievementsList;
    @FXML private ProgressIndicator loadingIndicator;

    private SessionManager sessionManager = SessionManager.getInstance();
    private UserDAO userDAO = new UserDAO();
    private ExamLogDAO examLogDAO = new ExamLogDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProfileController initialize called - loading real user data");
        setupTables();
        setupUI();
        loadUserDataAsync();
    }

    private void setupUI() {
        // Initialize loading state
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        setupPerformanceChart();
    }
    
    private void setupActionColumn() {
        actionColumn.setCellFactory(param -> new TableCell<ExamHistory, Void>() {
            private final Button viewButton = new Button("View Details");
            {
                viewButton.setOnAction(event -> {
                    ExamHistory examHistory = getTableView().getItems().get(getIndex());
                    viewExamDetails(examHistory.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
    }
    
    private void viewExamDetails(int examId) {
        SceneManager.getInstance().switchToExamDetail(examId);
    }

    private void setupTables() {
        setupActionColumn();
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
        examNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getExamName()));
        examScoreColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getScore()).asObject());
        examAccuracyColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAccuracy()).asObject());
        examQuestionsColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuestions()).asObject());
        examTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTimeTaken()));
        examTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getExamType()));
    }

    private void setupPerformanceChart() {
        if (performanceTrendChart == null) return;

        performanceTrendChart.setTitle("Performance Trends");
        performanceTrendChart.setAnimated(true);
        performanceTrendChart.setCreateSymbols(true);

        if (dateAxis != null) {
            dateAxis.setLabel("Date");
        }
        if (accuracyAxis != null) {
            accuracyAxis.setLabel("Accuracy %");
            accuracyAxis.setAutoRanging(true);
        }
    }

    // UPDATED: Asynchronous data loading to prevent UI blocking
    private void loadUserDataAsync() {
        User currentUser = sessionManager.getCurrentUser();

        if (currentUser == null) {
            SceneManager.getInstance().switchToLogin();
            return;
        }

        Task<ProfileData> profileTask = new Task<>() {
            @Override
            protected ProfileData call() throws Exception {
                ProfileData data = new ProfileData();

                // Load all profile data in background
                data.userStats = userDAO.getUserStats(currentUser.getId());
                data.recentActivity = userDAO.getRecentActivity(currentUser.getId(), 10);
                data.bestSubjects = userDAO.getBestSubjects(currentUser.getId(), 5);
                data.dailyStats = userDAO.getDailyStats(currentUser.getId(), 30);
                data.examHistory = loadExamHistory(currentUser.getId());
                data.topicPerformance = loadTopicPerformance(currentUser.getId());
                data.studyStreak = userDAO.getStudyStreak(currentUser.getId());
                data.isImproving = userDAO.isImproving(currentUser.getId());

                return data;
            }
        };

        profileTask.setOnSucceeded(e -> {
            ProfileData data = profileTask.getValue();
            Platform.runLater(() -> {
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }

                if (data.userStats.hasData()) {
                    updateProfileWithRealData(data);
                } else {
                    showWelcomeForNewUser();
                }
            });
        });

        profileTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }
                showErrorMessage("Failed to load profile data. Please try again.");
            });
        });

        new Thread(profileTask).start();
    }

    // UPDATED: Use real statistics from exam logs
    private void updateProfileWithRealData(ProfileData data) {
        System.out.println("DEBUG: Updating profile with real user data");
        UserStats stats = data.userStats;

        // Core statistics from real exam data
        overallAccuracyLabel.setText(String.format("%.1f%%", stats.getOverallAccuracy()));
        totalQuestionsLabel.setText(String.valueOf(stats.getTotalQuestionsAttempted()));
        totalExamsLabel.setText(String.valueOf(stats.getTotalExamsCompleted()));
        averageScoreLabel.setText(String.format("%.1f%%", stats.getAverageScore()));

        // Enhanced statistics
        if (personalExamsLabel != null) {
            personalExamsLabel.setText(String.valueOf(stats.getPersonalExams()));
        }
        if (roomExamsLabel != null) {
            roomExamsLabel.setText(String.valueOf(stats.getRoomExams()));
        }
        if (highestScoreLabel != null) {
            highestScoreLabel.setText(String.format("%.1f%%", stats.getHighestScore()));
        }
        if (studyStreakLabel != null) {
            studyStreakLabel.setText(data.studyStreak + " day" + (data.studyStreak != 1 ? "s" : ""));
        }

        // Update strengths and weaknesses from real data
        updateStrengthsAndWeaknesses(data.bestSubjects, data.isImproving);

        // Update charts and tables with real data
        updatePerformanceChart(data.dailyStats);
        updateTopicPerformanceTable(data.topicPerformance);
        updateExamHistoryTable(data.examHistory);
        updateRecentActivity(data.recentActivity);
        updateAchievements(stats, data.studyStreak, data.isImproving);
    }

    // NEW: Show encouraging welcome message for new users
    private void showWelcomeForNewUser() {
        System.out.println("DEBUG: Showing welcome message for new user with no exam history");

        // Reset all stats to zero
        setEmptyStats();

        // Show encouraging message
        if (profileContainer != null) {
            Label welcomeTitle = new Label("🎯 Your Learning Profile");
            welcomeTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label welcomeMessage = new Label(
                    "Your profile is empty because you haven't taken any exams yet.\n\n" +
                            "📝 Create questions in the Question Bank\n" +
                            "🎯 Take your first exam to see detailed analytics\n" +
                            "📊 Track your progress over time\n" +
                            "🏆 Earn achievements as you learn"
            );
            welcomeMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
            welcomeMessage.setWrapText(true);

            Button getStartedButton = new Button("🚀 Get Started with First Exam");
            getStartedButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 12;");
            getStartedButton.setOnAction(e -> SceneManager.getInstance().switchToExamSetup());

            VBox welcomeBox = new VBox(20, welcomeTitle, welcomeMessage, getStartedButton);
            welcomeBox.setAlignment(Pos.CENTER);
            welcomeBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 40; -fx-background-radius: 15;");

            // Replace profile container with welcome message
            profileContainer.getChildren().clear();
            profileContainer.getChildren().add(welcomeBox);
        }

        // Clear all data displays
        clearDataDisplays();
    }

    private void setEmptyStats() {
        overallAccuracyLabel.setText("0.0%");
        totalQuestionsLabel.setText("0");
        totalExamsLabel.setText("0");
        averageScoreLabel.setText("0.0%");

        if (personalExamsLabel != null) personalExamsLabel.setText("0");
        if (roomExamsLabel != null) roomExamsLabel.setText("0");
        if (highestScoreLabel != null) highestScoreLabel.setText("0.0%");
        if (studyStreakLabel != null) studyStreakLabel.setText("0 days");
    }

    private void clearDataDisplays() {
        if (performanceTrendChart != null) {
            performanceTrendChart.getData().clear();
            performanceTrendChart.setTitle("📈 Take exams to see performance trends!");
        }

        if (topicPerformanceTable != null) {
            topicPerformanceTable.getItems().clear();
        }

        if (examHistoryTable != null) {
            examHistoryTable.getItems().clear();
        }

        if (strengthsContainer != null) {
            strengthsContainer.getChildren().clear();
            strengthsContainer.getChildren().add(new Label("Take more exams to discover your strengths!"));
        }

        if (weaknessesContainer != null) {
            weaknessesContainer.getChildren().clear();
            weaknessesContainer.getChildren().add(new Label("Areas for improvement will appear here."));
        }
    }

    private void updateStrengthsAndWeaknesses(List<String> bestSubjects, boolean isImproving) {
        // Update strengths from real best subjects
        strengthsContainer.getChildren().clear();
        if (bestSubjects.isEmpty()) {
            strengthsContainer.getChildren().add(new Label("📈 Take more exams to identify your strengths"));
        } else {
            for (String subject : bestSubjects) {
                Label strengthLabel = new Label("✓ " + subject);
                strengthLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                strengthsContainer.getChildren().add(strengthLabel);
            }
        }

        // Update improvement areas based on trend analysis
        weaknessesContainer.getChildren().clear();
        if (isImproving) {
            Label improvingLabel = new Label("🎯 Keep up the great work! You're improving!");
            improvingLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            weaknessesContainer.getChildren().add(improvingLabel);
        } else {
            Label suggestionLabel = new Label("💡 Try taking more varied exams to improve");
            suggestionLabel.setStyle("-fx-text-fill: #f39c12;");
            weaknessesContainer.getChildren().add(suggestionLabel);
        }
    }

    private void updatePerformanceChart(List<DailyStat> dailyStats) {
        performanceTrendChart.getData().clear();

        if (dailyStats.isEmpty()) {
            performanceTrendChart.setTitle("📈 No performance data yet - take more exams!");
            return;
        }

        XYChart.Series<String, Number> accuracySeries = new XYChart.Series<>();
        accuracySeries.setName("Daily Accuracy");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (DailyStat stat : dailyStats) {
            String dateStr = stat.getDate().format(formatter);
            accuracySeries.getData().add(new XYChart.Data<>(dateStr, stat.getAccuracyPercentage()));
        }

        performanceTrendChart.getData().add(accuracySeries);
        performanceTrendChart.setTitle("Performance Over Last 30 Days");
    }

    private void updateTopicPerformanceTable(List<TopicPerformance> topicPerformances) {
        ObservableList<TopicPerformance> data = FXCollections.observableArrayList(topicPerformances);
        topicPerformanceTable.setItems(data);
    }

    private void updateExamHistoryTable(List<ExamHistory> examHistories) {
        ObservableList<ExamHistory> data = FXCollections.observableArrayList(examHistories);
        examHistoryTable.setItems(data);
    }

    private void updateRecentActivity(List<String> activities) {
        if (recentActivityList != null) {
            ObservableList<String> activityData = FXCollections.observableArrayList();
            if (activities.isEmpty()) {
                activityData.add("No recent activity - take an exam to get started!");
            } else {
                activityData.addAll(activities);
            }
            recentActivityList.setItems(activityData);
        }
    }

    private void updateAchievements(UserStats stats, int studyStreak, boolean isImproving) {
        if (achievementsList != null) {
            ObservableList<String> achievements = FXCollections.observableArrayList();

            // Generate achievements based on real data
            if (stats.getTotalExamsCompleted() >= 1) {
                achievements.add("🎯 First Exam Completed!");
            }
            if (stats.getTotalExamsCompleted() >= 5) {
                achievements.add("📚 Exam Enthusiast (5+ exams)");
            }
            if (stats.getTotalExamsCompleted() >= 10) {
                achievements.add("🏆 Exam Master (10+ exams)");
            }
            if (stats.getOverallAccuracy() >= 80) {
                achievements.add("🎯 High Achiever (80%+ accuracy)");
            }
            if (studyStreak >= 3) {
                achievements.add("🔥 Study Streak (" + studyStreak + " days)");
            }
            if (isImproving) {
                achievements.add("📈 Improving Learner");
            }
            if (stats.getRoomExams() > 0) {
                achievements.add("🤝 Team Player (Room exams)");
            }

            if (achievements.isEmpty()) {
                achievements.add("Take more exams to earn achievements!");
            }

            achievementsList.setItems(achievements);
        }
    }

    // NEW: Load real exam history from database
    private List<ExamHistory> loadExamHistory(int userId) {
        List<ExamHistory> examHistories = new ArrayList<>();
        List<ExamLog> examLogs = examLogDAO.getUserExamLogs(userId, 20, 0);

        for (ExamLog log : examLogs) {
            ExamHistory history = new ExamHistory(
                    log.getId(),
                    log.getCompletedAt().toLocalDate().toString(),
                    log.getExamName(),
                    log.getScore(),
                    log.getPercentage(),
                    log.getTotalQuestions(),
                    formatTime(log.getTimeTaken()),
                    log.getExamType() != null ? log.getExamType() : "personal"
            );
            examHistories.add(history);
        }

        return examHistories;
    }

    // NEW: Load topic performance from exam data
    private List<TopicPerformance> loadTopicPerformance(int userId) {
        List<TopicPerformance> topicPerformances = new ArrayList<>();

        // Get subject performance from UserDAO
        List<String> bestSubjects = userDAO.getBestSubjects(userId, 10);

        // Convert to TopicPerformance objects (simplified for now)
        for (String subject : bestSubjects) {
            // Extract exam name and stats (this is simplified - you might want more sophisticated parsing)
            String[] parts = subject.split("\\(");
            if (parts.length >= 2) {
                String topicName = parts[0].trim();
                String statsStr = parts[1].replace(")", "").trim();
                String[] statsParts = statsStr.split(",");

                if (statsParts.length >= 2) {
                    try {
                        double avgScore = Double.parseDouble(statsParts[0].replace("% avg", "").trim());
                        int attempts = Integer.parseInt(statsParts[1].replace("attempts", "").trim());

                        int solved = (int) (attempts * avgScore / 100);
                        String grade = getGradeFromScore(avgScore);

                        TopicPerformance performance = new TopicPerformance(
                                topicName, attempts, solved, avgScore, grade
                        );
                        topicPerformances.add(performance);
                    } catch (NumberFormatException e) {
                        // Skip malformed data
                    }
                }
            }
        }

        return topicPerformances;
    }

    private String getGradeFromScore(double score) {
        if (score >= 90) return "A+";
        else if (score >= 85) return "A";
        else if (score >= 80) return "A-";
        else if (score >= 75) return "B+";
        else if (score >= 70) return "B";
        else if (score >= 65) return "B-";
        else if (score >= 60) return "C+";
        else if (score >= 55) return "C";
        else if (score >= 50) return "C-";
        else return "D";
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Profile Loading Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // UPDATED: Refresh with real data
    @FXML
    private void refreshAnalytics() {
        System.out.println("DEBUG: Refreshing profile with latest real data");

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        loadUserDataAsync();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Refreshed");
        alert.setHeaderText(null);
        alert.setContentText("Your profile has been updated with the latest data!");
        alert.showAndWait();
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
        exportReport();
    }

    @FXML
    private void generateReport() {
        exportReport();
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
        // Already here, refresh instead
        refreshAnalytics();
    }

    @FXML
    private void goToRooms() {
        SceneManager.getInstance().switchToRooms();
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        SceneManager.getInstance().switchToLogin();
    }

    // Data container class for async loading
    private static class ProfileData {
        UserStats userStats;
        List<String> recentActivity;
        List<String> bestSubjects;
        List<DailyStat> dailyStats;
        List<ExamHistory> examHistory;
        List<TopicPerformance> topicPerformance;
        int studyStreak;
        boolean isImproving;
    }

    // Data classes for tables (enhanced with exam type)
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

    // End of class
}
