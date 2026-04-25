package com.example.campuscoret;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudyMaterialsActivity extends AppCompatActivity {
    private Spinner subjectFilterSpinner;
    private TextView emptyState;
    private StudyMaterialAdapter adapter;
    private String studentClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_materials);

        studentClassName = getIntent().getStringExtra("student_class");
        TextView classInfo = findViewById(R.id.materials_class_info);
        classInfo.setText(getString(R.string.study_materials_class_info, studentClassName));

        subjectFilterSpinner = findViewById(R.id.materials_subject_filter_spinner);
        emptyState = findViewById(R.id.materials_empty_state);
        RecyclerView recyclerView = findViewById(R.id.materials_recycler);

        bindSpinner();
        adapter = new StudyMaterialAdapter(new StudyMaterialAdapter.MaterialActionListener() {
            @Override
            public void onView(StudyMaterial material) {
                StudentProfile profile = StudentDirectoryRepository.findStudentByEmail(getIntent().getStringExtra("user_email"));
                if (profile != null) {
                    LiveMonitoringRepository.logStudentActivity(
                            profile.getStudentEmail(),
                            profile.getStudentName(),
                            profile.getClassName(),
                            "Viewed material: " + material.getTitle()
                    );
                }
                Toast.makeText(
                        StudyMaterialsActivity.this,
                        getString(R.string.study_material_view_toast, material.getTitle(), material.getFileName()),
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onDownload(StudyMaterial material) {
                StudentProfile profile = StudentDirectoryRepository.findStudentByEmail(getIntent().getStringExtra("user_email"));
                if (profile != null) {
                    LiveMonitoringRepository.logStudentActivity(
                            profile.getStudentEmail(),
                            profile.getStudentName(),
                            profile.getClassName(),
                            "Downloaded material: " + material.getFileName()
                    );
                }
                Toast.makeText(
                        StudyMaterialsActivity.this,
                        getString(R.string.study_material_download_toast, material.getFileName()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.filter_materials_button).setOnClickListener(v -> refreshMaterials());
        refreshMaterials();
    }

    private void bindSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.study_material_subject_filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectFilterSpinner.setAdapter(adapter);
    }

    private void refreshMaterials() {
        String subject = subjectFilterSpinner.getSelectedItem().toString();
        List<StudyMaterial> materials;

        if (getString(R.string.study_material_all_subjects).equals(subject)
                || getString(R.string.study_material_subject_filter_placeholder).equals(subject)) {
            materials = StudyMaterialRepository.getMaterialsForClass(studentClassName);
        } else {
            materials = StudyMaterialRepository.getMaterialsForClassAndSubject(studentClassName, subject);
        }

        adapter.submitList(materials);
        emptyState.setText(
                materials.isEmpty()
                        ? getString(R.string.study_materials_empty)
                        : getString(R.string.study_materials_count, materials.size())
        );
    }
}
