package com.example.campuscoret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AttendanceRepository {
    private static final Map<String, List<AttendanceRecord>> ATTENDANCE_BY_SESSION = new LinkedHashMap<>();
    private static final Map<String, AttendanceSummary> ATTENDANCE_SUMMARIES = buildAttendanceSummaries();

    private AttendanceRepository() {
    }

    public static void seedAttendanceForSession(String sessionId, String className) {
        List<AttendanceRecord> records = new ArrayList<>();
        long now = System.currentTimeMillis();

        records.add(new AttendanceRecord(sessionId, "Aarav Sharma", "aarav@" + sanitizeClassName(className) + ".edu", now - 420000, "Present"));
        records.add(new AttendanceRecord(sessionId, "Diya Patel", "diya@" + sanitizeClassName(className) + ".edu", now - 240000, "Present"));
        records.add(new AttendanceRecord(sessionId, "Kabir Singh", "kabir@" + sanitizeClassName(className) + ".edu", now - 120000, "Late"));
        records.add(new AttendanceRecord(sessionId, "Meera Nair", "meera@" + sanitizeClassName(className) + ".edu", now - 60000, "Present"));

        ATTENDANCE_BY_SESSION.put(sessionId, records);
        for (AttendanceRecord record : records) {
            FirebaseCampusSync.publishAttendanceRecord(record);
        }
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

    static Map<String, AttendanceStats> exportSummaryRecords() {
        Map<String, AttendanceStats> export = new LinkedHashMap<>();
        for (Map.Entry<String, AttendanceSummary> entry : ATTENDANCE_SUMMARIES.entrySet()) {
            AttendanceSummary summary = entry.getValue();
            export.put(entry.getKey(), new AttendanceStats(summary.presentClasses, summary.totalClasses));
        }
        return export;
    }

    private static String sanitizeClassName(String className) {
        return className.toLowerCase().replace(" ", "").replace("/", "");
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

    private static Map<String, AttendanceSummary> buildAttendanceSummaries() {
        Map<String, AttendanceSummary> summaries = new LinkedHashMap<>();
        summaries.put(buildStudentKey("aarav@campus.edu", "BCA 1A"), new AttendanceSummary(42, 50));
        summaries.put(buildStudentKey("sana@campus.edu", "BCA 1A"), new AttendanceSummary(36, 50));
        summaries.put(buildStudentKey("diya@campus.edu", "BCA 2B"), new AttendanceSummary(43, 50));
        summaries.put(buildStudentKey("rohan@campus.edu", "BCA 2B"), new AttendanceSummary(34, 50));
        summaries.put(buildStudentKey("meera@campus.edu", "MCA 1C"), new AttendanceSummary(45, 50));
        summaries.put(buildStudentKey("kabir@campus.edu", "BSc CS 3A"), new AttendanceSummary(39, 50));
        return summaries;
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
