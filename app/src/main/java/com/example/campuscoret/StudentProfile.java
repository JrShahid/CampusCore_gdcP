package com.example.campuscoret;

public class StudentProfile {
    private final String studentName;
    private final String studentEmail;
    private final String className;

    public StudentProfile(String studentName, String studentEmail, String className) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.className = className;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getClassName() {
        return className;
    }
}
