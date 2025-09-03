package org.example.studybuddy.database;

import org.example.studybuddy.model.Question;
import org.example.studybuddy.model.Subtopic;
import org.example.studybuddy.model.Topic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    private Connection connection;

    public QuestionDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    // TOPIC METHODS
    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT * FROM topics ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Topic topic = new Topic(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("created_by"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                topics.add(topic);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return topics;
    }

    public boolean createTopic(String name, String description, int createdBy) {
        String sql = "INSERT INTO topics (name, description, created_by) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, createdBy);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Topic getTopicById(int topicId) {
        String sql = "SELECT * FROM topics WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Topic(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getInt("created_by"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // SUBTOPIC METHODS
    public List<Subtopic> getSubtopicsByTopic(int topicId) {
        List<Subtopic> subtopics = new ArrayList<>();
        String sql = "SELECT * FROM subtopics WHERE topic_id = ? ORDER BY name";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, topicId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subtopic subtopic = new Subtopic(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("topic_id"),
                            rs.getString("description"),
                            rs.getInt("created_by"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    subtopics.add(subtopic);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subtopics;
    }

    public boolean createSubtopic(String name, int topicId, String description, int createdBy) {
        String sql = "INSERT INTO subtopics (name, topic_id, description, created_by) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, topicId);
            stmt.setString(3, description);
            stmt.setInt(4, createdBy);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Find or create topic by name
    public Topic findOrCreateTopic(String topicName, int userId) {
        // First, try to find existing topic
        String findSQL = "SELECT * FROM topics WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(findSQL)) {
            stmt.setString(1, topicName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Topic(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getInt("created_by"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If not found, create new topic
        if (createTopic(topicName, "Auto-created from CSV import", userId)) {
            return findOrCreateTopic(topicName, userId); // Recursive call to get the created topic
        }

        return null;
    }

    // Find or create subtopic by name and topic
    public Subtopic findOrCreateSubtopic(String subtopicName, int topicId, int userId) {
        // First, try to find existing subtopic
        String findSQL = "SELECT * FROM subtopics WHERE name = ? AND topic_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(findSQL)) {
            stmt.setString(1, subtopicName);
            stmt.setInt(2, topicId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Subtopic(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("topic_id"),
                            rs.getString("description"),
                            rs.getInt("created_by"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If not found, create new subtopic
        if (createSubtopic(subtopicName, topicId, "Auto-created from CSV import", userId)) {
            return findOrCreateSubtopic(subtopicName, topicId, userId); // Recursive call
        }

        return null;
    }

    // Export questions to CSV format
    public String exportQuestionsToCSV() {
        StringBuilder csv = new StringBuilder();
        // Add header
        csv.append("QuestionID,Topic,Subtopic,QuestionText,OptionA,OptionB,OptionC,OptionD,CorrectAnswer,Explanation,Difficulty,Weight,NegativeMarking\n");

        String sql = """
        SELECT q.*, s.name as subtopic_name, t.name as topic_name 
        FROM questions q 
        JOIN subtopics s ON q.subtopic_id = s.id 
        JOIN topics t ON s.topic_id = t.id 
        ORDER BY t.name, s.name, q.id
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                csv.append(rs.getInt("id")).append(",")
                        .append("\"").append(rs.getString("topic_name").replace("\"", "\"\"")).append("\",")
                        .append("\"").append(rs.getString("subtopic_name").replace("\"", "\"\"")).append("\",")
                        .append("\"").append(rs.getString("question_text").replace("\"", "\"\"")).append("\",")
                        .append("\"").append(rs.getString("option_a").replace("\"", "\"\"")).append("\",")
                        .append("\"").append(rs.getString("option_b").replace("\"", "\"\"")).append("\",")
                        .append("\"").append(rs.getString("option_c").replace("\"", "\"\"")).append("\",")
                        .append("\"").append(rs.getString("option_d").replace("\"", "\"\"")).append("\",")
                        .append(rs.getString("correct_answer")).append(",")
                        .append("\"").append(rs.getString("explanation") != null ? rs.getString("explanation").replace("\"", "\"\"") : "").append("\",")
                        .append(rs.getInt("difficulty_level")).append(",")
                        .append("1,") // Weight (default)
                        .append("false") // Negative marking (default)
                        .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return csv.toString();
    }



    // QUESTION METHODS
    public boolean createQuestion(Question question) {
        String sql = """
            INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, 
                                 correct_answer, explanation, difficulty_level, subtopic_id, created_by) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, question.getOptionA());
            stmt.setString(3, question.getOptionB());
            stmt.setString(4, question.getOptionC());
            stmt.setString(5, question.getOptionD());
            stmt.setString(6, question.getCorrectAnswer());
            stmt.setString(7, question.getExplanation());
            stmt.setInt(8, question.getDifficultyLevel());
            stmt.setInt(9, question.getSubtopicId());
            stmt.setInt(10, question.getCreatedBy());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Question> getQuestionsBySubtopic(int subtopicId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE subtopic_id = ? ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, subtopicId);
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

    public List<Question> searchQuestions(String searchTerm, int topicId, int subtopicId) {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT q.*, s.name as subtopic_name, t.name as topic_name 
            FROM questions q 
            JOIN subtopics s ON q.subtopic_id = s.id 
            JOIN topics t ON s.topic_id = t.id 
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (q.question_text LIKE ? OR q.option_a LIKE ? OR q.option_b LIKE ? OR q.option_c LIKE ? OR q.option_d LIKE ?)");
            String searchPattern = "%" + searchTerm.trim() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(searchPattern);
            }
        }

        if (topicId > 0) {
            sql.append(" AND t.id = ?");
            params.add(topicId);
        }

        if (subtopicId > 0) {
            sql.append(" AND s.id = ?");
            params.add(subtopicId);
        }

        sql.append(" ORDER BY q.created_at DESC");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

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

    public int getQuestionCountBySubtopic(int subtopicId) {
        String sql = "SELECT COUNT(*) FROM questions WHERE subtopic_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, subtopicId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM questions WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Add some sample data for testing
    public void addSampleData(int userId) {
        // Create sample topics
        createTopic("Mathematics", "Mathematical concepts and problems", userId);
        createTopic("Science", "General science questions", userId);
        createTopic("History", "Historical events and facts", userId);

        // Get topic IDs
        List<Topic> topics = getAllTopics();
        if (!topics.isEmpty()) {
            Topic mathTopic = topics.get(0);
            Topic scienceTopic = topics.get(1);

            // Create subtopics
            createSubtopic("Algebra", mathTopic.getId(), "Algebraic equations and expressions", userId);
            createSubtopic("Geometry", mathTopic.getId(), "Geometric shapes and calculations", userId);
            createSubtopic("Physics", scienceTopic.getId(), "Physics concepts", userId);
            createSubtopic("Chemistry", scienceTopic.getId(), "Chemistry basics", userId);

            // Create sample questions
            List<Subtopic> mathSubtopics = getSubtopicsByTopic(mathTopic.getId());
            if (!mathSubtopics.isEmpty()) {
                Subtopic algebra = mathSubtopics.get(0);

                Question q1 = new Question(
                        "What is the value of x in the equation 2x + 5 = 15?",
                        "x = 5", "x = 10", "x = 7.5", "x = 2.5",
                        "A", "2x + 5 = 15, so 2x = 10, therefore x = 5",
                        2, algebra.getId(), userId
                );
                createQuestion(q1);

                Question q2 = new Question(
                        "Solve for y: 3y - 7 = 14",
                        "y = 5", "y = 7", "y = 21", "y = 3",
                        "B", "3y - 7 = 14, so 3y = 21, therefore y = 7",
                        2, algebra.getId(), userId
                );
                createQuestion(q2);
            }
        }

        System.out.println("Sample question bank data added successfully!");
    }

    public Subtopic getSubtopicById(int subtopicId) {
        String sql = "SELECT * FROM subtopics WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, subtopicId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Subtopic(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("topic_id"),
                        rs.getString("description"),
                        rs.getInt("created_by"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTopicNameForQuestion(int questionId) {
        String sql = """
            SELECT t.name 
            FROM topics t 
            JOIN subtopics s ON t.id = s.topic_id 
            JOIN questions q ON s.id = q.subtopic_id 
            WHERE q.id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}
