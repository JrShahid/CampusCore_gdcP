package com.example.campuscore.activities.auth;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuscore.R;
import com.example.campuscore.databinding.ActivityForgotPasswordBinding;
import com.example.campuscore.firebase.FirebaseUserRepository;
import com.example.campuscore.firebase.FirestoreCallback;
import com.example.campuscore.utils.NetworkUtils;
import com.example.campuscore.utils.SnackbarUtils;
import com.example.campuscore.utils.ValidationUtils;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseUserRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new FirebaseUserRepository();
        binding.resetButton.setOnClickListener(view -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.emailInput.getText() == null ? "" : binding.emailInput.getText().toString().trim();
        binding.emailLayout.setError(null);

        if (!ValidationUtils.isValidEmail(email)) {
            binding.emailLayout.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!NetworkUtils.isOnline(this)) {
            SnackbarUtils.show(binding.rootLayout, getString(R.string.error_no_internet));
            return;
        }

        setLoading(true);
        repository.sendPasswordReset(email, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                setLoading(false);
                SnackbarUtils.show(binding.rootLayout, getString(R.string.reset_email_sent));
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                SnackbarUtils.show(binding.rootLayout, message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.resetButton.setEnabled(!loading);
    }
}
