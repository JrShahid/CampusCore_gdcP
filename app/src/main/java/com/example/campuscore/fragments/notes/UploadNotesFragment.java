package com.example.campuscore.fragments.notes;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.campuscore.R;
import com.example.campuscore.adapters.NotesAdapter;
import com.example.campuscore.databinding.FragmentUploadNotesBinding;
import com.example.campuscore.firebase.FirestoreCallback;
import com.example.campuscore.models.NotesModel;
import com.example.campuscore.repositories.NotesRepository;
import com.example.campuscore.utils.AcademicDataProvider;
import com.example.campuscore.utils.CloudinaryConstants;
import com.example.campuscore.utils.IntentConstants;
import com.example.campuscore.utils.NetworkUtils;
import com.example.campuscore.utils.SnackbarUtils;
import com.example.campuscore.utils.ValidationUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadNotesFragment extends Fragment {
    private FragmentUploadNotesBinding binding;
    private NotesRepository repository;
    private final List<NotesModel> uploadedNotes = new ArrayList<>();
    private NotesAdapter notesAdapter;
    private Uri selectedPdfUri;
    private String selectedFileName = "";
    private byte[] selectedPdfBytes;
    private final List<AcademicDataProvider.SubjectItem> currentSubjects = new ArrayList<>();

    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    SnackbarUtils.show(binding.rootLayout, getString(R.string.file_selection_cancelled));
                    return;
                }
                validateAndStoreSelectedPdf(uri);
            });

    public static UploadNotesFragment newInstance() {
        return new UploadNotesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadNotesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        repository = new NotesRepository();
        notesAdapter = new NotesAdapter(uploadedNotes, this::openPdf);
        binding.notesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notesRecyclerView.setAdapter(notesAdapter);

        setupAcademicSelectors();
        binding.selectPdfButton.setOnClickListener(v -> openFilePicker());
        binding.uploadButton.setOnClickListener(v -> uploadSelectedPdf());
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadUploadedNotes);
        loadUploadedNotes();
    }

    private void setupAcademicSelectors() {
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                AcademicDataProvider.departmentNames()
        );
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                AcademicDataProvider.semesterLabels()
        );
        binding.departmentSpinner.setAdapter(departmentAdapter);
        binding.semesterSpinner.setAdapter(semesterAdapter);

        if (departmentAdapter.getCount() > 0) {
            binding.departmentSpinner.setText(departmentAdapter.getItem(0), false);
        }
        if (semesterAdapter.getCount() > 0) {
            binding.semesterSpinner.setText(semesterAdapter.getItem(0), false);
        }

        binding.departmentSpinner.setOnItemClickListener((parent, view, position, id) -> updateSubjects());
        updateSubjects();
    }

    private void updateSubjects() {
        currentSubjects.clear();
        currentSubjects.addAll(AcademicDataProvider.subjectsForDepartment(selectedDepartment()));
        List<String> subjectLabels = new ArrayList<>();
        for (AcademicDataProvider.SubjectItem item : currentSubjects) {
            subjectLabels.add(item.toString());
        }
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                subjectLabels
        );
        binding.subjectSpinner.setAdapter(subjectAdapter);
        if (subjectAdapter.getCount() > 0) {
            binding.subjectSpinner.setText(subjectAdapter.getItem(0), false);
        }
    }

    private void openFilePicker() {
        filePickerLauncher.launch(new String[]{CloudinaryConstants.MIME_TYPE_PDF});
    }

    private void validateAndStoreSelectedPdf(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
        PdfFileMeta meta = readPdfMeta(uri);
        boolean looksLikePdf = CloudinaryConstants.MIME_TYPE_PDF.equalsIgnoreCase(mimeType)
                || meta.displayName.toLowerCase().endsWith(".pdf");
        if (!looksLikePdf) {
            clearSelectedPdf();
            SnackbarUtils.show(binding.rootLayout, getString(R.string.pdf_only_error));
            return;
        }

        if (meta.sizeBytes > CloudinaryConstants.MAX_PDF_SIZE_BYTES) {
            clearSelectedPdf();
            SnackbarUtils.show(binding.rootLayout, getString(R.string.pdf_size_error));
            return;
        }

        byte[] bytes;
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                clearSelectedPdf();
                SnackbarUtils.show(binding.rootLayout, getString(R.string.pdf_only_error));
                return;
            }
            bytes = getBytes(inputStream);
        } catch (IOException error) {
            clearSelectedPdf();
            SnackbarUtils.show(binding.rootLayout, "Unable to read the selected PDF.");
            return;
        }
        if (bytes.length > CloudinaryConstants.MAX_PDF_SIZE_BYTES) {
            clearSelectedPdf();
            SnackbarUtils.show(binding.rootLayout, getString(R.string.pdf_size_error));
            return;
        }

        selectedPdfUri = uri;
        selectedFileName = meta.displayName;
        selectedPdfBytes = bytes;
        binding.fileNameText.setText(meta.displayName);
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException ignored) {
        }
    }

    private void uploadSelectedPdf() {
        binding.titleLayout.setError(null);
        String title = binding.titleInput.getText() == null ? "" : binding.titleInput.getText().toString().trim();
        if (ValidationUtils.isBlank(title)) {
            binding.titleLayout.setError(getString(R.string.notes_title_required));
            return;
        }
        if (selectedPdfUri == null || selectedPdfBytes == null) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.select_valid_file_first));
            return;
        }
        if (!NetworkUtils.isOnline(requireContext())) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.upload_requires_network));
            return;
        }

        AcademicDataProvider.SubjectItem subjectItem = selectedSubjectItem();
        setUploadingState(true, 0);
        repository.uploadNote(
                selectedPdfBytes,
                title,
                selectedDepartment(),
                AcademicDataProvider.semesterValue(selectedSemesterLabel()),
                subjectItem.getCode(),
                subjectItem.getName(),
                teacherName(),
                selectedFileName,
                new NotesRepository.UploadNotesCallback() {
                    @Override
                    public void onProgress(int progressPercent) {
                        setUploadingState(true, progressPercent);
                    }

                    @Override
                    public void onSuccess() {
                        setUploadingState(false, 100);
                        resetFormAfterUpload();
                        SnackbarUtils.show(binding.rootLayout, getString(R.string.notes_uploaded));
                        loadUploadedNotes();
                    }

                    @Override
                    public void onError(String message) {
                        setUploadingState(false, 0);
                        SnackbarUtils.show(binding.rootLayout, message);
                    }
                }
        );
    }

    private void loadUploadedNotes() {
        if (!NetworkUtils.isOnline(requireContext())) {
            binding.swipeRefreshLayout.setRefreshing(false);
            SnackbarUtils.show(binding.rootLayout, getString(R.string.error_no_internet));
            return;
        }

        binding.listProgressBar.setVisibility(View.VISIBLE);
        repository.fetchTeacherNotes(new FirestoreCallback<List<NotesModel>>() {
            @Override
            public void onSuccess(List<NotesModel> data) {
                binding.listProgressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                notesAdapter.submitList(data);
                binding.emptyText.setVisibility(uploadedNotes.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                binding.listProgressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.emptyText.setVisibility(uploadedNotes.isEmpty() ? View.VISIBLE : View.GONE);
                SnackbarUtils.show(binding.rootLayout, message);
            }
        });
    }

    private void openPdf(NotesModel note) {
        if (ValidationUtils.isBlank(note.getPdfUrl())) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.notes_open_error));
            return;
        }
        try {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.download_ready));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(note.getPdfUrl()));
            intent.setDataAndType(Uri.parse(note.getPdfUrl()), CloudinaryConstants.MIME_TYPE_PDF);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception error) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.notes_open_error));
        }
    }

    private void setUploadingState(boolean loading, int progress) {
        binding.uploadButton.setEnabled(!loading);
        binding.selectPdfButton.setEnabled(!loading);
        binding.uploadProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.uploadProgressText.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.uploadProgressBar.setProgress(progress);
        binding.uploadProgressText.setText(getString(R.string.upload_progress_format, progress));
    }

    private void resetFormAfterUpload() {
        binding.titleInput.setText("");
        clearSelectedPdf();
    }

    private void clearSelectedPdf() {
        selectedPdfUri = null;
        selectedFileName = "";
        selectedPdfBytes = null;
        binding.fileNameText.setText(R.string.no_file_selected);
    }

    private String selectedDepartment() {
        return binding.departmentSpinner.getText().toString().trim();
    }

    private String selectedSemesterLabel() {
        return binding.semesterSpinner.getText().toString().trim();
    }

    private AcademicDataProvider.SubjectItem selectedSubjectItem() {
        String selected = binding.subjectSpinner.getText().toString().trim();
        for (AcademicDataProvider.SubjectItem item : currentSubjects) {
            if (item.toString().equals(selected)) {
                return item;
            }
        }
        return currentSubjects.isEmpty()
                ? new AcademicDataProvider.SubjectItem("GEN", "General")
                : currentSubjects.get(0);
    }

    private String teacherName() {
        String value = requireActivity().getIntent().getStringExtra(IntentConstants.EXTRA_USER_NAME);
        return value == null || value.trim().isEmpty() ? "Teacher" : value;
    }

    private PdfFileMeta readPdfMeta(Uri uri) {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        String name = "notes.pdf";
        long size = 0L;
        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) {
                        name = cursor.getString(nameIndex);
                    }
                    if (sizeIndex >= 0) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return new PdfFileMeta(name, size);
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8 * 1024];
        int read;
        while ((read = inputStream.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return buffer.toByteArray();
    }

    private static class PdfFileMeta {
        private final String displayName;
        private final long sizeBytes;

        private PdfFileMeta(String displayName, long sizeBytes) {
            this.displayName = displayName;
            this.sizeBytes = sizeBytes;
        }
    }
}
