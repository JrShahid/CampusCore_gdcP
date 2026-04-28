package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReportRepository {
    public static final String TYPE_ATTENDANCE = "Attendance Report";
    public static final String TYPE_ASSIGNMENT = "Assignment Report";
    public static final String TYPE_EXAM = "Exam Performance Report";
    public static final String TYPE_OVERALL = "Overall Performance Report";

    private ReportRepository() {
    }

    public static GeneratedReport generateReport(String reportType, String className) {
        if (TYPE_ATTENDANCE.equals(reportType)) {
            return buildAttendanceReport(className);
        }
        if (TYPE_ASSIGNMENT.equals(reportType)) {
            return buildAssignmentReport(className);
        }
        if (TYPE_EXAM.equals(reportType)) {
            return buildExamReport(className);
        }
        return buildOverallReport(className);
    }

    private static GeneratedReport buildAttendanceReport(String className) {
        List<ReportRow> rows = new ArrayList<>();
        StringBuilder export = new StringBuilder();
        export.append("Attendance Report - ").append(className).append("\n\n");

        int studentCount = 0;
        for (StudentProfile profile : StudentDirectoryRepository.getStudentsForClass(className)) {
            int attended = AttendanceRepository.getPresentClasses(profile.getStudentEmail(), className);
            int total = AttendanceRepository.getTotalClasses(profile.getStudentEmail(), className);
            int percentage = AttendanceRepository.getAttendancePercentage(profile.getStudentEmail(), className);

            rows.add(new ReportRow(
                    profile.getStudentName(),
                    "Student ID: " + profile.getStudentEmail(),
                    "Attended " + attended + " of " + total + " classes",
                    "Attendance percentage: " + percentage + "%"
            ));

            export.append(profile.getStudentName())
                    .append(" | ")
                    .append(attended)
                    .append("/")
                    .append(total)
                    .append(" | ")
                    .append(percentage)
                    .append("%\n");
            studentCount++;
        }

        return new GeneratedReport(
                TYPE_ATTENDANCE,
                "Students covered: " + studentCount,
                export.toString(),
                rows
        );
    }

    private static GeneratedReport buildAssignmentReport(String className) {
        List<ReportRow> rows = new ArrayList<>();
        StringBuilder export = new StringBuilder();
        export.append("Assignment Report - ").append(className).append("\n\n");

        for (Assignment assignment : AssignmentRepository.getAssignmentsForClass(className)) {
            for (StudentProfile profile : StudentDirectoryRepository.getStudentsForClass(className)) {
                AssignmentSubmission submission =
                        AssignmentRepository.getSubmissionForStudent(assignment.getAssignmentId(), profile.getStudentEmail());
                String marksSummary;
                String status;
                String feedback;

                if (submission == null) {
                    marksSummary = "Marks: Pending";
                    status = "Status: Not Submitted";
                    feedback = "Feedback: Awaiting submission";
                } else {
                    int maxMarks = AssignmentRepository.getMaxMarksForAssignment(assignment.getAssignmentId());
                    marksSummary = submission.getMarks() == null
                            ? "Marks: Pending Evaluation"
                            : "Marks: " + submission.getMarks() + "/" + maxMarks;
                    status = "Status: Submitted";
                    feedback = submission.getFeedback() == null
                            ? "Feedback: Pending"
                            : "Feedback: " + submission.getFeedback();
                }

                rows.add(new ReportRow(
                        profile.getStudentName() + " - " + assignment.getTitle(),
                        "Student ID: " + profile.getStudentEmail(),
                        marksSummary + " | " + status,
                        feedback
                ));

                export.append(profile.getStudentName())
                        .append(" | ")
                        .append(assignment.getTitle())
                        .append(" | ")
                        .append(marksSummary.replace("Marks: ", ""))
                        .append(" | ")
                        .append(status.replace("Status: ", ""))
                        .append("\n");
            }
        }

        return new GeneratedReport(
                TYPE_ASSIGNMENT,
                "Assignments in class: " + AssignmentRepository.getAssignmentsForClass(className).size(),
                export.toString(),
                rows
        );
    }

    private static GeneratedReport buildExamReport(String className) {
        List<ReportRow> rows = new ArrayList<>();
        StringBuilder export = new StringBuilder();
        export.append("Exam Performance Report - ").append(className).append("\n\n");

        for (ExamRecord exam : ExamRepository.getExamsForClass(className)) {
            for (StudentProfile profile : StudentDirectoryRepository.getStudentsForClass(className)) {
                ExamAttempt attempt = ExamRepository.getAttemptForStudent(exam.getExamId(), profile.getStudentEmail());
                String scoreLine;
                String levelLine;

                if (attempt == null) {
                    scoreLine = "Score: Pending";
                    levelLine = "Performance: Not Attempted";
                } else {
                    scoreLine = "Score: " + attempt.getScore() + "/" + exam.getTotalMarks()
                            + " | Percentage: " + attempt.getPercentage() + "%";
                    levelLine = "Performance: " + classifyExamPerformance(attempt.getPercentage());
                }

                rows.add(new ReportRow(
                        profile.getStudentName() + " - " + exam.getTitle(),
                        "Student ID: " + profile.getStudentEmail(),
                        scoreLine,
                        levelLine
                ));

                export.append(profile.getStudentName())
                        .append(" | ")
                        .append(exam.getTitle())
                        .append(" | ")
                        .append(scoreLine.replace("Score: ", ""))
                        .append(" | ")
                        .append(levelLine.replace("Performance: ", ""))
                        .append("\n");
            }
        }

        return new GeneratedReport(
                TYPE_EXAM,
                "Exams in class: " + ExamRepository.getExamsForClass(className).size(),
                export.toString(),
                rows
        );
    }

    private static GeneratedReport buildOverallReport(String className) {
        List<ReportRow> rows = new ArrayList<>();
        StringBuilder export = new StringBuilder();
        export.append("Overall Performance Report - ").append(className).append("\n\n");

        List<StudentPerformanceSummary> summaries = PerformanceAnalyticsRepository.getSummariesForClass(className);
        for (StudentPerformanceSummary summary : summaries) {
            rows.add(new ReportRow(
                    summary.getStudentName(),
                    "Attendance " + summary.getAttendancePercentage() + "% | Assignments " + summary.getAssignmentPercentage()
                            + "% | Exams " + summary.getExamPercentage() + "%",
                    String.format(Locale.getDefault(), "Final score %.2f | Status: %s", summary.getFinalScore(), summary.getStatus()),
                    summary.getSupportMessage()
            ));

            export.append(summary.getStudentName())
                    .append(" | Attendance ")
                    .append(summary.getAttendancePercentage())
                    .append("% | Assignments ")
                    .append(summary.getAssignmentPercentage())
                    .append("% | Exams ")
                    .append(summary.getExamPercentage())
                    .append("% | Final ")
                    .append(String.format(Locale.getDefault(), "%.2f", summary.getFinalScore()))
                    .append(" | ")
                    .append(summary.getStatus())
                    .append("\n");
        }

        return new GeneratedReport(
                TYPE_OVERALL,
                "Students covered: " + summaries.size(),
                export.toString(),
                rows
        );
    }

    private static String classifyExamPerformance(int percentage) {
        if (percentage >= 85) {
            return "Excellent";
        }
        if (percentage >= 70) {
            return "Good";
        }
        if (percentage >= 50) {
            return "Average";
        }
        return "Needs Improvement";
    }
}
