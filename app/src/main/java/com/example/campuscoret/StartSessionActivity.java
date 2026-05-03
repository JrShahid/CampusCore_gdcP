package com.example.campuscoret;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import com.google.zxing.WriterException;

public class StartSessionActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 501;

    private Spinner subjectSpinner;
    private Spinner classSpinner;
    private TextView summaryTitle;
    private TextView sessionIdValue;
    private TextView subjectValue;
    private TextView classValue;
    private TextView attendanceValue;
    private TextView statusValue;
    private TextView startedValue;
    private TextView sessionEmptyState;
    private View sessionSummaryCard;
    private Button endSessionButton;
    private AttendanceAdapter attendanceAdapter;
    private ImageView sessionQrImage;
    private TextView sessionQrLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_session);

        subjectSpinner = findViewById(R.id.subject_spinner);
        classSpinner = findViewById(R.id.class_spinner);
        Button createSessionButton = findViewById(R.id.create_session_button);
        summaryTitle = findViewById(R.id.session_summary_title);
        sessionIdValue = findViewById(R.id.session_id_value);
        subjectValue = findViewById(R.id.session_subject_value);
        classValue = findViewById(R.id.session_class_value);
        attendanceValue = findViewById(R.id.session_attendance_value);
        statusValue = findViewById(R.id.session_status_value);
        startedValue = findViewById(R.id.session_started_value);
        sessionEmptyState = findViewById(R.id.attendance_empty_state);
        sessionSummaryCard = findViewById(R.id.session_summary_card);
        endSessionButton = findViewById(R.id.end_session_button);
        sessionQrImage = findViewById(R.id.session_qr_image);
        sessionQrLabel = findViewById(R.id.session_qr_label);
        RecyclerView attendanceRecycler = findViewById(R.id.attendance_recycler);

        SpinnerUtils.bindDynamicSpinner(subjectSpinner, MetadataRepository.getSubjects(), getString(R.string.session_subject_placeholder));
        SpinnerUtils.bindDynamicSpinner(classSpinner, MetadataRepository.getClasses(), getString(R.string.session_class_placeholder));

        attendanceAdapter = new AttendanceAdapter();
        attendanceRecycler.setLayoutManager(new LinearLayoutManager(this));
        attendanceRecycler.setAdapter(attendanceAdapter);
        requestNotificationPermissionIfNeeded();

        populateActiveSession(SessionRepository.getActiveSession());

        createSessionButton.setOnClickListener(v -> {
            String subjectName = subjectSpinner.getSelectedItem().toString();
            String className = classSpinner.getSelectedItem().toString();

            if (getString(R.string.session_subject_placeholder).equals(subjectName)) {
                Toast.makeText(this, R.string.select_subject_error, Toast.LENGTH_SHORT).show();
                return;
            }

            if (getString(R.string.session_class_placeholder).equals(className)) {
                Toast.makeText(this, R.string.select_class_error, Toast.LENGTH_SHORT).show();
                return;
            }

            String teacherEmail = getIntent().getStringExtra("user_email");
            SessionRecord record = SessionRepository.createSession(teacherEmail, subjectName, className);
            populateActiveSession(record);
            NotificationHelper.notifySessionStarted(this, record);
            Toast.makeText(this, R.string.session_started_success, Toast.LENGTH_SHORT).show();
        });

        endSessionButton.setOnClickListener(v -> {
            SessionRecord sessionRecord = SessionRepository.endActiveSession();
            populateActiveSession(sessionRecord);
            NotificationHelper.notifySessionEnded(this, sessionRecord);
            Toast.makeText(this, R.string.session_ended_success, Toast.LENGTH_SHORT).show();
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }

        requestPermissions(
                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST_CODE
        );
    }



    private void populateActiveSession(SessionRecord sessionRecord) {
        if (sessionRecord == null) {
            sessionSummaryCard.setVisibility(View.GONE);
            attendanceAdapter.submitList(java.util.Collections.emptyList());
            sessionEmptyState.setVisibility(View.VISIBLE);
            endSessionButton.setVisibility(View.GONE);
            sessionQrImage.setImageDrawable(null);
            sessionQrLabel.setText(getString(R.string.session_qr_placeholder));
            return;
        }

        List<AttendanceRecord> attendanceRecords =
                AttendanceRepository.getAttendanceForSession(sessionRecord.getSessionId());

        sessionSummaryCard.setVisibility(View.VISIBLE);
        sessionSummaryCard.setAlpha(sessionRecord.isActive() ? 1f : 0.82f);
        summaryTitle.setText(
                sessionRecord.isActive()
                        ? getString(R.string.active_session_title)
                        : getString(R.string.inactive_session_title)
        );
        sessionIdValue.setText(sessionRecord.getSessionId());
        subjectValue.setText(sessionRecord.getSubjectName());
        classValue.setText(sessionRecord.getClassName());
        attendanceValue.setText(
                sessionRecord.isAttendanceActive()
                        ? getString(R.string.attendance_active_label)
                        : getString(R.string.attendance_inactive_label)
        );
        statusValue.setText(
                sessionRecord.isActive()
                        ? getString(R.string.session_status_active)
                        : getString(R.string.session_status_inactive)
        );
        startedValue.setText(
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(new Date(sessionRecord.getStartedAtMillis()))
        );
        attendanceAdapter.submitList(attendanceRecords);
        sessionEmptyState.setVisibility(attendanceRecords.isEmpty() ? View.VISIBLE : View.GONE);
        endSessionButton.setVisibility(sessionRecord.isActive() ? View.VISIBLE : View.GONE);
        renderQrCode(sessionRecord);
    }

    private void renderQrCode(SessionRecord sessionRecord) {
        String payload = SessionQrHelper.buildPayload(sessionRecord);
        sessionQrLabel.setText(getString(R.string.session_qr_payload_label, payload));

        try {
            Bitmap bitmap = SessionQrHelper.generateBitmap(payload, 640);
            sessionQrImage.setImageBitmap(bitmap);
        } catch (WriterException exception) {
            sessionQrImage.setImageDrawable(null);
            sessionQrLabel.setText(R.string.session_qr_error);
        }
    }
}
