package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;

public class ExamRecord {
    private final String examId;
    private final String title;
    private final String subjectName;
    private final String className;
    private final int durationMinutes;
    private final int totalMarks;
    private final long startTimeMillis;
    private final long endTimeMillis;
    private final String teacherEmail;
    private final List<ExamQuestion> questions;

    public ExamRecord(
            String examId,
            String title,
            String subjectName,
            String className,
            int durationMinutes,
            int totalMarks,
            long startTimeMillis,
            long endTimeMillis,
            String teacherEmail,
            List<ExamQuestion> questions
    ) {
        this.examId = examId;
        this.title = title;
        this.subjectName = subjectName;
        this.className = className;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        this.teacherEmail = teacherEmail;
        this.questions = new ArrayList<>(questions);
    }

    public String getExamId() {
        return examId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getClassName() {
        return className;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public List<ExamQuestion> getQuestions() {
        return new ArrayList<>(questions);
    }
}
