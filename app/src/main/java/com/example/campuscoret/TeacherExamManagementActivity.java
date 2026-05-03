package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeacherExamManagementActivity extends AppCompatActivity {
    private Spinner subjectSpinner;
    private Spinner classSpinner;
    private TextInputEditText titleInput;
    private TextInputEditText durationInput;
    private TextInputEditText totalMarksInput;
    private TextInputEditText startInMinutesInput;
    private LinearLayout questionContainer;
    private TextView emptyState;
    private TextView storageHint;
    private String teacherEmail;
    private TeacherExamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_exam_management);

        teacherEmail = getIntent().getStringExtra("user_email");
        TextView teacherInfo = findViewById(R.id.exam_teacher_info);
        teacherInfo.setText(getString(R.string.exam_teacher_info, teacherEmail));

        storageHint = findViewById(R.id.exam_storage_hint);
        storageHint.setText(getString(R.string.exam_firestore_hint));
        titleInput = findViewById(R.id.exam_title_input);
        durationInput = findViewById(R.id.exam_duration_input);
        totalMarksInput = findViewById(R.id.exam_total_marks_input);
        startInMinutesInput = findViewById(R.id.exam_start_minutes_input);
        subjectSpinner = findViewById(R.id.exam_subject_spinner);
        classSpinner = findViewById(R.id.exam_class_spinner);
        questionContainer = findViewById(R.id.exam_question_container);
        emptyState = findViewById(R.id.exam_teacher_empty_state);
        RecyclerView recyclerView = findViewById(R.id.exam_teacher_recycler);

        SpinnerUtils.bindDynamicSpinner(subjectSpinner, MetadataRepository.getSubjects(), getString(R.string.session_subject_placeholder));
        SpinnerUtils.bindDynamicSpinner(classSpinner, MetadataRepository.getClasses(), getString(R.string.session_class_placeholder));

        adapter = new TeacherExamAdapter(new TeacherExamAdapter.TeacherExamActionListener() {
            @Override
            public void onPrimaryAction(ExamRecord exam) {
                Intent intent = new Intent(TeacherExamManagementActivity.this, TeacherExamPerformanceActivity.class);
                intent.putExtra("exam_id", exam.getExamId());
                startActivity(intent);
            }

            @Override
            public void onSecondaryAction(ExamRecord exam) {
                Toast.makeText(
                        TeacherExamManagementActivity.this,
                        getString(R.string.exam_preview_toast, exam.getQuestions().size(), exam.getTotalMarks()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.add_exam_question_button).setOnClickListener(v -> addQuestionEditor(null));
        findViewById(R.id.create_exam_button).setOnClickListener(v -> createExam());

        addQuestionEditor(null);
        addQuestionEditor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshExams();
    }



    private void addQuestionEditor(ExamQuestion question) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_exam_question_editor, questionContainer, false);
        TextView questionIndex = view.findViewById(R.id.editor_question_index);
        questionIndex.setText(getString(R.string.exam_question_index, questionContainer.getChildCount() + 1));

        Spinner correctAnswerSpinner = view.findViewById(R.id.correct_answer_spinner);
        SpinnerUtils.bindSpinner(correctAnswerSpinner, R.array.exam_correct_answer_options);

        if (question != null) {
            ((TextInputEditText) view.findViewById(R.id.question_text_input)).setText(question.getQuestionText());
            ((TextInputEditText) view.findViewById(R.id.option_one_input)).setText(question.getOptions().get(0));
            ((TextInputEditText) view.findViewById(R.id.option_two_input)).setText(question.getOptions().get(1));
            ((TextInputEditText) view.findViewById(R.id.option_three_input)).setText(question.getOptions().get(2));
            ((TextInputEditText) view.findViewById(R.id.option_four_input)).setText(question.getOptions().get(3));
            correctAnswerSpinner.setSelection(question.getCorrectAnswerIndex());
        }

        questionContainer.addView(view);
    }

    private void createExam() {
        String title = readText(titleInput);
        String subject = subjectSpinner.getSelectedItem().toString();
        String className = classSpinner.getSelectedItem().toString();
        String durationText = readText(durationInput);
        String totalMarksText = readText(totalMarksInput);
        String startInText = readText(startInMinutesInput);

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.exam_title_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.session_subject_placeholder).equals(subject)) {
            Toast.makeText(this, R.string.exam_subject_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.session_class_placeholder).equals(className)) {
            Toast.makeText(this, R.string.exam_class_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (durationText.isEmpty() || totalMarksText.isEmpty() || startInText.isEmpty()) {
            Toast.makeText(this, R.string.exam_fill_numeric_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer durationMinutes = parseInteger(durationText);
        Integer totalMarks = parseInteger(totalMarksText);
        Integer startInMinutes = parseInteger(startInText);
        if (durationMinutes == null || totalMarks == null || startInMinutes == null) {
            Toast.makeText(this, R.string.exam_fill_numeric_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (durationMinutes <= 0 || totalMarks <= 0 || startInMinutes < 0) {
            Toast.makeText(this, R.string.exam_numeric_range_error, Toast.LENGTH_SHORT).show();
            return;
        }

        List<ExamQuestion> questions = buildQuestions();
        if (questions == null || questions.isEmpty()) {
            return;
        }

        long startTimeMillis = System.currentTimeMillis() + startInMinutes * 60_000L;
        long endTimeMillis = startTimeMillis + durationMinutes * 60_000L;
        ExamRecord exam = ExamRepository.createExam(
                title,
                subject,
                className,
                durationMinutes,
                totalMarks,
                startTimeMillis,
                endTimeMillis,
                teacherEmail,
                questions
        );

        Toast.makeText(
                this,
                getString(
                        R.string.exam_create_success,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                                .format(new Date(exam.getStartTimeMillis()))
                ),
                Toast.LENGTH_LONG
        ).show();
        resetForm();
        refreshExams();
    }

    private List<ExamQuestion> buildQuestions() {
        List<ExamQuestion> questions = new ArrayList<>();
        for (int i = 0; i < questionContainer.getChildCount(); i++) {
            View questionView = questionContainer.getChildAt(i);
            String questionText = readText(questionView.findViewById(R.id.question_text_input));
            String optionOne = readText(questionView.findViewById(R.id.option_one_input));
            String optionTwo = readText(questionView.findViewById(R.id.option_two_input));
            String optionThree = readText(questionView.findViewById(R.id.option_three_input));
            String optionFour = readText(questionView.findViewById(R.id.option_four_input));
            Spinner correctAnswerSpinner = questionView.findViewById(R.id.correct_answer_spinner);

            boolean allBlank = questionText.isEmpty()
                    && optionOne.isEmpty()
                    && optionTwo.isEmpty()
                    && optionThree.isEmpty()
                    && optionFour.isEmpty();
            if (allBlank) {
                continue;
            }

            if (questionText.isEmpty() || optionOne.isEmpty() || optionTwo.isEmpty()
                    || optionThree.isEmpty() || optionFour.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.exam_question_fill_error, i + 1),
                        Toast.LENGTH_SHORT
                ).show();
                return null;
            }

            List<String> options = new ArrayList<>();
            options.add(optionOne);
            options.add(optionTwo);
            options.add(optionThree);
            options.add(optionFour);

            questions.add(new ExamQuestion(
                    "Q-" + System.currentTimeMillis() + "-" + i,
                    questionText,
                    options,
                    correctAnswerSpinner.getSelectedItemPosition()
            ));
        }

        if (questions.isEmpty()) {
            Toast.makeText(this, R.string.exam_question_required_error, Toast.LENGTH_SHORT).show();
            return null;
        }
        return questions;
    }

    private void resetForm() {
        titleInput.setText("");
        durationInput.setText("");
        totalMarksInput.setText("");
        startInMinutesInput.setText("0");
        subjectSpinner.setSelection(0);
        classSpinner.setSelection(0);
        questionContainer.removeAllViews();
        addQuestionEditor(null);
        addQuestionEditor(null);
    }

    private void refreshExams() {
        List<ExamRecord> exams = ExamRepository.getExamsForTeacher(teacherEmail);
        adapter.submitList(exams);
        emptyState.setText(exams.isEmpty()
                ? getString(R.string.exam_teacher_empty)
                : getString(R.string.exam_count_label, exams.size()));
    }

    private String readText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String readText(View view) {
        if (view instanceof TextInputEditText) {
            return readText((TextInputEditText) view);
        }
        return "";
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
