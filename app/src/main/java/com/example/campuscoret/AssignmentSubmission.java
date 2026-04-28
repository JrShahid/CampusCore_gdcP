package com.example.campuscoret;

public class AssignmentSubmission {
    private final String submissionId;
    private final String assignmentId;
    private final String studentId;
    private final String studentName;
    private final String solutionText;
    private final String fileUrl;
    private final long submittedAtMillis;
    private final Integer marks;
    private final String feedback;

    public AssignmentSubmission(
            String submissionId,
            String assignmentId,
            String studentId,
            String studentName,
            String solutionText,
            String fileUrl,
            long submittedAtMillis,
            Integer marks,
            String feedback
    ) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.solutionText = solutionText;
        this.fileUrl = fileUrl;
        this.submittedAtMillis = submittedAtMillis;
        this.marks = marks;
        this.feedback = feedback;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getSolutionText() {
        return solutionText;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public long getSubmittedAtMillis() {
        return submittedAtMillis;
    }

    public Integer getMarks() {
        return marks;
    }

    public String getFeedback() {
        return feedback;
    }
}
