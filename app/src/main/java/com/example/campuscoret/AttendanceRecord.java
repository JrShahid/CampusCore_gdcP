package com.example.campuscoret;

public class AttendanceRecord {
    private final String sessionId;
    private final String studentName;
    private final String studentEmail;
    private final long markedAtMillis;
    private final String status;

    public AttendanceRecord(
            String sessionId,
            String studentName,
            String studentEmail,
            long markedAtMillis,
            String status
    ) {
        this.sessionId = sessionId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.markedAtMillis = markedAtMillis;
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public long getMarkedAtMillis() {
        return markedAtMillis;
    }

    public String getStatus() {
        return status;
    }
}
