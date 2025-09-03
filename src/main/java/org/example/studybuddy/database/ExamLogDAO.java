package org.example.studybuddy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.example.studybuddy.controller.ExamDetailController;
import org.example.studybuddy.model.ExamLog;

public class ExamLogDAO {
    private final Connection connection;

    public ExamLogDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    public boolean saveExamLog(ExamLog examLog) {
        String sql = """
            INSERT INTO exam_logs (user_id, exam_name, total_questions, correct_answers, 
                                 wrong_answers, unanswered, score, percentage, time_taken, 
                                 difficulty_avg, topics_covered, exam_type, room_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, examLog.getUserId());
            stmt.setString(2, examLog.getExamName());
            stmt.setInt(3, examLog.getTotalQuestions());
            stmt.setInt(4, examLog.getCorrectAnswers());
            stmt.setInt(5, examLog.getWrongAnswers());
            stmt.setInt(6, examLog.getUnanswered());
            stmt.setDouble(7, examLog.getScore());
            stmt.setDouble(8, examLog.getPercentage());
            stmt.setInt(9, examLog.getTimeTaken());
            stmt.setDouble(10, examLog.getDifficultyAvg());
            stmt.setString(11, examLog.getTopicsCovered());
            stmt.setString(12, examLog.getExamType());

            // Handle nullable room_id
            if (examLog.getRoomId() != null) {
                stmt.setInt(13, examLog.getRoomId());
            } else {
                stmt.setNull(13, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        examLog.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get user's exam history with pagination (UPDATED to read room_id)
    public List<ExamLog> getUserExamLogs(int userId, int limit, int offset) {
        List<ExamLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_logs 
            WHERE user_id = ? 
            ORDER BY completed_at DESC 
            LIMIT ? OFFSET ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToExamLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    // NEW: Convenience method without pagination
    public List<ExamLog> getUserExamLogs(int userId) {
        return getUserExamLogs(userId, 100, 0); // Default: last 100 exams
    }

    // NEW: Get only personal exams
    public List<ExamLog> getUserPersonalExamLogs(int userId) {
        List<ExamLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_logs 
            WHERE user_id = ? AND (exam_type = 'personal' OR exam_type IS NULL)
            ORDER BY completed_at DESC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToExamLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    // Get exam log by ID
    public ExamLog getExamLogById(int examId) {
        String sql = "SELECT * FROM exam_logs WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamLog(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // NEW: Get only room exams
    public List<ExamLog> getUserRoomExamLogs(int userId) {
        List<ExamLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_logs 
            WHERE user_id = ? AND exam_type = 'room'
            ORDER BY completed_at DESC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToExamLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    // NEW: Get exams for a specific room
    public List<ExamLog> getRoomExamLogs(int roomId) {
        List<ExamLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_logs 
            WHERE room_id = ? AND exam_type = 'room'
            ORDER BY completed_at DESC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToExamLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    // NEW: Helper method to map ResultSet to ExamLog (includes room_id)
    private ExamLog mapResultSetToExamLog(ResultSet rs) throws SQLException {
        ExamLog log = new ExamLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setExamName(rs.getString("exam_name"));
        log.setTotalQuestions(rs.getInt("total_questions"));
        log.setCorrectAnswers(rs.getInt("correct_answers"));
        log.setWrongAnswers(rs.getInt("wrong_answers"));
        log.setUnanswered(rs.getInt("unanswered"));
        log.setScore(rs.getDouble("score"));
        log.setPercentage(rs.getDouble("percentage"));
        log.setTimeTaken(rs.getInt("time_taken"));

        // Handle timestamp
        Timestamp timestamp = rs.getTimestamp("completed_at");
        if (timestamp != null) {
            log.setCompletedAt(timestamp.toLocalDateTime());
        }

        log.setExamType(rs.getString("exam_type"));

        // Handle nullable room_id
        try {
            Integer roomId = rs.getInt("room_id");
            if (rs.wasNull()) {
                roomId = null;
            }
            log.setRoomId(roomId);
        } catch (SQLException e) {
            log.setRoomId(null);
        }

        // Handle optional fields
        log.setDifficultyAvg(rs.getDouble("difficulty_avg"));
        log.setTopicsCovered(rs.getString("topics_covered"));

        return log;
    }

    // Get exam statistics (includes both personal and room exams)
    public ExamLogStats getUserExamStats(int userId) {
        String sql = """
            SELECT COUNT(*) as total_exams,
                   AVG(percentage) as avg_percentage,
                   MAX(percentage) as best_score,
                   AVG(time_taken) as avg_time,
                   COUNT(CASE WHEN exam_type = 'personal' OR exam_type IS NULL THEN 1 END) as personal_exams,
                   COUNT(CASE WHEN exam_type = 'room' THEN 1 END) as room_exams
            FROM exam_logs WHERE user_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ExamLogStats(
                            rs.getInt("total_exams"),
                            rs.getDouble("avg_percentage"),
                            rs.getDouble("best_score"),
                            rs.getInt("avg_time"),
                            rs.getInt("personal_exams"),
                            rs.getInt("room_exams")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ExamLogStats(0, 0, 0, 0, 0, 0);
    }

    // NEW: Get room statistics
    public RoomExamStats getRoomExamStats(int roomId) {
        String sql = """
            SELECT COUNT(*) as total_attempts,
                   COUNT(DISTINCT user_id) as unique_participants,
                   AVG(percentage) as avg_score,
                   MAX(percentage) as best_score
            FROM exam_logs 
            WHERE room_id = ? AND exam_type = 'room'
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new RoomExamStats(
                            rs.getInt("total_attempts"),
                            rs.getInt("unique_participants"),
                            rs.getDouble("avg_score"),
                            rs.getDouble("best_score")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new RoomExamStats(0, 0, 0, 0);
    }

    // UPDATED: Enhanced statistics class
    public static class ExamLogStats {
        public final int totalExams;
        public final double avgPercentage;
        public final double bestScore;
        public final int avgTime;
        public final int personalExams;
        public final int roomExams;

        public ExamLogStats(int totalExams, double avgPercentage, double bestScore,
                            int avgTime, int personalExams, int roomExams) {
            this.totalExams = totalExams;
            this.avgPercentage = avgPercentage;
            this.bestScore = bestScore;
            this.avgTime = avgTime;
            this.personalExams = personalExams;
            this.roomExams = roomExams;
        }
    }

    // NEW: Room statistics class
    public static class RoomExamStats {
        public final int totalAttempts;
        public final int uniqueParticipants;
        public final double avgScore;
        public final double bestScore;

        public RoomExamStats(int totalAttempts, int uniqueParticipants,
                             double avgScore, double bestScore) {
            this.totalAttempts = totalAttempts;
            this.uniqueParticipants = uniqueParticipants;
            this.avgScore = avgScore;
            this.bestScore = bestScore;
        }
    }

    // NEW: Delete exam log
    public boolean deleteExamLog(int examLogId, int userId) {
        String sql = "DELETE FROM exam_logs WHERE id = ? AND user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examLogId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Get recent exam activity across all rooms
    public List<ExamLog> getRecentRoomActivity(int limit) {
        List<ExamLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_logs 
            WHERE exam_type = 'room' 
            ORDER BY completed_at DESC 
            LIMIT ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToExamLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    // Get detailed question information for an exam
    public List<ExamDetailController.QuestionDetail> getExamQuestionDetails(int examId) {
        List<ExamDetailController.QuestionDetail> details = new ArrayList<>();
        String sql = """
            SELECT eq.question_number, 
                   q.question_text,
                   eq.selected_option,
                   q.correct_option,
                   CASE 
                       WHEN eq.selected_option IS NULL THEN 'Unanswered'
                       WHEN eq.selected_option = q.correct_option THEN 'Correct'
                       ELSE 'Incorrect'
                   END as result
            FROM exam_question_logs eq
            JOIN questions q ON eq.question_id = q.id
            WHERE eq.exam_log_id = ?
            ORDER BY eq.question_number
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    details.add(new ExamDetailController.QuestionDetail(
                        rs.getInt("question_number"),
                        rs.getString("question_text"),
                        rs.getString("selected_option"),
                        rs.getString("correct_option"),
                        rs.getString("result")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
}
