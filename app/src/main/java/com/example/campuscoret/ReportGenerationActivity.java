package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class ReportGenerationActivity extends AppCompatActivity {
    private Spinner classSpinner;
    private Spinner reportTypeSpinner;
    private TextView reportTitle;
    private TextView reportSummary;
    private TextView emptyState;
    private Button exportButton;
    private GeneratedReport currentReport;
    private ReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_generation);

        classSpinner = findViewById(R.id.report_class_spinner);
        reportTypeSpinner = findViewById(R.id.report_type_spinner);
        reportTitle = findViewById(R.id.report_generated_title);
        reportSummary = findViewById(R.id.report_generated_summary);
        emptyState = findViewById(R.id.report_empty_state);
        exportButton = findViewById(R.id.export_report_button);

        bindClassSpinner();
        bindTypeSpinner();

        RecyclerView recyclerView = findViewById(R.id.report_recycler);
        adapter = new ReportAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.generate_report_button).setOnClickListener(v -> generateReport());
        exportButton.setOnClickListener(v -> exportReport());
    }

    private void bindClassSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.session_classes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(adapter);
    }

    private void bindTypeSpinner() {
        List<String> types = Arrays.asList(
                ReportRepository.TYPE_ATTENDANCE,
                ReportRepository.TYPE_ASSIGNMENT,
                ReportRepository.TYPE_EXAM,
                ReportRepository.TYPE_OVERALL
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                types
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);
    }

    private void generateReport() {
        String className = classSpinner.getSelectedItem().toString();
        if (getString(R.string.session_class_placeholder).equals(className)) {
            Toast.makeText(this, R.string.report_select_class_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String reportType = reportTypeSpinner.getSelectedItem().toString();
        currentReport = ReportRepository.generateReport(reportType, className);

        reportTitle.setText(currentReport.getTitle() + " - " + className);
        reportSummary.setText(currentReport.getSummary());
        adapter.submitList(currentReport.getRows());
        emptyState.setText(currentReport.getRows().isEmpty()
                ? getString(R.string.report_empty)
                : getString(R.string.report_count, currentReport.getRows().size()));
        exportButton.setEnabled(true);
    }

    private void exportReport() {
        if (currentReport == null) {
            Toast.makeText(this, R.string.report_generate_first, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, currentReport.getTitle());
        sendIntent.putExtra(Intent.EXTRA_TEXT, currentReport.getExportText());
        startActivity(Intent.createChooser(sendIntent, getString(R.string.report_export_chooser)));
    }
}
