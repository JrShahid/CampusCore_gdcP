package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends AppCompatActivity {
    private TextView classInfoText;
    private TextView sessionStatusText;
    private Button joinSessionButton;
    private String studentClassName;
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        TextView welcomeText = findViewById(R.id.dashboard_welcome);
        classInfoText = findViewById(R.id.student_class_info);
        sessionStatusText = findViewById(R.id.student_session_status);
        joinSessionButton = findViewById(R.id.join_session_button);
        studentEmail = getIntent().getStringExtra("user_email");
        studentClassName = resolveStudentClass(studentEmail);
        welcomeText.setText(getString(R.string.dashboard_welcome_message, studentEmail));
        classInfoText.setText(getString(R.string.student_class_info, studentClassName));

        joinSessionButton.setOnClickListener(v -> {
            SessionRecord activeSession = SessionRepository.getActiveSession();
            if (isMatchingActiveSession(activeSession)) {
                Intent intent = new Intent(this, ScanQrActivity.class);
                intent.putExtra("user_email", studentEmail);
                intent.putExtra("student_class", studentClassName);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.student_no_live_session, Toast.LENGTH_SHORT).show();
            }
        });

        Button locateClassroomButton = findViewById(R.id.locate_classroom_button);
        locateClassroomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassroomLocatorActivity.class);
            intent.putExtra("user_email", studentEmail);
            intent.putExtra("student_class", studentClassName);
            startActivity(intent);
        });

        Button viewMaterialsButton = findViewById(R.id.view_materials_button);
        viewMaterialsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudyMaterialsActivity.class);
            intent.putExtra("user_email", studentEmail);
            intent.putExtra("student_class", studentClassName);
            startActivity(intent);
        });

        Button trackAssignmentsButton = findViewById(R.id.track_assignments_button);
        trackAssignmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentAssignmentsActivity.class);
            intent.putExtra("user_email", studentEmail);
            intent.putExtra("student_class", studentClassName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSessionState();
    }

    private void refreshSessionState() {
        SessionRecord activeSession = SessionRepository.getActiveSession();
        if (isMatchingActiveSession(activeSession)) {
            sessionStatusText.setVisibility(View.VISIBLE);
            sessionStatusText.setText(
                    getString(
                            R.string.student_live_session_status,
                            activeSession.getSubjectName(),
                            activeSession.getClassName()
                    )
            );
            joinSessionButton.setEnabled(true);
            NotificationHelper.notifyStudentSessionAvailable(this, activeSession);
        } else {
            sessionStatusText.setVisibility(View.VISIBLE);
            sessionStatusText.setText(getString(R.string.student_no_live_session_banner, studentClassName));
            joinSessionButton.setEnabled(false);
        }
    }

    private boolean isMatchingActiveSession(SessionRecord sessionRecord) {
        return sessionRecord != null
                && sessionRecord.isActive()
                && studentClassName.equals(sessionRecord.getClassName());
    }

    private String resolveStudentClass(String email) {
        if (email == null) {
            return "BCA 2B";
        }

        if (email.startsWith("student1") || email.startsWith("aarav")) {
            return "BCA 1A";
        }

        if (email.startsWith("student3") || email.startsWith("meera")) {
            return "MCA 1C";
        }

        if (email.startsWith("student4") || email.startsWith("kabir")) {
            return "BSc CS 3A";
        }

        return "BCA 2B";
    }
}
