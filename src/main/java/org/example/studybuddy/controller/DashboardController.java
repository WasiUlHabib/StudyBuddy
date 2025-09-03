package org.example.studybuddy.controller;

import org.example.studybuddy.database.UserDAO;
import org.example.studybuddy.model.DailyStat;
import org.example.studybuddy.model.User;
import org.example.studybuddy.model.UserStats;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label attemptedLabel;
    @FXML private Label solvedLabel;
    @FXML private Label examsLabel;
    @FXML private Label accuracyLabel;
    @FXML private Button takeExamButton;
    @FXML private Button refreshStatsButton;
    @FXML private LineChart<String, Number> performanceChart;
    @FXML private CategoryAxis dateAxis;
    @FXML private NumberAxis valueAxis;

    private SessionManager sessionManager = SessionManager.getInstance();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
        setupPerformanceChart();
        loadPerformanceData();
    }

    private void loadUserData() {
        User currentUser = sessionManager.getCurrentUser();
        UserStats userStats = sessionManager.getCurrentUserStats();

        if (currentUser != null) {
            // Welcome message with username
            welcomeLabel.setText("Hi, " + currentUser.getUsername() + "!");

            // Join date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            joinDateLabel.setText("Member since " + currentUser.getCreatedAt().format(formatter));

            if (userStats != null) {
                // Basic statistics
                attemptedLabel.setText(String.valueOf(userStats.getQuestionsAttempted()));
                solvedLabel.setText(String.valueOf(userStats.getQuestionsSolved()));
                examsLabel.setText(String.valueOf(userStats.getExamsTaken()));

                // Calculate accuracy
                double accuracy = userStats.getQuestionsAttempted() > 0
                        ? (double) userStats.getQuestionsSolved() / userStats.getQuestionsAttempted() * 100
                        : 0.0;
                accuracyLabel.setText(String.format("%.1f%%", accuracy));
            } else {
                setDefaultStats();
            }
        } else {
            // User not logged in, redirect to login
            SceneManager.getInstance().switchToLogin();
        }
    }

    private void setDefaultStats() {
        attemptedLabel.setText("0");
        solvedLabel.setText("0");
        examsLabel.setText("0");
        accuracyLabel.setText("0%");
    }

    private void setupPerformanceChart() {
        // Configure the chart
        performanceChart.setTitle("Daily Performance");
        performanceChart.setAnimated(true);
        performanceChart.setCreateSymbols(true);

        // Configure axes
        dateAxis.setLabel("Date");
        valueAxis.setLabel("Questions");
        valueAxis.setAutoRanging(true);
    }

    private void loadPerformanceData() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) return;

        // Clear existing data
        performanceChart.getData().clear();

        // Get daily stats for last 7 days
        List<DailyStat> dailyStats = userDAO.getDailyStats(currentUser.getId(), 7);

        if (dailyStats.isEmpty()) {
            // Show message if no data
            performanceChart.setTitle("No performance data yet - start taking quizzes!");
            return;
        }

        // Create data series
        XYChart.Series<String, Number> attemptedSeries = new XYChart.Series<>();
        attemptedSeries.setName("Questions Attempted");

        XYChart.Series<String, Number> solvedSeries = new XYChart.Series<>();
        solvedSeries.setName("Questions Solved");

        // Add data points
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        for (DailyStat stat : dailyStats) {
            String dateStr = stat.getDate().format(formatter);

            attemptedSeries.getData().add(new XYChart.Data<>(dateStr, stat.getQuestionsAttempted()));
            solvedSeries.getData().add(new XYChart.Data<>(dateStr, stat.getQuestionsSolved()));
        }

        // Add series to chart
        performanceChart.getData().addAll(attemptedSeries, solvedSeries);
        performanceChart.setTitle("Performance Over Last 7 Days");
    }

    @FXML
    private void handleTakeExam() {
        SceneManager.getInstance().switchToExamSetup();
    }

    @FXML
    private void goToRooms() {
        SceneManager.getInstance().switchToRooms();
    }

    @FXML
    private void refreshStats() {
        sessionManager.refreshUserStats();
        loadUserData();
        loadPerformanceData();

        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Stats Refreshed");
        alert.setHeaderText(null);
        alert.setContentText("Your statistics have been refreshed!");
        alert.showAndWait();
    }

    @FXML
    private void addTestData() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            userDAO.addTestData(currentUser.getId());

            // Refresh the display
            sessionManager.refreshUserStats();
            loadUserData();
            loadPerformanceData();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Test Data Added");
            alert.setHeaderText(null);
            alert.setContentText("Sample performance data has been added! Check out your updated statistics and performance graph.");
            alert.showAndWait();
        }
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
