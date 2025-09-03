package org.example.studybuddy.network;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;
    private boolean isConnected = false;

    public boolean connect(String username, int roomId, Consumer<String> messageHandler) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            this.onMessageReceived = messageHandler;

            // Send join message
            out.println("JOIN:" + username + ":" + roomId);

            // Start message listener thread
            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();

            isConnected = true;
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to chat server: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        if (isConnected && out != null) {
            out.println(message);
        }
    }

    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            if (out != null) {
                out.println("LEAVE");
            }
            cleanup();
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                final String finalMessage = message;
                Platform.runLater(() -> {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(finalMessage);
                    }
                });
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Error reading messages: " + e.getMessage());
            }
        }
    }

    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
