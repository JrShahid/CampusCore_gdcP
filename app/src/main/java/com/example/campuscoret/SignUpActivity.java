package com.example.campuscoret;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText nameInput = findViewById(R.id.signup_name_input);
        EditText emailInput = findViewById(R.id.signup_email_input);
        EditText passwordInput = findViewById(R.id.signup_password_input);
        Spinner classSpinner = findViewById(R.id.signup_class_spinner);
        Button createAccountButton = findViewById(R.id.signup_create_account_button);
        Button goToLoginButton = findViewById(R.id.signup_go_to_login_button);

        SpinnerUtils.bindDynamicSpinner(classSpinner, MetadataRepository.getClasses(), getString(R.string.session_class_placeholder));

        createAccountButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError(getString(R.string.signup_name_error));
                nameInput.requestFocus();
                return;
            }

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

            String className = classSpinner.getSelectedItem().toString();
            if (getString(R.string.session_class_placeholder).equals(className)) {
                Toast.makeText(this, R.string.signup_class_error, Toast.LENGTH_SHORT).show();
                return;
            }

            AppAuthCoordinator.register(this, name, email, password, className, outcome -> {
                if (outcome.getStatus() == AppAuthCoordinator.AuthOutcome.Status.DUPLICATE) {
                    Toast.makeText(this, R.string.signup_duplicate_error, Toast.LENGTH_SHORT).show();
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

                Toast.makeText(this, R.string.signup_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("prefill_email", outcome.getAccount().getEmail());
                startActivity(intent);
                finish();
            });
        });

        goToLoginButton.setOnClickListener(v -> finish());
    }
}
