package com.example.campuscoret;

public class LiveExamSummary {
    private final String examId;
    private final String title;
    private final String className;
    private final String subjectName;
    private final int attemptingCount;
    private final int submittedCount;
    private final int pendingCount;

    public LiveExamSummary(
            String examId,
            String title,
            String className,
            String subjectName,
            int attemptingCount,
            int submittedCount,
            int pendingCount
    ) {
        this.examId = examId;
        this.title = title;
        this.className = className;
        this.subjectName = subjectName;
        this.attemptingCount = attemptingCount;
        this.submittedCount = submittedCount;
        this.pendingCount = pendingCount;
    }

    public String getExamId() {
        return examId;
    }

    public String getTitle() {
        return title;
    }

    public String getClassName() {
        return className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getAttemptingCount() {
        return attemptingCount;
    }

    public int getSubmittedCount() {
        return submittedCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }
}
