package com.example.campuscoret;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UploadStudyMaterialActivity extends AppCompatActivity {
    private Spinner subjectSpinner;
    private Spinner classSpinner;
    private Spinner fileTypeSpinner;
    private EditText titleInput;
    private EditText fileNameInput;
    private TextView latestUploadSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_study_material);

        TextView teacherInfo = findViewById(R.id.material_teacher_info);
        subjectSpinner = findViewById(R.id.material_subject_spinner);
        classSpinner = findViewById(R.id.material_class_spinner);
        fileTypeSpinner = findViewById(R.id.material_file_type_spinner);
        titleInput = findViewById(R.id.material_title_input);
        fileNameInput = findViewById(R.id.material_file_name_input);
        latestUploadSummary = findViewById(R.id.material_latest_summary);
        android.widget.Button uploadButton = findViewById(R.id.upload_material_button);

        String teacherEmail = getIntent().getStringExtra("user_email");
        teacherInfo.setText(getString(R.string.study_material_teacher_info, teacherEmail));

        bindSpinner(subjectSpinner, R.array.session_subjects);
        bindSpinner(classSpinner, R.array.session_classes);
        bindSpinner(fileTypeSpinner, R.array.study_material_file_types);

        uploadButton.setOnClickListener(v -> uploadMaterial(teacherEmail));
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

    private void uploadMaterial(String teacherEmail) {
        String subjectName = subjectSpinner.getSelectedItem().toString();
        String className = classSpinner.getSelectedItem().toString();
        String fileType = fileTypeSpinner.getSelectedItem().toString();
        String title = titleInput.getText().toString().trim();
        String fileName = fileNameInput.getText().toString().trim();

        if (getString(R.string.session_subject_placeholder).equals(subjectName)) {
            Toast.makeText(this, R.string.study_material_select_subject_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.session_class_placeholder).equals(className)) {
            Toast.makeText(this, R.string.study_material_select_class_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (getString(R.string.study_material_file_type_placeholder).equals(fileType)) {
            Toast.makeText(this, R.string.study_material_select_type_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() || fileName.isEmpty()) {
            Toast.makeText(this, R.string.study_material_fill_fields_error, Toast.LENGTH_SHORT).show();
            return;
        }

        StudyMaterialRepository.addMaterial(
                title,
                subjectName,
                className,
                fileType,
                fileName,
                fileName,
                teacherEmail
        );
        latestUploadSummary.setText(
                getString(R.string.study_material_latest_summary, title, subjectName, className, fileType, fileName)
        );
        titleInput.setText("");
        fileNameInput.setText("");
        Toast.makeText(this, R.string.study_material_upload_success, Toast.LENGTH_SHORT).show();
    }
}
