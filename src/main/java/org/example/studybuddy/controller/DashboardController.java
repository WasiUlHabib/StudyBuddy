package org.example.studybuddy.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import org.example.studybuddy.database.UserDAO;
import org.example.studybuddy.model.DailyStat;
import org.example.studybuddy.model.User;
import org.example.studybuddy.model.UserStats;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;

import javafx.application.Platform;
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
import javafx.scene.layout.VBox;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label attemptedLabel;
    @FXML private Label solvedLabel;
    @FXML private Label examsLabel;
    @FXML private Label accuracyLabel;

    // NEW: Enhanced statistics labels
    @FXML private Label personalExamsLabel;
    @FXML private Label roomExamsLabel;
    @FXML private Label averageScoreLabel;
    @FXML private Label highestScoreLabel;
    @FXML private Label studyStreakLabel;

    @FXML private Button takeExamButton;
    @FXML private Button refreshStatsButton;
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private CategoryAxis dateAxis;
    @FXML private NumberAxis valueAxis;

    // NEW: UI components for better user experience
    @FXML private VBox statsContainer;
    @FXML private VBox welcomeContainer;
    @FXML private ListView<String> recentActivityList;
    @FXML private ListView<String> bestSubjectsList;
    @FXML private ProgressIndicator loadingIndicator;

    private SessionManager sessionManager = SessionManager.getInstance();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    // UPDATED: Asynchronous data loading to prevent UI blocking
    private void loadUserDataAsync() {
        User currentUser = sessionManager.getCurrentUser();

        if (currentUser == null) {
            SceneManager.getInstance().switchToLogin();
            return;
        }

        // Load basic user info immediately
        loadBasicUserInfo(currentUser);

        // Load statistics asynchronously
        Task<UserStats> statsTask = new Task<>() {
            @Override
            protected UserStats call() throws Exception {
                return userDAO.getUserStats(currentUser.getId());
            }
        };

        statsTask.setOnSucceeded(e -> {
            UserStats stats = statsTask.getValue();
            Platform.runLater(() -> {
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }

                if (stats.hasData()) {
                    updateUIWithRealData(stats);
                    loadPerformanceDataAsync();
                    loadAdditionalDataAsync();
                } else {
                    showWelcomeForNewUser();
                }
            });
        });

        statsTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }
                showErrorMessage("Failed to load statistics. Please try again.");
            });
        });

        new Thread(statsTask).start();
    }

    private void loadBasicUserInfo(User currentUser) {
        // Welcome message with username
        welcomeLabel.setText("Welcome back, " + currentUser.getUsername() + "!");

        // Join date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        joinDateLabel.setText("Member since " + currentUser.getCreatedAt().format(formatter));
    }

    // UPDATED: Use real statistics from exam logs
    private void updateUIWithRealData(UserStats stats) {
        System.out.println("DEBUG: Updating dashboard with real user stats");

        // Core statistics from real exam data
        attemptedLabel.setText(String.valueOf(stats.getTotalQuestionsAttempted()));
        solvedLabel.setText(String.valueOf(stats.getTotalCorrectAnswers()));
        examsLabel.setText(String.valueOf(stats.getTotalExamsCompleted()));
        accuracyLabel.setText(String.format("%.1f%%", stats.getOverallAccuracy()));

        // Enhanced statistics
        if (personalExamsLabel != null) {
            personalExamsLabel.setText(String.valueOf(stats.getPersonalExams()));
        }
        if (roomExamsLabel != null) {
            roomExamsLabel.setText(String.valueOf(stats.getRoomExams()));
        }
        if (averageScoreLabel != null) {
            averageScoreLabel.setText(String.format("%.1f%%", stats.getAverageScore()));
        }
        if (highestScoreLabel != null) {
            highestScoreLabel.setText(String.format("%.1f%%", stats.getHighestScore()));
        }

        // Show study insights
        showStudyInsights(stats);

        // Update session stats
        sessionManager.refreshUserStats();
    }

    // NEW: Show encouraging welcome message for new users
    private void showWelcomeForNewUser() {
        System.out.println("DEBUG: Showing welcome message for new user with no exam history");

        // Reset all stats to zero
        setEmptyStats();

        // Show encouraging message
        if (welcomeContainer != null) {
            Label welcomeTitle = new Label("🎯 Start Your Learning Journey!");
            welcomeTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label welcomeMessage = new Label(
                    "Welcome to StudyBuddy! You haven't taken any exams yet.\n" +
                            "Create some questions and take your first exam to see your progress here.");
            welcomeMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
            welcomeMessage.setWrapText(true);

            Button getStartedButton = new Button("📝 Create Questions & Take First Exam");
            getStartedButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px;");
            getStartedButton.setOnAction(e -> SceneManager.getInstance().switchToQuestionBank());

            VBox welcomeBox = new VBox(15, welcomeTitle, welcomeMessage, getStartedButton);
            welcomeBox.setAlignment(Pos.CENTER);
            welcomeBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 30; -fx-background-radius: 10;");

            // Replace stats container with welcome message
            if (statsContainer != null) {
                statsContainer.getChildren().clear();
                statsContainer.getChildren().add(welcomeBox);
            }
        }

        // Clear charts
        if (performanceChart != null) {
            performanceChart.getData().clear();
            performanceChart.setTitle("📈 Take your first exam to see performance trends!");
        }
    }

    private void setEmptyStats() {
        attemptedLabel.setText("0");
        solvedLabel.setText("0");
        examsLabel.setText("0");
        accuracyLabel.setText("0.0%");

        if (personalExamsLabel != null) personalExamsLabel.setText("0");
        if (roomExamsLabel != null) roomExamsLabel.setText("0");
        if (averageScoreLabel != null) averageScoreLabel.setText("0.0%");
        if (highestScoreLabel != null) highestScoreLabel.setText("0.0%");
        if (studyStreakLabel != null) studyStreakLabel.setText("0 days");
    }

    // NEW: Show study insights and achievements
    private void showStudyInsights(UserStats stats) {
        // Study streak
        User currentUser = sessionManager.getCurrentUser();
        int streak = userDAO.getStudyStreak(currentUser.getId());
        if (studyStreakLabel != null) {
            studyStreakLabel.setText(streak + " day" + (streak != 1 ? "s" : ""));
        }

        // Performance trend
        boolean improving = userDAO.isImproving(currentUser.getId());
        String trendEmoji = improving ? "📈" : "📊";
        String trendText = improving ? "Improving!" : "Steady";

        // You can add a trend label if you have one in FXML
        // trendLabel.setText(trendEmoji + " " + trendText);
    }

    private void setupPerformanceChart() {
        if (performanceChart == null) return;

        // Configure the chart
        performanceChart.setTitle("Performance Trends");
        performanceChart.setAnimated(true);
        performanceChart.setCreateSymbols(true);

        // Configure axes
        if (dateAxis != null) {
            dateAxis.setLabel("Date");
        }
        if (valueAxis != null) {
            valueAxis.setLabel("Score %");
            valueAxis.setAutoRanging(true);
        }
    }

    // UPDATED: Load real performance data from exam logs
    private void loadPerformanceDataAsync() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || performanceChart == null) return;

        Task<List<DailyStat>> performanceTask = new Task<>() {
            @Override
            protected List<DailyStat> call() throws Exception {
                return userDAO.getDailyStats(currentUser.getId(), 14); // Last 2 weeks
            }
        };

        performanceTask.setOnSucceeded(e -> {
            List<DailyStat> dailyStats = performanceTask.getValue();
            Platform.runLater(() -> updatePerformanceChart(dailyStats));
        });

        new Thread(performanceTask).start();
    }

    private void updatePerformanceChart(List<DailyStat> dailyStats) {
        // Clear existing data
        performanceChart.getData().clear();

        if (dailyStats.isEmpty()) {
            performanceChart.setTitle("📈 No performance data yet - take more exams to see trends!");
            return;
        }

        // Create accuracy trend series
        XYChart.Series<String, Number> accuracySeries = new XYChart.Series<>();
        accuracySeries.setName("Accuracy %");

        // Create exam count series
        XYChart.Series<String, Number> examsSeries = new XYChart.Series<>();
        examsSeries.setName("Exams Taken");

        // Add data points
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (DailyStat stat : dailyStats) {
            String dateStr = stat.getDate().format(formatter);

            accuracySeries.getData().add(new XYChart.Data<>(dateStr, stat.getAccuracyPercentage()));
            examsSeries.getData().add(new XYChart.Data<>(dateStr, stat.getExamsTaken()));
        }

        // Add series to chart
        performanceChart.getData().addAll(accuracySeries, examsSeries);
        performanceChart.setTitle("Performance Over Last 14 Days");
    }

    // NEW: Load additional data for enhanced dashboard
    private void loadAdditionalDataAsync() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) return;

        // Load recent activity
        Task<List<String>> activityTask = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return userDAO.getRecentActivity(currentUser.getId(), 5);
            }
        };

        activityTask.setOnSucceeded(e -> {
            List<String> activities = activityTask.getValue();
            Platform.runLater(() -> updateRecentActivity(activities));
        });

        // Load best subjects
        Task<List<String>> subjectsTask = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return userDAO.getBestSubjects(currentUser.getId(), 5);
            }
        };

        subjectsTask.setOnSucceeded(e -> {
            List<String> subjects = subjectsTask.getValue();
            Platform.runLater(() -> updateBestSubjects(subjects));
        });

        new Thread(activityTask).start();
        new Thread(subjectsTask).start();
    }

    private void updateRecentActivity(List<String> activities) {
        if (recentActivityList != null) {
            recentActivityList.getItems().clear();
            if (activities.isEmpty()) {
                recentActivityList.getItems().add("No recent activity - take an exam to get started!");
            } else {
                recentActivityList.getItems().addAll(activities);
            }
        }
    }

    private void updateBestSubjects(List<String> subjects) {
        if (bestSubjectsList != null) {
            bestSubjectsList.getItems().clear();
            if (subjects.isEmpty()) {
                bestSubjectsList.getItems().add("Take more exams to see your best subjects!");
            } else {
                bestSubjectsList.getItems().addAll(subjects);
            }
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Statistics Loading Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleTakeExam() {
        // Smart exam navigation based on user's last exam type
        if (sessionManager.wasLastExamFromRoom()) {
            SceneManager.getInstance().switchToRooms();
        } else {
            SceneManager.getInstance().switchToExamSetup();
        }
    }

    @FXML
    private void goToRooms() {
        SceneManager.getInstance().switchToRooms();
    }

    // UPDATED: Refresh with real data
    @FXML
    private void refreshStats() {
        System.out.println("DEBUG: Refreshing dashboard with latest real data");

        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        // Reload all data asynchronously
        loadUserDataAsync();

        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistics Refreshed");
        alert.setHeaderText(null);
        alert.setContentText("Your dashboard has been updated with the latest data!");
        alert.showAndWait();
    }

    // REMOVED: addTestData() method - no more simulated data!

    @FXML
    private void goToDashboard() {
        // Already here, refresh instead
        refreshStats();
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
