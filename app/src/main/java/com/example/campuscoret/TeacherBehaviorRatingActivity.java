package com.example.campuscoret;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TeacherBehaviorRatingActivity extends AppCompatActivity {
    private Spinner classSpinner;
    private Spinner studentSpinner;
    private TextInputEditText disciplineInput;
    private TextInputEditText participationInput;
    private TextInputEditText punctualityInput;
    private TextInputEditText respectInput;
    private List<StudentProfile> currentStudents = new ArrayList<>();
    private String teacherEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_behavior_rating);

        teacherEmail = getIntent().getStringExtra("user_email");
        ((TextView) findViewById(R.id.behavior_teacher_info))
                .setText(getString(R.string.exam_teacher_info, teacherEmail));

        classSpinner = findViewById(R.id.behavior_class_spinner);
        studentSpinner = findViewById(R.id.behavior_student_spinner);
        disciplineInput = findViewById(R.id.behavior_discipline_input);
        participationInput = findViewById(R.id.behavior_participation_input);
        punctualityInput = findViewById(R.id.behavior_punctuality_input);
        respectInput = findViewById(R.id.behavior_respect_input);

        bindClassSpinner();
        classSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                refreshStudents();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        findViewById(R.id.save_behavior_button).setOnClickListener(v -> saveBehaviorRating());
        refreshStudents();
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

    private void refreshStudents() {
        String className = classSpinner.getSelectedItem() == null ? "" : classSpinner.getSelectedItem().toString();
        currentStudents = getString(R.string.session_class_placeholder).equals(className)
                ? new ArrayList<>()
                : StudentDirectoryRepository.getStudentsForClass(className);

        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.behavior_student_placeholder));
        for (StudentProfile profile : currentStudents) {
            labels.add(profile.getStudentName() + " - " + profile.getStudentEmail());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studentSpinner.setAdapter(adapter);
    }

    private void saveBehaviorRating() {
        String className = classSpinner.getSelectedItem().toString();
        if (getString(R.string.session_class_placeholder).equals(className)) {
            Toast.makeText(this, R.string.behavior_class_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int studentIndex = studentSpinner.getSelectedItemPosition() - 1;
        if (studentIndex < 0 || studentIndex >= currentStudents.size()) {
            Toast.makeText(this, R.string.behavior_student_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer discipline = parseScore(disciplineInput);
        Integer participation = parseScore(participationInput);
        Integer punctuality = parseScore(punctualityInput);
        Integer respect = parseScore(respectInput);
        if (discipline == null || participation == null || punctuality == null || respect == null) {
            Toast.makeText(this, R.string.behavior_fill_error, Toast.LENGTH_SHORT).show();
            return;
        }

        StudentProfile profile = currentStudents.get(studentIndex);
        BehaviorRepository.upsertRating(
                profile.getStudentEmail(),
                profile.getStudentName(),
                discipline,
                participation,
                punctuality,
                respect,
                teacherEmail
        );

        BehaviorRating rating = BehaviorRepository.getRatingForStudent(profile.getStudentEmail());
        ((TextView) findViewById(R.id.behavior_saved_summary)).setText(
                getString(R.string.behavior_saved_summary, profile.getStudentName(), rating.getAverageScore())
        );
        Toast.makeText(this, R.string.behavior_saved_toast, Toast.LENGTH_SHORT).show();
    }

    private Integer parseScore(TextInputEditText input) {
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 1 || value > 10) {
                return null;
            }
            return value;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
