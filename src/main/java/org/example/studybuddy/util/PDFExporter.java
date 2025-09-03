package org.example.studybuddy.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.studybuddy.model.ExamLog;
import org.example.studybuddy.model.Question;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExporter {

    // Font definitions
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLUE);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    // Export exam result as PDF
    public static boolean exportExamResult(ExamLog examLog, List<Question> questions,
                                           List<String> userAnswers, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add metadata
            addMetadata(document, examLog);

            // Add header
            addHeader(document, examLog);

            // Add exam summary
            addExamSummary(document, examLog);

            // Add questions and answers
            addQuestionsSection(document, questions, userAnswers);

            // Add footer
            addFooter(document);

            document.close();
            return true;

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addMetadata(Document document, ExamLog examLog) {
        document.addTitle("StudyBuddy Exam Report - " + examLog.getExamName());
        document.addSubject("Exam Results");
        document.addKeywords("StudyBuddy, Exam, Results, PDF");
        document.addAuthor("StudyBuddy Application");
        document.addCreator("StudyBuddy PDF Exporter");
    }

    private static void addHeader(Document document, ExamLog examLog) throws DocumentException {
        // Title
        Paragraph title = new Paragraph("STUDYBUDDY EXAM REPORT", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Exam name
        Paragraph examName = new Paragraph(examLog.getExamName(), HEADER_FONT);
        examName.setAlignment(Element.ALIGN_CENTER);
        examName.setSpacingAfter(10);
        document.add(examName);

        // Date and time
        Paragraph dateTime = new Paragraph("Completed: " + examLog.getFormattedDate(), NORMAL_FONT);
        dateTime.setAlignment(Element.ALIGN_CENTER);
        dateTime.setSpacingAfter(20);
        document.add(dateTime);

        // Add line separator
        Paragraph separator = new Paragraph("_".repeat(80), SMALL_FONT);
        separator.setAlignment(Element.ALIGN_CENTER);
        separator.setSpacingAfter(20);
        document.add(separator);
    }

    private static void addExamSummary(Document document, ExamLog examLog) throws DocumentException {
        Paragraph summaryTitle = new Paragraph("EXAM SUMMARY", HEADER_FONT);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        // Create summary table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);

        addTableRow(summaryTable, "Total Questions:", String.valueOf(examLog.getTotalQuestions()));
        addTableRow(summaryTable, "Correct Answers:", String.valueOf(examLog.getCorrectAnswers()));
        addTableRow(summaryTable, "Wrong Answers:", String.valueOf(examLog.getWrongAnswers()));
        addTableRow(summaryTable, "Unanswered:", String.valueOf(examLog.getUnanswered()));
        addTableRow(summaryTable, "Score:", String.format("%.1f/%.0f", examLog.getScore(), (double)examLog.getTotalQuestions()));
        addTableRow(summaryTable, "Percentage:", String.format("%.1f%%", examLog.getPercentage()));
        addTableRow(summaryTable, "Grade:", examLog.getGrade());
        addTableRow(summaryTable, "Time Taken:", examLog.getFormattedTime());

        document.add(summaryTable);
    }

    private static void addQuestionsSection(Document document, List<Question> questions,
                                            List<String> userAnswers) throws DocumentException {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        document.newPage();

        Paragraph questionsTitle = new Paragraph("DETAILED QUESTIONS & ANSWERS", HEADER_FONT);
        questionsTitle.setSpacingAfter(15);
        document.add(questionsTitle);

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = i < userAnswers.size() ? userAnswers.get(i) : "Not Answered";
            boolean isCorrect = userAnswer.equals(question.getCorrectAnswer());

            // Question number and text
            Paragraph questionPara = new Paragraph();
            questionPara.add(new Chunk("Q" + (i + 1) + ". ", HEADER_FONT));
            questionPara.add(new Chunk(question.getQuestionText(), NORMAL_FONT));
            questionPara.setSpacingAfter(5);
            document.add(questionPara);

            // Options
            String[] options = {question.getOptionA(), question.getOptionB(),
                    question.getOptionC(), question.getOptionD()};
            String[] labels = {"A", "B", "C", "D"};

            for (int j = 0; j < options.length; j++) {
                Paragraph optionPara = new Paragraph();

                if (labels[j].equals(question.getCorrectAnswer())) {
                    optionPara.add(new Chunk(labels[j] + ". ", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GREEN)));
                    optionPara.add(new Chunk(options[j] + " ✓", new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GREEN)));
                } else if (labels[j].equals(userAnswer) && !isCorrect) {
                    optionPara.add(new Chunk(labels[j] + ". ", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED)));
                    optionPara.add(new Chunk(options[j] + " ✗", new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.RED)));
                } else {
                    optionPara.add(new Chunk(labels[j] + ". " + options[j], NORMAL_FONT));
                }

                document.add(optionPara);
            }

            // User answer status
            Paragraph statusPara = new Paragraph();
            if (userAnswer.equals("Not Answered")) {
                statusPara.add(new Chunk("Your Answer: Not Answered", new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC, BaseColor.GRAY)));
            } else if (isCorrect) {
                statusPara.add(new Chunk("Your Answer: " + userAnswer + " - CORRECT", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.GREEN)));
            } else {
                statusPara.add(new Chunk("Your Answer: " + userAnswer + " - INCORRECT", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.RED)));
            }
            statusPara.setSpacingAfter(15);
            document.add(statusPara);

            // Add explanation if available
            if (question.getExplanation() != null && !question.getExplanation().trim().isEmpty()) {
                Paragraph explanationPara = new Paragraph();
                explanationPara.add(new Chunk("Explanation: ", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));
                explanationPara.add(new Chunk(question.getExplanation(), new Font(Font.FontFamily.HELVETICA, 11, Font.ITALIC)));
                explanationPara.setSpacingAfter(20);
                document.add(explanationPara);
            }
        }
    }

    private static void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("Generated by StudyBuddy on " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                SMALL_FONT));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private static void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

}
