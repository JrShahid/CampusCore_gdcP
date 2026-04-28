package com.example.campuscoret;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LiveMonitoringRepository {
    private static final long ONLINE_WINDOW_MILLIS = 10 * 60_000L;
    private static final int MAX_EVENTS = 30;

    private static final List<StudentActivityEvent> RECENT_EVENTS = new ArrayList<>();
    private static final Map<String, StudentLiveStatus> LAST_STATUS_BY_STUDENT = new LinkedHashMap<>();
    private static final Map<String, Set<String>> ACTIVE_EXAM_PARTICIPANTS = new LinkedHashMap<>();

    static {
        seedState();
    }

    private LiveMonitoringRepository() {
    }

    public static void logStudentActivity(String studentId, String studentName, String className, String actionLabel) {
        long now = System.currentTimeMillis();
        StudentActivityEvent event = new StudentActivityEvent(studentId, studentName, className, actionLabel, now);
        RECENT_EVENTS.add(0, event);
        if (RECENT_EVENTS.size() > MAX_EVENTS) {
            RECENT_EVENTS.remove(RECENT_EVENTS.size() - 1);
        }

        StudentLiveStatus status = new StudentLiveStatus(
                studentId,
                studentName,
                className,
                true,
                now,
                actionLabel
        );
        LAST_STATUS_BY_STUDENT.put(studentId, status);
        FirebaseCampusSync.publishStudentActivity(event);
        FirebaseCampusSync.publishStudentStatus(status);
    }

    public static void recordExamStarted(ExamRecord exam, String studentId, String studentName) {
        Set<String> participants = ACTIVE_EXAM_PARTICIPANTS.get(exam.getExamId());
        if (participants == null) {
            participants = new LinkedHashSet<>();
            ACTIVE_EXAM_PARTICIPANTS.put(exam.getExamId(), participants);
        }
        participants.add(studentId);
        FirebaseCampusSync.publishExamParticipant(exam.getExamId(), studentId, studentName, exam.getClassName(), true);
        logStudentActivity(studentId, studentName, exam.getClassName(), "Started exam: " + exam.getTitle());
    }

    public static void recordExamSubmitted(ExamRecord exam, String studentId, String studentName) {
        Set<String> participants = ACTIVE_EXAM_PARTICIPANTS.get(exam.getExamId());
        if (participants != null) {
            participants.remove(studentId);
        }
        FirebaseCampusSync.publishExamParticipant(exam.getExamId(), studentId, studentName, exam.getClassName(), false);
        logStudentActivity(studentId, studentName, exam.getClassName(), "Submitted exam: " + exam.getTitle());
    }

    public static List<StudentActivityEvent> getRecentEvents() {
        return new ArrayList<>(RECENT_EVENTS);
    }

    public static List<StudentLiveStatus> getStudentStatuses() {
        List<StudentLiveStatus> statuses = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (StudentProfile profile : getAllStudents()) {
            StudentLiveStatus lastStatus = LAST_STATUS_BY_STUDENT.get(profile.getStudentEmail());
            if (lastStatus == null) {
                statuses.add(new StudentLiveStatus(
                        profile.getStudentEmail(),
                        profile.getStudentName(),
                        profile.getClassName(),
                        false,
                        now - (ONLINE_WINDOW_MILLIS * 2),
                        "No recent activity"
                ));
            } else {
                statuses.add(new StudentLiveStatus(
                        lastStatus.getStudentId(),
                        lastStatus.getStudentName(),
                        lastStatus.getClassName(),
                        now - lastStatus.getLastActiveMillis() <= ONLINE_WINDOW_MILLIS,
                        lastStatus.getLastActiveMillis(),
                        lastStatus.getLastAction()
                ));
            }
        }
        return statuses;
    }

    public static int getOnlineStudentCount() {
        int count = 0;
        for (StudentLiveStatus status : getStudentStatuses()) {
            if (status.isOnline()) {
                count++;
            }
        }
        return count;
    }

    public static List<LiveSessionSummary> getActiveSessionSummaries() {
        List<LiveSessionSummary> summaries = new ArrayList<>();
        SessionRecord session = SessionRepository.getActiveSession();
        if (session != null && session.isActive()) {
            int totalStudents = StudentDirectoryRepository.getStudentsForClass(session.getClassName()).size();
            int presentCount = AttendanceRepository.getAttendanceForSession(session.getSessionId()).size();
            summaries.add(new LiveSessionSummary(
                    session.getSessionId(),
                    session.getClassName(),
                    session.getSubjectName(),
                    session.getTeacherEmail(),
                    session.getStartedAtMillis(),
                    presentCount,
                    totalStudents
            ));
        }
        return summaries;
    }

    public static List<LiveExamSummary> getLiveExamSummaries() {
        List<LiveExamSummary> summaries = new ArrayList<>();
        for (ExamRecord exam : ExamRepository.getActiveExams(System.currentTimeMillis())) {
            int attemptingCount = getActiveExamParticipantCount(exam.getExamId());
            int submittedCount = ExamRepository.getAttemptsForExam(exam.getExamId()).size();
            int totalStudents = StudentDirectoryRepository.getStudentsForClass(exam.getClassName()).size();
            int pendingCount = Math.max(0, totalStudents - submittedCount);
            summaries.add(new LiveExamSummary(
                    exam.getExamId(),
                    exam.getTitle(),
                    exam.getClassName(),
                    exam.getSubjectName(),
                    attemptingCount,
                    submittedCount,
                    pendingCount
            ));
        }
        return summaries;
    }

    private static int getActiveExamParticipantCount(String examId) {
        Set<String> participants = ACTIVE_EXAM_PARTICIPANTS.get(examId);
        return participants == null ? 0 : participants.size();
    }

    private static List<StudentProfile> getAllStudents() {
        List<StudentProfile> students = new ArrayList<>();
        students.addAll(StudentDirectoryRepository.getStudentsForClass("BCA 1A"));
        students.addAll(StudentDirectoryRepository.getStudentsForClass("BCA 2B"));
        students.addAll(StudentDirectoryRepository.getStudentsForClass("BSc CS 3A"));
        students.addAll(StudentDirectoryRepository.getStudentsForClass("MCA 1C"));
        return students;
    }

    private static void seedState() {
        List<StudentProfile> students = getAllStudents();
        long now = System.currentTimeMillis();
        int index = 0;
        for (StudentProfile profile : students) {
            long lastSeen = now - (index * 90_000L);
            String action = index % 3 == 0
                    ? "Viewed study material"
                    : index % 3 == 1
                    ? "Opened dashboard"
                    : "Checked assignments";
            LAST_STATUS_BY_STUDENT.put(profile.getStudentEmail(), new StudentLiveStatus(
                    profile.getStudentEmail(),
                    profile.getStudentName(),
                    profile.getClassName(),
                    true,
                    lastSeen,
                    action
            ));
            RECENT_EVENTS.add(new StudentActivityEvent(
                    profile.getStudentEmail(),
                    profile.getStudentName(),
                    profile.getClassName(),
                    action,
                    lastSeen
            ));
            index++;
        }
        Collections.sort(RECENT_EVENTS, (left, right) -> Long.compare(right.getOccurredAtMillis(), left.getOccurredAtMillis()));
    }

    static void replaceEventsFromFirebase(List<StudentActivityEvent> events) {
        RECENT_EVENTS.clear();
        if (events == null) {
            return;
        }
        RECENT_EVENTS.addAll(events);
        Collections.sort(RECENT_EVENTS, (left, right) -> Long.compare(right.getOccurredAtMillis(), left.getOccurredAtMillis()));
        while (RECENT_EVENTS.size() > MAX_EVENTS) {
            RECENT_EVENTS.remove(RECENT_EVENTS.size() - 1);
        }
    }

    static void replaceStatusesFromFirebase(Map<String, StudentLiveStatus> statuses) {
        LAST_STATUS_BY_STUDENT.clear();
        if (statuses != null) {
            LAST_STATUS_BY_STUDENT.putAll(statuses);
        }
    }

    static void replaceExamParticipantsFromFirebase(Map<String, Set<String>> participants) {
        ACTIVE_EXAM_PARTICIPANTS.clear();
        if (participants == null) {
            return;
        }
        for (Map.Entry<String, Set<String>> entry : participants.entrySet()) {
            ACTIVE_EXAM_PARTICIPANTS.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
        }
    }

    static List<StudentActivityEvent> exportEvents() {
        return new ArrayList<>(RECENT_EVENTS);
    }

    static Map<String, StudentLiveStatus> exportStatuses() {
        return new LinkedHashMap<>(LAST_STATUS_BY_STUDENT);
    }
}
