package com.example.campuscoret;

public class StudyMaterial {
    private final String title;
    private final String subjectName;
    private final String className;
    private final String fileType;
    private final String fileName;
    private final String uploadedBy;
    private final long uploadedAtMillis;

    public StudyMaterial(
            String title,
            String subjectName,
            String className,
            String fileType,
            String fileName,
            String uploadedBy,
            long uploadedAtMillis
    ) {
        this.title = title;
        this.subjectName = subjectName;
        this.className = className;
        this.fileType = fileType;
        this.fileName = fileName;
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

    public String getUploadedBy() {
        return uploadedBy;
    }

    public long getUploadedAtMillis() {
        return uploadedAtMillis;
    }
}
