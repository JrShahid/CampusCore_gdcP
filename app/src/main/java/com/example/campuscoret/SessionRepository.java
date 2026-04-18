package com.example.campuscoret;

public final class SessionRepository {
    private static SessionRecord activeSession;

    private SessionRepository() {
    }

    public static SessionRecord createSession(String teacherEmail, String subjectName, String className) {
        String sessionId = "SES-" + System.currentTimeMillis();
        activeSession = new SessionRecord(
                sessionId,
                teacherEmail,
                subjectName,
                className,
                System.currentTimeMillis(),
                true,
                true
        );
        AttendanceRepository.seedAttendanceForSession(sessionId, className);
        return activeSession;
    }

    public static SessionRecord getActiveSession() {
        return activeSession;
    }

    public static SessionRecord endActiveSession() {
        if (activeSession == null) {
            return null;
        }

        activeSession = new SessionRecord(
                activeSession.getSessionId(),
                activeSession.getTeacherEmail(),
                activeSession.getSubjectName(),
                activeSession.getClassName(),
                activeSession.getStartedAtMillis(),
                false,
                false
        );
        return activeSession;
    }
}
