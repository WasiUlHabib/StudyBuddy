package org.example.studybuddy.controller;

import org.example.studybuddy.database.QuestionDAO;
import org.example.studybuddy.model.Question;
import org.example.studybuddy.model.Subtopic;
import org.example.studybuddy.model.Topic;
import org.example.studybuddy.util.SceneManager;
import org.example.studybuddy.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.example.studybuddy.util.CSVImporter;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class QuestionBankController implements Initializable {

    // Upload Tab Controls
    @FXML private ComboBox<Topic> topicComboBox;
    @FXML private ComboBox<Subtopic> subtopicComboBox;
    @FXML private Button createTopicButton;
    @FXML private Button createSubtopicButton;
    @FXML private TextArea questionTextArea;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private RadioButton answerARadio;
    @FXML private RadioButton answerBRadio;
    @FXML private RadioButton answerCRadio;
    @FXML private RadioButton answerDRadio;
    @FXML private ComboBox<String> difficultyComboBox;
    @FXML private TextArea explanationTextArea;
    @FXML private Button uploadQuestionButton;
    @FXML private Button resetFormButton;
    @FXML private Label uploadMessageLabel;

    // Browse Tab Controls
    @FXML private ComboBox<Topic> filterTopicComboBox;
    @FXML private ComboBox<Subtopic> filterSubtopicComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearFiltersButton;
    @FXML private Label questionCountLabel;
    @FXML private ListView<Question> questionsListView;
    @FXML private VBox questionDetailsBox;
    @FXML private TextArea questionDetailsArea;
    @FXML private Button deleteQuestionButton;

    private SessionManager sessionManager = SessionManager.getInstance();
    private QuestionDAO questionDAO = new QuestionDAO();
    private ToggleGroup answerGroup = new ToggleGroup();
    private ObservableList<Question> allQuestions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAnswerGroup();
        setupDifficultyComboBox();
        setupTopicComboBoxes();
        setupQuestionsList();
        loadTopics();

        // Add listeners for dynamic updates
        setupListeners();
    }


    // Add these methods to the class:

    @FXML
    private void handleImportQuestions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File to Import");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(uploadQuestionButton.getScene().getWindow());

        if (selectedFile != null) {
            importQuestionsFromCSV(selectedFile);
        }
    }

    @FXML
    private void handleExportQuestions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Questions as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("studybuddy_questions_" + java.time.LocalDate.now() + ".csv");

        File saveFile = fileChooser.showSaveDialog(uploadQuestionButton.getScene().getWindow());

        if (saveFile != null) {
            exportQuestionsToCSV(saveFile);
        }
    }

    @FXML
    private void handleDownloadTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Template");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("studybuddy_template.csv");

        File saveFile = fileChooser.showSaveDialog(uploadQuestionButton.getScene().getWindow());

        if (saveFile != null) {
            createCSVTemplate(saveFile);
        }
    }

    private void importQuestionsFromCSV(File csvFile) {
        try {
            CSVImporter importer = new CSVImporter(questionDAO, sessionManager.getCurrentUser().getId());
            CSVImporter.CSVImportResult result = importer.importQuestions(csvFile);

            if (result.hasErrors()) {
                // Show detailed error dialog
                showImportResultDialog(result);
            } else {
                showMessage("Successfully imported " + result.successCount + " questions!", false);
            }

            // Refresh the question bank
            loadTopics();
            loadAllQuestions();

        } catch (Exception e) {
            showMessage("Import failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void exportQuestionsToCSV(File saveFile) {
        try {
            String csvContent = questionDAO.exportQuestionsToCSV();

            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(csvContent);
            }

            showMessage("Questions exported successfully to " + saveFile.getName(), false);

        } catch (IOException e) {
            showMessage("Export failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void createCSVTemplate(File saveFile) {
        try {
            StringBuilder template = new StringBuilder();
            template.append("QuestionID,Topic,Subtopic,QuestionText,OptionA,OptionB,OptionC,OptionD,CorrectAnswer,Explanation,Difficulty,Weight,NegativeMarking\n");
            template.append(",Mathematics,Algebra,\"What is the value of x in 2x + 5 = 15?\",3,5,7,10,B,\"2x + 5 = 15, so 2x = 10, therefore x = 5\",2,1,false\n");
            template.append(",Science,Chemistry,\"What is the chemical symbol for gold?\",Au,Ag,Go,Gd,A,\"Gold's chemical symbol is Au from the Latin 'aurum'\",1,1,false\n");
            template.append(",Programming,Java,\"Which keyword is used to create a class in Java?\",class,Class,new,create,A,\"The 'class' keyword defines a class in Java\",3,1,false\n");

            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(template.toString());
            }

            showMessage("CSV template created successfully!", false);

        } catch (IOException e) {
            showMessage("Template creation failed: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void showImportResultDialog(CSVImporter.CSVImportResult result) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Import Results");
        alert.setHeaderText(result.getSummary());

        // Create detailed error message
        if (result.hasErrors()) {
            StringBuilder details = new StringBuilder();
            details.append("Errors encountered:\n\n");
            for (String error : result.errors) {
                details.append("• ").append(error).append("\n");
            }

            TextArea textArea = new TextArea(details.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(10);
            textArea.setPrefColumnCount(50);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.getDialogPane().setExpanded(true);
        }

        alert.showAndWait();
    }

    private void setupAnswerGroup() {
        answerARadio.setToggleGroup(answerGroup);
        answerBRadio.setToggleGroup(answerGroup);
        answerCRadio.setToggleGroup(answerGroup);
        answerDRadio.setToggleGroup(answerGroup);
    }

    private void setupDifficultyComboBox() {
        ObservableList<String> difficulties = FXCollections.observableArrayList(
                "1 - Very Easy", "2 - Easy", "3 - Medium", "4 - Hard", "5 - Very Hard"
        );
        difficultyComboBox.setItems(difficulties);
        difficultyComboBox.setValue("3 - Medium"); // Default to medium
    }

    private void setupTopicComboBoxes() {
        // Setup string converters for topics
        StringConverter<Topic> topicConverter = new StringConverter<Topic>() {
            @Override
            public String toString(Topic topic) {
                return topic != null ? topic.getName() : "";
            }

            @Override
            public Topic fromString(String string) {
                return null;
            }
        };

        topicComboBox.setConverter(topicConverter);
        filterTopicComboBox.setConverter(topicConverter);

        // Setup string converters for subtopics
        StringConverter<Subtopic> subtopicConverter = new StringConverter<Subtopic>() {
            @Override
            public String toString(Subtopic subtopic) {
                return subtopic != null ? subtopic.getName() : "";
            }

            @Override
            public Subtopic fromString(String string) {
                return null;
            }
        };

        subtopicComboBox.setConverter(subtopicConverter);
        filterSubtopicComboBox.setConverter(subtopicConverter);
    }

    private void setupQuestionsList() {
        // Custom cell factory for questions list
        questionsListView.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                if (empty || question == null) {
                    setText(null);
                } else {
                    String text = String.format("[%s] %s",
                            question.getDifficultyText(),
                            question.getQuestionText().length() > 80 ?
                                    question.getQuestionText().substring(0, 80) + "..." :
                                    question.getQuestionText()
                    );
                    setText(text);
                }
            }
        });

        // Add selection listener
        questionsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showQuestionDetails(newSelection);
            }
        });
    }

    private void setupListeners() {
        // Topic selection listener
        topicComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSubtopics(newVal.getId());
                createSubtopicButton.setDisable(false);
            } else {
                subtopicComboBox.getItems().clear();
                createSubtopicButton.setDisable(true);
            }
        });

        // Filter topic listener
        filterTopicComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadFilterSubtopics(newVal.getId());
            } else {
                filterSubtopicComboBox.getItems().clear();
            }
            filterQuestions();
        });

        // Filter subtopic listener
        filterSubtopicComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterQuestions());

        // Search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.trim().length() > 2) {
                filterQuestions();
            } else if (newVal == null || newVal.trim().isEmpty()) {
                filterQuestions();
            }
        });
    }

    private void loadTopics() {
        List<Topic> topics = questionDAO.getAllTopics();

        ObservableList<Topic> topicList = FXCollections.observableArrayList(topics);
        topicComboBox.setItems(topicList);

        // Add "All Topics" option for filter
        ObservableList<Topic> filterTopicList = FXCollections.observableArrayList();
        filterTopicList.add(null); // Represents "All Topics"
        filterTopicList.addAll(topics);
        filterTopicComboBox.setItems(filterTopicList);

        loadAllQuestions();
    }

    private void loadSubtopics(int topicId) {
        List<Subtopic> subtopics = questionDAO.getSubtopicsByTopic(topicId);
        ObservableList<Subtopic> subtopicList = FXCollections.observableArrayList(subtopics);
        subtopicComboBox.setItems(subtopicList);
    }

    private void loadFilterSubtopics(int topicId) {
        List<Subtopic> subtopics = questionDAO.getSubtopicsByTopic(topicId);
        ObservableList<Subtopic> subtopicList = FXCollections.observableArrayList();
        subtopicList.add(null); // Represents "All Subtopics"
        subtopicList.addAll(subtopics);
        filterSubtopicComboBox.setItems(subtopicList);
    }

    private void loadAllQuestions() {
        // Load all questions from database
        allQuestions.clear();
        List<Topic> topics = questionDAO.getAllTopics();

        for (Topic topic : topics) {
            List<Subtopic> subtopics = questionDAO.getSubtopicsByTopic(topic.getId());
            for (Subtopic subtopic : subtopics) {
                List<Question> questions = questionDAO.getQuestionsBySubtopic(subtopic.getId());
                allQuestions.addAll(questions);
            }
        }

        questionsListView.setItems(allQuestions);
        updateQuestionCount();
    }

    private void filterQuestions() {
        Topic selectedTopic = filterTopicComboBox.getValue();
        Subtopic selectedSubtopic = filterSubtopicComboBox.getValue();
        String searchTerm = searchField.getText();

        int topicId = selectedTopic != null ? selectedTopic.getId() : 0;
        int subtopicId = selectedSubtopic != null ? selectedSubtopic.getId() : 0;

        List<Question> filteredQuestions = questionDAO.searchQuestions(searchTerm, topicId, subtopicId);

        ObservableList<Question> filteredList = FXCollections.observableArrayList(filteredQuestions);
        questionsListView.setItems(filteredList);
        updateQuestionCount();
    }

    private void updateQuestionCount() {
        int count = questionsListView.getItems().size();
        questionCountLabel.setText("Total Questions: " + count);
    }

    private void showQuestionDetails(Question question) {
        StringBuilder details = new StringBuilder();
        details.append("Question: ").append(question.getQuestionText()).append("\n\n");
        details.append("A) ").append(question.getOptionA()).append("\n");
        details.append("B) ").append(question.getOptionB()).append("\n");
        details.append("C) ").append(question.getOptionC()).append("\n");
        details.append("D) ").append(question.getOptionD()).append("\n\n");
        details.append("Correct Answer: ").append(question.getCorrectAnswer())
                .append(" (").append(question.getCorrectOptionText()).append(")\n");
        details.append("Difficulty: ").append(question.getDifficultyText()).append("\n");

        if (question.getExplanation() != null && !question.getExplanation().trim().isEmpty()) {
            details.append("Explanation: ").append(question.getExplanation());
        }

        questionDetailsArea.setText(details.toString());
        questionDetailsBox.setVisible(true);
    }

    @FXML
    private void handleCreateTopic() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Topic");
        dialog.setHeaderText("Create a new topic for questions");
        dialog.setContentText("Topic name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String topicName = result.get().trim();

            // Get optional description
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Topic Description");
            descDialog.setHeaderText("Add a description (optional)");
            descDialog.setContentText("Description:");

            String description = descDialog.showAndWait().orElse("");

            if (questionDAO.createTopic(topicName, description, sessionManager.getCurrentUser().getId())) {
                showMessage("Topic created successfully!", false);
                loadTopics();
            } else {
                showMessage("Failed to create topic. Name might already exist.", true);
            }
        }
    }

    @FXML
    private void handleCreateSubtopic() {
        Topic selectedTopic = topicComboBox.getValue();
        if (selectedTopic == null) {
            showMessage("Please select a topic first.", true);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Subtopic");
        dialog.setHeaderText("Create a new subtopic under: " + selectedTopic.getName());
        dialog.setContentText("Subtopic name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String subtopicName = result.get().trim();

            // Get optional description
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Subtopic Description");
            descDialog.setHeaderText("Add a description (optional)");
            descDialog.setContentText("Description:");

            String description = descDialog.showAndWait().orElse("");

            if (questionDAO.createSubtopic(subtopicName, selectedTopic.getId(), description, sessionManager.getCurrentUser().getId())) {
                showMessage("Subtopic created successfully!", false);
                loadSubtopics(selectedTopic.getId());
            } else {
                showMessage("Failed to create subtopic. Name might already exist under this topic.", true);
            }
        }
    }

    @FXML
    private void handleUploadQuestion() {
        // Validate form
        if (!validateQuestionForm()) {
            return;
        }

        // Create question object
        Question question = new Question(
                questionTextArea.getText().trim(),
                optionAField.getText().trim(),
                optionBField.getText().trim(),
                optionCField.getText().trim(),
                optionDField.getText().trim(),
                getSelectedAnswer(),
                explanationTextArea.getText().trim(),
                getDifficultyLevel(),
                subtopicComboBox.getValue().getId(),
                sessionManager.getCurrentUser().getId()
        );

        // Save to database
        if (questionDAO.createQuestion(question)) {
            showMessage("Question uploaded successfully!", false);
            handleResetForm();
            loadAllQuestions();
        } else {
            showMessage("Failed to upload question. Please try again.", true);
        }
    }

    private boolean validateQuestionForm() {
        if (topicComboBox.getValue() == null) {
            showMessage("Please select a topic.", true);
            return false;
        }

        if (subtopicComboBox.getValue() == null) {
            showMessage("Please select a subtopic.", true);
            return false;
        }

        if (questionTextArea.getText().trim().isEmpty()) {
            showMessage("Please enter a question.", true);
            return false;
        }

        if (optionAField.getText().trim().isEmpty() || optionBField.getText().trim().isEmpty() ||
                optionCField.getText().trim().isEmpty() || optionDField.getText().trim().isEmpty()) {
            showMessage("Please fill in all four options (A, B, C, D).", true);
            return false;
        }

        if (answerGroup.getSelectedToggle() == null) {
            showMessage("Please select the correct answer.", true);
            return false;
        }

        return true;
    }

    private String getSelectedAnswer() {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        return selected.getText();
    }

    private int getDifficultyLevel() {
        String difficulty = difficultyComboBox.getValue();
        return Integer.parseInt(difficulty.substring(0, 1));
    }

    @FXML
    private void handleResetForm() {
        questionTextArea.clear();
        optionAField.clear();
        optionBField.clear();
        optionCField.clear();
        optionDField.clear();
        explanationTextArea.clear();
        answerGroup.selectToggle(null);
        difficultyComboBox.setValue("3 - Medium");
        topicComboBox.setValue(null);
        subtopicComboBox.getItems().clear();
        uploadMessageLabel.setText("");
    }

    @FXML
    private void handleSearch() {
        filterQuestions();
    }

    @FXML
    private void handleClearFilters() {
        filterTopicComboBox.setValue(null);
        filterSubtopicComboBox.setValue(null);
        searchField.clear();
        questionsListView.setItems(allQuestions);
        updateQuestionCount();
        questionDetailsBox.setVisible(false);
    }

    @FXML
    private void handleDeleteQuestion() {
        Question selectedQuestion = questionsListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Question");
        alert.setHeaderText("Are you sure you want to delete this question?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (questionDAO.deleteQuestion(selectedQuestion.getId())) {
                showMessage("Question deleted successfully!", false);
                loadAllQuestions();
                questionDetailsBox.setVisible(false);
            } else {
                showMessage("Failed to delete question.", true);
            }
        }
    }

    @FXML
    private void addSampleData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Sample Data");
        alert.setHeaderText("This will add sample topics, subtopics, and questions");
        alert.setContentText("Continue?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            questionDAO.addSampleData(sessionManager.getCurrentUser().getId());
            showMessage("Sample data added successfully!", false);
            loadTopics();
        }
    }

    @FXML
    private void clearAllQuestions() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Clear All Questions");
        alert.setHeaderText("This will delete ALL questions, subtopics, and topics!");
        alert.setContentText("This action cannot be undone. Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Note: You'd need to implement this method in QuestionDAO
            showMessage("Clear all functionality would be implemented here.", false);
        }
    }

    private void showMessage(String message, boolean isError) {
        uploadMessageLabel.setText(message);
        uploadMessageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");

        // Clear message after 5 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        delay.setOnFinished(e -> uploadMessageLabel.setText(""));
        delay.play();
    }

    @FXML
    private void goToDashboard() {
        SceneManager.getInstance().switchToDashboard();
    }

    @FXML
    private void goToQuestionBank() {
        SceneManager.getInstance().switchToQuestionBank();
    }

    @FXML
    private void goToProfile() {
        SceneManager.getInstance().switchToProfile();
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        SceneManager.getInstance().switchToLogin();
    }
}
