package org.example.studybuddy.database;

import org.example.studybuddy.model.User;
import org.example.studybuddy.model.UserStats;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDate;
import org.example.studybuddy.model.DailyStat;
import java.util.List;
import java.util.ArrayList;

import java.sql.*;

public class UserDAO {
    private Connection connection;
    private BCryptPasswordEncoder passwordEncoder;

    public UserDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Register a new user
    public boolean registerUser(String username, String password) {
        if (usernameExists(username)) {
            return false;
        }

        String hashedPassword = passwordEncoder.encode(password);

        String insertUserSQL = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        String insertStatsSQL = "INSERT INTO user_stats (user_id) VALUES (?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement userStmt = connection.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, username);
                userStmt.setString(2, hashedPassword);

                int affectedRows = userStmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                int userId;
                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        userId = generatedKeys.getInt(1);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }

                try (PreparedStatement statsStmt = connection.prepareStatement(insertStatsSQL)) {
                    statsStmt.setInt(1, userId);
                    statsStmt.executeUpdate();
                }

                connection.commit();
                System.out.println("User registered successfully: " + username);
                return true;
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Check if username already exists
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Login user - verify credentials
    public User loginUser(String username, String password) {
        System.out.println("UserDAO.loginUser called for: " + username);

        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User found in database: " + username);
                    String storedHash = rs.getString("password_hash");

                    // Verify password - THIS LINE SHOULD NOW WORK
                    boolean passwordMatch = passwordEncoder.matches(password, storedHash);
                    System.out.println("Password match result: " + passwordMatch);

                    if (passwordMatch) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                storedHash,
                                rs.getTimestamp("created_at").toLocalDateTime()
                        );
                    }
                } else {
                    System.out.println("User not found in database: " + username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Login failed
    }

    // Get user statistics
    public UserStats getUserStats(int userId) {
        String sql = "SELECT * FROM user_stats WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserStats(
                            rs.getInt("user_id"),
                            rs.getInt("questions_attempted"),
                            rs.getInt("questions_solved"),
                            rs.getInt("exams_taken")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new UserStats(userId);
    }

    // Get user by ID
    public User getUserById(int userId) {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Update user statistics (call this when user completes questions/exams)
    public boolean updateUserStats(int userId, int questionsAttempted, int questionsSolved, int examsTaken) {
        String updateStatsSQL = "UPDATE user_stats SET questions_attempted = questions_attempted + ?, questions_solved = questions_solved + ?, exams_taken = exams_taken + ? WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateStatsSQL)) {
            stmt.setInt(1, questionsAttempted);
            stmt.setInt(2, questionsSolved);
            stmt.setInt(3, examsTaken);
            stmt.setInt(4, userId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Also update daily stats
                updateDailyStats(userId, questionsAttempted, questionsSolved, examsTaken);
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Update daily statistics for performance graphs
    public void updateDailyStats(int userId, int questionsAttempted, int questionsSolved, int examsTaken) {
        LocalDate today = LocalDate.now();

        String insertOrUpdateSQL = """
        INSERT INTO daily_stats (user_id, date, questions_attempted, questions_solved, exams_taken, accuracy_percentage)
        VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT(user_id, date) DO UPDATE SET
            questions_attempted = questions_attempted + excluded.questions_attempted,
            questions_solved = questions_solved + excluded.questions_solved,
            exams_taken = exams_taken + excluded.exams_taken,
            accuracy_percentage = CASE 
                WHEN (questions_attempted + excluded.questions_attempted) > 0 
                THEN ((questions_solved + excluded.questions_solved) * 100.0) / (questions_attempted + excluded.questions_attempted)
                ELSE 0.0 
            END
    """;

        try (PreparedStatement stmt = connection.prepareStatement(insertOrUpdateSQL)) {
            stmt.setInt(1, userId);
            stmt.setString(2, today.toString());
            stmt.setInt(3, questionsAttempted);
            stmt.setInt(4, questionsSolved);
            stmt.setInt(5, examsTaken);
            stmt.setDouble(6, questionsAttempted > 0 ? (double) questionsSolved / questionsAttempted * 100 : 0.0);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get daily statistics for performance graphs
    public List<DailyStat> getDailyStats(int userId, int daysBack) {
        List<DailyStat> dailyStats = new ArrayList<>();

        String sql = """
        SELECT * FROM daily_stats 
        WHERE user_id = ? AND date >= date('now', '-' || ? || ' days')
        ORDER BY date ASC
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, daysBack);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DailyStat stat = new DailyStat();
                    stat.setId(rs.getInt("id"));
                    stat.setUserId(rs.getInt("user_id"));
                    stat.setDate(LocalDate.parse(rs.getString("date")));
                    stat.setQuestionsAttempted(rs.getInt("questions_attempted"));
                    stat.setQuestionsSolved(rs.getInt("questions_solved"));
                    stat.setExamsTaken(rs.getInt("exams_taken"));
                    stat.setAccuracyPercentage(rs.getDouble("accuracy_percentage"));

                    dailyStats.add(stat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dailyStats;
    }

    // Add some test data for demonstration (temporary method)
    public void addTestData(int userId) {
        // Add some sample performance data for the last 7 days
        for (int i = 7; i >= 1; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            int attempted = (int) (Math.random() * 10) + 5; // 5-15 questions
            int solved = (int) (attempted * (0.6 + Math.random() * 0.3)); // 60-90% accuracy

            String sql = "INSERT OR REPLACE INTO daily_stats (user_id, date, questions_attempted, questions_solved, exams_taken, accuracy_percentage) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, date.toString());
                stmt.setInt(3, attempted);
                stmt.setInt(4, solved);
                stmt.setInt(5, i % 3 == 0 ? 1 : 0); // Exam every 3rd day
                stmt.setDouble(6, attempted > 0 ? (double) solved / attempted * 100 : 0.0);

                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Also update overall user stats
        updateUserStats(userId, 50, 35, 3); // Sample total stats
    }
}
