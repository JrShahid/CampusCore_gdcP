package com.example.campuscoret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StudentDirectoryRepository {
    private static final Map<String, StudentProfile> LOCAL_STUDENTS = buildStudents();
    private static final Map<String, StudentProfile> FIREBASE_STUDENTS = new LinkedHashMap<>();

    private StudentDirectoryRepository() {
    }

    public static List<StudentProfile> getStudentsForClass(String className) {
        List<StudentProfile> results = new ArrayList<>();
        for (StudentProfile profile : getAllProfiles().values()) {
            if (profile.getClassName().equals(className)) {
                results.add(profile);
            }
        }
        return results;
    }

    public static StudentProfile findStudentByEmail(String studentEmail) {
        return getAllProfiles().get(normalizeEmail(studentEmail));
    }

    public static void upsertProfile(StudentProfile profile) {
        if (profile == null) {
            return;
        }
        LOCAL_STUDENTS.put(normalizeEmail(profile.getStudentEmail()), profile);
    }

    public static void replaceFirebaseStudents(List<StudentProfile> profiles) {
        FIREBASE_STUDENTS.clear();
        if (profiles == null) {
            return;
        }
        for (StudentProfile profile : profiles) {
            if (profile != null) {
                FIREBASE_STUDENTS.put(normalizeEmail(profile.getStudentEmail()), profile);
            }
        }
    }

    private static Map<String, StudentProfile> buildStudents() {
        Map<String, StudentProfile> students = new LinkedHashMap<>();
        upsert(students, new StudentProfile("Aarav Sharma", "aarav@campus.edu", "BCA 1A"));
        upsert(students, new StudentProfile("Diya Patel", "diya@campus.edu", "BCA 2B"));
        upsert(students, new StudentProfile("Rohan Verma", "rohan@campus.edu", "BCA 2B"));
        upsert(students, new StudentProfile("Meera Nair", "meera@campus.edu", "MCA 1C"));
        upsert(students, new StudentProfile("Kabir Singh", "kabir@campus.edu", "BSc CS 3A"));
        upsert(students, new StudentProfile("Sana Khan", "sana@campus.edu", "BCA 1A"));
        return students;
    }

    private static void upsert(Map<String, StudentProfile> students, StudentProfile profile) {
        students.put(normalizeEmail(profile.getStudentEmail()), profile);
    }

    private static Map<String, StudentProfile> getAllProfiles() {
        Map<String, StudentProfile> combined = new LinkedHashMap<>(LOCAL_STUDENTS);
        combined.putAll(FIREBASE_STUDENTS);
        return combined;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
