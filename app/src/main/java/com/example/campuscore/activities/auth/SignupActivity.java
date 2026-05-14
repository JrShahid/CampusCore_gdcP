package com.example.campuscore.activities.auth;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuscore.R;
import com.example.campuscore.databinding.ActivitySignupBinding;
import com.example.campuscore.firebase.FirebaseUserRepository;
import com.example.campuscore.firebase.FirestoreCallback;
import com.example.campuscore.models.UserModel;
import com.example.campuscore.utils.NavigationUtils;
import com.example.campuscore.utils.NetworkUtils;
import com.example.campuscore.utils.SnackbarUtils;
import com.example.campuscore.utils.ValidationUtils;

public class SignupActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private FirebaseUserRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new FirebaseUserRepository();

        binding.signupButton.setOnClickListener(view -> signup());
        binding.loginText.setOnClickListener(view -> finish());
    }

    private void signup() {
        String name = text(binding.nameInput.getText());
        String email = text(binding.emailInput.getText());
        String password = text(binding.passwordInput.getText());
        String department = text(binding.departmentInput.getText());
        String semester = text(binding.semesterInput.getText());
        clearErrors();

        if (!validate(name, email, password, department, semester) || !checkNetwork()) {
            return;
        }

        setLoading(true);
        repository.signup(name, email, password, department, semester, new FirestoreCallback<UserModel>() {
            @Override
            public void onSuccess(UserModel data) {
                setLoading(false);
                NavigationUtils.openDashboard(SignupActivity.this, data);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                SnackbarUtils.show(binding.rootLayout, message);
            }
        });
    }

    private boolean validate(String name, String email, String password, String department, String semester) {
        boolean valid = true;
        if (ValidationUtils.isBlank(name)) {
            binding.nameLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.emailLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.passwordLayout.setError(getString(R.string.error_password_length));
            valid = false;
        }
        if (ValidationUtils.isBlank(department)) {
            binding.departmentLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        if (ValidationUtils.isBlank(semester)) {
            binding.semesterLayout.setError(getString(R.string.error_required));
            valid = false;
        }
        return valid;
    }

    private boolean checkNetwork() {
        if (!NetworkUtils.isOnline(this)) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.error_no_internet));
            return false;
        }
        return true;
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.signupButton.setEnabled(!loading);
    }

    private void clearErrors() {
        binding.nameLayout.setError(null);
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.departmentLayout.setError(null);
        binding.semesterLayout.setError(null);
    }

    private String text(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
