package com.example.campuscoret;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText emailInput = findViewById(R.id.email_input);
        EditText passwordInput = findViewById(R.id.password_input);
        RadioGroup roleGroup = findViewById(R.id.role_group);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            int selectedRoleId = roleGroup.getCheckedRadioButtonId();
            String selectedRole;

            if (selectedRoleId == R.id.role_student) {
                selectedRole = getString(R.string.role_student);
            } else if (selectedRoleId == R.id.role_teacher) {
                selectedRole = getString(R.string.role_teacher);
            } else if (selectedRoleId == R.id.role_admin) {
                selectedRole = getString(R.string.role_admin);
            } else {
                Toast.makeText(this, R.string.select_role_error, Toast.LENGTH_SHORT).show();
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

            Toast.makeText(
                    this,
                    getString(R.string.login_success_message, selectedRole, email),
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent;
            if (selectedRoleId == R.id.role_student) {
                intent = new Intent(this, StudentDashboardActivity.class);
            } else if (selectedRoleId == R.id.role_teacher) {
                intent = new Intent(this, TeacherDashboardActivity.class);
            } else {
                intent = new Intent(this, AdminDashboardActivity.class);
            }

            intent.putExtra("user_email", email);
            intent.putExtra("user_role", selectedRole);
            startActivity(intent);
        });
    }
}
