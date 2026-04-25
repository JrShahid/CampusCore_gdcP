package com.example.campuscoret;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExamRepository {
    private static final List<ExamRecord> EXAMS = buildSeedExams();
    private static final Map<String, List<ExamAttempt>> ATTEMPTS_BY_EXAM = buildSeedAttempts();

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

    private static List<ExamRecord> buildSeedExams() {
        long now = System.currentTimeMillis();
        List<ExamRecord> exams = new ArrayList<>();

        List<ExamQuestion> activeQuestions = new ArrayList<>();
        activeQuestions.add(new ExamQuestion(
                "Q-CS-1",
                "Which data structure works on the FIFO principle?",
                buildOptions("Stack", "Queue", "Tree", "Graph"),
                1
        ));
        activeQuestions.add(new ExamQuestion(
                "Q-CS-2",
                "Which keyword is used to inherit a class in Java?",
                buildOptions("this", "implement", "extends", "import"),
                2
        ));
        activeQuestions.add(new ExamQuestion(
                "Q-CS-3",
                "Which memory area stores local variables in Java methods?",
                buildOptions("Heap", "Stack", "Metaspace", "Cache"),
                1
        ));

        exams.add(new ExamRecord(
                "EXM-DEMO-1",
                "Computer Science Quiz 1",
                "Computer Science",
                "BCA 2B",
                20,
                30,
                now - 300_000L,
                now + 3_300_000L,
                "teacher@campus.edu",
                activeQuestions
        ));

        List<ExamQuestion> futureQuestions = new ArrayList<>();
        futureQuestions.add(new ExamQuestion(
                "Q-MTH-1",
                "The derivative of x^2 is:",
                buildOptions("x", "2x", "x^3", "2"),
                1
        ));
        futureQuestions.add(new ExamQuestion(
                "Q-MTH-2",
                "sin(90°) equals:",
                buildOptions("0", "1", "-1", "0.5"),
                1
        ));

        exams.add(new ExamRecord(
                "EXM-DEMO-2",
                "Mathematics Practice Test",
                "Mathematics",
                "BCA 1A",
                15,
                20,
                now + 3_600_000L,
                now + 4_500_000L,
                "teacher@campus.edu",
                futureQuestions
        ));

        List<ExamQuestion> bscQuestions = new ArrayList<>();
        bscQuestions.add(new ExamQuestion(
                "Q-BSC-1",
                "Which layer handles routing in the OSI model?",
                buildOptions("Transport", "Network", "Session", "Physical"),
                1
        ));
        bscQuestions.add(new ExamQuestion(
                "Q-BSC-2",
                "A process in waiting state is:",
                buildOptions("Running", "Blocked", "Executing", "Terminated"),
                1
        ));
        exams.add(new ExamRecord(
                "EXM-DEMO-3",
                "Systems Test",
                "Computer Science",
                "BSc CS 3A",
                25,
                40,
                now - 5_400_000L,
                now - 3_600_000L,
                "teacher@campus.edu",
                bscQuestions
        ));

        List<ExamQuestion> mcaQuestions = new ArrayList<>();
        mcaQuestions.add(new ExamQuestion(
                "Q-MCA-1",
                "The SI unit of frequency is:",
                buildOptions("Joule", "Pascal", "Hertz", "Newton"),
                2
        ));
        mcaQuestions.add(new ExamQuestion(
                "Q-MCA-2",
                "Light bends when moving between media due to:",
                buildOptions("Reflection", "Refraction", "Dispersion", "Diffraction"),
                1
        ));
        exams.add(new ExamRecord(
                "EXM-DEMO-4",
                "Physics Mid Test",
                "Physics",
                "MCA 1C",
                20,
                30,
                now - 7_200_000L,
                now - 5_400_000L,
                "teacher@campus.edu",
                mcaQuestions
        ));
        return exams;
    }

    private static Map<String, List<ExamAttempt>> buildSeedAttempts() {
        Map<String, List<ExamAttempt>> attempts = new LinkedHashMap<>();
        List<ExamAttempt> demoAttempts = new ArrayList<>();

        Map<String, Integer> answers = new LinkedHashMap<>();
        answers.put("Q-CS-1", 1);
        answers.put("Q-CS-2", 2);
        answers.put("Q-CS-3", 0);

        demoAttempts.add(new ExamAttempt(
                "ATT-DEMO-1",
                "EXM-DEMO-1",
                "aarav@campus.edu",
                "Aarav",
                answers,
                System.currentTimeMillis() - 120_000L,
                20,
                67,
                "Good attempt. Review memory management questions."
        ));
        demoAttempts.add(new ExamAttempt(
                "ATT-DEMO-2",
                "EXM-DEMO-1",
                "diya@campus.edu",
                "Diya Patel",
                answers,
                System.currentTimeMillis() - 90_000L,
                24,
                80,
                "Strong performance and steady accuracy."
        ));
        demoAttempts.add(new ExamAttempt(
                "ATT-DEMO-3",
                "EXM-DEMO-1",
                "rohan@campus.edu",
                "Rohan Verma",
                answers,
                System.currentTimeMillis() - 60_000L,
                15,
                50,
                "Needs practice on core concepts before the next test."
        ));
        attempts.put("EXM-DEMO-1", demoAttempts);

        List<ExamAttempt> mathAttempts = new ArrayList<>();
        Map<String, Integer> mathAnswers = new LinkedHashMap<>();
        mathAnswers.put("Q-MTH-1", 1);
        mathAnswers.put("Q-MTH-2", 1);
        mathAttempts.add(new ExamAttempt(
                "ATT-DEMO-4",
                "EXM-DEMO-2",
                "aarav@campus.edu",
                "Aarav Sharma",
                mathAnswers,
                System.currentTimeMillis() - 30_000L,
                18,
                90,
                "Excellent performance."
        ));
        mathAttempts.add(new ExamAttempt(
                "ATT-DEMO-5",
                "EXM-DEMO-2",
                "sana@campus.edu",
                "Sana Khan",
                mathAnswers,
                System.currentTimeMillis() - 20_000L,
                12,
                60,
                "Fair attempt. Revisit the basics."
        ));
        attempts.put("EXM-DEMO-2", mathAttempts);

        List<ExamAttempt> bscAttempts = new ArrayList<>();
        Map<String, Integer> bscAnswers = new LinkedHashMap<>();
        bscAnswers.put("Q-BSC-1", 1);
        bscAnswers.put("Q-BSC-2", 1);
        bscAttempts.add(new ExamAttempt(
                "ATT-DEMO-6",
                "EXM-DEMO-3",
                "kabir@campus.edu",
                "Kabir Singh",
                bscAnswers,
                System.currentTimeMillis() - 3_500_000L,
                32,
                80,
                "Good system-level understanding."
        ));
        attempts.put("EXM-DEMO-3", bscAttempts);

        List<ExamAttempt> mcaAttempts = new ArrayList<>();
        Map<String, Integer> mcaAnswers = new LinkedHashMap<>();
        mcaAnswers.put("Q-MCA-1", 2);
        mcaAnswers.put("Q-MCA-2", 1);
        mcaAttempts.add(new ExamAttempt(
                "ATT-DEMO-7",
                "EXM-DEMO-4",
                "meera@campus.edu",
                "Meera Nair",
                mcaAnswers,
                System.currentTimeMillis() - 5_300_000L,
                27,
                90,
                "Excellent conceptual clarity."
        ));
        attempts.put("EXM-DEMO-4", mcaAttempts);
        return attempts;
    }

    private static List<String> buildOptions(String option1, String option2, String option3, String option4) {
        List<String> options = new ArrayList<>();
        options.add(option1);
        options.add(option2);
        options.add(option3);
        options.add(option4);
        return options;
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

    static List<ExamRecord> exportExams() {
        return new ArrayList<>(EXAMS);
    }

    static List<ExamAttempt> exportAttempts() {
        List<ExamAttempt> all = new ArrayList<>();
        for (List<ExamAttempt> attempts : ATTEMPTS_BY_EXAM.values()) {
            all.addAll(attempts);
        }
        return all;
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
