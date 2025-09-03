package org.example.studybuddy.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

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

    public void switchToLogin() {
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
    public void switchToRooms() {
        loadScene("/org/example/studybuddy/view/Rooms.fxml", "Rooms - StudyBuddy");
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlPath);
        }
    }
}
