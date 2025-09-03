package org.example.studybuddy.util;

import com.opencsv.CSVReader;
import org.example.studybuddy.database.QuestionDAO;
import org.example.studybuddy.model.Question;
import org.example.studybuddy.model.Subtopic;
import org.example.studybuddy.model.Topic;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVImporter {
    private QuestionDAO questionDAO;
    private int userId;

    public CSVImporter(QuestionDAO questionDAO, int userId) {
        this.questionDAO = questionDAO;
        this.userId = userId;
    }

    public CSVImportResult importQuestions(File csvFile) {
        CSVImportResult result = new CSVImportResult();

        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> records = reader.readAll();

            // Skip header row
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                int lineNumber = i + 1;

                try {
                    if (validateAndImportRecord(record, lineNumber, result)) {
                        result.successCount++;
                    }
                } catch (Exception e) {
                    result.errors.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            result.errors.add("File reading error: " + e.getMessage());
        }

        return result;
    }

    private boolean validateAndImportRecord(String[] record, int lineNumber, CSVImportResult result) {
        // Basic validation
        if (record.length < 9) {
            result.errors.add("Line " + lineNumber + ": Missing required fields");
            return false;
        }

        String topicName = record[1].trim();
        String subtopicName = record[2].trim();
        String questionText = record[3].trim();
        String optionA = record[4].trim();
        String optionB = record[5].trim();
        String optionC = record[6].trim();
        String optionD = record[7].trim();
        String correctAnswer = record[8].trim().toUpperCase();

        // Validation
        if (topicName.isEmpty() || subtopicName.isEmpty() || questionText.isEmpty()) {
            result.errors.add("Line " + lineNumber + ": Topic, Subtopic, and Question text are required");
            return false;
        }

        if (!Arrays.asList("A", "B", "C", "D").contains(correctAnswer)) {
            result.errors.add("Line " + lineNumber + ": Correct answer must be A, B, C, or D");
            return false;
        }

        // Create or find topic and subtopic
        Topic topic = questionDAO.findOrCreateTopic(topicName, userId);
        if (topic == null) {
            result.errors.add("Line " + lineNumber + ": Failed to create/find topic");
            return false;
        }

        Subtopic subtopic = questionDAO.findOrCreateSubtopic(subtopicName, topic.getId(), userId);
        if (subtopic == null) {
            result.errors.add("Line " + lineNumber + ": Failed to create/find subtopic");
            return false;
        }

        // Create question
        String explanation = record.length > 9 ? record[9].trim() : "";
        int difficulty = record.length > 10 && !record[10].trim().isEmpty() ?
                Integer.parseInt(record[10].trim()) : 3;

        Question question = new Question(
                questionText, optionA, optionB, optionC, optionD,
                correctAnswer, explanation, difficulty, subtopic.getId(), userId
        );

        return questionDAO.createQuestion(question);
    }

    public static class CSVImportResult {
        public int successCount = 0;
        public List<String> errors = new ArrayList<>();

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getSummary() {
            if (hasErrors()) {
                return String.format("Imported %d questions with %d errors", successCount, errors.size());
            } else {
                return String.format("Successfully imported %d questions", successCount);
            }
        }
    }
}
