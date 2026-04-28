package com.example.campuscoret;

public class LiveSessionSummary {
    private final String sessionId;
    private final String className;
    private final String subjectName;
    private final String teacherEmail;
    private final long startedAtMillis;
    private final int presentCount;
    private final int totalStudents;

    public LiveSessionSummary(
            String sessionId,
            String className,
            String subjectName,
            String teacherEmail,
            long startedAtMillis,
            int presentCount,
            int totalStudents
    ) {
        this.sessionId = sessionId;
        this.className = className;
        this.subjectName = subjectName;
        this.teacherEmail = teacherEmail;
        this.startedAtMillis = startedAtMillis;
        this.presentCount = presentCount;
        this.totalStudents = totalStudents;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getClassName() {
        return className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public int getAttendancePercentage() {
        if (totalStudents == 0) {
            return 0;
        }
        return Math.round((presentCount * 100f) / totalStudents);
    }
}
