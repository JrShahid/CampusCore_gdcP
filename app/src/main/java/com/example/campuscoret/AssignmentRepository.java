package com.example.campuscoret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AssignmentRepository {
    private static final List<Assignment> ASSIGNMENTS = buildSeedAssignments();
    private static final Map<String, List<AssignmentSubmission>> SUBMISSIONS = buildSeedSubmissions();
    private static final Map<String, Integer> MAX_MARKS_BY_ASSIGNMENT = buildMaxMarks();

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

    static List<Assignment> exportAssignments() {
        return new ArrayList<>(ASSIGNMENTS);
    }

    static List<AssignmentSubmission> exportSubmissions() {
        List<AssignmentSubmission> all = new ArrayList<>();
        for (List<AssignmentSubmission> submissions : SUBMISSIONS.values()) {
            all.addAll(submissions);
        }
        return all;
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
        assignments.add(new Assignment(
                "ASN-DEMO-3",
                "Mathematics Revision Set",
                "Complete the differentiation exercises and submit concise steps.",
                "Mathematics",
                "BCA 1A",
                "teacher@campus.edu",
                now + 172_800_000L,
                now - 18_000_000L
        ));
        assignments.add(new Assignment(
                "ASN-DEMO-4",
                "Advanced CS Worksheet",
                "Solve the networking and operating system MCQs.",
                "Computer Science",
                "BSc CS 3A",
                "teacher@campus.edu",
                now + 86_400_000L,
                now - 12_000_000L
        ));
        assignments.add(new Assignment(
                "ASN-DEMO-5",
                "Physics Concept Note",
                "Summarize the assigned concept note and attach your short explanation.",
                "Physics",
                "MCA 1C",
                "teacher@campus.edu",
                now + 259_200_000L,
                now - 9_000_000L
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
        demoSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-2",
                "ASN-DEMO-1",
                "diya@campus.edu",
                "Diya Patel",
                "Submitted worksheet with notes and examples.",
                "files/boolean_algebra_diya.pdf",
                System.currentTimeMillis() - 6_000_000L,
                17,
                "Consistent work. Recheck one simplification step."
        ));
        demoSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-3",
                "ASN-DEMO-1",
                "rohan@campus.edu",
                "Rohan Verma",
                "Uploaded partial worksheet.",
                "files/boolean_algebra_rohan.pdf",
                System.currentTimeMillis() - 4_000_000L,
                11,
                "Complete the remaining questions and improve accuracy."
        ));
        submissions.put("ASN-DEMO-1", demoSubmissions);

        List<AssignmentSubmission> mathSubmissions = new ArrayList<>();
        mathSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-4",
                "ASN-DEMO-3",
                "aarav@campus.edu",
                "Aarav Sharma",
                "Added full working for the revision set.",
                "files/math_revision_aarav.pdf",
                System.currentTimeMillis() - 5_400_000L,
                9,
                "Clear method. Improve presentation in the final question."
        ));
        mathSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-5",
                "ASN-DEMO-3",
                "sana@campus.edu",
                "Sana Khan",
                "Submitted incomplete revision answers.",
                "files/math_revision_sana.pdf",
                System.currentTimeMillis() - 3_600_000L,
                6,
                "Needs more complete working and practice."
        ));
        submissions.put("ASN-DEMO-3", mathSubmissions);

        List<AssignmentSubmission> bscSubmissions = new ArrayList<>();
        bscSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-6",
                "ASN-DEMO-4",
                "kabir@campus.edu",
                "Kabir Singh",
                "Completed the worksheet with short notes.",
                "files/advanced_cs_kabir.pdf",
                System.currentTimeMillis() - 2_400_000L,
                15,
                "Solid work across most questions."
        ));
        submissions.put("ASN-DEMO-4", bscSubmissions);

        List<AssignmentSubmission> mcaSubmissions = new ArrayList<>();
        mcaSubmissions.add(new AssignmentSubmission(
                "SUB-DEMO-7",
                "ASN-DEMO-5",
                "meera@campus.edu",
                "Meera Nair",
                "Uploaded concise concept summary.",
                "files/physics_note_meera.pdf",
                System.currentTimeMillis() - 1_800_000L,
                18,
                "Well structured and thoughtful summary."
        ));
        submissions.put("ASN-DEMO-5", mcaSubmissions);
        return submissions;
    }

    private static Map<String, Integer> buildMaxMarks() {
        Map<String, Integer> maxMarks = new LinkedHashMap<>();
        maxMarks.put("ASN-DEMO-1", 20);
        maxMarks.put("ASN-DEMO-2", 20);
        maxMarks.put("ASN-DEMO-3", 10);
        maxMarks.put("ASN-DEMO-4", 20);
        maxMarks.put("ASN-DEMO-5", 20);
        return maxMarks;
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
