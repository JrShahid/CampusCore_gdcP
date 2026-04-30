package com.example.campuscoret;

public class StudyMaterial {
    private final String title;
    private final String subjectName;
    private final String className;
    private final String fileType;
    private final String fileName;
    private final String fileUrl;
    private final String uploadedBy;
    private final long uploadedAtMillis;

    public StudyMaterial(
            String title,
            String subjectName,
            String className,
            String fileType,
            String fileName,
            String fileUrl,
            String uploadedBy,
            long uploadedAtMillis
    ) {
        this.title = title;
        this.subjectName = subjectName;
        this.className = className;
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedBy = uploadedBy;
        this.uploadedAtMillis = uploadedAtMillis;
    }

    public String getTitle() {
        return title;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getClassName() {
        return className;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public long getUploadedAtMillis() {
        return uploadedAtMillis;
    }
}
