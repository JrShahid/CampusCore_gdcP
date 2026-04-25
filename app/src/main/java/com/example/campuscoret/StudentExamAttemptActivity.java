package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StudentExamAttemptActivity extends AppCompatActivity {
    private ExamRecord exam;
    private String studentId;
    private TextView timerValue;
    private CountDownTimer countDownTimer;
    private boolean submitted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_exam_attempt);

        String examId = getIntent().getStringExtra("exam_id");
        studentId = getIntent().getStringExtra("student_id");
        exam = ExamRepository.findExamById(examId);
        if (exam == null) {
            Toast.makeText(this, R.string.exam_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ExamRepository.getAttemptForStudent(examId, studentId) != null) {
            openResult();
            return;
        }

        if (!ExamRepository.isExamActive(exam, System.currentTimeMillis())) {
            Toast.makeText(this, R.string.exam_not_active, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        LiveMonitoringRepository.recordExamStarted(exam, studentId, deriveStudentName(studentId));

        ((TextView) findViewById(R.id.exam_attempt_title)).setText(exam.getTitle());
        ((TextView) findViewById(R.id.exam_attempt_meta)).setText(
                getString(
                        R.string.exam_attempt_meta,
                        exam.getSubjectName(),
                        exam.getClassName(),
                        exam.getTotalMarks()
                )
        );
        timerValue = findViewById(R.id.exam_timer_value);
        LinearLayout questionContainer = findViewById(R.id.exam_attempt_question_container);
        bindQuestions(questionContainer, exam.getQuestions());
        findViewById(R.id.submit_exam_button).setOnClickListener(v -> submitAttempt());
    }

    @Override
    protected void onStart() {
        super.onStart();
        long remainingTimeMillis = ExamRepository.getRemainingTimeMillis(exam, System.currentTimeMillis());
        if (remainingTimeMillis <= 0L) {
            Toast.makeText(this, R.string.exam_time_over, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        countDownTimer = new CountDownTimer(remainingTimeMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerValue.setText(formatTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                timerValue.setText(formatTime(0L));
                submitAttempt();
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void bindQuestions(LinearLayout container, List<ExamQuestion> questions) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < questions.size(); i++) {
            ExamQuestion question = questions.get(i);
            View view = inflater.inflate(R.layout.item_exam_question_attempt, container, false);
            ((TextView) view.findViewById(R.id.attempt_question_index))
                    .setText(getString(R.string.exam_question_index, i + 1));
            ((TextView) view.findViewById(R.id.attempt_question_text))
                    .setText(question.getQuestionText());

            RadioGroup group = view.findViewById(R.id.attempt_question_group);
            List<String> options = question.getOptions();
            for (int optionIndex = 0; optionIndex < options.size(); optionIndex++) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setId(View.generateViewId());
                radioButton.setTag(optionIndex);
                radioButton.setText(options.get(optionIndex));
                group.addView(radioButton);
            }
            view.setTag(question.getQuestionId());
            container.addView(view);
        }
    }

    private void submitAttempt() {
        if (submitted) {
            return;
        }
        submitted = true;

        Map<String, Integer> selectedAnswers = new LinkedHashMap<>();
        LinearLayout container = findViewById(R.id.exam_attempt_question_container);
        for (int i = 0; i < container.getChildCount(); i++) {
            View questionView = container.getChildAt(i);
            String questionId = (String) questionView.getTag();
            RadioGroup group = questionView.findViewById(R.id.attempt_question_group);
            int checkedId = group.getCheckedRadioButtonId();
            if (checkedId == -1) {
                continue;
            }
            RadioButton radioButton = group.findViewById(checkedId);
            selectedAnswers.put(questionId, (Integer) radioButton.getTag());
        }

        ExamRepository.SubmissionResult result = ExamRepository.submitAttempt(
                exam.getExamId(),
                studentId,
                deriveStudentName(studentId),
                selectedAnswers,
                Math.min(System.currentTimeMillis(), exam.getEndTimeMillis())
        );

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (result.getStatus() == ExamRepository.SubmissionResult.Status.SUCCESS
                || result.getStatus() == ExamRepository.SubmissionResult.Status.DUPLICATE) {
            LiveMonitoringRepository.recordExamSubmitted(exam, studentId, deriveStudentName(studentId));
            openResult();
            return;
        }

        if (result.getStatus() == ExamRepository.SubmissionResult.Status.NOT_ACTIVE) {
            Toast.makeText(this, R.string.exam_not_active, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.exam_not_found, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void openResult() {
        Intent intent = new Intent(this, StudentExamResultActivity.class);
        intent.putExtra("exam_id", exam.getExamId());
        intent.putExtra("student_id", studentId);
        startActivity(intent);
        finish();
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String deriveStudentName(String email) {
        if (email == null || !email.contains("@")) {
            return "Student";
        }

        String localPart = email.substring(0, email.indexOf('@'));
        String normalized = localPart.replace('.', ' ').replace('_', ' ').trim();
        if (normalized.isEmpty()) {
            return "Student";
        }

        String[] words = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
            builder.append(' ');
        }
        return builder.toString().trim();
    }
}
