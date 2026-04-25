package com.example.campuscoret;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UserAccountRepository {
    private static final String PREFS_NAME = "campuscoret_accounts";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String FIELD_SEPARATOR = "\u001f";
    private static final String RECORD_SEPARATOR = "\u001e";

    private UserAccountRepository() {
    }

    public static synchronized RegistrationResult registerAccount(
            Context context,
            String name,
            String email,
            String password,
            String role,
            String className
    ) {
        Map<String, UserAccount> accounts = loadAccounts(context);
        String normalizedEmail = normalizeEmail(email);
        if (accounts.containsKey(normalizedEmail)) {
            return RegistrationResult.duplicate();
        }

        UserAccount account = new UserAccount(name, normalizedEmail, password, role, className);
        accounts.put(normalizedEmail, account);
        persistAccounts(context, accounts);

        if ("Student".equals(role)) {
            StudentDirectoryRepository.upsertProfile(new StudentProfile(name, normalizedEmail, className));
        }
        return RegistrationResult.success(account);
    }

    public static synchronized AuthResult authenticate(
            Context context,
            String email,
            String password
    ) {
        Map<String, UserAccount> accounts = loadAccounts(context);
        UserAccount account = accounts.get(normalizeEmail(email));
        if (account == null) {
            return AuthResult.notFound();
        }
        if (!account.getPassword().equals(password)) {
            return AuthResult.invalidPassword();
        }

        if ("Student".equals(account.getRole())) {
            StudentDirectoryRepository.upsertProfile(
                    new StudentProfile(account.getName(), account.getEmail(), account.getClassName())
            );
        }
        return AuthResult.success(account);
    }

    public static synchronized List<UserAccount> getAccounts(Context context) {
        return new ArrayList<>(loadAccounts(context).values());
    }

    private static Map<String, UserAccount> loadAccounts(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String stored = preferences.getString(KEY_ACCOUNTS, "");
        Map<String, UserAccount> accounts = new LinkedHashMap<>();

        if (stored == null || stored.isEmpty()) {
            for (UserAccount account : buildDefaultAccounts()) {
                accounts.put(normalizeEmail(account.getEmail()), account);
            }
            persistAccounts(context, accounts);
            return accounts;
        }

        String[] records = stored.split(RECORD_SEPARATOR);
        for (String record : records) {
            if (record.isEmpty()) {
                continue;
            }
            String[] parts = record.split(FIELD_SEPARATOR, -1);
            if (parts.length < 5) {
                continue;
            }
            UserAccount account = new UserAccount(parts[0], parts[1], parts[2], parts[3], parts[4]);
            accounts.put(normalizeEmail(account.getEmail()), account);
            if ("Student".equals(account.getRole())) {
                StudentDirectoryRepository.upsertProfile(
                        new StudentProfile(account.getName(), account.getEmail(), account.getClassName())
                );
            }
        }
        return accounts;
    }

    private static void persistAccounts(Context context, Map<String, UserAccount> accounts) {
        StringBuilder builder = new StringBuilder();
        for (UserAccount account : accounts.values()) {
            builder.append(account.getName()).append(FIELD_SEPARATOR)
                    .append(normalizeEmail(account.getEmail())).append(FIELD_SEPARATOR)
                    .append(account.getPassword()).append(FIELD_SEPARATOR)
                    .append(account.getRole()).append(FIELD_SEPARATOR)
                    .append(account.getClassName() == null ? "" : account.getClassName())
                    .append(RECORD_SEPARATOR);
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACCOUNTS, builder.toString())
                .apply();
    }

    private static List<UserAccount> buildDefaultAccounts() {
        List<UserAccount> accounts = new ArrayList<>();
        accounts.add(new UserAccount("Campus Admin", "admin@campus.edu", "campus123", "Admin", ""));
        accounts.add(new UserAccount("Teacher", "teacher@campus.edu", "campus123", "Teacher", ""));
        accounts.add(new UserAccount("Aarav Sharma", "aarav@campus.edu", "campus123", "Student", "BCA 1A"));
        accounts.add(new UserAccount("Diya Patel", "diya@campus.edu", "campus123", "Student", "BCA 2B"));
        accounts.add(new UserAccount("Rohan Verma", "rohan@campus.edu", "campus123", "Student", "BCA 2B"));
        accounts.add(new UserAccount("Meera Nair", "meera@campus.edu", "campus123", "Student", "MCA 1C"));
        accounts.add(new UserAccount("Kabir Singh", "kabir@campus.edu", "campus123", "Student", "BSc CS 3A"));
        accounts.add(new UserAccount("Sana Khan", "sana@campus.edu", "campus123", "Student", "BCA 1A"));
        return accounts;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public static final class RegistrationResult {
        public enum Status {
            SUCCESS,
            DUPLICATE
        }

        private final Status status;
        private final UserAccount account;

        private RegistrationResult(Status status, UserAccount account) {
            this.status = status;
            this.account = account;
        }

        public static RegistrationResult success(UserAccount account) {
            return new RegistrationResult(Status.SUCCESS, account);
        }

        public static RegistrationResult duplicate() {
            return new RegistrationResult(Status.DUPLICATE, null);
        }

        public Status getStatus() {
            return status;
        }

        public UserAccount getAccount() {
            return account;
        }
    }

    public static final class AuthResult {
        public enum Status {
            SUCCESS,
            NOT_FOUND,
            INVALID_PASSWORD
        }

        private final Status status;
        private final UserAccount account;

        private AuthResult(Status status, UserAccount account) {
            this.status = status;
            this.account = account;
        }

        public static AuthResult success(UserAccount account) {
            return new AuthResult(Status.SUCCESS, account);
        }

        public static AuthResult notFound() {
            return new AuthResult(Status.NOT_FOUND, null);
        }

        public static AuthResult invalidPassword() {
            return new AuthResult(Status.INVALID_PASSWORD, null);
        }

        public Status getStatus() {
            return status;
        }

        public UserAccount getAccount() {
            return account;
        }
    }
}
