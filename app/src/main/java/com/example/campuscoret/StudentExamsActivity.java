package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentExamsActivity extends AppCompatActivity {
    private StudentExamAdapter adapter;
    private TextView emptyState;
    private String studentClass;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_exams);

        studentClass = getIntent().getStringExtra("student_class");
        studentId = getIntent().getStringExtra("user_email");

        TextView classInfo = findViewById(R.id.student_exams_class_info);
        classInfo.setText(getString(R.string.study_materials_class_info, studentClass));
        emptyState = findViewById(R.id.student_exams_empty_state);
        RecyclerView recyclerView = findViewById(R.id.student_exams_recycler);

        adapter = new StudentExamAdapter(studentId, new StudentExamAdapter.StudentExamActionListener() {
            @Override
            public void onPrimaryAction(ExamRecord exam) {
                ExamAttempt attempt = ExamRepository.getAttemptForStudent(exam.getExamId(), studentId);
                if (attempt != null) {
                    openResult(exam.getExamId());
                    return;
                }

                Intent intent = new Intent(StudentExamsActivity.this, StudentExamAttemptActivity.class);
                intent.putExtra("exam_id", exam.getExamId());
                intent.putExtra("student_id", studentId);
                startActivity(intent);
            }

            @Override
            public void onSecondaryAction(ExamRecord exam) {
                ExamAttempt attempt = ExamRepository.getAttemptForStudent(exam.getExamId(), studentId);
                if (attempt == null) {
                    Toast.makeText(StudentExamsActivity.this, R.string.exam_result_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                openResult(exam.getExamId());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshExams();
    }

    private void refreshExams() {
        List<ExamRecord> exams = ExamRepository.getActiveExamsForClass(studentClass, System.currentTimeMillis());
        adapter.submitList(exams);
        emptyState.setText(exams.isEmpty()
                ? getString(R.string.exam_student_empty)
                : getString(R.string.exam_count_label, exams.size()));
    }

    private void openResult(String examId) {
        Intent intent = new Intent(this, StudentExamResultActivity.class);
        intent.putExtra("exam_id", examId);
        intent.putExtra("student_id", studentId);
        startActivity(intent);
    }
}
