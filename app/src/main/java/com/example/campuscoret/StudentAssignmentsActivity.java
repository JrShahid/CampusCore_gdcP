package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StudentAssignmentsActivity extends AppCompatActivity {
    private AssignmentAdapter adapter;
    private TextView emptyState;
    private String studentClass;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignments);

        studentClass = getIntent().getStringExtra("student_class");
        studentId = getIntent().getStringExtra("user_email");

        TextView classInfo = findViewById(R.id.student_assignments_class_info);
        classInfo.setText(getString(R.string.study_materials_class_info, studentClass));
        emptyState = findViewById(R.id.student_assignments_empty_state);
        RecyclerView recyclerView = findViewById(R.id.student_assignments_recycler);

        adapter = new AssignmentAdapter(new AssignmentAdapter.AssignmentActionListener() {
            @Override
            public void onPrimaryAction(Assignment assignment) {
                Intent intent = new Intent(StudentAssignmentsActivity.this, SubmitAssignmentActivity.class);
                intent.putExtra("assignment_id", assignment.getAssignmentId());
                intent.putExtra("student_id", studentId);
                startActivity(intent);
            }

            @Override
            public void onSecondaryAction(Assignment assignment) {
                AssignmentSubmission submission =
                        AssignmentRepository.getSubmissionForStudent(assignment.getAssignmentId(), studentId);
                if (submission == null) {
                    android.widget.Toast.makeText(
                            StudentAssignmentsActivity.this,
                            R.string.assignment_not_submitted_yet,
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                } else if (submission.getMarks() == null) {
                    android.widget.Toast.makeText(
                            StudentAssignmentsActivity.this,
                            R.string.assignment_result_pending,
                            android.widget.Toast.LENGTH_SHORT
                    ).show();
                } else {
                    android.widget.Toast.makeText(
                            StudentAssignmentsActivity.this,
                            getString(R.string.assignment_marks_feedback, submission.getMarks(), submission.getFeedback()),
                            android.widget.Toast.LENGTH_LONG
                    ).show();
                }
            }
        }, R.string.assignment_submit_button, R.string.assignment_result_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        java.util.List<Assignment> assignments = AssignmentRepository.getAssignmentsForClass(studentClass);
        adapter.submitList(assignments);
        emptyState.setText(assignments.isEmpty()
                ? getString(R.string.assignment_student_empty)
                : getString(R.string.assignment_count_label, assignments.size()));
    }
}
