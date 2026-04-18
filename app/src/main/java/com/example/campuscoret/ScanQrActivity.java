package com.example.campuscoret;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanQrActivity extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(this, R.string.scan_cancelled, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                handleQrPayload(result.getContents());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        TextView subtitle = findViewById(R.id.scan_qr_subtitle);
        String className = getIntent().getStringExtra("student_class");
        subtitle.setText(getString(R.string.scan_qr_subtitle, className));

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt(getString(R.string.scan_qr_prompt));
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        scanLauncher.launch(options);
    }

    private void handleQrPayload(String payload) {
        SessionQrPayload qrPayload = SessionQrHelper.parsePayload(payload);
        String studentEmail = getIntent().getStringExtra("user_email");
        String studentClass = getIntent().getStringExtra("student_class");
        SessionRecord activeSession = SessionRepository.getActiveSession();

        if (qrPayload == null) {
            showResult(getString(R.string.qr_invalid_title), getString(R.string.qr_invalid_body));
            return;
        }

        if (activeSession == null || !activeSession.isActive()) {
            showResult(getString(R.string.qr_invalid_title), getString(R.string.qr_no_active_session));
            return;
        }

        if (!activeSession.getSessionId().equals(qrPayload.getSessionId())) {
            showResult(getString(R.string.qr_invalid_title), getString(R.string.qr_session_mismatch));
            return;
        }

        if (!studentClass.equals(qrPayload.getClassName())) {
            showResult(getString(R.string.qr_invalid_title), getString(R.string.qr_class_mismatch));
            return;
        }

        boolean added = AttendanceRepository.markAttendance(
                activeSession.getSessionId(),
                deriveStudentName(studentEmail),
                studentEmail,
                "Present"
        );

        if (!added) {
            showResult(getString(R.string.qr_duplicate_title), getString(R.string.qr_duplicate_body));
            return;
        }

        showResult(
                getString(R.string.qr_success_title),
                getString(R.string.qr_success_body, activeSession.getSubjectName(), activeSession.getClassName())
        );
    }

    private void showResult(String title, String body) {
        TextView titleView = findViewById(R.id.scan_result_title);
        TextView bodyView = findViewById(R.id.scan_result_body);
        titleView.setText(title);
        bodyView.setText(body);
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
            if (word.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
            builder.append(' ');
        }
        return builder.toString().trim();
    }
}
