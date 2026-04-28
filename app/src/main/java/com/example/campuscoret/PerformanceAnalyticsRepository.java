package com.example.campuscoret;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PerformanceAnalyticsRepository {
    private static final double ATTENDANCE_WEIGHT = 0.20d;
    private static final double ASSIGNMENT_WEIGHT = 0.25d;
    private static final double EXAM_WEIGHT = 0.45d;
    private static final double BEHAVIOR_WEIGHT = 0.10d;

    private PerformanceAnalyticsRepository() {
    }

    public static List<StudentPerformanceSummary> getSummariesForClass(String className) {
        List<StudentPerformanceSummary> results = new ArrayList<>();
        for (StudentProfile profile : StudentDirectoryRepository.getStudentsForClass(className)) {
            results.add(buildSummary(profile));
        }
        return results;
    }

    public static StudentPerformanceSummary getSummaryForStudent(String studentId) {
        StudentProfile profile = StudentDirectoryRepository.findStudentByEmail(studentId);
        if (profile == null) {
            return null;
        }
        return buildSummary(profile);
    }

    private static StudentPerformanceSummary buildSummary(StudentProfile profile) {
        int attendance = AttendanceRepository.getAttendancePercentage(profile.getStudentEmail(), profile.getClassName());
        int assignments = AssignmentRepository.getAssignmentAveragePercentage(profile.getStudentEmail(), profile.getClassName());
        int onTimeRate = AssignmentRepository.getOnTimeSubmissionRate(profile.getStudentEmail(), profile.getClassName());
        int exams = ExamRepository.getAveragePercentageForStudent(profile.getStudentEmail(), profile.getClassName());
        int behavior = BehaviorRepository.getBehaviorScore(profile.getStudentEmail());

        double finalScore = (attendance * ATTENDANCE_WEIGHT)
                + (assignments * ASSIGNMENT_WEIGHT)
                + (exams * EXAM_WEIGHT)
                + (behavior * BEHAVIOR_WEIGHT);

        return new StudentPerformanceSummary(
                profile.getStudentName(),
                profile.getStudentEmail(),
                profile.getClassName(),
                attendance,
                assignments,
                exams,
                behavior,
                onTimeRate,
                finalScore,
                classify(finalScore),
                buildSupportMessage(attendance, assignments, exams, behavior, onTimeRate)
        );
    }

    private static String classify(double finalScore) {
        if (finalScore >= 85d) {
            return "Excellent";
        }
        if (finalScore >= 70d) {
            return "Good";
        }
        if (finalScore >= 50d) {
            return "Average";
        }
        return "Needs Improvement";
    }

    private static String buildSupportMessage(
            int attendance,
            int assignments,
            int exams,
            int behavior,
            int onTimeRate
    ) {
        if (attendance < 75) {
            return String.format(Locale.getDefault(), "Needs attendance support. Current attendance is %d%%.", attendance);
        }
        if (onTimeRate < 70) {
            return String.format(Locale.getDefault(), "Needs assignment discipline support. On-time submission rate is %d%%.", onTimeRate);
        }
        if (exams < 60) {
            return String.format(Locale.getDefault(), "Needs academic support before the next exam cycle. Exam average is %d%%.", exams);
        }
        if (behavior < 70) {
            return String.format(Locale.getDefault(), "Needs classroom behavior coaching. Behavior score is %d%%.", behavior);
        }
        if (assignments < 70) {
            return String.format(Locale.getDefault(), "Needs assignment improvement support. Assignment score is %d%%.", assignments);
        }
        return "No immediate academic support flag. Student is performing within expected range.";
    }
}
