package com.example.campuscoret;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewSubmissionsActivity extends AppCompatActivity {
    private SubmissionAdapter adapter;
    private String pendingSubmissionId;
    private EditText marksInput;
    private EditText feedbackInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_submissions);

        String assignmentId = getIntent().getStringExtra("assignment_id");
        Assignment assignment = AssignmentRepository.findAssignmentById(assignmentId);
        if (assignment == null) {
            Toast.makeText(this, R.string.assignment_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.review_assignment_title)).setText(assignment.getTitle());
        ((TextView) findViewById(R.id.review_assignment_meta)).setText(
                assignment.getSubjectName() + " • " + assignment.getClassName()
        );
        marksInput = findViewById(R.id.review_marks_input);
        feedbackInput = findViewById(R.id.review_feedback_input);
        TextView emptyState = findViewById(R.id.review_submissions_empty_state);
        Button saveEvaluationButton = findViewById(R.id.save_evaluation_button);
        RecyclerView recyclerView = findViewById(R.id.review_submissions_recycler);

        adapter = new SubmissionAdapter(submission -> {
            pendingSubmissionId = submission.getSubmissionId();
            marksInput.setText(submission.getMarks() == null ? "" : String.valueOf(submission.getMarks()));
            feedbackInput.setText(submission.getFeedback() == null ? "" : submission.getFeedback());
            Toast.makeText(
                    ReviewSubmissionsActivity.this,
                    getString(R.string.assignment_selected_submission, submission.getStudentName()),
                    Toast.LENGTH_SHORT
            ).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        java.util.List<AssignmentSubmission> submissions = AssignmentRepository.getSubmissionsForAssignment(assignmentId);
        adapter.submitList(submissions);
        emptyState.setText(submissions.isEmpty()
                ? getString(R.string.assignment_review_empty)
                : getString(R.string.assignment_submission_count, submissions.size()));

        saveEvaluationButton.setOnClickListener(v -> {
            if (pendingSubmissionId == null) {
                Toast.makeText(this, R.string.assignment_select_submission_first, Toast.LENGTH_SHORT).show();
                return;
            }

            String marksText = marksInput.getText().toString().trim();
            String feedback = feedbackInput.getText().toString().trim();
            if (marksText.isEmpty() || feedback.isEmpty()) {
                Toast.makeText(this, R.string.assignment_evaluation_fill_error, Toast.LENGTH_SHORT).show();
                return;
            }

            AssignmentRepository.evaluateSubmission(pendingSubmissionId, Integer.parseInt(marksText), feedback);
            adapter.submitList(AssignmentRepository.getSubmissionsForAssignment(assignmentId));
            Toast.makeText(this, R.string.assignment_evaluation_saved, Toast.LENGTH_SHORT).show();
        });
    }
}
