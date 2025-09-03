package org.example.studybuddy.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.example.studybuddy.database.ExamLogDAO;
import org.example.studybuddy.database.ExamQuestionLogDAO;
import org.example.studybuddy.model.ExamLog;
import org.example.studybuddy.util.SceneManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ExamDetailController implements Initializable {
    @FXML private Label examNameLabel;
    @FXML private Label examDateLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Label correctAnswersLabel;
    @FXML private Label wrongAnswersLabel;
    @FXML private Label unansweredLabel;
    
    @FXML private TableView<QuestionDetail> questionsTable;
    @FXML private TableColumn<QuestionDetail, Integer> questionNumberColumn;
    @FXML private TableColumn<QuestionDetail, String> questionTextColumn;
    @FXML private TableColumn<QuestionDetail, String> yourAnswerColumn;
    @FXML private TableColumn<QuestionDetail, String> correctAnswerColumn;
    @FXML private TableColumn<QuestionDetail, String> resultColumn;

    private ExamLogDAO examLogDAO;
    private ExamQuestionLogDAO examQuestionLogDAO;
    private ExamLog examLog;

    // Inner class to represent question details
    public static class QuestionDetail {
        private int number;
        private String questionText;
        private String yourAnswer;
        private String correctAnswer;
        private String result;

        public QuestionDetail(int number, String questionText, String yourAnswer, 
                            String correctAnswer, String result) {
            this.number = number;
            this.questionText = questionText;
            this.yourAnswer = yourAnswer;
            this.correctAnswer = correctAnswer;
            this.result = result;
        }

        // Getters
        public int getNumber() { return number; }
        public String getQuestionText() { return questionText; }
        public String getYourAnswer() { return yourAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String getResult() { return result; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        examLogDAO = new ExamLogDAO();
        examQuestionLogDAO = new ExamQuestionLogDAO();
        examQuestionLogDAO.createTable(); // Ensure the table exists
        setupTable();
    }

    private void setupTable() {
        // Configure the columns
        questionNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        questionTextColumn.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        yourAnswerColumn.setCellValueFactory(new PropertyValueFactory<>("yourAnswer"));
        correctAnswerColumn.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));

        // Style the result column based on correct/incorrect
        resultColumn.setCellFactory(column -> new TableCell<QuestionDetail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Correct")) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item.equals("Incorrect")) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: orange;"); // For unanswered
                    }
                }
            }
        });
    }

    public void loadExamDetails(int examId) {
        examLog = examLogDAO.getExamLogById(examId);
        if (examLog != null) {
            updateUI();
            loadQuestions(examId);
            System.out.println("Loading exam details for exam ID: " + examId); // Debug log
        } else {
            System.out.println("Failed to load exam with ID: " + examId); // Debug log
        }
    }

    private void updateUI() {
        examNameLabel.setText(examLog.getExamName());
        examDateLabel.setText("Taken on " + examLog.getFormattedDate());
        scoreLabel.setText(String.format("Score: %.1f%%", examLog.getPercentage()));
        timeLabel.setText("Time taken: " + examLog.getFormattedTime());
        
        totalQuestionsLabel.setText(String.valueOf(examLog.getTotalQuestions()));
        correctAnswersLabel.setText(String.valueOf(examLog.getCorrectAnswers()));
        wrongAnswersLabel.setText(String.valueOf(examLog.getWrongAnswers()));
        unansweredLabel.setText(String.valueOf(examLog.getUnanswered()));
    }

    private void loadQuestions(int examId) {
        System.out.println("Fetching question details for exam ID: " + examId); // Debug log
        var questionDetails = examQuestionLogDAO.getExamQuestionDetails(examId);
        System.out.println("Found " + questionDetails.size() + " questions"); // Debug log
        
        if (questionDetails.isEmpty()) {
            System.out.println("No question details found for exam ID: " + examId); // Debug log
        } else {
            questionsTable.setItems(FXCollections.observableArrayList(questionDetails));
        }
    }

    @FXML
    private void handleExport() {
        // TODO: Implement export functionality
    }

    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchToProfile();
    }
}
