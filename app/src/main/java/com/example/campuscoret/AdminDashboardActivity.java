package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView welcomeText = findViewById(R.id.dashboard_welcome);
        Button userManagementButton = findViewById(R.id.open_user_management_button);
        Button metadataButton = findViewById(R.id.open_metadata_management_button);
        Button analyticsButton = findViewById(R.id.open_analytics_button);
        Button monitoringButton = findViewById(R.id.open_monitoring_button);
        Button reportsButton = findViewById(R.id.open_reports_button);
        String email = getIntent().getStringExtra("user_email");
        welcomeText.setText(getString(R.string.dashboard_welcome_message, email));

        userManagementButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminUserProvisioningActivity.class);
            startActivity(intent);
        });

        metadataButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminMetadataActivity.class);
            startActivity(intent);
        });

        analyticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PerformanceAnalyticsActivity.class);
            startActivity(intent);
        });

        monitoringButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LiveMonitoringActivity.class);
            startActivity(intent);
        });

        reportsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportGenerationActivity.class);
            startActivity(intent);
        });
    }
}
