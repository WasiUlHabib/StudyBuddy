package org.example.studybuddy.util;

import java.io.IOException;
import java.util.Stack;

import org.example.studybuddy.controller.ExamDetailController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SceneManager {
    private FXMLLoader currentLoader;
    private static SceneManager instance;
    private Stage primaryStage;

    // NEW: Navigation history stack
    private Stack<String> sceneHistory = new Stack<>();
    private String currentScenePath = null;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.primaryStage = stage;
    }

    public Object getController() {
        return currentLoader != null ? currentLoader.getController() : null;
    }

    // UPDATED: Enhanced loadScene with history tracking
    private void loadScene(String fxmlPath, String title) {
        try {
            currentLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane root = currentLoader.load();

            // Create new scene while preserving current window size and state
            Scene currentScene = primaryStage.getScene();
            Scene scene;
            boolean wasMaximized = primaryStage.isMaximized();
            
            if (currentScene != null) {
                scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            } else {
                scene = new Scene(root, 800, 600);
            }
            
            primaryStage.setScene(scene);
            
            // Restore maximized state immediately after setting the scene
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }

            // Push current scene to history before switching (except for login)
            if (primaryStage.getScene() != null && currentScenePath != null && !isLoginOrRegister(fxmlPath)) {
                sceneHistory.push(currentScenePath);
                System.out.println("DEBUG: Pushed to history: " + currentScenePath);
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setMaximized(true); // Always maximize window for every screen
            currentScenePath = fxmlPath;

            System.out.println("DEBUG: Switched to scene: " + fxmlPath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlPath);
        }
    }

    // NEW: Check if scene is login or register (don't add to history)
    private boolean isLoginOrRegister(String fxmlPath) {
        return fxmlPath.contains("Login.fxml") || fxmlPath.contains("Register.fxml");
    }

    public void switchToLogin() {
        // Clear history on login/logout
        sceneHistory.clear();
        currentScenePath = null;
        loadScene("/org/example/studybuddy/view/Login.fxml", "Login - StudyBuddy");
    }

    public void switchToRegister() {
        loadScene("/org/example/studybuddy/view/Register.fxml", "Register - StudyBuddy");
    }

    public void switchToDashboard() {
        loadScene("/org/example/studybuddy/view/Dashboard.fxml", "Dashboard - StudyBuddy");
    }

    public void switchToQuestionBank() {
        loadScene("/org/example/studybuddy/view/QuestionBank.fxml", "Question Bank - StudyBuddy");
    }

    public void switchToProfile() {
        loadScene("/org/example/studybuddy/view/Profile.fxml", "Profile - StudyBuddy");
    }

    public void switchToExamSetup() {
        loadScene("/org/example/studybuddy/view/ExamSetup.fxml", "Exam Setup - StudyBuddy");
    }

    public void switchToExam() {
        loadScene("/org/example/studybuddy/view/Exam.fxml", "Taking Exam - StudyBuddy");
    }

    public void switchToExamResult() {
        loadScene("/org/example/studybuddy/view/ExamResult.fxml", "Exam Results - StudyBuddy");
    }

    public void switchToExamDetail(int examId) {
        loadScene("/org/example/studybuddy/view/ExamDetail.fxml", "Exam Details - StudyBuddy");
        var controller = (ExamDetailController) getController();
        if (controller != null) {
            controller.loadExamDetails(examId);
        }
    }

    public void switchToRooms() {
        loadScene("/org/example/studybuddy/view/Rooms.fxml", "Rooms - StudyBuddy");
    }

    // NEW: Navigate back to previous scene
    public void goBack() {
        if (!sceneHistory.isEmpty()) {
            String previousScene = sceneHistory.pop();
            System.out.println("DEBUG: Going back to: " + previousScene);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(previousScene));
                Pane root = loader.load();

                Scene scene = new Scene(root, 800, 600);
                primaryStage.setScene(scene);
                primaryStage.setTitle(getTitleFromPath(previousScene));
                currentScenePath = previousScene;

                System.out.println("DEBUG: Successfully navigated back to: " + previousScene);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error navigating back to: " + previousScene);
                // Fallback to dashboard on error
                switchToDashboard();
            }
        } else {
            System.out.println("DEBUG: No previous scene in history, going to dashboard");
            // No history available, go to dashboard
            switchToDashboard();
        }
    }

    // NEW: Generate appropriate title from FXML path
    private String getTitleFromPath(String fxmlPath) {
        if (fxmlPath == null) return "StudyBuddy";

        if (fxmlPath.contains("Dashboard.fxml")) return "Dashboard - StudyBuddy";
        else if (fxmlPath.contains("QuestionBank.fxml")) return "Question Bank - StudyBuddy";
        else if (fxmlPath.contains("Profile.fxml")) return "Profile - StudyBuddy";
        else if (fxmlPath.contains("Rooms.fxml")) return "Rooms - StudyBuddy";
        else if (fxmlPath.contains("ExamSetup.fxml")) return "Exam Setup - StudyBuddy";
        else if (fxmlPath.contains("Exam.fxml")) return "Taking Exam - StudyBuddy";
        else if (fxmlPath.contains("ExamResult.fxml")) return "Exam Results - StudyBuddy";
        else return "StudyBuddy";
    }

    // NEW: Clear navigation history (useful for logout)
    public void clearHistory() {
        sceneHistory.clear();
        System.out.println("DEBUG: Navigation history cleared");
    }

    // NEW: Get current scene info for debugging
    public String getCurrentScene() {
        return currentScenePath;
    }

    // NEW: Check if there's history to go back to
    public boolean canGoBack() {
        return !sceneHistory.isEmpty();
    }

    // NEW: Get history size for debugging
    public int getHistorySize() {
        return sceneHistory.size();
    }
}
