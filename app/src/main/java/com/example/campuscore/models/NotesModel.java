package com.example.campuscore.models;

import com.google.firebase.Timestamp;

public class NotesModel {
    private String noteId;
    private String title;
    private String subjectCode;
    private String subjectName;
    private String department;
    private String semester;
    private String uploadedByUid;
    private String uploadedByName;
    private String pdfUrl;
    private String fileName;
    private Timestamp timestamp;

    public NotesModel() {
    }

    public NotesModel(String noteId, String title, String subjectCode, String subjectName, String department,
                      String semester, String uploadedByUid, String uploadedByName, String pdfUrl,
                      String fileName, Timestamp timestamp) {
        this.noteId = noteId;
        this.title = title;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.department = department;
        this.semester = semester;
        this.uploadedByUid = uploadedByUid;
        this.uploadedByName = uploadedByName;
        this.pdfUrl = pdfUrl;
        this.fileName = fileName;
        this.timestamp = timestamp;
    }

    public String getNoteId() {
        return noteId == null ? "" : noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubjectCode() {
        return subjectCode == null ? "" : subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName == null ? "" : subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public String getUploadedByUid() {
        return uploadedByUid == null ? "" : uploadedByUid;
    }

    public void setUploadedByUid(String uploadedByUid) {
        this.uploadedByUid = uploadedByUid;
    }

    public String getUploadedByName() {
        return uploadedByName == null ? "" : uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public String getPdfUrl() {
        return pdfUrl == null ? "" : pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getFileName() {
        return fileName == null ? "" : fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
