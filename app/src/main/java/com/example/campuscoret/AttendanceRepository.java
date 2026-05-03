package com.example.campuscoret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AttendanceRepository {
    private static final Map<String, List<AttendanceRecord>> ATTENDANCE_BY_SESSION = new LinkedHashMap<>();
    private static final Map<String, AttendanceSummary> ATTENDANCE_SUMMARIES = new LinkedHashMap<>();

    private AttendanceRepository() {
    }

    public static List<AttendanceRecord> getAttendanceForSession(String sessionId) {
        List<AttendanceRecord> records = ATTENDANCE_BY_SESSION.get(sessionId);
        if (records == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(records);
    }

    public static boolean hasAttendanceForStudent(String sessionId, String studentEmail) {
        List<AttendanceRecord> records = ATTENDANCE_BY_SESSION.get(sessionId);
        if (records == null) {
            return false;
        }

        for (AttendanceRecord record : records) {
            if (record.getStudentEmail().equalsIgnoreCase(studentEmail)) {
                return true;
            }
        }
        return false;
    }

    public static boolean markAttendance(String sessionId, String studentName, String studentEmail, String status) {
        if (hasAttendanceForStudent(sessionId, studentEmail)) {
            return false;
        }

        List<AttendanceRecord> records = ATTENDANCE_BY_SESSION.get(sessionId);
        if (records == null) {
            records = new ArrayList<>();
            ATTENDANCE_BY_SESSION.put(sessionId, records);
        }

        records.add(new AttendanceRecord(
                sessionId,
                studentName,
                studentEmail,
                System.currentTimeMillis(),
                status
        ));
        FirebaseCampusSync.publishAttendanceRecord(records.get(records.size() - 1));
        return true;
    }

    static void replaceSessionRecords(Map<String, List<AttendanceRecord>> recordsBySession) {
        ATTENDANCE_BY_SESSION.clear();
        if (recordsBySession == null) {
            return;
        }
        for (Map.Entry<String, List<AttendanceRecord>> entry : recordsBySession.entrySet()) {
            ATTENDANCE_BY_SESSION.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    static void replaceSummaryRecords(Map<String, AttendanceStats> summaries) {
        ATTENDANCE_SUMMARIES.clear();
        if (summaries == null) {
            return;
        }
        for (Map.Entry<String, AttendanceStats> entry : summaries.entrySet()) {
            AttendanceStats stats = entry.getValue();
            ATTENDANCE_SUMMARIES.put(entry.getKey(), new AttendanceSummary(
                    stats.getPresentClasses(),
                    stats.getTotalClasses()
            ));
        }
    }

    public static int getPresentClasses(String studentEmail, String className) {
        AttendanceSummary summary = ATTENDANCE_SUMMARIES.get(buildStudentKey(studentEmail, className));
        return summary == null ? 0 : summary.presentClasses;
    }

    public static int getTotalClasses(String studentEmail, String className) {
        AttendanceSummary summary = ATTENDANCE_SUMMARIES.get(buildStudentKey(studentEmail, className));
        return summary == null ? 0 : summary.totalClasses;
    }

    public static int getAttendancePercentage(String studentEmail, String className) {
        int totalClasses = getTotalClasses(studentEmail, className);
        if (totalClasses == 0) {
            return 0;
        }
        return Math.round((getPresentClasses(studentEmail, className) * 100f) / totalClasses);
    }

    private static String buildStudentKey(String studentEmail, String className) {
        return studentEmail.toLowerCase() + "|" + className;
    }

    public static final class AttendanceStats {
        private final int presentClasses;
        private final int totalClasses;

        public AttendanceStats(int presentClasses, int totalClasses) {
            this.presentClasses = presentClasses;
            this.totalClasses = totalClasses;
        }

        public int getPresentClasses() {
            return presentClasses;
        }

        public int getTotalClasses() {
            return totalClasses;
        }
    }

    private static final class AttendanceSummary {
        private final int presentClasses;
        private final int totalClasses;

        private AttendanceSummary(int presentClasses, int totalClasses) {
            this.presentClasses = presentClasses;
            this.totalClasses = totalClasses;
        }
    }
}
