package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

public class AssignmentManagementActivity extends AppCompatActivity {
    private Spinner subjectSpinner;
    private Spinner classSpinner;
    private Spinner deadlineSpinner;
    private EditText titleInput;
    private EditText descriptionInput;
    private TextView emptyState;
    private AssignmentAdapter adapter;
    private String teacherEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_management);

        teacherEmail = getIntent().getStringExtra("user_email");
        TextView teacherInfo = findViewById(R.id.assignment_teacher_info);
        teacherInfo.setText(getString(R.string.assignment_teacher_info, teacherEmail));

        subjectSpinner = findViewById(R.id.assignment_subject_spinner);
        classSpinner = findViewById(R.id.assignment_class_spinner);
        deadlineSpinner = findViewById(R.id.assignment_deadline_spinner);
        titleInput = findViewById(R.id.assignment_title_input);
        descriptionInput = findViewById(R.id.assignment_description_input);
        emptyState = findViewById(R.id.assignment_teacher_empty_state);
        Button createButton = findViewById(R.id.create_assignment_button);
        RecyclerView recyclerView = findViewById(R.id.assignment_teacher_recycler);

        bindSpinner(subjectSpinner, R.array.session_subjects);
        bindSpinner(classSpinner, R.array.session_classes);
        bindSpinner(deadlineSpinner, R.array.assignment_deadline_options);

        adapter = new AssignmentAdapter(new AssignmentAdapter.AssignmentActionListener() {
            @Override
            public void onPrimaryAction(Assignment assignment) {
                Intent intent = new Intent(AssignmentManagementActivity.this, ReviewSubmissionsActivity.class);
                intent.putExtra("assignment_id", assignment.getAssignmentId());
                startActivity(intent);
            }

            @Override
            public void onSecondaryAction(Assignment assignment) {
                Toast.makeText(
                        AssignmentManagementActivity.this,
                        getString(R.string.assignment_created_summary, assignment.getTitle(), assignment.getClassName()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }, R.string.assignment_review_button, R.string.assignment_summary_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        createButton.setOnClickListener(v -> createAssignment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAssignments();
    }

    private void bindSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void createAssignment() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String subject = subjectSpinner.getSelectedItem().toString();
        String className = classSpinner.getSelectedItem().toString();
        String deadlineOption = deadlineSpinner.getSelectedItem().toString();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, R.string.assignment_fill_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.session_subject_placeholder).equals(subject)) {
            Toast.makeText(this, R.string.assignment_select_subject_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.session_class_placeholder).equals(className)) {
            Toast.makeText(this, R.string.assignment_select_class_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.assignment_deadline_placeholder).equals(deadlineOption)) {
            Toast.makeText(this, R.string.assignment_select_deadline_error, Toast.LENGTH_SHORT).show();
            return;
        }

        AssignmentRepository.createAssignment(
                title,
                description,
                subject,
                className,
                teacherEmail,
                resolveDeadline(deadlineOption)
        );

        titleInput.setText("");
        descriptionInput.setText("");
        Toast.makeText(this, R.string.assignment_create_success, Toast.LENGTH_SHORT).show();
        refreshAssignments();
    }

    private long resolveDeadline(String deadlineOption) {
        Calendar calendar = Calendar.getInstance();
        if (deadlineOption.contains("1")) {
            calendar.add(Calendar.DATE, 1);
        } else if (deadlineOption.contains("3")) {
            calendar.add(Calendar.DATE, 3);
        } else {
            calendar.add(Calendar.DATE, 7);
        }
        return calendar.getTimeInMillis();
    }

    private void refreshAssignments() {
        java.util.List<Assignment> assignments = AssignmentRepository.getAssignmentsForTeacher(teacherEmail);
        adapter.submitList(assignments);
        emptyState.setText(assignments.isEmpty()
                ? getString(R.string.assignment_teacher_empty)
                : getString(R.string.assignment_count_label, assignments.size()));
    }
}
