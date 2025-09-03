package org.example.studybuddy.util;

import org.example.studybuddy.model.RoomMessage;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChatSimulator {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<Consumer<RoomMessage>> messageListeners = new ArrayList<>();
    private Random random = new Random();
    private int currentRoomId;

    // Sample bot users and messages for simulation
    private String[] botUsers = {"StudyBot", "QuizMaster", "Helper", "MathTutor", "ScienceGuru"};
    private String[] sampleMessages = {
            "Good luck with your studies!",
            "Anyone need help with this topic?",
            "Let's work together on this problem.",
            "Great question! Here's my approach...",
            "I found this concept challenging too.",
            "Has anyone tried the practice problems?",
            "The exam is coming up soon!",
            "Remember to review the key concepts.",
            "This study group is really helpful!",
            "Don't forget to take breaks while studying."
    };

    public void startSimulation(int roomId) {
        this.currentRoomId = roomId;

        // Simulate messages every 15-30 seconds
        scheduler.scheduleWithFixedDelay(() -> {
            if (!messageListeners.isEmpty()) {
                simulateMessage();
            }
        }, 10, 15 + random.nextInt(15), TimeUnit.SECONDS);

        // Add initial welcome message
        Platform.runLater(() -> {
            RoomMessage welcomeMessage = new RoomMessage(roomId, 0, "System",
                    "Welcome to the study room! Feel free to ask questions and help each other.", "system");
            notifyListeners(welcomeMessage);
        });
    }

    private void simulateMessage() {
        String botUser = botUsers[random.nextInt(botUsers.length)];
        String message = sampleMessages[random.nextInt(sampleMessages.length)];

        RoomMessage simulatedMessage = new RoomMessage(currentRoomId, 999, botUser, message, "text");

        Platform.runLater(() -> notifyListeners(simulatedMessage));
    }

    public void addMessageListener(Consumer<RoomMessage> listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Consumer<RoomMessage> listener) {
        messageListeners.remove(listener);
    }

    private void notifyListeners(RoomMessage message) {
        messageListeners.forEach(listener -> listener.accept(message));
    }

    public void stopSimulation() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    // Send user join/leave notifications
    public void notifyUserJoined(int roomId, String username) {
        Platform.runLater(() -> {
            RoomMessage joinMessage = new RoomMessage(roomId, 0, "System",
                    username + " joined the room", "system");
            notifyListeners(joinMessage);
        });
    }

    public void notifyUserLeft(int roomId, String username) {
        Platform.runLater(() -> {
            RoomMessage leaveMessage = new RoomMessage(roomId, 0, "System",
                    username + " left the room", "system");
            notifyListeners(leaveMessage);
        });
    }
}
