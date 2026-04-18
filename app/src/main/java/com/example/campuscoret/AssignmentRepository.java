package com.example.campuscoret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssignmentRepository {
    private static final List<Assignment> ASSIGNMENTS = buildSeedAssignments();
    private static final Map<String, List<AssignmentSubmission>> SUBMISSIONS = buildSeedSubmissions();

    private AssignmentRepository() {
    }

    public static Assignment createAssignment(
            String title,
            String description,
            String subjectName,
            String className,
            String teacherId,
            long deadlineMillis
    ) {
        Assignment assignment = new Assignment(
                "ASN-" + System.currentTimeMillis(),
                title,
                description,
                subjectName,
                className,
                teacherId,
                deadlineMillis,
                System.currentTimeMillis()
        );
        ASSIGNMENTS.add(0, assignment);
        return assignment;
    }

    public static List<Assignment> getAssignmentsForTeacher(String teacherId) {
        List<Assignment> results = new ArrayList<>();
        for (Assignment assignment : ASSIGNMENTS) {
            if (assignment.getTeacherId().equals(teacherId)) {
                results.add(assignment);
            }
        }
        return results;
    }

    public static List<Assignment> getAssignmentsForClass(String className) {
        List<Assignment> results = new ArrayList<>();
        for (Assignment assignment : ASSIGNMENTS) {
            if (assignment.getClassName().equals(className)) {
                results.add(assignment);
            }
        }
        return results;
    }

    public static Assignment findAssignmentById(String assignmentId) {
        for (Assignment assignment : ASSIGNMENTS) {
            if (assignment.getAssignmentId().equals(assignmentId)) {
                return assignment;
            }
        }
        return null;
    }

    public static SubmissionResult submitAssignment(
            String assignmentId,
            String studentId,
            String studentName,
            String solutionText,
            String fileUrl
    ) {
        Assignment assignment = findAssignmentById(assignmentId);
        if (assignment == null) {
            return SubmissionResult.notFound();
        }

        if (System.currentTimeMillis() > assignment.getDeadlineMillis()) {
            return SubmissionResult.deadlinePassed();
        }

        List<AssignmentSubmission> submissions = SUBMISSIONS.get(assignmentId);
        if (submissions == null) {
            submissions = new ArrayList<>();
            SUBMISSIONS.put(assignmentId, submissions);
        }

        for (AssignmentSubmission submission : submissions) {
            if (submission.getStudentId().equals(studentId)) {
                return SubmissionResult.duplicate();
            }
        }

        AssignmentSubmission submission = new AssignmentSubmission(
                "SUB-" + System.currentTimeMillis(),
                assignmentId,
                studentId,
                studentName,
                solutionText,
                fileUrl,
                System.currentTimeMillis(),
                null,
                null
        );
        submissions.add(submission);
        return SubmissionResult.success(submission);
    }

    public static List<AssignmentSubmission> getSubmissionsForAssignment(String assignmentId) {
        List<AssignmentSubmission> submissions = SUBMISSIONS.get(assignmentId);
        if (submissions == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(submissions);
    }

    public static AssignmentSubmission getSubmissionForStudent(String assignmentId, String studentId) {
        List<AssignmentSubmission> submissions = SUBMISSIONS.get(assignmentId);
        if (submissions == null) {
            return null;
        }
        for (AssignmentSubmission submission : submissions) {
            if (submission.getStudentId().equals(studentId)) {
                return submission;
            }
        }
        return null;
    }

    public static AssignmentSubmission evaluateSubmission(
            String submissionId,
            int marks,
            String feedback
    ) {
        for (Map.Entry<String, List<AssignmentSubmission>> entry : SUBMISSIONS.entrySet()) {
            List<AssignmentSubmission> submissions = entry.getValue();
            for (int i = 0; i < submissions.size(); i++) {
                AssignmentSubmission submission = submissions.get(i);
                if (submission.getSubmissionId().equals(submissionId)) {
                    AssignmentSubmission updated = new AssignmentSubmission(
                            submission.getSubmissionId(),
                            submission.getAssignmentId(),
                            submission.getStudentId(),
                            submission.getStudentName(),
                            submission.getSolutionText(),
                            submission.getFileUrl(),
                            submission.getSubmittedAtMillis(),
                            marks,
                            feedback
                    );
                    submissions.set(i, updated);
                    return updated;
                }
            }
        }
        return null;
    }

    private static List<Assignment> buildSeedAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        long now = System.currentTimeMillis();
        assignments.add(new Assignment(
                "ASN-DEMO-1",
                "Boolean Algebra Worksheet",
                "Solve the attached worksheet and explain any two logic simplification steps.",
                "Computer Science",
                "BCA 2B",
                "teacher@campus.edu",
                now + 172_800_000L,
                now - 21_600_000L
        ));
        assignments.add(new Assignment(
                "ASN-DEMO-2",
                "Physics Practical Report",
                "Submit your optics lab report in PDF or typed notes form.",
                "Physics",
                "BCA 2B",
                "teacher@campus.edu",
                now + 259_200_000L,
                now - 10_800_000L
        ));
        return assignments;
    }

    private static Map<String, List<AssignmentSubmission>> buildSeedSubmissions() {
        Map<String, List<AssignmentSubmission>> submissions = new LinkedHashMap<>();
        List<AssignmentSubmission> demoSubmissions = new ArrayList<>();
        demoSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-1",
                "ASN-DEMO-1",
                "aarav@campus.edu",
                "Aarav",
                "Uploaded solved worksheet and a short explanation section.",
                "files/boolean_algebra_aarav.pdf",
                System.currentTimeMillis() - 7_200_000L,
                18,
                "Good logic reduction steps. Add clearer working for question 4."
        ));
        submissions.put("ASN-DEMO-1", demoSubmissions);
        return submissions;
    }

    public static final class SubmissionResult {
        public enum Status {
            SUCCESS,
            DUPLICATE,
            DEADLINE_PASSED,
            NOT_FOUND
        }

        private final Status status;
        private final AssignmentSubmission submission;

        private SubmissionResult(Status status, AssignmentSubmission submission) {
            this.status = status;
            this.submission = submission;
        }

        public static SubmissionResult success(AssignmentSubmission submission) {
            return new SubmissionResult(Status.SUCCESS, submission);
        }

        public static SubmissionResult duplicate() {
            return new SubmissionResult(Status.DUPLICATE, null);
        }

        public static SubmissionResult deadlinePassed() {
            return new SubmissionResult(Status.DEADLINE_PASSED, null);
        }

        public static SubmissionResult notFound() {
            return new SubmissionResult(Status.NOT_FOUND, null);
        }

        public Status getStatus() {
            return status;
        }

        public AssignmentSubmission getSubmission() {
            return submission;
        }
    }
}
