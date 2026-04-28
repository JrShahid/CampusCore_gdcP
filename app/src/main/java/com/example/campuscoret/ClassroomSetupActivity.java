package com.example.campuscoret;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ClassroomSetupActivity extends AppCompatActivity {
    private Spinner classSpinner;
    private Spinner subjectSpinner;
    private Spinner slotSpinner;
    private EditText buildingInput;
    private EditText floorInput;
    private EditText roomInput;
    private TextView savedSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_setup);

        TextView teacherInfo = findViewById(R.id.setup_teacher_info);
        classSpinner = findViewById(R.id.setup_class_spinner);
        subjectSpinner = findViewById(R.id.setup_subject_spinner);
        slotSpinner = findViewById(R.id.setup_slot_spinner);
        buildingInput = findViewById(R.id.setup_building_input);
        floorInput = findViewById(R.id.setup_floor_input);
        roomInput = findViewById(R.id.setup_room_input);
        savedSummary = findViewById(R.id.setup_saved_summary);
        Button saveButton = findViewById(R.id.save_classroom_button);

        String email = getIntent().getStringExtra("user_email");
        teacherInfo.setText(getString(R.string.classroom_setup_teacher_info, email));

        bindSpinner(classSpinner, R.array.session_classes);
        bindSpinner(subjectSpinner, R.array.session_subjects);
        bindSpinner(slotSpinner, R.array.classroom_slot_options);

        saveButton.setOnClickListener(v -> saveClassroomEntry());
    }

    private void bindSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void saveClassroomEntry() {
        String className = classSpinner.getSelectedItem().toString();
        String subjectName = subjectSpinner.getSelectedItem().toString();
        String slotLabel = slotSpinner.getSelectedItem().toString();
        String building = buildingInput.getText().toString().trim();
        String floor = floorInput.getText().toString().trim();
        String room = roomInput.getText().toString().trim();

        if (getString(R.string.session_class_placeholder).equals(className)) {
            Toast.makeText(this, R.string.classroom_setup_select_class_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.session_subject_placeholder).equals(subjectName)) {
            Toast.makeText(this, R.string.classroom_setup_select_subject_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.classroom_setup_slot_placeholder).equals(slotLabel)) {
            Toast.makeText(this, R.string.classroom_setup_select_slot_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (building.isEmpty() || floor.isEmpty() || room.isEmpty()) {
            Toast.makeText(this, R.string.classroom_setup_fill_location_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int slotOrder = parseSlot(slotLabel);
        TimetableRepository.upsertEntry(className, subjectName, building, floor, room, slotOrder);
        savedSummary.setText(
                getString(R.string.classroom_setup_saved_summary, subjectName, className, building, floor, room, slotOrder)
        );
        Toast.makeText(this, R.string.classroom_setup_saved_toast, Toast.LENGTH_SHORT).show();
    }

    private int parseSlot(String slotLabel) {
        String digits = slotLabel.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 1;
        }
        return Integer.parseInt(digits);
    }
}
