package com.example.campuscoret;

public class SessionRecord {
    private final String sessionId;
    private final String teacherEmail;
    private final String subjectName;
    private final String className;
    private final long startedAtMillis;
    private final boolean attendanceActive;
    private final boolean active;

    public SessionRecord(
            String sessionId,
            String teacherEmail,
            String subjectName,
            String className,
            long startedAtMillis,
            boolean attendanceActive,
            boolean active
    ) {
        this.sessionId = sessionId;
        this.teacherEmail = teacherEmail;
        this.subjectName = subjectName;
        this.className = className;
        this.startedAtMillis = startedAtMillis;
        this.attendanceActive = attendanceActive;
        this.active = active;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getClassName() {
        return className;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public boolean isAttendanceActive() {
        return attendanceActive;
    }

    public boolean isActive() {
        return active;
    }
}
