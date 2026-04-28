package com.example.campuscoret;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StudentExamResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_exam_result);

        String examId = getIntent().getStringExtra("exam_id");
        String studentId = getIntent().getStringExtra("student_id");
        ExamRecord exam = ExamRepository.findExamById(examId);
        ExamAttempt attempt = ExamRepository.getAttemptForStudent(examId, studentId);

        if (exam == null || attempt == null) {
            Toast.makeText(this, R.string.exam_result_not_available, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.exam_result_title)).setText(exam.getTitle());
        ((TextView) findViewById(R.id.exam_result_meta)).setText(
                getString(R.string.exam_result_meta, exam.getSubjectName(), exam.getClassName())
        );
        ((TextView) findViewById(R.id.exam_result_score)).setText(
                getString(R.string.exam_result_score, attempt.getScore(), exam.getTotalMarks())
        );
        ((TextView) findViewById(R.id.exam_result_percentage)).setText(
                getString(R.string.exam_result_percentage, attempt.getPercentage())
        );
        ((TextView) findViewById(R.id.exam_result_feedback)).setText(attempt.getFeedback());
    }
}
