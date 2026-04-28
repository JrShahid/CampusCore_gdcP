package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;

public class ExamQuestion {
    private final String questionId;
    private final String questionText;
    private final List<String> options;
    private final int correctAnswerIndex;

    public ExamQuestion(String questionId, String questionText, List<String> options, int correctAnswerIndex) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.options = new ArrayList<>(options);
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getOptions() {
        return new ArrayList<>(options);
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
}
