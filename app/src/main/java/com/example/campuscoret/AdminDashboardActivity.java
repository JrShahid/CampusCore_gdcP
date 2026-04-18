package com.example.campuscoret;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView welcomeText = findViewById(R.id.dashboard_welcome);
        String email = getIntent().getStringExtra("user_email");
        welcomeText.setText(getString(R.string.dashboard_welcome_message, email));
    }
}
