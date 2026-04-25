package com.example.campuscoret;

public class StudentPerformanceSummary {
    private final String studentName;
    private final String studentId;
    private final String className;
    private final int attendancePercentage;
    private final int assignmentPercentage;
    private final int examPercentage;
    private final int behaviorPercentage;
    private final int onTimeAssignmentRate;
    private final double finalScore;
    private final String status;
    private final String supportMessage;

    public StudentPerformanceSummary(
            String studentName,
            String studentId,
            String className,
            int attendancePercentage,
            int assignmentPercentage,
            int examPercentage,
            int behaviorPercentage,
            int onTimeAssignmentRate,
            double finalScore,
            String status,
            String supportMessage
    ) {
        this.studentName = studentName;
        this.studentId = studentId;
        this.className = className;
        this.attendancePercentage = attendancePercentage;
        this.assignmentPercentage = assignmentPercentage;
        this.examPercentage = examPercentage;
        this.behaviorPercentage = behaviorPercentage;
        this.onTimeAssignmentRate = onTimeAssignmentRate;
        this.finalScore = finalScore;
        this.status = status;
        this.supportMessage = supportMessage;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getClassName() {
        return className;
    }

    public int getAttendancePercentage() {
        return attendancePercentage;
    }

    public int getAssignmentPercentage() {
        return assignmentPercentage;
    }

    public int getExamPercentage() {
        return examPercentage;
    }

    public int getBehaviorPercentage() {
        return behaviorPercentage;
    }

    public int getOnTimeAssignmentRate() {
        return onTimeAssignmentRate;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public String getStatus() {
        return status;
    }

    public String getSupportMessage() {
        return supportMessage;
    }
}
