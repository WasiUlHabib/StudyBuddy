package org.example.studybuddy.controller;

import org.example.studybuddy.database.RoomDAO;
import org.example.studybuddy.model.Room;
import org.example.studybuddy.model.RoomParticipant;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RoomsController implements Initializable {

    // My Rooms Tab
    @FXML private Button createRoomButton;
    @FXML private TextField roomCodeField;
    @FXML private Button joinRoomButton;
    @FXML private ListView<Room> roomsListView;
    @FXML private Label roomMessageLabel;

    // Active Room Tab
    @FXML private Tab activeRoomTab;
    @FXML private Label activeRoomNameLabel;
    @FXML private Label activeRoomCodeLabel;
    @FXML private Label activeRoomDescriptionLabel;
    @FXML private Button leaveRoomButton;
    @FXML private TextArea chatArea;
    @FXML private TextField chatMessageField;
    @FXML private Button sendMessageButton;
    @FXML private ListView<String> sharedQuestionsListView;
    @FXML private Button shareQuestionsButton;
    @FXML private ListView<RoomParticipant> participantsListView;
    @FXML private Label participantCountLabel;
    @FXML private Button inviteParticipantsButton;

    private SessionManager sessionManager = SessionManager.getInstance();
    private RoomDAO roomDAO = new RoomDAO();
    private ObservableList<Room> userRooms = FXCollections.observableArrayList();
    private Room currentActiveRoom;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRoomsList();
        setupParticipantsList();
        loadUserRooms();
    }

    private void setupRoomsList() {
        roomsListView.setItems(userRooms);
        roomsListView.setCellFactory(new Callback<ListView<Room>, ListCell<Room>>() {
            @Override
            public ListCell<Room> call(ListView<Room> param) {
                return new ListCell<Room>() {
                    @Override
                    protected void updateItem(Room room, boolean empty) {
                        super.updateItem(room, empty);
                        if (empty || room == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(String.format("%s (%s) - %d/%d participants",
                                    room.getName(),
                                    room.getRoomCode(),
                                    room.getParticipantCount(),
                                    room.getMaxParticipants()
                            ));
                        }
                    }
                };
            }
        });

        // Double-click to enter room
        roomsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Room selectedRoom = roomsListView.getSelectionModel().getSelectedItem();
                if (selectedRoom != null) {
                    enterRoom(selectedRoom);
                }
            }
        });
    }

    private void setupParticipantsList() {
        participantsListView.setCellFactory(new Callback<ListView<RoomParticipant>, ListCell<RoomParticipant>>() {
            @Override
            public ListCell<RoomParticipant> call(ListView<RoomParticipant> param) {
                return new ListCell<RoomParticipant>() {
                    @Override
                    protected void updateItem(RoomParticipant participant, boolean empty) {
                        super.updateItem(participant, empty);
                        if (empty || participant == null) {
                            setText(null);
                        } else {
                            String roleIcon = participant.isAdmin() ? "👑" :
                                    participant.isModerator() ? "⭐" : "👤";
                            setText(roleIcon + " " + participant.getUsername());
                        }
                    }
                };
            }
        });
    }

    private void loadUserRooms() {
        List<Room> rooms = roomDAO.getUserRooms(sessionManager.getCurrentUser().getId());
        userRooms.setAll(rooms);
    }

    @FXML
    private void handleCreateRoom() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/studybuddy/view/CreateRoom.fxml"));
            Parent root = loader.load();

            CreateRoomController controller = loader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Create Room - StudyBuddy");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Failed to open create room dialog.", true);
        }
    }

    @FXML
    private void handleJoinRoom() {
        String roomCode = roomCodeField.getText().trim().toUpperCase();

        if (roomCode.isEmpty()) {
            showMessage("Please enter a room code.", true);
            return;
        }

        boolean success = roomDAO.joinRoom(roomCode, sessionManager.getCurrentUser().getId());

        if (success) {
            showMessage("Successfully joined room: " + roomCode, false);
            roomCodeField.clear();
            loadUserRooms();
        } else {
            showMessage("Failed to join room. Check the code or room may be full.", true);
        }
    }

    private void enterRoom(Room room) {
        currentActiveRoom = room;

        // Update active room tab
        activeRoomNameLabel.setText(room.getName());
        activeRoomCodeLabel.setText("(" + room.getRoomCode() + ")");
        activeRoomDescriptionLabel.setText(room.getDescription() != null ? room.getDescription() : "No description");

        // Load participants
        loadRoomParticipants();

        // Enable active room tab and switch to it
        activeRoomTab.setDisable(false);
        activeRoomTab.getTabPane().getSelectionModel().select(activeRoomTab);

        showMessage("Entered room: " + room.getName(), false);
    }

    private void loadRoomParticipants() {
        if (currentActiveRoom == null) return;

        List<RoomParticipant> participants = roomDAO.getRoomParticipants(currentActiveRoom.getId());
        ObservableList<RoomParticipant> participantsList = FXCollections.observableArrayList(participants);
        participantsListView.setItems(participantsList);

        participantCountLabel.setText(participants.size() + " members");
    }

    @FXML
    private void handleLeaveRoom() {
        if (currentActiveRoom == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Room");
        alert.setHeaderText("Are you sure you want to leave this room?");
        alert.setContentText("You can rejoin using the room code: " + currentActiveRoom.getRoomCode());

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean success = roomDAO.leaveRoom(currentActiveRoom.getId(), sessionManager.getCurrentUser().getId());

                if (success) {
                    showMessage("Left room successfully.", false);

                    // Disable active room tab and switch back to My Rooms
                    activeRoomTab.setDisable(true);
                    activeRoomTab.getTabPane().getSelectionModel().selectFirst();

                    currentActiveRoom = null;
                    loadUserRooms();
                } else {
                    showMessage("Failed to leave room.", true);
                }
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        String message = chatMessageField.getText().trim();
        if (!message.isEmpty() && currentActiveRoom != null) {
            // Add message to chat (for now, just display locally)
            String username = sessionManager.getCurrentUser().getUsername();
            String timestamp = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            chatArea.appendText(String.format("[%s] %s: %s\n", timestamp, username, message));
            chatMessageField.clear();

            // Scroll to bottom
            chatArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    @FXML
    private void handleShareQuestions() {
        // TODO: Implement question sharing functionality
        showMessage("Question sharing feature coming soon!", false);
    }

    @FXML
    private void handleInviteParticipants() {
        if (currentActiveRoom == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invite Participants");
        alert.setHeaderText("Room Code: " + currentActiveRoom.getRoomCode());
        alert.setContentText("Share this room code with others to invite them to join.");
        alert.showAndWait();
    }

    // Called by CreateRoomController when room is created
    public void onRoomCreated(Room room) {
        loadUserRooms();
        enterRoom(room);
    }

    private void showMessage(String message, boolean isError) {
        roomMessageLabel.setText(message);
        roomMessageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");

        // Clear message after 5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> roomMessageLabel.setText(""));
        delay.play();
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
    private void goToRooms() {
        // Already here
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
