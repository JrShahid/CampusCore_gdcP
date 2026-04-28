package com.example.campuscoret;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class StudentPerformanceDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_performance_detail);

        String studentId = getIntent().getStringExtra("student_id");
        StudentPerformanceSummary summary = PerformanceAnalyticsRepository.getSummaryForStudent(studentId);
        if (summary == null) {
            Toast.makeText(this, R.string.analytics_student_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.performance_detail_name)).setText(summary.getStudentName());
        ((TextView) findViewById(R.id.performance_detail_meta)).setText(
                getString(R.string.performance_detail_meta, summary.getStudentId(), summary.getClassName())
        );
        ((TextView) findViewById(R.id.performance_detail_final_score)).setText(
                getString(
                        R.string.performance_final_score,
                        String.format(Locale.getDefault(), "%.2f", summary.getFinalScore()),
                        summary.getStatus()
                )
        );
        ((TextView) findViewById(R.id.performance_detail_attendance)).setText(
                getString(R.string.performance_attendance_value, summary.getAttendancePercentage())
        );
        ((TextView) findViewById(R.id.performance_detail_assignments)).setText(
                getString(R.string.performance_assignment_value, summary.getAssignmentPercentage(), summary.getOnTimeAssignmentRate())
        );
        ((TextView) findViewById(R.id.performance_detail_exams)).setText(
                getString(R.string.performance_exam_value, summary.getExamPercentage())
        );
        ((TextView) findViewById(R.id.performance_detail_behavior)).setText(
                getString(R.string.performance_behavior_value, summary.getBehaviorPercentage())
        );
        ((TextView) findViewById(R.id.performance_detail_support)).setText(summary.getSupportMessage());
    }
}
