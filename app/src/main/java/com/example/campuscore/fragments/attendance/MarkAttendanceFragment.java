package com.example.campuscore.fragments.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.campuscore.R;
import com.example.campuscore.adapters.AttendanceHistoryAdapter;
import com.example.campuscore.adapters.StudentAttendanceAdapter;
import com.example.campuscore.databinding.FragmentMarkAttendanceBinding;
import com.example.campuscore.firebase.FirestoreCallback;
import com.example.campuscore.models.AttendanceModel;
import com.example.campuscore.models.StudentAttendanceItem;
import com.example.campuscore.models.UserModel;
import com.example.campuscore.repositories.AttendanceRepository;
import com.example.campuscore.utils.AcademicDataProvider;
import com.example.campuscore.utils.NetworkUtils;
import com.example.campuscore.utils.SnackbarUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkAttendanceFragment extends Fragment {
    private FragmentMarkAttendanceBinding binding;
    private AttendanceRepository repository;
    private final List<StudentAttendanceItem> studentItems = new ArrayList<>();
    private final List<AttendanceModel> historyItems = new ArrayList<>();
    private StudentAttendanceAdapter studentAttendanceAdapter;
    private AttendanceHistoryAdapter historyAdapter;

    public static MarkAttendanceFragment newInstance() {
        return new MarkAttendanceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMarkAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = new AttendanceRepository();
        setupDropdowns();
        setupLists();
        binding.loadStudentsButton.setOnClickListener(v -> loadStudents());
        binding.saveAttendanceButton.setOnClickListener(v -> saveAttendance());
        binding.refreshHistoryButton.setOnClickListener(v -> loadHistory());
        updateFilterContext();
        loadHistory();
    }

    private void setupDropdowns() {
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                AcademicDataProvider.semesterValues());
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                AcademicDataProvider.departmentNames());

        binding.semesterSpinner.setAdapter(semesterAdapter);
        binding.departmentSpinner.setAdapter(departmentAdapter);

        if (semesterAdapter.getCount() > 0) {
            binding.semesterSpinner.setText(semesterAdapter.getItem(0), false);
        }
        if (departmentAdapter.getCount() > 0) {
            binding.departmentSpinner.setText(departmentAdapter.getItem(0), false);
        }
        binding.departmentSpinner.setOnItemClickListener((parent, view, position, id) -> {
            updateSubjectDropdown();
            updateFilterContext();
        });
        binding.subjectSpinner.setOnItemClickListener((parent, view, position, id) -> updateFilterContext());
        binding.semesterSpinner.setOnItemClickListener((parent, view, position, id) -> updateFilterContext());
        updateSubjectDropdown();
    }

    private void updateSubjectDropdown() {
        List<String> subjects = new ArrayList<>();
        for (AcademicDataProvider.SubjectItem item : AcademicDataProvider.subjectsForDepartment(selectedDepartment())) {
            subjects.add(item.getCode());
        }
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                subjects
        );
        binding.subjectSpinner.setAdapter(subjectAdapter);
        if (subjectAdapter.getCount() > 0) {
            binding.subjectSpinner.setText(subjectAdapter.getItem(0), false);
        }
    }

    private void setupLists() {
        studentAttendanceAdapter = new StudentAttendanceAdapter(studentItems);
        historyAdapter = new AttendanceHistoryAdapter(historyItems, true);

        binding.studentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.studentsRecyclerView.setAdapter(studentAttendanceAdapter);

        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyRecyclerView.setAdapter(historyAdapter);
    }

    private void loadStudents() {
        if (!NetworkUtils.isOnline(requireContext())) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.query_requires_network));
            return;
        }

        updateFilterContext();
        setFormLoading(true);
        repository.fetchStudentsForAttendance(selectedSemester(), selectedDepartment(), new FirestoreCallback<List<UserModel>>() {
            @Override
            public void onSuccess(List<UserModel> data) {
                setFormLoading(false);
                List<StudentAttendanceItem> nextItems = new ArrayList<>();
                for (UserModel user : data) {
                    nextItems.add(new StudentAttendanceItem(user, true));
                }
                studentAttendanceAdapter.submitList(nextItems);
                binding.studentsEmptyText.setVisibility(studentItems.isEmpty() ? View.VISIBLE : View.GONE);
                binding.studentsInfoText.setText(studentItems.isEmpty()
                        ? getString(R.string.no_students_found)
                        : getString(R.string.students_loaded_format, studentItems.size()));
            }

            @Override
            public void onError(String message) {
                setFormLoading(false);
                binding.studentsEmptyText.setVisibility(studentItems.isEmpty() ? View.VISIBLE : View.GONE);
                SnackbarUtils.show(binding.rootLayout, message);
            }
        });
    }

    private void saveAttendance() {
        if (studentItems.isEmpty()) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.save_requires_students));
            return;
        }
        if (!NetworkUtils.isOnline(requireContext())) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.query_requires_network));
            return;
        }

        setFormLoading(true);
        repository.saveAttendance(selectedSubject(), selectedSemester(), selectedDepartment(), studentItems,
                new FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        setFormLoading(false);
                        SnackbarUtils.show(binding.rootLayout, getString(R.string.attendance_saved));
                        loadHistory();
                    }

                    @Override
                    public void onError(String message) {
                        setFormLoading(false);
                        SnackbarUtils.show(binding.rootLayout, message);
                    }
                });
    }

    private void loadHistory() {
        if (!NetworkUtils.isOnline(requireContext())) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.query_requires_network));
            return;
        }

        binding.historyProgressBar.setVisibility(View.VISIBLE);
        repository.fetchTeacherAttendanceHistory(new FirestoreCallback<List<AttendanceModel>>() {
            @Override
            public void onSuccess(List<AttendanceModel> data) {
                binding.historyProgressBar.setVisibility(View.GONE);
                List<AttendanceModel> nextItems = new ArrayList<>();
                for (AttendanceModel record : data) {
                    if (selectedSubject().equals(record.getSubject())
                            && selectedSemester().equals(record.getSemester())
                            && selectedDepartment().equals(record.getDepartment())) {
                        nextItems.add(record);
                    }
                }
                sortHistory(nextItems);
                historyAdapter.submitList(nextItems);
                binding.historyEmptyText.setVisibility(historyItems.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.historyProgressBar.setVisibility(View.GONE);
                binding.historyEmptyText.setVisibility(historyItems.isEmpty() ? View.VISIBLE : View.GONE);
                SnackbarUtils.show(binding.rootLayout, message);
            }
        });
    }

    private void sortHistory(List<AttendanceModel> items) {
        Collections.sort(items, (first, second) -> second.getDate().compareTo(first.getDate()));
    }

    private void setFormLoading(boolean loading) {
        binding.formProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.loadStudentsButton.setEnabled(!loading);
        binding.saveAttendanceButton.setEnabled(!loading);
    }

    private void updateFilterContext() {
        binding.filterContextText.setText(getString(
                R.string.filter_context_format,
                selectedSubject(),
                selectedSemester(),
                selectedDepartment()
        ));
    }

    private String selectedSubject() {
        return binding.subjectSpinner.getText().toString().trim();
    }

    private String selectedSemester() {
        return binding.semesterSpinner.getText().toString().trim();
    }

    private String selectedDepartment() {
        return binding.departmentSpinner.getText().toString().trim();
    }
}
