package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PerformanceAnalyticsActivity extends AppCompatActivity {
    private Spinner classSpinner;
    private TextView emptyState;
    private StudentPerformanceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_analytics);

        classSpinner = findViewById(R.id.analytics_class_spinner);
        emptyState = findViewById(R.id.analytics_empty_state);
        RecyclerView recyclerView = findViewById(R.id.analytics_recycler);

        SpinnerUtils.bindDynamicSpinner(classSpinner, MetadataRepository.getClasses(), getString(R.string.session_class_placeholder));

        adapter = new StudentPerformanceAdapter(summary -> {
            Intent intent = new Intent(PerformanceAnalyticsActivity.this, StudentPerformanceDetailActivity.class);
            intent.putExtra("student_id", summary.getStudentId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.analytics_refresh_button).setOnClickListener(v -> refreshAnalytics());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAnalytics();
    }

    private void refreshAnalytics() {
        String className = classSpinner.getSelectedItem() == null ? "" : classSpinner.getSelectedItem().toString();
        if (getString(R.string.session_class_placeholder).equals(className)) {
            adapter.submitList(java.util.Collections.emptyList());
            emptyState.setText(getString(R.string.analytics_select_class));
            return;
        }

        List<StudentPerformanceSummary> summaries = PerformanceAnalyticsRepository.getSummariesForClass(className);
        adapter.submitList(summaries);
        emptyState.setText(summaries.isEmpty()
                ? getString(R.string.analytics_empty)
                : getString(R.string.analytics_count, summaries.size()));
    }
}
