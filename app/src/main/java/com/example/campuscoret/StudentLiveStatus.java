package com.example.campuscoret;

public class StudentLiveStatus {
    private final String studentId;
    private final String studentName;
    private final String className;
    private final boolean online;
    private final long lastActiveMillis;
    private final String lastAction;

    public StudentLiveStatus(
            String studentId,
            String studentName,
            String className,
            boolean online,
            long lastActiveMillis,
            String lastAction
    ) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.online = online;
        this.lastActiveMillis = lastActiveMillis;
        this.lastAction = lastAction;
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

    public boolean isOnline() {
        return online;
    }

    public long getLastActiveMillis() {
        return lastActiveMillis;
    }

    public String getLastAction() {
        return lastAction;
    }
}
