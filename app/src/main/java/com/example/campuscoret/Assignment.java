package com.example.campuscoret;

public class Assignment {
    private final String assignmentId;
    private final String title;
    private final String description;
    private final String subjectName;
    private final String className;
    private final String teacherId;
    private final long deadlineMillis;
    private final long createdAtMillis;

    public Assignment(
            String assignmentId,
            String title,
            String description,
            String subjectName,
            String className,
            String teacherId,
            long deadlineMillis,
            long createdAtMillis
    ) {
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.subjectName = subjectName;
        this.className = className;
        this.teacherId = teacherId;
        this.deadlineMillis = deadlineMillis;
        this.createdAtMillis = createdAtMillis;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getClassName() {
        return className;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public long getDeadlineMillis() {
        return deadlineMillis;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }
}
