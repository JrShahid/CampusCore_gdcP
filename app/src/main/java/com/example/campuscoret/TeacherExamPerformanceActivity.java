package com.example.campuscoret;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class TeacherExamPerformanceActivity extends AppCompatActivity {
    private ExamAttemptAdapter adapter;
    private TextView emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_exam_performance);

        String examId = getIntent().getStringExtra("exam_id");
        ExamRecord exam = ExamRepository.findExamById(examId);
        if (exam == null) {
            Toast.makeText(this, R.string.exam_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.exam_performance_title)).setText(exam.getTitle());
        ((TextView) findViewById(R.id.exam_performance_meta)).setText(
                getString(R.string.exam_performance_meta, exam.getSubjectName(), exam.getClassName())
        );
        ((TextView) findViewById(R.id.exam_performance_schedule)).setText(
                getString(
                        R.string.exam_schedule_label,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(new Date(exam.getStartTimeMillis())),
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(new Date(exam.getEndTimeMillis()))
                )
        );

        List<ExamAttempt> attempts = ExamRepository.getAttemptsForExam(examId);
        ((TextView) findViewById(R.id.exam_attempt_count_value)).setText(
                getString(R.string.exam_attempt_count_value, attempts.size())
        );
        ((TextView) findViewById(R.id.exam_average_value)).setText(
                getString(R.string.exam_average_value, ExamRepository.getAveragePercentage(examId))
        );
        ((TextView) findViewById(R.id.exam_highest_value)).setText(
                getString(R.string.exam_highest_value, ExamRepository.getHighestScore(examId), exam.getTotalMarks())
        );

        emptyState = findViewById(R.id.exam_attempts_empty_state);
        RecyclerView recyclerView = findViewById(R.id.exam_attempts_recycler);
        adapter = new ExamAttemptAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.submitList(attempts);
        emptyState.setText(attempts.isEmpty()
                ? getString(R.string.exam_attempts_empty)
                : getString(R.string.exam_attempts_count, attempts.size()));
    }
}
