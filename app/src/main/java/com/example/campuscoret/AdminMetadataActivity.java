package com.example.campuscoret;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminMetadataActivity extends AppCompatActivity {
    private EditText classInput;
    private EditText subjectInput;
    private MetadataAdapter classAdapter;
    private MetadataAdapter subjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_metadata);

        classInput = findViewById(R.id.new_class_input);
        subjectInput = findViewById(R.id.new_subject_input);
        Button addClassButton = findViewById(R.id.add_class_button);
        Button addSubjectButton = findViewById(R.id.add_subject_button);

        RecyclerView classRecycler = findViewById(R.id.classes_recycler);
        RecyclerView subjectRecycler = findViewById(R.id.subjects_recycler);

        classAdapter = new MetadataAdapter(item -> {
            MetadataRepository.removeClass(item);
            refreshLists();
        });
        classRecycler.setLayoutManager(new LinearLayoutManager(this));
        classRecycler.setAdapter(classAdapter);

        subjectAdapter = new MetadataAdapter(item -> {
            MetadataRepository.removeSubject(item);
            refreshLists();
        });
        subjectRecycler.setLayoutManager(new LinearLayoutManager(this));
        subjectRecycler.setAdapter(subjectAdapter);

        addClassButton.setOnClickListener(v -> {
            String name = classInput.getText().toString().trim();
            if (!name.isEmpty()) {
                MetadataRepository.addClass(name);
                classInput.setText("");
                refreshLists();
            }
        });

        addSubjectButton.setOnClickListener(v -> {
            String name = subjectInput.getText().toString().trim();
            if (!name.isEmpty()) {
                MetadataRepository.addSubject(name);
                subjectInput.setText("");
                refreshLists();
            }
        });

        refreshLists();
    }

    private void refreshLists() {
        classAdapter.submitList(MetadataRepository.getClasses());
        subjectAdapter.submitList(MetadataRepository.getSubjects());
    }
}
