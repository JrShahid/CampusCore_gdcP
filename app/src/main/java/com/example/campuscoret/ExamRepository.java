package com.example.campuscoret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExamRepository {
    private static final List<ExamRecord> EXAMS = new ArrayList<>();
    private static final Map<String, List<ExamAttempt>> ATTEMPTS_BY_EXAM = new LinkedHashMap<>();

    private ExamRepository() {
    }

    public static ExamRecord createExam(
            String title,
            String subjectName,
            String className,
            int durationMinutes,
            int totalMarks,
            long startTimeMillis,
            long endTimeMillis,
            String teacherEmail,
            List<ExamQuestion> questions
    ) {
        ExamRecord exam = new ExamRecord(
                "EXM-" + System.currentTimeMillis(),
                title,
                subjectName,
                className,
                durationMinutes,
                totalMarks,
                startTimeMillis,
                endTimeMillis,
                teacherEmail,
                questions
        );
        EXAMS.add(0, exam);
        FirebaseCampusSync.publishExam(exam);
        return exam;
    }

    public static List<ExamRecord> getExamsForTeacher(String teacherEmail) {
        List<ExamRecord> results = new ArrayList<>();
        for (ExamRecord exam : EXAMS) {
            if (exam.getTeacherEmail().equalsIgnoreCase(teacherEmail)) {
                results.add(exam);
            }
        }
        return results;
    }

    public static List<ExamRecord> getActiveExamsForClass(String className, long nowMillis) {
        List<ExamRecord> results = new ArrayList<>();
        for (ExamRecord exam : EXAMS) {
            if (exam.getClassName().equals(className) && isExamActive(exam, nowMillis)) {
                results.add(exam);
            }
        }
        return results;
    }

    public static List<ExamRecord> getActiveExams(long nowMillis) {
        List<ExamRecord> results = new ArrayList<>();
        for (ExamRecord exam : EXAMS) {
            if (isExamActive(exam, nowMillis)) {
                results.add(exam);
            }
        }
        return results;
    }

    public static List<ExamRecord> getExamsForClass(String className) {
        List<ExamRecord> results = new ArrayList<>();
        for (ExamRecord exam : EXAMS) {
            if (exam.getClassName().equals(className)) {
                results.add(exam);
            }
        }
        return results;
    }

    public static ExamRecord findExamById(String examId) {
        for (ExamRecord exam : EXAMS) {
            if (exam.getExamId().equals(examId)) {
                return exam;
            }
        }
        return null;
    }

    public static boolean isExamActive(ExamRecord exam, long nowMillis) {
        return nowMillis >= exam.getStartTimeMillis() && nowMillis <= exam.getEndTimeMillis();
    }

    public static boolean isExamLocked(ExamRecord exam, long nowMillis) {
        return nowMillis > exam.getEndTimeMillis();
    }

    public static boolean isExamUpcoming(ExamRecord exam, long nowMillis) {
        return nowMillis < exam.getStartTimeMillis();
    }

    public static long getRemainingTimeMillis(ExamRecord exam, long nowMillis) {
        long durationWindow = exam.getDurationMinutes() * 60_000L;
        long remainingUntilEnd = exam.getEndTimeMillis() - nowMillis;
        return Math.max(0L, Math.min(durationWindow, remainingUntilEnd));
    }

    public static SubmissionResult submitAttempt(
            String examId,
            String studentId,
            String studentName,
            Map<String, Integer> selectedAnswers,
            long submittedAtMillis
    ) {
        ExamRecord exam = findExamById(examId);
        if (exam == null) {
            return SubmissionResult.notFound();
        }

        if (!isExamActive(exam, submittedAtMillis)) {
            return SubmissionResult.notActive();
        }

        ExamAttempt existingAttempt = getAttemptForStudent(examId, studentId);
        if (existingAttempt != null) {
            return SubmissionResult.duplicate(existingAttempt);
        }

        int correctCount = 0;
        List<ExamQuestion> questions = exam.getQuestions();
        for (ExamQuestion question : questions) {
            Integer selectedAnswer = selectedAnswers.get(question.getQuestionId());
            if (selectedAnswer != null && selectedAnswer == question.getCorrectAnswerIndex()) {
                correctCount++;
            }
        }

        int totalQuestions = Math.max(1, questions.size());
        int score = (int) Math.round((double) correctCount * exam.getTotalMarks() / totalQuestions);
        int percentage = (int) Math.round((score * 100.0d) / Math.max(1, exam.getTotalMarks()));
        ExamAttempt attempt = new ExamAttempt(
                "ATT-" + System.currentTimeMillis(),
                examId,
                studentId,
                studentName,
                selectedAnswers,
                submittedAtMillis,
                score,
                percentage,
                buildFeedback(percentage)
        );

        List<ExamAttempt> attempts = ATTEMPTS_BY_EXAM.get(examId);
        if (attempts == null) {
            attempts = new ArrayList<>();
            ATTEMPTS_BY_EXAM.put(examId, attempts);
        }
        attempts.add(attempt);
        FirebaseCampusSync.publishExamAttempt(attempt);
        return SubmissionResult.success(attempt);
    }

    public static ExamAttempt getAttemptForStudent(String examId, String studentId) {
        List<ExamAttempt> attempts = ATTEMPTS_BY_EXAM.get(examId);
        if (attempts == null) {
            return null;
        }

        for (ExamAttempt attempt : attempts) {
            if (attempt.getStudentId().equalsIgnoreCase(studentId)) {
                return attempt;
            }
        }
        return null;
    }

    public static List<ExamAttempt> getAttemptsForExam(String examId) {
        List<ExamAttempt> attempts = ATTEMPTS_BY_EXAM.get(examId);
        if (attempts == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(attempts);
    }

    public static int getAttemptCount(String examId) {
        return getAttemptsForExam(examId).size();
    }

    public static int getAveragePercentage(String examId) {
        List<ExamAttempt> attempts = getAttemptsForExam(examId);
        if (attempts.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (ExamAttempt attempt : attempts) {
            total += attempt.getPercentage();
        }
        return Math.round((float) total / attempts.size());
    }

    public static int getHighestScore(String examId) {
        int highest = 0;
        for (ExamAttempt attempt : getAttemptsForExam(examId)) {
            highest = Math.max(highest, attempt.getScore());
        }
        return highest;
    }

    public static int getAveragePercentageForStudent(String studentId, String className) {
        int totalPercentage = 0;
        int attemptCount = 0;

        for (ExamRecord exam : EXAMS) {
            if (!exam.getClassName().equals(className)) {
                continue;
            }

            ExamAttempt attempt = getAttemptForStudent(exam.getExamId(), studentId);
            if (attempt == null) {
                continue;
            }

            totalPercentage += attempt.getPercentage();
            attemptCount++;
        }

        if (attemptCount == 0) {
            return 0;
        }
        return Math.round((float) totalPercentage / attemptCount);
    }

    private static String buildFeedback(int percentage) {
        if (percentage >= 85) {
            return "Excellent accuracy across the exam.";
        }
        if (percentage >= 70) {
            return "Strong performance with a few areas to revisit.";
        }
        if (percentage >= 50) {
            return "Fair attempt. Review the incorrect concepts before the next test.";
        }
        return "Needs improvement. Revisit the core topics and retry practice questions.";
    }

    static void replaceExamsFromFirebase(List<ExamRecord> exams) {
        EXAMS.clear();
        if (exams != null) {
            EXAMS.addAll(exams);
        }
    }

    static void replaceAttemptsFromFirebase(Map<String, List<ExamAttempt>> attempts) {
        ATTEMPTS_BY_EXAM.clear();
        if (attempts == null) {
            return;
        }
        for (Map.Entry<String, List<ExamAttempt>> entry : attempts.entrySet()) {
            ATTEMPTS_BY_EXAM.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }

    public static final class SubmissionResult {
        public enum Status {
            SUCCESS,
            DUPLICATE,
            NOT_ACTIVE,
            NOT_FOUND
        }

        private final Status status;
        private final ExamAttempt attempt;

        private SubmissionResult(Status status, ExamAttempt attempt) {
            this.status = status;
            this.attempt = attempt;
        }

        public static SubmissionResult success(ExamAttempt attempt) {
            return new SubmissionResult(Status.SUCCESS, attempt);
        }

        public static SubmissionResult duplicate(ExamAttempt attempt) {
            return new SubmissionResult(Status.DUPLICATE, attempt);
        }

        public static SubmissionResult notActive() {
            return new SubmissionResult(Status.NOT_ACTIVE, null);
        }

        public static SubmissionResult notFound() {
            return new SubmissionResult(Status.NOT_FOUND, null);
        }

        public Status getStatus() {
            return status;
        }

        public ExamAttempt getAttempt() {
            return attempt;
        }
    }
}
