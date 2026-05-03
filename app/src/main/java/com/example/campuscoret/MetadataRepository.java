package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;

public final class MetadataRepository {
    private static final List<String> CLASSES = new ArrayList<>();
    private static final List<String> SUBJECTS = new ArrayList<>();

    private MetadataRepository() {
    }

    public static List<String> getClasses() {
        return new ArrayList<>(CLASSES);
    }

    public static void addClass(String className) {
        if (className != null && !className.trim().isEmpty() && !CLASSES.contains(className)) {
            CLASSES.add(className);
            FirebaseCampusSync.publishClass(className);
        }
    }

    public static void removeClass(String className) {
        CLASSES.remove(className);
        FirebaseCampusSync.removeClass(className);
    }

    public static List<String> getSubjects() {
        return new ArrayList<>(SUBJECTS);
    }

    public static void addSubject(String subjectName) {
        if (subjectName != null && !subjectName.trim().isEmpty() && !SUBJECTS.contains(subjectName)) {
            SUBJECTS.add(subjectName);
            FirebaseCampusSync.publishSubject(subjectName);
        }
    }

    public static void removeSubject(String subjectName) {
        SUBJECTS.remove(subjectName);
        FirebaseCampusSync.removeSubject(subjectName);
    }

    static void replaceClassesFromFirebase(List<String> classes) {
        CLASSES.clear();
        if (classes != null) {
            CLASSES.addAll(classes);
        }
    }

    static void replaceSubjectsFromFirebase(List<String> subjects) {
        SUBJECTS.clear();
        if (subjects != null) {
            SUBJECTS.addAll(subjects);
        }
    }
}
