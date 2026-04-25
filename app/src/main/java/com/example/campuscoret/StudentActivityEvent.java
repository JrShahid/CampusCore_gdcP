package com.example.campuscoret;

public class StudentActivityEvent {
    private final String studentId;
    private final String studentName;
    private final String className;
    private final String actionLabel;
    private final long occurredAtMillis;

    public StudentActivityEvent(
            String studentId,
            String studentName,
            String className,
            String actionLabel,
            long occurredAtMillis
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.actionLabel = actionLabel;
        this.occurredAtMillis = occurredAtMillis;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getClassName() {
        return className;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public long getOccurredAtMillis() {
        return occurredAtMillis;
    }
}
