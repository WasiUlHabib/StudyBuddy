package org.example.studybuddy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.studybuddy.controller.ExamDetailController.QuestionDetail;

public class ExamQuestionLogDAO {
    private Connection connection;

    public ExamQuestionLogDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // Create table if not exists
    public void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS exam_question_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                exam_log_id INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                question_text TEXT NOT NULL,
                chosen_answer TEXT,
                correct_answer TEXT NOT NULL,
                is_correct BOOLEAN NOT NULL,
                FOREIGN KEY (exam_log_id) REFERENCES exam_logs(id) ON DELETE CASCADE,
                FOREIGN KEY (question_id) REFERENCES questions(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Save question details for an exam
    public boolean saveQuestionLog(int examLogId, int questionId, String questionText,
                                 String chosenAnswer, String correctAnswer, boolean isCorrect) {
        String sql = """
            INSERT INTO exam_question_logs (exam_log_id, question_id, question_text, 
                                         chosen_answer, correct_answer, is_correct)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examLogId);
            stmt.setInt(2, questionId);
            stmt.setString(3, questionText);
            stmt.setString(4, chosenAnswer);
            stmt.setString(5, correctAnswer);
            stmt.setBoolean(6, isCorrect);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get question details for an exam
    public List<QuestionDetail> getExamQuestionDetails(int examLogId) {
        List<QuestionDetail> details = new ArrayList<>();
        String sql = """
            SELECT question_text, chosen_answer, correct_answer, is_correct 
            FROM exam_question_logs 
            WHERE exam_log_id = ? 
            ORDER BY id ASC
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, examLogId);
            try (ResultSet rs = stmt.executeQuery()) {
                int questionNumber = 1;
                while (rs.next()) {
                    String questionText = rs.getString("question_text");
                    String chosenAnswer = rs.getString("chosen_answer");
                    String correctAnswer = rs.getString("correct_answer");
                    boolean isCorrect = rs.getBoolean("is_correct");

                    String result = chosenAnswer == null ? "Unanswered" : 
                                  (isCorrect ? "Correct" : "Incorrect");

                    details.add(new QuestionDetail(
                        questionNumber++,
                        questionText,
                        chosenAnswer != null ? chosenAnswer : "Not answered",
                        correctAnswer,
                        result
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return details;
    }
}
