package com.example.campuscoret;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExamAttempt {
    private final String attemptId;
    private final String examId;
    private final String studentId;
    private final String studentName;
    private final Map<String, Integer> selectedAnswers;
    private final long submittedAtMillis;
    private final int score;
    private final int percentage;
    private final String feedback;

    public ExamAttempt(
            String attemptId,
            String examId,
            String studentId,
            String studentName,
            Map<String, Integer> selectedAnswers,
            long submittedAtMillis,
            int score,
            int percentage,
            String feedback
    ) {
        this.attemptId = attemptId;
        this.examId = examId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.selectedAnswers = new LinkedHashMap<>(selectedAnswers);
        this.submittedAtMillis = submittedAtMillis;
        this.score = score;
        this.percentage = percentage;
        this.feedback = feedback;
    }

    public String getAttemptId() {
        return attemptId;
    }

    public String getExamId() {
        return examId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public Map<String, Integer> getSelectedAnswers() {
        return new LinkedHashMap<>(selectedAnswers);
    }

    public long getSubmittedAtMillis() {
        return submittedAtMillis;
    }

    public int getScore() {
        return score;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getFeedback() {
        return feedback;
    }
}
