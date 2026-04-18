package com.example.campuscoret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AttendanceRepository {
    private static final Map<String, List<AttendanceRecord>> ATTENDANCE_BY_SESSION = new LinkedHashMap<>();

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
        return true;
    }

    private static String sanitizeClassName(String className) {
        return className.toLowerCase().replace(" ", "").replace("/", "");
    }
}
