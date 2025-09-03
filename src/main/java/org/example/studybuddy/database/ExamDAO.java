package org.example.studybuddy.database;

import org.example.studybuddy.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ExamDAO {
    private Connection connection;
    private QuestionDAO questionDAO;

    public ExamDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.questionDAO = new QuestionDAO();
    }

    // Create a new exam and return its ID
    public int createExam(Exam exam) {
        String sql = "INSERT INTO exams (user_id, name, time_limit, negative_marks, total_questions) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, exam.getUserId());
            stmt.setString(2, exam.getName());
            stmt.setInt(3, exam.getTimeLimit());
            stmt.setBoolean(4, exam.isNegativeMarks());
            stmt.setInt(5, exam.getTotalQuestions());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int examId = generatedKeys.getInt(1);
                        exam.setId(examId);
                        return examId;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // Generate random questions for exam
    public List<Question> generateExamQuestions(Map<Integer, Integer> subtopicQuestionCounts) {
        List<Question> examQuestions = new ArrayList<>();
        Random random = new Random();

        for (Map.Entry<Integer, Integer> entry : subtopicQuestionCounts.entrySet()) {
            int subtopicId = entry.getKey();
            int questionCount = entry.getValue();

            // Get all questions from this subtopic
            List<Question> availableQuestions = questionDAO.getQuestionsBySubtopic(subtopicId);

            if (availableQuestions.size() < questionCount) {
                throw new IllegalArgumentException("Not enough questions in subtopic ID: " + subtopicId +
                        ". Available: " + availableQuestions.size() + ", Requested: " + questionCount);
            }

            // Randomly select questions
            Collections.shuffle(availableQuestions);
            examQuestions.addAll(availableQuestions.subList(0, questionCount));
        }

        // Shuffle the final question list
        Collections.shuffle(examQuestions);
        return examQuestions;
    }

    // Save exam questions to database
    public boolean saveExamQuestions(int examId, List<Question> questions) {
        String sql = "INSERT INTO exam_questions (exam_id, question_id, question_order) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (int i = 0; i < questions.size(); i++) {
                stmt.setInt(1, examId);
                stmt.setInt(2, questions.get(i).getId());
                stmt.setInt(3, i + 1); // 1-based ordering
                stmt.addBatch();
            }

            stmt.executeBatch();
            connection.commit();
            return true;

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

    // Get exam questions in order
    public List<Question> getExamQuestions(int examId) {
        List<Question> questions = new ArrayList<>();
        String sql = """
            SELECT q.*, eq.question_order, eq.user_answer 
            FROM exam_questions eq 
            JOIN questions q ON eq.question_id = q.id 
            WHERE eq.exam_id = ? 
            ORDER BY eq.question_order
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question(
                            rs.getInt("id"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_answer"),
                            rs.getString("explanation"),
                            rs.getInt("difficulty_level"),
                            rs.getInt("subtopic_id"),
                            rs.getInt("created_by"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }

    // Save user's answer for a question
    public boolean saveUserAnswer(int examId, int questionOrder, String userAnswer) {
        String sql = "UPDATE exam_questions SET user_answer = ? WHERE exam_id = ? AND question_order = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userAnswer);
            stmt.setInt(2, examId);
            stmt.setInt(3, questionOrder);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Calculate and save exam result
    public ExamResult calculateExamResult(int examId, int userId, boolean negativeMarks) {
        List<Question> questions = getExamQuestions(examId);
        int correct = 0, wrong = 0, unanswered = 0;
        double score = 0.0;

        String getUserAnswersSQL = "SELECT user_answer FROM exam_questions WHERE exam_id = ? ORDER BY question_order";

        try (PreparedStatement stmt = connection.prepareStatement(getUserAnswersSQL)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                int index = 0;
                while (rs.next() && index < questions.size()) {
                    String userAnswer = rs.getString("user_answer");
                    Question question = questions.get(index);

                    if (userAnswer == null || userAnswer.trim().isEmpty()) {
                        unanswered++;
                    } else if (userAnswer.equals(question.getCorrectAnswer())) {
                        correct++;
                        score += 1.0;
                    } else {
                        wrong++;
                        if (negativeMarks) {
                            score -= 0.25; // Deduct 0.25 for wrong answer
                        }
                    }
                    index++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Create and save result
        ExamResult result = new ExamResult(examId, userId, score, questions.size(), correct, wrong, unanswered, 0);
        saveExamResult(result);

        return result;
    }

    // Save exam result to database
    public boolean saveExamResult(ExamResult result) {
        String sql = "INSERT INTO exam_results (exam_id, user_id, score, total_questions, correct_answers, wrong_answers, unanswered, time_taken) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, result.getExamId());
            stmt.setInt(2, result.getUserId());
            stmt.setDouble(3, result.getScore());
            stmt.setInt(4, result.getTotalQuestions());
            stmt.setInt(5, result.getCorrectAnswers());
            stmt.setInt(6, result.getWrongAnswers());
            stmt.setInt(7, result.getUnanswered());
            stmt.setInt(8, result.getTimeTaken());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        result.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Exam getExamById(int examId) {
        String sql = "SELECT * FROM exams WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Exam exam = new Exam();
                    exam.setId(rs.getInt("id"));
                    exam.setUserId(rs.getInt("user_id"));
                    exam.setName(rs.getString("name"));
                    exam.setTimeLimit(rs.getInt("time_limit"));
                    exam.setNegativeMarks(rs.getBoolean("negative_marks"));
                    exam.setTotalQuestions(rs.getInt("total_questions"));
                    exam.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return exam;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
