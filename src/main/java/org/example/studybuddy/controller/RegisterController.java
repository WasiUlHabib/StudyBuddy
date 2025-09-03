package org.example.studybuddy.controller;

import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label messageLabel;

    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Clear previous messages
        messageLabel.setText("");

        // Validation
        if (username.isEmpty()) {
            messageLabel.setText("Please enter a username");
            return;
        }

        if (username.length() < 3) {
            messageLabel.setText("Username must be at least 3 characters long");
            return;
        }

        if (password.isEmpty()) {
            messageLabel.setText("Please enter a password");
            return;
        }

        if (password.length() < 4) {
            messageLabel.setText("Password must be at least 4 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            confirmPasswordField.clear();
            return;
        }

        // FIXED: Use setDisable instead of setDisabled
        registerButton.setDisable(true);

        // Attempt registration
        if (sessionManager.register(username, password, confirmPassword)) {
            // Registration successful
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            messageLabel.setText("Registration successful! Please login.");

            // Clear fields
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

                // Directly navigate to dashboard after successful registration
                SceneManager.getInstance().switchToDashboard();

        } else {
            // Registration failed
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            messageLabel.setText("Registration failed. Username may already exist.");
        }

        // FIXED: Use setDisable instead of setDisabled
        registerButton.setDisable(false);
    }

    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchToLogin();
    }
}
