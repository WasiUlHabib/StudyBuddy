package org.example.studybuddy.database;

import org.example.studybuddy.model.ExamLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamLogDAO {
    private Connection connection;

    public ExamLogDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Save exam log
    public boolean saveExamLog(ExamLog examLog) {
        String sql = """
            INSERT INTO exam_logs (user_id, exam_name, total_questions, correct_answers, 
                                 wrong_answers, unanswered, score, percentage, time_taken, 
                                 difficulty_avg, topics_covered, exam_type, room_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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

            if (examLog.getRoomId() != null) {
                stmt.setInt(13, examLog.getRoomId());
            } else {
                stmt.setNull(13, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get user's exam history with pagination
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
                    log.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());
                    log.setExamType(rs.getString("exam_type"));

                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    // Get exam statistics
    public ExamLogStats getUserExamStats(int userId) {
        String sql = """
            SELECT COUNT(*) as total_exams,
                   AVG(percentage) as avg_percentage,
                   MAX(percentage) as best_score,
                   AVG(time_taken) as avg_time
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
                            rs.getInt("avg_time")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ExamLogStats(0, 0, 0, 0);
    }

    // Inner class for statistics
    public static class ExamLogStats {
        public final int totalExams;
        public final double avgPercentage;
        public final double bestScore;
        public final int avgTime;

        public ExamLogStats(int totalExams, double avgPercentage, double bestScore, int avgTime) {
            this.totalExams = totalExams;
            this.avgPercentage = avgPercentage;
            this.bestScore = bestScore;
            this.avgTime = avgTime;
        }
    }
}
