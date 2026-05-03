package com.example.campuscoret;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminUserProvisioningActivity extends AppCompatActivity {
    private Spinner roleSpinner;
    private Spinner classSpinner;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_provisioning);

        EditText nameInput = findViewById(R.id.admin_user_name_input);
        EditText emailInput = findViewById(R.id.admin_user_email_input);
        EditText passwordInput = findViewById(R.id.admin_user_password_input);
        roleSpinner = findViewById(R.id.admin_user_role_spinner);
        classSpinner = findViewById(R.id.admin_user_class_spinner);
        createButton = findViewById(R.id.admin_user_create_button);

        SpinnerUtils.bindSpinner(roleSpinner, R.array.admin_manage_user_roles);
        SpinnerUtils.bindDynamicSpinner(classSpinner, MetadataRepository.getClasses(), getString(R.string.session_class_placeholder));

        roleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateClassVisibility();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                updateClassVisibility();
            }
        });
        updateClassVisibility();

        createButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();
            String className = "";

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

            if (getString(R.string.admin_manage_user_role_placeholder).equals(role)) {
                Toast.makeText(this, R.string.admin_manage_user_role_error, Toast.LENGTH_SHORT).show();
                return;
            }

            if (getString(R.string.role_student).equals(role)) {
                className = classSpinner.getSelectedItem().toString();
                if (getString(R.string.session_class_placeholder).equals(className)) {
                    Toast.makeText(this, R.string.signup_class_error, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            createButton.setEnabled(false);
            AppAuthCoordinator.AuthCallback callback = outcome -> {
                createButton.setEnabled(true);
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

                Toast.makeText(
                        this,
                        getString(R.string.admin_manage_user_success, role, email),
                        Toast.LENGTH_SHORT
                ).show();
                nameInput.setText("");
                emailInput.setText("");
                passwordInput.setText("");
                roleSpinner.setSelection(0);
                classSpinner.setSelection(0);
                updateClassVisibility();
            };

            AppAuthCoordinator.createManagedAccount(
                    this,
                    name,
                    email,
                    password,
                    role,
                    className,
                    callback
            );
        });
    }



    private void updateClassVisibility() {
        String role = roleSpinner.getSelectedItem() == null ? "" : roleSpinner.getSelectedItem().toString();
        boolean isStudent = getString(R.string.role_student).equals(role);
        classSpinner.setVisibility(isStudent ? View.VISIBLE : View.GONE);
        if (!isStudent) {
            classSpinner.setSelection(0);
        }
    }
}
