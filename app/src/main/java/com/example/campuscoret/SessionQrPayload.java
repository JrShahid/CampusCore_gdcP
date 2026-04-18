package com.example.campuscoret;

public class SessionQrPayload {
    private final String sessionId;
    private final String className;
    private final String subjectName;
    private final long issuedAtMillis;

    public SessionQrPayload(String sessionId, String className, String subjectName, long issuedAtMillis) {
        this.sessionId = sessionId;
        this.className = className;
        this.subjectName = subjectName;
        this.issuedAtMillis = issuedAtMillis;
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

    public long getIssuedAtMillis() {
        return issuedAtMillis;
    }
}
