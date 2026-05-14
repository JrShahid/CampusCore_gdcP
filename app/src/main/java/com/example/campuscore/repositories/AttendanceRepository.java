package com.example.campuscore.repositories;

import com.example.campuscore.firebase.FirebaseUserRepository;
import com.example.campuscore.firebase.FirestoreCallback;
import com.example.campuscore.models.AttendanceModel;
import com.example.campuscore.models.StudentAttendanceItem;
import com.example.campuscore.models.UserModel;
import com.example.campuscore.utils.AttendanceConstants;
import com.example.campuscore.utils.FirestoreCollections;
import com.example.campuscore.utils.FirestoreFields;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceRepository {
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public AttendanceRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void fetchStudentsForAttendance(String semester, String department, FirestoreCallback<List<UserModel>> callback) {
        firestore.collection(FirestoreCollections.USERS)
                .whereEqualTo(FirestoreFields.ROLE, FirebaseUserRepository.ROLE_STUDENT)
                .whereEqualTo(FirestoreFields.SEMESTER, semester)
                .whereEqualTo(FirestoreFields.DEPARTMENT, department)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> callback.onSuccess(parseUsers(queryDocumentSnapshots)))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void saveAttendance(String subject, String semester, String department, List<StudentAttendanceItem> students,
                               FirestoreCallback<Void> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Session expired. Please login again.");
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        WriteBatch batch = firestore.batch();
        for (StudentAttendanceItem item : students) {
            UserModel student = item.getUser();
            String attendanceId = buildAttendanceId(student.getUid(), subject, today);
            Map<String, Object> record = new HashMap<>();
            record.put(FirestoreFields.ATTENDANCE_ID, attendanceId);
            record.put(FirestoreFields.STUDENT_UID, student.getUid());
            record.put(FirestoreFields.STUDENT_NAME, student.getName());
            record.put(FirestoreFields.ROLL_NUMBER, student.getRollNumber());
            record.put(FirestoreFields.TEACHER_UID, currentUser.getUid());
            record.put(FirestoreFields.SUBJECT, subject);
            record.put(FirestoreFields.SEMESTER, semester);
            record.put(FirestoreFields.DEPARTMENT, department);
            record.put(FirestoreFields.DATE, today);
            record.put(FirestoreFields.STATUS, item.isPresent()
                    ? AttendanceConstants.STATUS_PRESENT
                    : AttendanceConstants.STATUS_ABSENT);
            record.put(FirestoreFields.TIMESTAMP, FieldValue.serverTimestamp());
            batch.set(firestore.collection(FirestoreCollections.ATTENDANCE_RECORDS).document(attendanceId), record);
        }

        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void fetchTeacherAttendanceHistory(FirestoreCallback<List<AttendanceModel>> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Session expired. Please login again.");
            return;
        }

        firestore.collection(FirestoreCollections.ATTENDANCE_RECORDS)
                .whereEqualTo(FirestoreFields.TEACHER_UID, currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> callback.onSuccess(parseAttendance(queryDocumentSnapshots)))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void fetchStudentAttendanceRecords(FirestoreCallback<List<AttendanceModel>> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("Session expired. Please login again.");
            return;
        }

        firestore.collection(FirestoreCollections.ATTENDANCE_RECORDS)
                .whereEqualTo(FirestoreFields.STUDENT_UID, currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> callback.onSuccess(parseAttendance(queryDocumentSnapshots)))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    private List<UserModel> parseUsers(QuerySnapshot snapshots) {
        List<UserModel> users = new ArrayList<>();
        for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
            UserModel user = snapshot.toObject(UserModel.class);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    private List<AttendanceModel> parseAttendance(QuerySnapshot snapshots) {
        List<AttendanceModel> records = new ArrayList<>();
        for (DocumentSnapshot snapshot : snapshots.getDocuments()) {
            AttendanceModel model = snapshot.toObject(AttendanceModel.class);
            if (model != null) {
                records.add(model);
            }
        }
        Collections.sort(records, (first, second) -> second.getDate().compareTo(first.getDate()));
        return records;
    }

    private String buildAttendanceId(String studentUid, String subject, String date) {
        String normalizedSubject = subject.trim().replaceAll("[^A-Za-z0-9]+", "_");
        return studentUid + "_" + normalizedSubject + "_" + date;
    }

    private String readableError(Exception error) {
        if (error instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) error;
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return "Attendance access was denied by Firestore rules.";
            }
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                return "Firestore query needs an index or additional setup. Please check the Firebase Console.";
            }
        }
        String message = error.getMessage();
        return message == null || message.trim().isEmpty()
                ? "Attendance request failed. Please try again."
                : message;
    }
}
