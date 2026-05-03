package com.example.campuscoret;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ClassroomLocatorActivity extends AppCompatActivity {
    private Spinner subjectSpinner;
    private TextView resultSubject;
    private TextView resultBuilding;
    private TextView resultFloor;
    private TextView resultRoom;
    private View resultCard;
    private String studentClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_locator);

        studentClassName = getIntent().getStringExtra("student_class");
        TextView classInfo = findViewById(R.id.locator_class_info);
        classInfo.setText(getString(R.string.classroom_locator_class_info, studentClassName));

        subjectSpinner = findViewById(R.id.locator_subject_spinner);
        Button findButton = findViewById(R.id.find_classroom_button);
        resultCard = findViewById(R.id.locator_result_card);
        resultSubject = findViewById(R.id.locator_result_subject);
        resultBuilding = findViewById(R.id.locator_result_building);
        resultFloor = findViewById(R.id.locator_result_floor);
        resultRoom = findViewById(R.id.locator_result_room);

        bindSpinner();

        findButton.setOnClickListener(v -> {
            String selected = subjectSpinner.getSelectedItem().toString();
            TimetableEntry entry;

            if (getString(R.string.classroom_locator_next_class_option).equals(selected)) {
                entry = TimetableRepository.findNextClass(studentClassName);
            } else if (getString(R.string.classroom_locator_placeholder).equals(selected)) {
                Toast.makeText(this, R.string.classroom_locator_select_error, Toast.LENGTH_SHORT).show();
                return;
            } else {
                entry = TimetableRepository.findBySubject(studentClassName, selected);
            }

            if (entry == null) {
                Toast.makeText(this, R.string.classroom_locator_not_found, Toast.LENGTH_SHORT).show();
                resultCard.setVisibility(View.GONE);
                return;
            }

            resultCard.setVisibility(View.VISIBLE);
            resultSubject.setText(entry.getSubjectName());
            resultBuilding.setText(entry.getBuilding());
            resultFloor.setText(entry.getFloor());
            resultRoom.setText(entry.getRoomNumber());
        });
    }

    private void bindSpinner() {
        List<String> options = new java.util.ArrayList<>();
        options.add(getString(R.string.classroom_locator_next_class_option));
        options.addAll(MetadataRepository.getSubjects());
        SpinnerUtils.bindDynamicSpinner(subjectSpinner, options, getString(R.string.classroom_locator_placeholder));
    }
}
