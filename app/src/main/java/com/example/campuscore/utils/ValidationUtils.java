package com.example.campuscore.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {
    private ValidationUtils() {
    }

    public static boolean isBlank(String value) {
        return TextUtils.isEmpty(value) || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isBlank(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isBlank(password) && password.length() >= 6;
    }
}
