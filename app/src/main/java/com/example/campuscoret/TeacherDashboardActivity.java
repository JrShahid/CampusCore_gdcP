package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        TextView welcomeText = findViewById(R.id.dashboard_welcome);
        Button startSessionButton = findViewById(R.id.start_session_button);
        Button setClassroomButton = findViewById(R.id.set_classroom_button);
        Button uploadMaterialsButton = findViewById(R.id.upload_materials_button);
        Button manageAssignmentsButton = findViewById(R.id.manage_assignments_button);
        Button createExamsButton = findViewById(R.id.create_exams_button);
        Button rateBehaviorButton = findViewById(R.id.rate_behavior_button);
        Button monitorButton = findViewById(R.id.open_monitor_button);
        Button reportsButton = findViewById(R.id.open_reports_button);
        String email = getIntent().getStringExtra("user_email");
        welcomeText.setText(getString(R.string.dashboard_welcome_message, email));

        startSessionButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StartSessionActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });

        setClassroomButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassroomSetupActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });

        uploadMaterialsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadStudyMaterialActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });

        manageAssignmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AssignmentManagementActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });

        createExamsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherExamManagementActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });

        rateBehaviorButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, TeacherBehaviorRatingActivity.class);
            intent.putExtra("user_email", email);
            startActivity(intent);
        });

        monitorButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LiveMonitoringActivity.class);
            startActivity(intent);
        });

        reportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportGenerationActivity.class);
            startActivity(intent);
        });
    }
}
