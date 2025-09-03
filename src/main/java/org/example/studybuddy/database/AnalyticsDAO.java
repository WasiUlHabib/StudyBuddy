package org.example.studybuddy.database;

import org.example.studybuddy.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDAO {
    private Connection connection;

    public AnalyticsDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Get topic-wise performance for a user
    public List<TopicPerformance> getTopicPerformance(int userId) {
        List<TopicPerformance> performances = new ArrayList<>();
        String sql = """
            SELECT tp.*, t.name as topic_name 
            FROM topic_performance tp 
            JOIN topics t ON tp.topic_id = t.id 
            WHERE tp.user_id = ? 
            ORDER BY tp.accuracy_percentage DESC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TopicPerformance performance = new TopicPerformance();
                    performance.setId(rs.getInt("id"));
                    performance.setUserId(rs.getInt("user_id"));
                    performance.setTopicId(rs.getInt("topic_id"));
                    performance.setTopicName(rs.getString("topic_name"));
                    performance.setQuestionsAttempted(rs.getInt("questions_attempted"));
                    performance.setQuestionsSolved(rs.getInt("questions_solved"));
                    performance.setAccuracyPercentage(rs.getDouble("accuracy_percentage"));
                    performance.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());

                    performances.add(performance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return performances;
    }

    // Update topic performance
    public void updateTopicPerformance(int userId, int topicId, boolean isCorrect) {
        String sql = """
            INSERT INTO topic_performance (user_id, topic_id, questions_attempted, questions_solved, accuracy_percentage) 
            VALUES (?, ?, 1, ?, ?) 
            ON CONFLICT(user_id, topic_id) DO UPDATE SET
                questions_attempted = questions_attempted + 1,
                questions_solved = questions_solved + ?,
                accuracy_percentage = ((questions_solved + ?) * 100.0) / (questions_attempted + 1),
                last_updated = CURRENT_TIMESTAMP
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int solved = isCorrect ? 1 : 0;
            stmt.setInt(1, userId);
            stmt.setInt(2, topicId);
            stmt.setInt(3, solved);
            stmt.setDouble(4, solved * 100.0);
            stmt.setInt(5, solved);
            stmt.setInt(6, solved);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Record question attempt
    public void recordQuestionAttempt(QuestionAttempt attempt) {
        String sql = "INSERT INTO question_attempts (user_id, question_id, selected_answer, is_correct, time_taken, exam_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, attempt.getUserId());
            stmt.setInt(2, attempt.getQuestionId());
            stmt.setString(3, attempt.getSelectedAnswer());
            stmt.setBoolean(4, attempt.isCorrect());
            stmt.setInt(5, attempt.getTimeTaken());
            if (attempt.getExamId() != null) {
                stmt.setInt(6, attempt.getExamId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        // Update overall user performance after each exam
        public void updateUserPerformanceAfterExam(int userId, int correctAnswers, int totalQuestions) {
            String sql = """
                INSERT INTO user_performance (user_id, exams_count, correct_answers, total_questions, accuracy_percentage)
                VALUES (?, 1, ?, ?, ?)
                ON CONFLICT(user_id) DO UPDATE SET
                    exams_count = exams_count + 1,
                    correct_answers = correct_answers + ?,
                    total_questions = total_questions + ?,
                    accuracy_percentage = ((correct_answers + ?) * 100.0) / (total_questions + ?)
            """;
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, correctAnswers);
                stmt.setInt(3, totalQuestions);
                stmt.setDouble(4, totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0);
                stmt.setInt(5, correctAnswers);
                stmt.setInt(6, totalQuestions);
                stmt.setInt(7, correctAnswers);
                stmt.setInt(8, totalQuestions);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    // Get exam history with detailed information
    public List<ExamResult> getDetailedExamHistory(int userId, int limit) {
        List<ExamResult> results = new ArrayList<>();
        String sql = """
            SELECT er.*, e.name as exam_name, e.time_limit, e.negative_marks 
            FROM exam_results er 
            JOIN exams e ON er.exam_id = e.id 
            WHERE er.user_id = ? 
            ORDER BY er.completed_at DESC 
            LIMIT ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExamResult result = new ExamResult();
                    result.setId(rs.getInt("id"));
                    result.setExamId(rs.getInt("exam_id"));
                    result.setUserId(rs.getInt("user_id"));
                    result.setScore(rs.getDouble("score"));
                    result.setTotalQuestions(rs.getInt("total_questions"));
                    result.setCorrectAnswers(rs.getInt("correct_answers"));
                    result.setWrongAnswers(rs.getInt("wrong_answers"));
                    result.setUnanswered(rs.getInt("unanswered"));
                    result.setTimeTaken(rs.getInt("time_taken"));
                    result.setCompletedAt(rs.getTimestamp("completed_at").toLocalDateTime());

                    results.add(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    // Get performance trends over time
    public Map<LocalDate, Double> getPerformanceTrends(int userId, int days) {
        Map<LocalDate, Double> trends = new HashMap<>();
        String sql = """
            SELECT DATE(completed_at) as exam_date, AVG(score * 100.0 / total_questions) as avg_accuracy
            FROM exam_results 
            WHERE user_id = ? AND completed_at >= date('now', '-' || ? || ' days')
            GROUP BY DATE(completed_at)
            ORDER BY exam_date
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = LocalDate.parse(rs.getString("exam_date"));
                    double accuracy = rs.getDouble("avg_accuracy");
                    trends.put(date, accuracy);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return trends;
    }

    // Get question difficulty statistics
    public Map<Integer, Double> getQuestionDifficultyStats() {
        Map<Integer, Double> difficultyStats = new HashMap<>();
        String sql = """
            SELECT q.difficulty_level, 
                   AVG(CASE WHEN qa.is_correct THEN 1.0 ELSE 0.0 END) * 100 as success_rate
            FROM questions q
            LEFT JOIN question_attempts qa ON q.id = qa.question_id
            GROUP BY q.difficulty_level
            ORDER BY q.difficulty_level
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int difficulty = rs.getInt("difficulty_level");
                    double successRate = rs.getDouble("success_rate");
                    difficultyStats.put(difficulty, successRate);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return difficultyStats;
    }

    // Get user's strongest and weakest topics
    public Map<String, List<TopicPerformance>> getTopicStrengthsWeaknesses(int userId) {
        Map<String, List<TopicPerformance>> result = new HashMap<>();
        List<TopicPerformance> allPerformances = getTopicPerformance(userId);

        if (allPerformances.size() >= 2) {
            // Sort by accuracy
            allPerformances.sort((a, b) -> Double.compare(b.getAccuracyPercentage(), a.getAccuracyPercentage()));

            // Get top 3 strengths and bottom 3 weaknesses
            int strengthCount = Math.min(3, allPerformances.size());
            int weaknessCount = Math.min(3, allPerformances.size());

            result.put("strengths", allPerformances.subList(0, strengthCount));
            result.put("weaknesses", allPerformances.subList(
                    Math.max(0, allPerformances.size() - weaknessCount), allPerformances.size()));
        }

        return result;
    }
}
