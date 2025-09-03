package org.example.studybuddy.controller;

import org.example.studybuddy.database.RoomDAO;
import org.example.studybuddy.model.Room;
import org.example.studybuddy.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CreateRoomController implements Initializable {

    @FXML private TextField roomNameField;
    @FXML private TextArea roomDescriptionArea;
    @FXML private Spinner<Integer> maxParticipantsSpinner;
    @FXML private CheckBox allowChatCheckBox;
    @FXML private CheckBox allowQuestionSharingCheckBox;
    @FXML private CheckBox allowGroupExamsCheckBox;
    @FXML private Button createButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    private SessionManager sessionManager = SessionManager.getInstance();
    private RoomDAO roomDAO = new RoomDAO();
    private RoomsController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup spinner for max participants
        maxParticipantsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 100, 10));
        maxParticipantsSpinner.setEditable(true);
    }

    public void setParentController(RoomsController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleCreateRoom() {
        String roomName = roomNameField.getText().trim();
        String description = roomDescriptionArea.getText().trim();
        int maxParticipants = maxParticipantsSpinner.getValue();

        // Validation
        if (roomName.isEmpty()) {
            showMessage("Room name is required.", true);
            return;
        }

        if (roomName.length() < 3) {
            showMessage("Room name must be at least 3 characters long.", true);
            return;
        }

        // Create room
        createButton.setDisable(true);

        try {
            Room room = roomDAO.createRoom(roomName, description.isEmpty() ? null : description,
                    sessionManager.getCurrentUser().getId(), maxParticipants);

            if (room != null) {
                showMessage("Room created successfully! Code: " + room.getRoomCode(), false);

                // Notify parent and close
                if (parentController != null) {
                    parentController.onRoomCreated(room);
                }

                // Close dialog after 2 seconds
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                delay.setOnFinished(e -> handleCancel());
                delay.play();

            } else {
                showMessage("Failed to create room. Please try again.", true);
            }

        } catch (Exception e) {
            showMessage("Error: " + e.getMessage(), true);
            e.printStackTrace();
        } finally {
            createButton.setDisable(false);
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
}
