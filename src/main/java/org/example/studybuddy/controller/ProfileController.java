package org.example.studybuddy.controller;

import org.example.studybuddy.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private Label profileLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: Load actual user profile in Phase 2
        profileLabel.setText("User profile will be loaded here");
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
        SceneManager.getInstance().switchToLogin();
    }
}
