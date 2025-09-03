package org.example.studybuddy.util;

import org.example.studybuddy.model.User;
import org.example.studybuddy.model.UserStats;
import org.example.studybuddy.model.Room; // NEW: Import Room model
import org.example.studybuddy.database.UserDAO;

import java.util.prefs.Preferences;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private UserStats currentUserStats;
    private UserDAO userDAO;

    // Preferences for "Remember Me" functionality
    private static final String PREF_USERNAME = "remembered_username";
    private static final String PREF_REMEMBER = "remember_me";
    private Preferences preferences;

    // NEW: Exam mode tracking
    public enum ExamMode {
        PERSONAL, ROOM
    }

    private ExamMode lastExamMode = ExamMode.PERSONAL;
    private Room lastActiveRoom = null;

    private SessionManager() {
        this.userDAO = new UserDAO();
        this.preferences = Preferences.userNodeForPackage(SessionManager.class);
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Login user and create session
    public boolean login(String username, String password, boolean rememberMe) {
        System.out.println("SessionManager.login called for: " + username); // Debug

        User user = userDAO.loginUser(username, password);

        if (user != null) {
            System.out.println("Database authentication successful for: " + username); // Debug
            this.currentUser = user;
            this.currentUserStats = userDAO.getUserStats(user.getId());

            // Handle "Remember Me" functionality
            if (rememberMe) {
                preferences.put(PREF_USERNAME, username);
                preferences.putBoolean(PREF_REMEMBER, true);
            } else {
                clearRememberedUser();
            }

            // NEW: Reset exam context on fresh login
            this.lastExamMode = ExamMode.PERSONAL;
            this.lastActiveRoom = null;

            System.out.println("Login successful for user: " + username);
            return true;
        } else {
            System.out.println("Database authentication failed for: " + username); // Debug
        }

        return false;
    }

    // Register new user
    public boolean register(String username, String password, String confirmPassword) {
        // Validation
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (password == null || password.length() < 4) {
            return false;
        }

        if (!password.equals(confirmPassword)) {
            return false;
        }

        return userDAO.registerUser(username.trim(), password);
    }

    // UPDATED: Logout user with exam context reset
    public void logout() {
        this.currentUser = null;
        this.currentUserStats = null;

        // NEW: Reset exam tracking on logout
        this.lastExamMode = ExamMode.PERSONAL;
        this.lastActiveRoom = null;

        System.out.println("User logged out successfully");
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Get current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Get current user stats
    public UserStats getCurrentUserStats() {
        return currentUserStats;
    }

    // Refresh user stats from database
    public void refreshUserStats() {
        if (currentUser != null) {
            this.currentUserStats = userDAO.getUserStats(currentUser.getId());
        }
    }

    // Remember Me functionality
    public boolean hasRememberedUser() {
        return preferences.getBoolean(PREF_REMEMBER, false);
    }

    public String getRememberedUsername() {
        return preferences.get(PREF_USERNAME, "");
    }

    public void clearRememberedUser() {
        preferences.remove(PREF_USERNAME);
        preferences.putBoolean(PREF_REMEMBER, false);
    }

    // Auto-login on startup
    public boolean attemptAutoLogin() {
        if (hasRememberedUser()) {
            String username = getRememberedUsername();
            if (!username.isEmpty()) {
                // For auto-login, we'd need to store the password too (not recommended)
                // Instead, we'll just pre-fill the username
                return false; // Don't auto-login, just remember username
            }
        }
        return false;
    }

    // NEW: Exam Mode Tracking Methods

    /**
     * Set the last exam mode (PERSONAL or ROOM)
     * @param examMode The exam mode that was just used
     */
    public void setLastExamMode(ExamMode examMode) {
        this.lastExamMode = examMode;
        System.out.println("DEBUG: Last exam mode set to: " + examMode);
    }

    /**
     * Get the last exam mode used
     * @return The last exam mode (defaults to PERSONAL)
     */
    public ExamMode getLastExamMode() {
        return lastExamMode;
    }

    /**
     * Set the last active room (for room-based exams)
     * @param room The room where the exam was taken
     */
    public void setLastActiveRoom(Room room) {
        this.lastActiveRoom = room;
        if (room != null) {
            System.out.println("DEBUG: Last active room set to: " + room.getName());
        } else {
            System.out.println("DEBUG: Last active room cleared");
        }
    }

    /**
     * Get the last active room
     * @return The last room where an exam was taken, or null if none
     */
    public Room getLastActiveRoom() {
        return lastActiveRoom;
    }

    /**
     * Check if the last exam was taken from a room context
     * @return true if last exam was from a room and room context is available
     */
    public boolean wasLastExamFromRoom() {
        return lastExamMode == ExamMode.ROOM && lastActiveRoom != null;
    }

    /**
     * Get a descriptive string of the current exam context
     * @return Description of current exam context for debugging
     */
    public String getExamContextDescription() {
        if (wasLastExamFromRoom()) {
            return String.format("Room exam from '%s' (ID: %d)",
                    lastActiveRoom.getName(), lastActiveRoom.getId());
        } else {
            return "Personal exam";
        }
    }

    /**
     * Reset exam context to default state
     * Useful when explicitly changing contexts
     */
    public void resetExamContext() {
        this.lastExamMode = ExamMode.PERSONAL;
        this.lastActiveRoom = null;
        System.out.println("DEBUG: Exam context reset to default (PERSONAL)");
    }
}
