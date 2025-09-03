package org.example.studybuddy.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.example.studybuddy.database.RoomDAO;
import org.example.studybuddy.database.QuestionDAO;
import org.example.studybuddy.model.Room;
import org.example.studybuddy.model.RoomParticipant;
import org.example.studybuddy.model.RoomMessage;
import org.example.studybuddy.model.Question;
import org.example.studybuddy.model.Topic;
import org.example.studybuddy.model.Subtopic;
import org.example.studybuddy.util.ChatSimulator;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;

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
    private QuestionDAO questionDAO = new QuestionDAO();
    private ObservableList<Room> userRooms = FXCollections.observableArrayList();
    private Room currentActiveRoom;
    private ObservableList<RoomMessage> chatMessages = FXCollections.observableArrayList();
    private ChatSimulator chatSimulator = new ChatSimulator();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRoomsList();
        setupParticipantsList();
        setupSharedQuestionsList();
        loadUserRooms();
    }

    private void setupSharedQuestionsList() {
        sharedQuestionsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedQuestion = sharedQuestionsListView.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    showQuestionDetails(selectedQuestion);
                }
            }
        });
    }

    private void showQuestionDetails(String questionSummary) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Question Details");
        alert.setHeaderText("Shared Question");
        alert.setContentText("Full question details would be displayed here in a real implementation.\n\nSummary: " + questionSummary);
        alert.showAndWait();
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

    // UPDATED: Enhanced enterRoom method with shared questions loading
    private void enterRoom(Room room) {
        currentActiveRoom = room;

        // Update active room tab
        activeRoomNameLabel.setText(room.getName());
        activeRoomCodeLabel.setText("(" + room.getRoomCode() + ")");
        activeRoomDescriptionLabel.setText(room.getDescription() != null ? room.getDescription() : "No description");

        // Load participants
        loadRoomParticipants();

        // ADDED: Load shared questions visible to all members
        loadRoomSharedQuestions();

        // Load and setup chat
        setupRoomChat();

        // Enable active room tab and switch to it
        activeRoomTab.setDisable(false);
        activeRoomTab.getTabPane().getSelectionModel().select(activeRoomTab);

        showMessage("Entered room: " + room.getName(), false);

        // Notify system of user join
        roomDAO.addSystemMessage(room.getId(), sessionManager.getCurrentUser().getUsername() + " joined the room");
    }

    // NEW: Load shared questions for all room members
    private void loadRoomSharedQuestions() {
        if (currentActiveRoom == null) return;

        List<Question> roomQuestions = roomDAO.getRoomSharedQuestions(currentActiveRoom.getId());
        ObservableList<String> questionSummaries = FXCollections.observableArrayList();

        for (Question q : roomQuestions) {
            String summary = String.format("[%s] %s",
                    q.getSharedByUsername(),
                    q.getQuestionText().length() > 50 ?
                            q.getQuestionText().substring(0, 50) + "..." :
                            q.getQuestionText());
            questionSummaries.add(summary);
        }

        sharedQuestionsListView.setItems(questionSummaries);
    }

    // New method to setup chat functionality
    private void setupRoomChat() {
        if (currentActiveRoom == null) return;

        // Load recent messages
        loadRecentMessages();

        // Setup chat message display
        chatMessages.addListener((javafx.collections.ListChangeListener<RoomMessage>) change -> {
            updateChatDisplay();
        });

        // Start chat simulation
        chatSimulator.addMessageListener(this::handleIncomingMessage);
        chatSimulator.startSimulation(currentActiveRoom.getId());

        // Enable enter key for sending messages
        chatMessageField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSendMessage();
            }
        });
    }

    // Load recent chat messages
    private void loadRecentMessages() {
        if (currentActiveRoom == null) return;

        List<RoomMessage> recentMessages = roomDAO.getRecentMessages(currentActiveRoom.getId(), 50);
        chatMessages.setAll(recentMessages);
    }

    // Handle incoming messages (from simulation or real network)
    private void handleIncomingMessage(RoomMessage message) {
        Platform.runLater(() -> {
            chatMessages.add(message);
            roomDAO.saveMessage(message); // Save to database
        });
    }

    // Update chat display
    private void updateChatDisplay() {
        StringBuilder chatContent = new StringBuilder();
        for (RoomMessage message : chatMessages) {
            chatContent.append(message.getDisplayText()).append("\n");
        }
        chatArea.setText(chatContent.toString());

        // Auto-scroll to bottom
        chatArea.setScrollTop(Double.MAX_VALUE);
    }

    private void loadRoomParticipants() {
        if (currentActiveRoom == null) return;

        List<RoomParticipant> participants = roomDAO.getRoomParticipants(currentActiveRoom.getId());
        ObservableList<RoomParticipant> participantsList = FXCollections.observableArrayList(participants);
        participantsListView.setItems(participantsList);

        participantCountLabel.setText(participants.size() + " members");
    }

    // Enhanced leave room method
    @FXML
    private void handleLeaveRoom() {
        if (currentActiveRoom == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Room");
        alert.setHeaderText("Are you sure you want to leave this room?");
        alert.setContentText("You can rejoin using the room code: " + currentActiveRoom.getRoomCode());

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Add system message for user leaving
                roomDAO.addSystemMessage(currentActiveRoom.getId(),
                        sessionManager.getCurrentUser().getUsername() + " left the room");

                // Stop chat simulation
                chatSimulator.stopSimulation();
                chatSimulator.removeMessageListener(this::handleIncomingMessage);

                boolean success = roomDAO.leaveRoom(currentActiveRoom.getId(), sessionManager.getCurrentUser().getId());

                if (success) {
                    showMessage("Left room successfully.", false);

                    // Clear chat and shared questions
                    chatMessages.clear();
                    chatArea.clear();
                    sharedQuestionsListView.getItems().clear();

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
        String messageText = chatMessageField.getText().trim();
        if (!messageText.isEmpty() && currentActiveRoom != null) {
            // Create user message
            RoomMessage userMessage = new RoomMessage(
                    currentActiveRoom.getId(),
                    sessionManager.getCurrentUser().getId(),
                    sessionManager.getCurrentUser().getUsername(),
                    messageText,
                    "text"
            );

            // Add to chat immediately
            chatMessages.add(userMessage);

            // Save to database
            roomDAO.saveMessage(userMessage);

            // Clear input
            chatMessageField.clear();

            // In a real application, this would send to other connected users
            // For now, we just save it locally
        }
    }

    @FXML
    private void handleShareQuestions() {
        if (currentActiveRoom == null) return;

        // Create dialog to select questions to share
        Dialog<List<Question>> dialog = new Dialog<>();
        dialog.setTitle("Share Questions with Room");
        dialog.setHeaderText("Select questions to share with all room members");

        // Create UI for question selection
        VBox content = new VBox(10);

        // Get user's questions
        List<Topic> userTopics = questionDAO.getAllTopics();
        CheckBox selectAllCheckBox = new CheckBox("Select All");
        content.getChildren().add(selectAllCheckBox);

        List<CheckBox> questionCheckBoxes = new ArrayList<>();
        ScrollPane scrollPane = new ScrollPane();
        VBox questionsList = new VBox(5);

        for (Topic topic : userTopics) {
            Label topicLabel = new Label(topic.getName());
            topicLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            questionsList.getChildren().add(topicLabel);

            List<Subtopic> subtopics = questionDAO.getSubtopicsByTopic(topic.getId());
            for (Subtopic subtopic : subtopics) {
                List<Question> questions = questionDAO.getQuestionsBySubtopic(subtopic.getId());
                for (Question question : questions) {
                    CheckBox questionCB = new CheckBox(question.getQuestionText().length() > 60 ?
                            question.getQuestionText().substring(0, 60) + "..." :
                            question.getQuestionText());
                    questionCB.setUserData(question);
                    questionCheckBoxes.add(questionCB);
                    questionsList.getChildren().add(questionCB);
                }
            }
        }

        scrollPane.setContent(questionsList);
        scrollPane.setPrefHeight(300);
        content.getChildren().add(scrollPane);

        // Select all functionality
        selectAllCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            questionCheckBoxes.forEach(cb -> cb.setSelected(newVal));
        });

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return questionCheckBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (Question) cb.getUserData())
                        .collect(Collectors.toList());
            }
            return null;
        });

        Optional<List<Question>> result = dialog.showAndWait();

        result.ifPresent(selectedQuestions -> {
            if (!selectedQuestions.isEmpty()) {
                shareQuestionsWithAllMembers(selectedQuestions);
            }
        });
    }

    // UPDATED: Enhanced question sharing for all room members
    private void shareQuestionsWithAllMembers(List<Question> questions) {
        if (currentActiveRoom == null) return;

        int successCount = 0;

        // Add each question to room's shared collection
        for (Question question : questions) {
            if (roomDAO.addQuestionToRoom(currentActiveRoom.getId(), question.getId(),
                    sessionManager.getCurrentUser().getId())) {
                successCount++;
            }
        }

        if (successCount > 0) {
            // Refresh shared questions display for current user
            loadRoomSharedQuestions();

            // Add system message visible to all members
            String username = sessionManager.getCurrentUser().getUsername();
            String message = username + " shared " + successCount + " questions with the room";
            roomDAO.addSystemMessage(currentActiveRoom.getId(), message);

            // Add to chat
            RoomMessage shareMessage = new RoomMessage(currentActiveRoom.getId(), 0, "System", message, "system");
            handleIncomingMessage(shareMessage);

            showMessage("Successfully shared " + successCount + " questions with all members!", false);
        } else {
            showMessage("Failed to share questions. They may already be shared.", true);
        }
    }

    // REMOVED: Old shareQuestionsWithRoom method - replaced with shareQuestionsWithAllMembers

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
