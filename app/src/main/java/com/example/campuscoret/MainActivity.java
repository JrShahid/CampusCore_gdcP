package com.example.campuscoret;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.login_button);
        TextView createAccountText = findViewById(R.id.create_account_text);

        prefillFromSignup(emailInput);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError(getString(R.string.invalid_email_error));
                emailInput.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                passwordInput.setError(getString(R.string.invalid_password_error));
                passwordInput.requestFocus();
                return;
            }

            AppAuthCoordinator.login(this, email, password, outcome -> {
                if (outcome.getStatus() == AppAuthCoordinator.AuthOutcome.Status.NOT_FOUND) {
                    Toast.makeText(this, R.string.login_account_not_found_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (outcome.getStatus() == AppAuthCoordinator.AuthOutcome.Status.INVALID_PASSWORD) {
                    passwordInput.setError(getString(R.string.login_password_mismatch_error));
                    passwordInput.requestFocus();
                    return;
                }
                if (outcome.getStatus() == AppAuthCoordinator.AuthOutcome.Status.REMOTE_ERROR) {
                    Toast.makeText(
                            this,
                            getString(R.string.firebase_error_message, outcome.getMessage()),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                UserAccount account = outcome.getAccount();
                String resolvedRole = account.getRole();
                Toast.makeText(
                        this,
                        getString(R.string.login_success_message, resolvedRole, account.getEmail()),
                        Toast.LENGTH_SHORT
                ).show();
                StudentProfile profile = StudentDirectoryRepository.findStudentByEmail(account.getEmail());
                if (profile != null) {
                    LiveMonitoringRepository.logStudentActivity(
                            profile.getStudentEmail(),
                            profile.getStudentName(),
                            profile.getClassName(),
                            "Signed in"
                    );
                }

                Intent intent;
                if (getString(R.string.role_student).equals(resolvedRole)) {
                    intent = new Intent(this, StudentDashboardActivity.class);
                } else if (getString(R.string.role_teacher).equals(resolvedRole)) {
                    intent = new Intent(this, TeacherDashboardActivity.class);
                } else {
                    intent = new Intent(this, AdminDashboardActivity.class);
                }

                intent.putExtra("user_email", account.getEmail());
                intent.putExtra("user_role", resolvedRole);
                startActivity(intent);
            });
        });

        createAccountText.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });


    }

    private void prefillFromSignup(EditText emailInput) {
        String prefillEmail = getIntent().getStringExtra("prefill_email");
        if (prefillEmail != null) {
            emailInput.setText(prefillEmail);
        }
    }
}
