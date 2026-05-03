package com.example.campuscoret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssignmentRepository {
    private static final List<Assignment> ASSIGNMENTS = new ArrayList<>();
    private static final Map<String, List<AssignmentSubmission>> SUBMISSIONS = new LinkedHashMap<>();
    private static final Map<String, Integer> MAX_MARKS_BY_ASSIGNMENT = new LinkedHashMap<>();

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
        MAX_MARKS_BY_ASSIGNMENT.put(assignment.getAssignmentId(), 10);
        FirebaseCampusSync.publishAssignment(assignment);
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
        FirebaseCampusSync.publishAssignmentSubmission(submission);
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
                    FirebaseCampusSync.publishAssignmentSubmission(updated);
                    return updated;
                }
            }
        }
        return null;
    }

    public static int getAssignmentAveragePercentage(String studentId, String className) {
        int obtained = 0;
        int possible = 0;

        for (Assignment assignment : ASSIGNMENTS) {
            if (!assignment.getClassName().equals(className)) {
                continue;
            }

            AssignmentSubmission submission = getSubmissionForStudent(assignment.getAssignmentId(), studentId);
            if (submission == null || submission.getMarks() == null) {
                continue;
            }

            obtained += submission.getMarks();
            possible += getMaxMarksForAssignment(assignment.getAssignmentId());
        }

        if (possible == 0) {
            return 0;
        }
        return Math.round((obtained * 100f) / possible);
    }

    public static int getOnTimeSubmissionRate(String studentId, String className) {
        int totalAssignments = 0;
        int onTimeSubmissions = 0;

        for (Assignment assignment : ASSIGNMENTS) {
            if (!assignment.getClassName().equals(className)) {
                continue;
            }

            totalAssignments++;
            AssignmentSubmission submission = getSubmissionForStudent(assignment.getAssignmentId(), studentId);
            if (submission != null && submission.getSubmittedAtMillis() <= assignment.getDeadlineMillis()) {
                onTimeSubmissions++;
            }
        }

        if (totalAssignments == 0) {
            return 0;
        }
        return Math.round((onTimeSubmissions * 100f) / totalAssignments);
    }

    public static int getMaxMarksForAssignment(String assignmentId) {
        Integer maxMarks = MAX_MARKS_BY_ASSIGNMENT.get(assignmentId);
        return maxMarks == null ? 10 : maxMarks;
    }

    static void replaceAssignmentsFromFirebase(List<Assignment> assignments, Map<String, Integer> maxMarks) {
        ASSIGNMENTS.clear();
        if (assignments != null) {
            ASSIGNMENTS.addAll(assignments);
        }
        MAX_MARKS_BY_ASSIGNMENT.clear();
        if (maxMarks != null) {
            MAX_MARKS_BY_ASSIGNMENT.putAll(maxMarks);
        }
    }

    static void replaceSubmissionsFromFirebase(Map<String, List<AssignmentSubmission>> submissions) {
        SUBMISSIONS.clear();
        if (submissions == null) {
            return;
        }
        for (Map.Entry<String, List<AssignmentSubmission>> entry : submissions.entrySet()) {
            SUBMISSIONS.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
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
