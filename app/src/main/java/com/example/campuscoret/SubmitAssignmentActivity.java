package com.example.campuscoret;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SubmitAssignmentActivity extends AppCompatActivity {
    private Assignment assignment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        String assignmentId = getIntent().getStringExtra("assignment_id");
        assignment = AssignmentRepository.findAssignmentById(assignmentId);

        TextView title = findViewById(R.id.submit_assignment_title);
        TextView description = findViewById(R.id.submit_assignment_description);
        TextView deadline = findViewById(R.id.submit_assignment_deadline);
        EditText solutionInput = findViewById(R.id.submit_assignment_solution_input);
        EditText fileUrlInput = findViewById(R.id.submit_assignment_file_input);
        Button submitButton = findViewById(R.id.submit_assignment_button);

        if (assignment == null) {
            Toast.makeText(this, R.string.assignment_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        title.setText(assignment.getTitle());
        description.setText(assignment.getDescription());
        deadline.setText(getString(
                R.string.assignment_deadline_label,
                java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT)
                        .format(new java.util.Date(assignment.getDeadlineMillis()))
        ));

        submitButton.setOnClickListener(v -> {
            String solution = solutionInput.getText().toString().trim();
            String fileUrl = fileUrlInput.getText().toString().trim();
            if (solution.isEmpty() && fileUrl.isEmpty()) {
                Toast.makeText(this, R.string.assignment_submission_fill_error, Toast.LENGTH_SHORT).show();
                return;
            }

            String studentId = getIntent().getStringExtra("student_id");
            AssignmentRepository.SubmissionResult result = AssignmentRepository.submitAssignment(
                    assignment.getAssignmentId(),
                    studentId,
                    deriveStudentName(studentId),
                    solution,
                    fileUrl.isEmpty() ? "text-only" : fileUrl
            );

            switch (result.getStatus()) {
                case SUCCESS: {
                    StudentProfile profile = StudentDirectoryRepository.findStudentByEmail(studentId);
                    if (profile != null) {
                        LiveMonitoringRepository.logStudentActivity(
                                profile.getStudentEmail(),
                                profile.getStudentName(),
                                profile.getClassName(),
                                "Submitted assignment: " + assignment.getTitle()
                        );
                    }
                    Toast.makeText(this, R.string.assignment_submission_success, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }
                case DUPLICATE:
                    Toast.makeText(this, R.string.assignment_submission_duplicate, Toast.LENGTH_SHORT).show();
                    break;
                case DEADLINE_PASSED:
                    Toast.makeText(this, R.string.assignment_submission_deadline_passed, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, R.string.assignment_not_found, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private String deriveStudentName(String email) {
        if (email == null || !email.contains("@")) {
            return "Student";
        }
        String local = email.substring(0, email.indexOf('@')).replace('.', ' ').replace('_', ' ').trim();
        if (local.isEmpty()) {
            return "Student";
        }
        return Character.toUpperCase(local.charAt(0)) + local.substring(1);
    }
}
