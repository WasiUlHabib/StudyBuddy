package org.example.studybuddy.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private Map<Integer, List<ClientHandler>> roomClients = new ConcurrentHashMap<>();
    private boolean isRunning = false;

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("StudyBuddy Chat Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }

    public synchronized void addClientToRoom(int roomId, ClientHandler client) {
        roomClients.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>()).add(client);
        System.out.println("User " + client.getUsername() + " joined room " + roomId);
    }

    public synchronized void removeClientFromRoom(int roomId, ClientHandler client) {
        List<ClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            clients.remove(client);
            if (clients.isEmpty()) {
                roomClients.remove(roomId);
            }
        }
    }

    public void broadcastToRoom(int roomId, String message, ClientHandler sender) {
        List<ClientHandler> clients = roomClients.get(roomId);
        if (clients != null) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private int roomId;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Protocol: JOIN:username:roomId
            String joinMessage = in.readLine();
            String[] parts = joinMessage.split(":");

            if (parts.length == 3 && parts[0].equals("JOIN")) {
                username = parts[1];
                roomId = Integer.parseInt(parts[2]);

                server.addClientToRoom(roomId, this);
                server.broadcastToRoom(roomId, "SYSTEM:" + username + " joined the room", this);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("LEAVE")) {
                        break;
                    }
                    server.broadcastToRoom(roomId, "MESSAGE:" + username + ":" + message, this);
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    private void cleanup() {
        try {
            if (roomId > 0) {
                server.removeClientFromRoom(roomId, this);
                server.broadcastToRoom(roomId, "SYSTEM:" + username + " left the room", this);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}
