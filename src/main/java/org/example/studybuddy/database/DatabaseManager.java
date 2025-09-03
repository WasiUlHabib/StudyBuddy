package org.example.studybuddy.database;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DATABASE_NAME = "studybuddy.db";

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabase() {
        try {
            // Create connection to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            System.out.println("Connected to SQLite database successfully!");

            // Create tables
            createTables();

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    private void createTables() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createUserStatsTable = """
            CREATE TABLE IF NOT EXISTS user_stats (
                user_id INTEGER PRIMARY KEY,
                questions_attempted INTEGER DEFAULT 0,
                questions_solved INTEGER DEFAULT 0,
                exams_taken INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

        // NEW: Track daily performance for graphs
        String createDailyStatsTable = """
            CREATE TABLE IF NOT EXISTS daily_stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                date DATE,
                questions_attempted INTEGER DEFAULT 0,
                questions_solved INTEGER DEFAULT 0,
                exams_taken INTEGER DEFAULT 0,
                accuracy_percentage REAL DEFAULT 0.0,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, date)
            )
        """;

        // NEW: Topics table
        String createTopicsTable = """
        CREATE TABLE IF NOT EXISTS topics (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT UNIQUE NOT NULL,
            description TEXT,
            created_by INTEGER,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (created_by) REFERENCES users(id)
        )
    """;

        // NEW: Subtopics table
        String createSubtopicsTable = """
        CREATE TABLE IF NOT EXISTS subtopics (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            topic_id INTEGER,
            description TEXT,
            created_by INTEGER,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (topic_id) REFERENCES topics(id),
            FOREIGN KEY (created_by) REFERENCES users(id),
            UNIQUE(name, topic_id)
        )
    """;

        // NEW: Questions table
        String createQuestionsTable = """
        CREATE TABLE IF NOT EXISTS questions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            question_text TEXT NOT NULL,
            option_a TEXT NOT NULL,
            option_b TEXT NOT NULL,
            option_c TEXT NOT NULL,
            option_d TEXT NOT NULL,
            correct_answer TEXT NOT NULL CHECK (correct_answer IN ('A', 'B', 'C', 'D')),
            explanation TEXT,
            difficulty_level INTEGER DEFAULT 1 CHECK (difficulty_level BETWEEN 1 AND 5),
            subtopic_id INTEGER,
            created_by INTEGER,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (subtopic_id) REFERENCES subtopics(id),
            FOREIGN KEY (created_by) REFERENCES users(id)
        )
    """;

        // NEW: Exams table
        String createExamsTable = """
    CREATE TABLE IF NOT EXISTS exams (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        name TEXT,
        time_limit INTEGER, -- in minutes
        negative_marks BOOLEAN DEFAULT 0,
        total_questions INTEGER,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id)
    )
""";

// NEW: Exam questions mapping
        String createExamQuestionsTable = """
    CREATE TABLE IF NOT EXISTS exam_questions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        exam_id INTEGER,
        question_id INTEGER,
        question_order INTEGER,
        user_answer TEXT,
        is_correct BOOLEAN,
        time_taken INTEGER DEFAULT 0, -- in seconds
        FOREIGN KEY (exam_id) REFERENCES exams(id),
        FOREIGN KEY (question_id) REFERENCES questions(id),
        UNIQUE(exam_id, question_order)
    )
""";

// NEW: Exam results
        String createExamResultsTable = """
    CREATE TABLE IF NOT EXISTS exam_results (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        exam_id INTEGER,
        user_id INTEGER,
        score DECIMAL,
        total_questions INTEGER,
        correct_answers INTEGER,
        wrong_answers INTEGER,
        unanswered INTEGER,
        time_taken INTEGER, -- in seconds
        completed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (exam_id) REFERENCES exams(id),
        FOREIGN KEY (user_id) REFERENCES users(id)
    )
""";

        // NEW: Topic-wise performance tracking
        String createTopicPerformanceTable = """
    CREATE TABLE IF NOT EXISTS topic_performance (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        topic_id INTEGER,
        questions_attempted INTEGER DEFAULT 0,
        questions_solved INTEGER DEFAULT 0,
        accuracy_percentage REAL DEFAULT 0.0,
        last_updated DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id),
        FOREIGN KEY (topic_id) REFERENCES topics(id),
        UNIQUE(user_id, topic_id)
    )
""";

// NEW: Detailed question attempt tracking
        String createQuestionAttemptsTable = """
    CREATE TABLE IF NOT EXISTS question_attempts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER,
        question_id INTEGER,
        selected_answer TEXT,
        is_correct BOOLEAN,
        time_taken INTEGER, -- in seconds
        attempted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        exam_id INTEGER, -- null if standalone practice
        FOREIGN KEY (user_id) REFERENCES users(id),
        FOREIGN KEY (question_id) REFERENCES questions(id),
        FOREIGN KEY (exam_id) REFERENCES exams(id)
    )
""";
// NEW: Rooms table
        String createRoomsTable = """
    CREATE TABLE IF NOT EXISTS rooms (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        description TEXT,
        room_code TEXT UNIQUE NOT NULL,
        created_by INTEGER,
        max_participants INTEGER DEFAULT 50,
        is_active BOOLEAN DEFAULT 1,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (created_by) REFERENCES users(id)
    )
""";

// NEW: Room participants
        String createRoomParticipantsTable = """
    CREATE TABLE IF NOT EXISTS room_participants (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        room_id INTEGER,
        user_id INTEGER,
        role TEXT DEFAULT 'member', -- 'admin', 'moderator', 'member'
        joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        is_active BOOLEAN DEFAULT 1,
        FOREIGN KEY (room_id) REFERENCES rooms(id),
        FOREIGN KEY (user_id) REFERENCES users(id),
        UNIQUE(room_id, user_id)
    )
""";

// NEW: Room question access
        String createRoomQuestionsTable = """
    CREATE TABLE IF NOT EXISTS room_questions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        room_id INTEGER,
        question_id INTEGER,
        shared_by INTEGER,
        shared_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (room_id) REFERENCES rooms(id),
        FOREIGN KEY (question_id) REFERENCES questions(id),
        FOREIGN KEY (shared_by) REFERENCES users(id),
        UNIQUE(room_id, question_id)
    )
""";

// NEW: Room chat messages
        String createRoomMessagesTable = """
    CREATE TABLE IF NOT EXISTS room_messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        room_id INTEGER,
        user_id INTEGER,
        message TEXT NOT NULL,
        message_type TEXT DEFAULT 'text', -- 'text', 'system', 'file'
        sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (room_id) REFERENCES rooms(id),
        FOREIGN KEY (user_id) REFERENCES users(id)
    )
""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createUserStatsTable);
            stmt.execute(createDailyStatsTable);
            stmt.execute(createTopicsTable);
            stmt.execute(createSubtopicsTable);
            stmt.execute(createQuestionsTable);
            stmt.execute(createExamsTable);
            stmt.execute(createExamQuestionsTable);
            stmt.execute(createExamResultsTable);
            stmt.execute(createTopicPerformanceTable);
            stmt.execute(createQuestionAttemptsTable);
            stmt.execute(createRoomsTable);
            stmt.execute(createRoomParticipantsTable);
            stmt.execute(createRoomQuestionsTable);
            stmt.execute(createRoomMessagesTable);
            System.out.println("Database tables created successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to create tables: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
