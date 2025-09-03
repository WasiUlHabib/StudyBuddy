package org.example.studybuddy;

import org.example.studybuddy.database.DatabaseManager;
import org.example.studybuddy.util.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class StudyBuddyApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseManager.getInstance().initializeDatabase();

            // Initialize Scene Manager
            SceneManager.getInstance().initialize(primaryStage);

            // Show login screen
            SceneManager.getInstance().switchToLogin();

            primaryStage.setTitle("StudyBuddy - Quiz Application");
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true); // Make window full screen by default
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // Close database connection when app closes
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
