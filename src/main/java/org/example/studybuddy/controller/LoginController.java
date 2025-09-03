package org.example.studybuddy.controller;

import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeBox;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    private SessionManager sessionManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionManager = SessionManager.getInstance();

        // Pre-fill username if remembered
        if (sessionManager.hasRememberedUser()) {
            usernameField.setText(sessionManager.getRememberedUsername());
            rememberMeBox.setSelected(true);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeBox.isSelected();

        // Clear previous messages
        messageLabel.setText("");

        // Validation
        if (username.isEmpty()) {
            messageLabel.setText("Please enter a username");
            return;
        }

        if (password.isEmpty()) {
            messageLabel.setText("Please enter a password");
            return;
        }

        // TEMPORARY DEBUG CODE - Add this section
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Attempting login for: " + username);
        System.out.println("Password length: " + password.length());

        // Test database directly
        try {
            org.example.studybuddy.database.UserDAO testDAO = new org.example.studybuddy.database.UserDAO();
            boolean userExists = testDAO.usernameExists(username);
            System.out.println("Username exists in database: " + userExists);

            if (userExists) {
                org.example.studybuddy.model.User testUser = testDAO.loginUser(username, password);
                System.out.println("Login result: " + (testUser != null ? "SUCCESS" : "FAILED"));
            }
        } catch (Exception e) {
            System.out.println("Database test error: " + e.getMessage());
            e.printStackTrace();
        }
        // END DEBUG CODE

        // Disable button to prevent multiple clicks
        loginButton.setDisable(true);

        // Attempt login
        if (sessionManager.login(username, password, rememberMe)) {
            // Login successful
            SceneManager.getInstance().switchToDashboard();
        } else {
            // Login failed
            messageLabel.setText("Invalid username or password");
            passwordField.clear();
        }

        // Re-enable button
        loginButton.setDisable(false);
    }

    @FXML
    private void handleRegister() {
        SceneManager.getInstance().switchToRegister();
    }
}
