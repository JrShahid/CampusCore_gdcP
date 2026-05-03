package com.example.campuscoret;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FirebaseCampusSync {
    private static boolean initialized;
    private static FirebaseFirestore firestore;

    private FirebaseCampusSync() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        initialized = true;

        if (FirebaseApp.getApps(context).isEmpty() && FirebaseApp.initializeApp(context) == null) {
            return;
        }

        firestore = FirebaseFirestore.getInstance();
        firestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build());

        attachUsersListener();
        attachSessionsListener();
        attachAttendanceListener();
        attachAttendanceSummaryListener();
        attachAssignmentsListener();
        attachAssignmentSubmissionsListener();
        attachStudyMaterialsListener();
        attachBehaviorListener();
        attachExamsListener();
        attachExamAttemptsListener();
        attachActivityListener();
        attachStatusListener();
        attachExamParticipantsListener();
        attachClassesListener();
        attachSubjectsListener();
    }

    public static boolean isAvailable() {
        return firestore != null;
    }

    public static void publishClass(String className) {
        if (!isAvailable()) return;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", className);
        firestore.collection("classes").document(sanitizeId(className)).set(data);
    }

    public static void removeClass(String className) {
        if (!isAvailable()) return;
        firestore.collection("classes").document(sanitizeId(className)).delete();
    }

    public static void publishSubject(String subjectName) {
        if (!isAvailable()) return;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", subjectName);
        firestore.collection("subjects").document(sanitizeId(subjectName)).set(data);
    }

    public static void removeSubject(String subjectName) {
        if (!isAvailable()) return;
        firestore.collection("subjects").document(sanitizeId(subjectName)).delete();
    }

    public static void publishSession(SessionRecord session) {
        if (!isAvailable() || session == null) {
            return;
        }
        firestore.collection("sessions")
                .document(session.getSessionId())
                .set(toSessionMap(session));
    }

    public static void publishAttendanceRecord(AttendanceRecord record) {
        if (!isAvailable() || record == null) {
            return;
        }
        firestore.collection("attendance")
                .document(record.getSessionId() + "_" + sanitizeId(record.getStudentEmail()))
                .set(toAttendanceMap(record));
    }

    public static void publishAssignment(Assignment assignment) {
        if (!isAvailable() || assignment == null) {
            return;
        }
        firestore.collection("assignments")
                .document(assignment.getAssignmentId())
                .set(toAssignmentMap(assignment, AssignmentRepository.getMaxMarksForAssignment(assignment.getAssignmentId())));
    }

    public static void publishAssignmentSubmission(AssignmentSubmission submission) {
        if (!isAvailable() || submission == null) {
            return;
        }
        firestore.collection("assignmentSubmissions")
                .document(submission.getSubmissionId())
                .set(toAssignmentSubmissionMap(submission));
    }

    public static void publishStudyMaterial(StudyMaterial material) {
        if (!isAvailable() || material == null) {
            return;
        }
        firestore.collection("studyMaterials")
                .document(buildStudyMaterialId(material))
                .set(toStudyMaterialMap(material));
    }

    public static void publishBehaviorRating(BehaviorRating rating) {
        if (!isAvailable() || rating == null) {
            return;
        }
        firestore.collection("behaviorRatings")
                .document(sanitizeId(rating.getStudentId()))
                .set(toBehaviorMap(rating));
    }

    public static void publishExam(ExamRecord exam) {
        if (!isAvailable() || exam == null) {
            return;
        }
        firestore.collection("exams")
                .document(exam.getExamId())
                .set(toExamMap(exam));
    }

    public static void publishExamAttempt(ExamAttempt attempt) {
        if (!isAvailable() || attempt == null) {
            return;
        }
        firestore.collection("examAttempts")
                .document(attempt.getAttemptId())
                .set(toExamAttemptMap(attempt));
    }

    public static void publishStudentActivity(StudentActivityEvent event) {
        if (!isAvailable() || event == null) {
            return;
        }
        firestore.collection("studentActivity")
                .document(buildActivityId(event))
                .set(toActivityMap(event));
    }

    public static void publishStudentStatus(StudentLiveStatus status) {
        if (!isAvailable() || status == null) {
            return;
        }
        firestore.collection("studentStatuses")
                .document(sanitizeId(status.getStudentId()))
                .set(toStatusMap(status));
    }

    public static void publishExamParticipant(String examId, String studentId, String studentName, String className, boolean active) {
        if (!isAvailable()) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("examId", examId);
        data.put("studentId", studentId);
        data.put("studentName", studentName);
        data.put("className", className);
        data.put("active", active);
        data.put("updatedAtMillis", System.currentTimeMillis());
        firestore.collection("examParticipants")
                .document(examId + "_" + sanitizeId(studentId))
                .set(data);
    }

    private static void attachUsersListener() {
        firestore.collection("users").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            List<StudentProfile> profiles = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                String role = document.getString("role");
                if (!"Student".equals(role)) {
                    continue;
                }
                String name = stringValue(document, "name");
                String email = stringValue(document, "email");
                String className = stringValue(document, "className");
                if (!email.isEmpty()) {
                    profiles.add(new StudentProfile(name.isEmpty() ? email : name, email, className));
                }
            }
            StudentDirectoryRepository.replaceFirebaseStudents(profiles);
        });
    }

    private static void attachSessionsListener() {
        firestore.collection("sessions").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            SessionRecord latest = null;
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                SessionRecord session = toSessionRecord(document);
                if (session == null) {
                    continue;
                }
                if (latest == null || session.getStartedAtMillis() > latest.getStartedAtMillis()) {
                    latest = session;
                }
            }
            SessionRepository.replaceFromFirebase(latest);
        });
    }

    private static void attachAttendanceListener() {
        firestore.collection("attendance").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, List<AttendanceRecord>> bySession = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                AttendanceRecord record = toAttendanceRecord(document);
                if (record == null) {
                    continue;
                }
                List<AttendanceRecord> records = bySession.get(record.getSessionId());
                if (records == null) {
                    records = new ArrayList<>();
                    bySession.put(record.getSessionId(), records);
                }
                records.add(record);
            }
            AttendanceRepository.replaceSessionRecords(bySession);
        });
    }

    private static void attachAttendanceSummaryListener() {
        firestore.collection("attendanceSummaries").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, AttendanceRepository.AttendanceStats> summaries = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                String email = stringValue(document, "studentEmail");
                String className = stringValue(document, "className");
                if (email.isEmpty() || className.isEmpty()) {
                    continue;
                }
                String key = email.toLowerCase() + "|" + className;
                summaries.put(key, new AttendanceRepository.AttendanceStats(
                        intValue(document, "presentClasses"),
                        intValue(document, "totalClasses")
                ));
            }
            AttendanceRepository.replaceSummaryRecords(summaries);
        });
    }

    private static void attachAssignmentsListener() {
        firestore.collection("assignments").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            List<Assignment> assignments = new ArrayList<>();
            Map<String, Integer> maxMarks = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                Assignment assignment = toAssignment(document);
                if (assignment == null) {
                    continue;
                }
                assignments.add(assignment);
                maxMarks.put(assignment.getAssignmentId(), intValue(document, "maxMarks", 10));
            }
            AssignmentRepository.replaceAssignmentsFromFirebase(assignments, maxMarks);
        });
    }

    private static void attachAssignmentSubmissionsListener() {
        firestore.collection("assignmentSubmissions").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, List<AssignmentSubmission>> submissions = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                AssignmentSubmission submission = toAssignmentSubmission(document);
                if (submission == null) {
                    continue;
                }
                List<AssignmentSubmission> rows = submissions.get(submission.getAssignmentId());
                if (rows == null) {
                    rows = new ArrayList<>();
                    submissions.put(submission.getAssignmentId(), rows);
                }
                rows.add(submission);
            }
            AssignmentRepository.replaceSubmissionsFromFirebase(submissions);
        });
    }

    private static void attachStudyMaterialsListener() {
        firestore.collection("studyMaterials").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            List<StudyMaterial> materials = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                StudyMaterial material = toStudyMaterial(document);
                if (material != null) {
                    materials.add(material);
                }
            }
            StudyMaterialRepository.replaceMaterialsFromFirebase(materials);
        });
    }

    private static void attachBehaviorListener() {
        firestore.collection("behaviorRatings").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, BehaviorRating> ratings = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                BehaviorRating rating = toBehaviorRating(document);
                if (rating != null) {
                    ratings.put(rating.getStudentId(), rating);
                }
            }
            BehaviorRepository.replaceRatingsFromFirebase(ratings);
        });
    }

    private static void attachExamsListener() {
        firestore.collection("exams").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            List<ExamRecord> exams = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                ExamRecord exam = toExamRecord(document);
                if (exam != null) {
                    exams.add(exam);
                }
            }
            ExamRepository.replaceExamsFromFirebase(exams);
        });
    }

    private static void attachExamAttemptsListener() {
        firestore.collection("examAttempts").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, List<ExamAttempt>> attempts = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                ExamAttempt attempt = toExamAttempt(document);
                if (attempt == null) {
                    continue;
                }
                List<ExamAttempt> rows = attempts.get(attempt.getExamId());
                if (rows == null) {
                    rows = new ArrayList<>();
                    attempts.put(attempt.getExamId(), rows);
                }
                rows.add(attempt);
            }
            ExamRepository.replaceAttemptsFromFirebase(attempts);
        });
    }

    private static void attachActivityListener() {
        firestore.collection("studentActivity").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            List<StudentActivityEvent> events = new ArrayList<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                StudentActivityEvent event = toActivityEvent(document);
                if (event != null) {
                    events.add(event);
                }
            }
            LiveMonitoringRepository.replaceEventsFromFirebase(events);
        });
    }

    private static void attachStatusListener() {
        firestore.collection("studentStatuses").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, StudentLiveStatus> statuses = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                StudentLiveStatus status = toStudentStatus(document);
                if (status != null) {
                    statuses.put(status.getStudentId(), status);
                }
            }
            LiveMonitoringRepository.replaceStatusesFromFirebase(statuses);
        });
    }

    private static void attachExamParticipantsListener() {
        firestore.collection("examParticipants").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) {
                return;
            }
            Map<String, Set<String>> participants = new LinkedHashMap<>();
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                boolean active = booleanValue(document, "active");
                if (!active) {
                    continue;
                }
                String examId = stringValue(document, "examId");
                String studentId = stringValue(document, "studentId");
                if (examId.isEmpty() || studentId.isEmpty()) {
                    continue;
                }
                Set<String> set = participants.get(examId);
                if (set == null) {
                    set = new LinkedHashSet<>();
                    participants.put(examId, set);
                }
                set.add(studentId);
            }
            LiveMonitoringRepository.replaceExamParticipantsFromFirebase(participants);
        });
    }

    private static void attachClassesListener() {
        firestore.collection("classes").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) return;
            List<String> classes = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String name = doc.getString("name");
                if (name != null) classes.add(name);
            }
            MetadataRepository.replaceClassesFromFirebase(classes);
        });
    }

    private static void attachSubjectsListener() {
        firestore.collection("subjects").addSnapshotListener((snapshot, error) -> {
            if (snapshot == null || error != null) return;
            List<String> subjects = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String name = doc.getString("name");
                if (name != null) subjects.add(name);
            }
            MetadataRepository.replaceSubjectsFromFirebase(subjects);
        });
    }

    private static Map<String, Object> toSessionMap(SessionRecord session) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", session.getSessionId());
        data.put("teacherEmail", session.getTeacherEmail());
        data.put("subjectName", session.getSubjectName());
        data.put("className", session.getClassName());
        data.put("startedAtMillis", session.getStartedAtMillis());
        data.put("attendanceActive", session.isAttendanceActive());
        data.put("active", session.isActive());
        return data;
    }

    private static Map<String, Object> toAttendanceMap(AttendanceRecord record) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", record.getSessionId());
        data.put("studentName", record.getStudentName());
        data.put("studentEmail", record.getStudentEmail());
        data.put("markedAtMillis", record.getMarkedAtMillis());
        data.put("status", record.getStatus());
        return data;
    }

    private static Map<String, Object> toAssignmentMap(Assignment assignment, int maxMarks) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assignmentId", assignment.getAssignmentId());
        data.put("title", assignment.getTitle());
        data.put("description", assignment.getDescription());
        data.put("subjectName", assignment.getSubjectName());
        data.put("className", assignment.getClassName());
        data.put("teacherId", assignment.getTeacherId());
        data.put("deadlineMillis", assignment.getDeadlineMillis());
        data.put("createdAtMillis", assignment.getCreatedAtMillis());
        data.put("maxMarks", maxMarks);
        return data;
    }

    private static Map<String, Object> toAssignmentSubmissionMap(AssignmentSubmission submission) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("submissionId", submission.getSubmissionId());
        data.put("assignmentId", submission.getAssignmentId());
        data.put("studentId", submission.getStudentId());
        data.put("studentName", submission.getStudentName());
        data.put("solutionText", submission.getSolutionText());
        data.put("fileUrl", submission.getFileUrl());
        data.put("submittedAtMillis", submission.getSubmittedAtMillis());
        data.put("marks", submission.getMarks());
        data.put("feedback", submission.getFeedback());
        return data;
    }

    private static Map<String, Object> toStudyMaterialMap(StudyMaterial material) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", material.getTitle());
        data.put("subjectName", material.getSubjectName());
        data.put("className", material.getClassName());
        data.put("fileType", material.getFileType());
        data.put("fileName", material.getFileName());
        data.put("fileUrl", material.getFileUrl());
        data.put("uploadedBy", material.getUploadedBy());
        data.put("uploadedAtMillis", material.getUploadedAtMillis());
        return data;
    }

    private static Map<String, Object> toBehaviorMap(BehaviorRating rating) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", rating.getStudentId());
        data.put("studentName", rating.getStudentName());
        data.put("discipline", rating.getDiscipline());
        data.put("participation", rating.getParticipation());
        data.put("punctuality", rating.getPunctuality());
        data.put("respectfulness", rating.getRespectfulness());
        data.put("teacherEmail", rating.getTeacherEmail());
        data.put("updatedAtMillis", rating.getUpdatedAtMillis());
        return data;
    }

    private static Map<String, Object> toExamMap(ExamRecord exam) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("examId", exam.getExamId());
        data.put("title", exam.getTitle());
        data.put("subjectName", exam.getSubjectName());
        data.put("className", exam.getClassName());
        data.put("durationMinutes", exam.getDurationMinutes());
        data.put("totalMarks", exam.getTotalMarks());
        data.put("startTimeMillis", exam.getStartTimeMillis());
        data.put("endTimeMillis", exam.getEndTimeMillis());
        data.put("teacherEmail", exam.getTeacherEmail());
        List<Map<String, Object>> questions = new ArrayList<>();
        for (ExamQuestion question : exam.getQuestions()) {
            Map<String, Object> questionData = new LinkedHashMap<>();
            questionData.put("questionId", question.getQuestionId());
            questionData.put("questionText", question.getQuestionText());
            questionData.put("options", new ArrayList<>(question.getOptions()));
            questionData.put("correctAnswerIndex", question.getCorrectAnswerIndex());
            questions.add(questionData);
        }
        data.put("questions", questions);
        return data;
    }

    private static Map<String, Object> toExamAttemptMap(ExamAttempt attempt) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("attemptId", attempt.getAttemptId());
        data.put("examId", attempt.getExamId());
        data.put("studentId", attempt.getStudentId());
        data.put("studentName", attempt.getStudentName());
        data.put("selectedAnswers", new LinkedHashMap<>(attempt.getSelectedAnswers()));
        data.put("submittedAtMillis", attempt.getSubmittedAtMillis());
        data.put("score", attempt.getScore());
        data.put("percentage", attempt.getPercentage());
        data.put("feedback", attempt.getFeedback());
        return data;
    }

    private static Map<String, Object> toActivityMap(StudentActivityEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", event.getStudentId());
        data.put("studentName", event.getStudentName());
        data.put("className", event.getClassName());
        data.put("actionLabel", event.getActionLabel());
        data.put("occurredAtMillis", event.getOccurredAtMillis());
        return data;
    }

    private static Map<String, Object> toStatusMap(StudentLiveStatus status) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentId", status.getStudentId());
        data.put("studentName", status.getStudentName());
        data.put("className", status.getClassName());
        data.put("online", status.isOnline());
        data.put("lastActiveMillis", status.getLastActiveMillis());
        data.put("lastAction", status.getLastAction());
        return data;
    }

    private static SessionRecord toSessionRecord(DocumentSnapshot document) {
        String sessionId = stringValue(document, "sessionId");
        if (sessionId.isEmpty()) {
            return null;
        }
        return new SessionRecord(
                sessionId,
                stringValue(document, "teacherEmail"),
                stringValue(document, "subjectName"),
                stringValue(document, "className"),
                longValue(document, "startedAtMillis"),
                booleanValue(document, "attendanceActive"),
                booleanValue(document, "active")
        );
    }

    private static AttendanceRecord toAttendanceRecord(DocumentSnapshot document) {
        String sessionId = stringValue(document, "sessionId");
        String studentEmail = stringValue(document, "studentEmail");
        if (sessionId.isEmpty() || studentEmail.isEmpty()) {
            return null;
        }
        return new AttendanceRecord(
                sessionId,
                stringValue(document, "studentName"),
                studentEmail,
                longValue(document, "markedAtMillis"),
                stringValue(document, "status")
        );
    }

    private static Assignment toAssignment(DocumentSnapshot document) {
        String assignmentId = stringValue(document, "assignmentId");
        if (assignmentId.isEmpty()) {
            return null;
        }
        return new Assignment(
                assignmentId,
                stringValue(document, "title"),
                stringValue(document, "description"),
                stringValue(document, "subjectName"),
                stringValue(document, "className"),
                stringValue(document, "teacherId"),
                longValue(document, "deadlineMillis"),
                longValue(document, "createdAtMillis")
        );
    }

    private static AssignmentSubmission toAssignmentSubmission(DocumentSnapshot document) {
        String submissionId = stringValue(document, "submissionId");
        String assignmentId = stringValue(document, "assignmentId");
        if (submissionId.isEmpty() || assignmentId.isEmpty()) {
            return null;
        }
        Long marks = document.getLong("marks");
        return new AssignmentSubmission(
                submissionId,
                assignmentId,
                stringValue(document, "studentId"),
                stringValue(document, "studentName"),
                stringValue(document, "solutionText"),
                stringValue(document, "fileUrl"),
                longValue(document, "submittedAtMillis"),
                marks == null ? null : marks.intValue(),
                stringValue(document, "feedback")
        );
    }

    private static StudyMaterial toStudyMaterial(DocumentSnapshot document) {
        String title = stringValue(document, "title");
        if (title.isEmpty()) {
            return null;
        }
        return new StudyMaterial(
                title,
                stringValue(document, "subjectName"),
                stringValue(document, "className"),
                stringValue(document, "fileType"),
                stringValue(document, "fileName"),
                stringValue(document, "fileUrl"),
                stringValue(document, "uploadedBy"),
                longValue(document, "uploadedAtMillis")
        );
    }

    private static BehaviorRating toBehaviorRating(DocumentSnapshot document) {
        String studentId = stringValue(document, "studentId");
        if (studentId.isEmpty()) {
            return null;
        }
        return new BehaviorRating(
                studentId,
                stringValue(document, "studentName"),
                intValue(document, "discipline"),
                intValue(document, "participation"),
                intValue(document, "punctuality"),
                intValue(document, "respectfulness"),
                stringValue(document, "teacherEmail"),
                longValue(document, "updatedAtMillis")
        );
    }

    @SuppressWarnings("unchecked")
    private static ExamRecord toExamRecord(DocumentSnapshot document) {
        String examId = stringValue(document, "examId");
        if (examId.isEmpty()) {
            return null;
        }
        List<Map<String, Object>> rawQuestions = (List<Map<String, Object>>) document.get("questions");
        List<ExamQuestion> questions = new ArrayList<>();
        if (rawQuestions != null) {
            for (Map<String, Object> rawQuestion : rawQuestions) {
                if (rawQuestion == null) {
                    continue;
                }
                Object optionsValue = rawQuestion.get("options");
                List<String> options = new ArrayList<>();
                if (optionsValue instanceof List) {
                    List<?> rawOptions = (List<?>) optionsValue;
                    for (Object option : rawOptions) {
                        options.add(option == null ? "" : String.valueOf(option));
                    }
                }
                Object correctAnswerIndex = rawQuestion.get("correctAnswerIndex");
                int answerIndex = correctAnswerIndex instanceof Number ? ((Number) correctAnswerIndex).intValue() : 0;
                questions.add(new ExamQuestion(
                        stringValue(rawQuestion, "questionId"),
                        stringValue(rawQuestion, "questionText"),
                        options,
                        answerIndex
                ));
            }
        }
        return new ExamRecord(
                examId,
                stringValue(document, "title"),
                stringValue(document, "subjectName"),
                stringValue(document, "className"),
                intValue(document, "durationMinutes"),
                intValue(document, "totalMarks"),
                longValue(document, "startTimeMillis"),
                longValue(document, "endTimeMillis"),
                stringValue(document, "teacherEmail"),
                questions
        );
    }

    @SuppressWarnings("unchecked")
    private static ExamAttempt toExamAttempt(DocumentSnapshot document) {
        String attemptId = stringValue(document, "attemptId");
        String examId = stringValue(document, "examId");
        if (attemptId.isEmpty() || examId.isEmpty()) {
            return null;
        }
        Map<String, Integer> answers = new LinkedHashMap<>();
        Object selectedAnswers = document.get("selectedAnswers");
        if (selectedAnswers instanceof Map) {
            Map<?, ?> rawAnswers = (Map<?, ?>) selectedAnswers;
            for (Map.Entry<?, ?> entry : rawAnswers.entrySet()) {
                Object value = entry.getValue();
                if (entry.getKey() != null && value instanceof Number) {
                    answers.put(String.valueOf(entry.getKey()), ((Number) value).intValue());
                }
            }
        }
        return new ExamAttempt(
                attemptId,
                examId,
                stringValue(document, "studentId"),
                stringValue(document, "studentName"),
                answers,
                longValue(document, "submittedAtMillis"),
                intValue(document, "score"),
                intValue(document, "percentage"),
                stringValue(document, "feedback")
        );
    }

    private static StudentActivityEvent toActivityEvent(DocumentSnapshot document) {
        String studentId = stringValue(document, "studentId");
        if (studentId.isEmpty()) {
            return null;
        }
        return new StudentActivityEvent(
                studentId,
                stringValue(document, "studentName"),
                stringValue(document, "className"),
                stringValue(document, "actionLabel"),
                longValue(document, "occurredAtMillis")
        );
    }

    private static StudentLiveStatus toStudentStatus(DocumentSnapshot document) {
        String studentId = stringValue(document, "studentId");
        if (studentId.isEmpty()) {
            return null;
        }
        return new StudentLiveStatus(
                studentId,
                stringValue(document, "studentName"),
                stringValue(document, "className"),
                booleanValue(document, "online"),
                longValue(document, "lastActiveMillis"),
                stringValue(document, "lastAction")
        );
    }

    private static String buildStudyMaterialId(StudyMaterial material) {
        return sanitizeId(material.getClassName() + "_" + material.getSubjectName() + "_" + material.getFileName() + "_" + material.getUploadedAtMillis());
    }

    private static String buildActivityId(StudentActivityEvent event) {
        return sanitizeId(event.getStudentId() + "_" + event.getOccurredAtMillis() + "_" + event.getActionLabel());
    }

    private static String sanitizeId(String raw) {
        return raw == null ? "" : raw.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static String stringValue(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

    private static String stringValue(Map<String, Object> map, String field) {
        Object value = map.get(field);
        return value == null ? "" : String.valueOf(value);
    }

    private static long longValue(DocumentSnapshot document, String field) {
        Long value = document.getLong(field);
        return value == null ? 0L : value;
    }

    private static boolean booleanValue(DocumentSnapshot document, String field) {
        Boolean value = document.getBoolean(field);
        return value != null && value;
    }

    private static int intValue(DocumentSnapshot document, String field) {
        return intValue(document, field, 0);
    }

    private static int intValue(DocumentSnapshot document, String field, int defaultValue) {
        Long value = document.getLong(field);
        return value == null ? defaultValue : value.intValue();
    }
}
