package com.example.campuscore.models;

import com.example.campuscore.utils.AppRoles;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String role;
    private String department;
    private String semester;
    private String rollNumber;
    private String section;
    private String batch;

    public UserModel() {
        // Required for Firestore deserialization.
    }

    public UserModel(String uid, String name, String email, String role, String department, String semester, String rollNumber) {
        this(uid, name, email, role, department, semester, rollNumber, "", "");
    }

    public UserModel(String uid, String name, String email, String role, String department, String semester,
                     String rollNumber, String section, String batch) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
        this.semester = semester;
        this.rollNumber = rollNumber;
        this.section = section;
        this.batch = batch;
    }

    public String getUid() {
        return uid == null ? "" : uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role == null ? AppRoles.STUDENT : role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department == null ? "" : department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester == null ? "" : semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRollNumber() {
        return rollNumber == null ? "" : rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getSection() {
        return section == null ? "" : section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getBatch() {
        return batch == null ? "" : batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }
}
