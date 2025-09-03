package org.example.studybuddy.util;

import org.example.studybuddy.model.User;
import org.example.studybuddy.model.UserStats;
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

    // Logout user
    public void logout() {
        this.currentUser = null;
        this.currentUserStats = null;
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
}
