package org.example.studybuddy.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

public class ExamSettingsDialog {

    public static class ExamSettings {
        private final int numberOfQuestions;
        private final int timeInMinutes;

        public ExamSettings(int numberOfQuestions, int timeInMinutes) {
            this.numberOfQuestions = numberOfQuestions;
            this.timeInMinutes = timeInMinutes;
        }

        public int getNumberOfQuestions() { return numberOfQuestions; }
        public int getTimeInMinutes() { return timeInMinutes; }
    }

    public static Optional<ExamSettings> showDialog(int availableQuestions) {
        // Create the custom dialog
        Dialog<ExamSettings> dialog = new Dialog<>();
        dialog.setTitle("Configure Room Exam");
        dialog.setHeaderText("Customize your exam settings");

        // Set the icon (optional)
        dialog.setGraphic(null);

        // Set the button types
        ButtonType startButtonType = new ButtonType("Start Exam", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);

        // Create the grid layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create input fields
        Spinner<Integer> questionSpinner = new Spinner<>(1, availableQuestions,
                Math.min(10, availableQuestions));
        questionSpinner.setEditable(true);
        questionSpinner.setPrefWidth(100);

        Spinner<Integer> timeSpinner = new Spinner<>(5, 180, 30, 5);
        timeSpinner.setEditable(true);
        timeSpinner.setPrefWidth(100);

        // Labels with information
        Label questionLabel = new Label("Number of Questions:");
        Label questionInfo = new Label("(Available: " + availableQuestions + ")");
        questionInfo.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");

        Label timeLabel = new Label("Exam Duration (minutes):");
        Label timeInfo = new Label("(Recommended: 1-2 min per question)");
        timeInfo.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");

        // Add components to grid
        grid.add(questionLabel, 0, 0);
        grid.add(questionSpinner, 1, 0);
        grid.add(questionInfo, 2, 0);

        grid.add(timeLabel, 0, 1);
        grid.add(timeSpinner, 1, 1);
        grid.add(timeInfo, 2, 1);

        // Add validation info
        Label validationLabel = new Label("⚠️ Make sure to select reasonable values for a fair exam.");
        validationLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");
        grid.add(validationLabel, 0, 2, 3, 1);

        // Enable/Disable start button based on validation
        Node startButton = dialog.getDialogPane().lookupButton(startButtonType);
        startButton.setDisable(false); // Enable by default since spinners have valid ranges

        dialog.getDialogPane().setContent(grid);

        // Request focus on the question spinner by default
        Platform.runLater(() -> questionSpinner.requestFocus());

        // Convert the result when start button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == startButtonType) {
                return new ExamSettings(questionSpinner.getValue(), timeSpinner.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // Alternative simpler version using text fields
    public static Optional<ExamSettings> showSimpleDialog(int availableQuestions) {
        Dialog<ExamSettings> dialog = new Dialog<>();
        dialog.setTitle("Exam Settings");
        dialog.setHeaderText("Configure your room exam");

        ButtonType startButtonType = new ButtonType("Start Exam", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField questionsField = new TextField();
        questionsField.setPromptText("e.g. 10");
        questionsField.setText("10");

        TextField timeField = new TextField();
        timeField.setPromptText("e.g. 30");
        timeField.setText("30");

        grid.add(new Label("Number of Questions:"), 0, 0);
        grid.add(questionsField, 1, 0);
        grid.add(new Label("(Max: " + availableQuestions + ")"), 2, 0);

        grid.add(new Label("Time (minutes):"), 0, 1);
        grid.add(timeField, 1, 1);
        grid.add(new Label("(5-180 min)"), 2, 1);

        Node startButton = dialog.getDialogPane().lookupButton(startButtonType);
        startButton.setDisable(true);

        // Add validation listeners
        questionsField.textProperty().addListener((observable, oldValue, newValue) -> {
            startButton.setDisable(!validateInputs(questionsField.getText(), timeField.getText(), availableQuestions));
        });

        timeField.textProperty().addListener((observable, oldValue, newValue) -> {
            startButton.setDisable(!validateInputs(questionsField.getText(), timeField.getText(), availableQuestions));
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == startButtonType) {
                try {
                    int questions = Integer.parseInt(questionsField.getText());
                    int time = Integer.parseInt(timeField.getText());
                    return new ExamSettings(questions, time);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Platform.runLater(() -> questionsField.requestFocus());

        return dialog.showAndWait();
    }

    private static boolean validateInputs(String questionsStr, String timeStr, int maxQuestions) {
        try {
            int questions = Integer.parseInt(questionsStr.trim());
            int time = Integer.parseInt(timeStr.trim());

            return questions > 0 && questions <= maxQuestions &&
                    time >= 5 && time <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
