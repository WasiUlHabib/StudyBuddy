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

        return null;
    }

    // UPDATED: Get real user statistics from exam logs
    public UserStats getUserStats(int userId) {
        String sql = """
            SELECT 
                COUNT(*) as total_exams,
                SUM(total_questions) as total_questions_attempted,
                SUM(correct_answers) as total_correct,
                SUM(wrong_answers) as total_wrong,
                SUM(unanswered) as total_unanswered,
                AVG(percentage) as average_percentage,
                MAX(percentage) as highest_score,
                AVG(time_taken) as average_time,
                COUNT(CASE WHEN exam_type = 'personal' OR exam_type IS NULL THEN 1 END) as personal_exams,
                COUNT(CASE WHEN exam_type = 'room' THEN 1 END) as room_exams
            FROM exam_logs 
            WHERE user_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserStats stats = new UserStats();
                    stats.setUserId(userId);
                    stats.setTotalExamsCompleted(rs.getInt("total_exams"));
                    stats.setTotalQuestionsAttempted(rs.getInt("total_questions_attempted"));
                    stats.setTotalCorrectAnswers(rs.getInt("total_correct"));
                    stats.setTotalWrongAnswers(rs.getInt("total_wrong"));
                    stats.setTotalUnanswered(rs.getInt("total_unanswered"));
                    stats.setAverageScore(rs.getDouble("average_percentage"));
                    stats.setHighestScore(rs.getDouble("highest_score"));
                    stats.setAverageTimePerExam(rs.getInt("average_time"));
                    stats.setPersonalExams(rs.getInt("personal_exams"));
                    stats.setRoomExams(rs.getInt("room_exams"));

                    System.out.println("DEBUG: Real user stats loaded - Total exams: " +
                            stats.getTotalExamsCompleted() + ", Accuracy: " +
                            String.format("%.1f%%", stats.getOverallAccuracy()));

                    return stats;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return empty stats for new users (no fake data!)
        System.out.println("DEBUG: New user with no exam history - returning empty stats");
        return new UserStats(); // Default constructor with zeros
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

    // NEW: Get daily statistics from real exam data (last N days)
    public List<DailyStat> getDailyStats(int userId, int daysBack) {
        List<DailyStat> dailyStats = new ArrayList<>();

        String sql = """
            SELECT 
                DATE(completed_at) as exam_date,
                COUNT(*) as exams_taken,
                SUM(total_questions) as questions_attempted,
                SUM(correct_answers) as questions_solved,
                AVG(percentage) as accuracy_percentage
            FROM exam_logs 
            WHERE user_id = ? 
            AND completed_at >= datetime('now', '-' || ? || ' days')
            GROUP BY DATE(completed_at)
            ORDER BY exam_date ASC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, daysBack);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DailyStat stat = new DailyStat();
                    stat.setUserId(userId);
                    stat.setDate(LocalDate.parse(rs.getString("exam_date")));
                    stat.setExamsTaken(rs.getInt("exams_taken"));
                    stat.setQuestionsAttempted(rs.getInt("questions_attempted"));
                    stat.setQuestionsSolved(rs.getInt("questions_solved"));
                    stat.setAccuracyPercentage(rs.getDouble("accuracy_percentage"));

                    dailyStats.add(stat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dailyStats;
    }

    // NEW: Get user's recent exam activity (for activity feed)
    public List<String> getRecentActivity(int userId, int limit) {
        List<String> activities = new ArrayList<>();

        String sql = """
            SELECT exam_name, percentage, correct_answers, total_questions, 
                   exam_type, completed_at
            FROM exam_logs 
            WHERE user_id = ? 
            ORDER BY completed_at DESC 
            LIMIT ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String examType = rs.getString("exam_type");
                    String typeIcon = "room".equals(examType) ? "🏠" : "📝";

                    String activity = String.format("%s %s - %.1f%% (%d/%d correct) - %s",
                            typeIcon,
                            rs.getString("exam_name"),
                            rs.getDouble("percentage"),
                            rs.getInt("correct_answers"),
                            rs.getInt("total_questions"),
                            rs.getTimestamp("completed_at").toLocalDateTime().toLocalDate()
                    );
                    activities.add(activity);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    // NEW: Get study streak (consecutive days with exam activity)
    public int getStudyStreak(int userId) {
        String sql = """
            SELECT COUNT(*) as streak
            FROM (
                SELECT DATE(completed_at) as exam_date
                FROM exam_logs 
                WHERE user_id = ?
                AND DATE(completed_at) >= (
                    SELECT DATE(completed_at)
                    FROM exam_logs 
                    WHERE user_id = ?
                    ORDER BY completed_at DESC 
                    LIMIT 1
                ) - (
                    SELECT COUNT(DISTINCT DATE(completed_at)) - 1
                    FROM exam_logs el1
                    WHERE user_id = ?
                    AND NOT EXISTS (
                        SELECT 1 FROM exam_logs el2 
                        WHERE el2.user_id = ?
                        AND DATE(el2.completed_at) = DATE(el1.completed_at) - 1
                    )
                )
                GROUP BY DATE(completed_at)
                ORDER BY exam_date DESC
            )
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("streak");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // NEW: Get performance trends (improvement over time)
    public boolean isImproving(int userId) {
        String sql = """
            SELECT 
                AVG(CASE WHEN row_num <= total_rows/2 THEN percentage END) as first_half_avg,
                AVG(CASE WHEN row_num > total_rows/2 THEN percentage END) as second_half_avg
            FROM (
                SELECT percentage, 
                       ROW_NUMBER() OVER (ORDER BY completed_at) as row_num,
                       COUNT(*) OVER () as total_rows
                FROM exam_logs 
                WHERE user_id = ?
            )
            WHERE total_rows >= 4
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double firstHalf = rs.getDouble("first_half_avg");
                    double secondHalf = rs.getDouble("second_half_avg");
                    return secondHalf > firstHalf;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Not enough data or error
    }

    // NEW: Get user's best subjects (by exam names)
    public List<String> getBestSubjects(int userId, int limit) {
        List<String> bestSubjects = new ArrayList<>();

        String sql = """
            SELECT exam_name, AVG(percentage) as avg_score, COUNT(*) as attempts
            FROM exam_logs 
            WHERE user_id = ?
            GROUP BY exam_name
            HAVING attempts >= 2
            ORDER BY avg_score DESC, attempts DESC
            LIMIT ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String subject = String.format("%s (%.1f%% avg, %d attempts)",
                            rs.getString("exam_name"),
                            rs.getDouble("avg_score"),
                            rs.getInt("attempts")
                    );
                    bestSubjects.add(subject);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bestSubjects;
    }

    // NEW: Check if user has any exam history (for UI decisions)
    public boolean hasExamHistory(int userId) {
        String sql = "SELECT COUNT(*) FROM exam_logs WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
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

    // REMOVED: addTestData method - no more simulated data!
    // REMOVED: updateUserStats method - stats come directly from exam_logs
    // REMOVED: updateDailyStats method - calculated dynamically from exam_logs
}
