package com.example.campuscoret;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AppAuthCoordinator {
    private AppAuthCoordinator() {
    }

    public static void register(
            Activity activity,
            String name,
            String email,
            String password,
            String className,
            AuthCallback callback
    ) {
        String role = "Student";
        if (!isFirebaseAvailable(activity)) {
            UserAccountRepository.RegistrationResult result = UserAccountRepository.registerAccount(
                    activity,
                    name,
                    email,
                    password,
                    role,
                    className
            );
            if (result.getStatus() == UserAccountRepository.RegistrationResult.Status.DUPLICATE) {
                callback.onResult(AuthOutcome.duplicate());
                return;
            }
            callback.onResult(AuthOutcome.success(result.getAccount(), false));
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                        callback.onResult(mapFirebaseFailure(task.getException()));
                        return;
                    }

                    String uid = auth.getCurrentUser().getUid();
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("email", email.trim().toLowerCase());
                    profile.put("role", role);
                    profile.put("className", className == null ? "" : className);
                    profile.put("createdAt", System.currentTimeMillis());

                    firestore.collection("users")
                            .document(uid)
                            .set(profile)
                            .addOnSuccessListener(unused -> {
                                auth.signOut();
                                UserAccount account = new UserAccount(name, email.trim().toLowerCase(), password, role, className);
                                if ("Student".equals(role)) {
                                    StudentDirectoryRepository.upsertProfile(
                                            new StudentProfile(name, account.getEmail(), className)
                                    );
                                }
                                callback.onResult(AuthOutcome.success(account, true));
                            })
                            .addOnFailureListener(exception -> {
                                if (auth.getCurrentUser() != null) {
                                    auth.getCurrentUser().delete();
                                }
                                callback.onResult(AuthOutcome.remoteError(exception.getMessage()));
                            });
                });
    }

    public static void login(
            Activity activity,
            String email,
            String password,
            AuthCallback callback
    ) {
        if (!isFirebaseAvailable(activity)) {
            UserAccountRepository.AuthResult result =
                    UserAccountRepository.authenticate(activity, email, password);
            callback.onResult(mapLocalAuthResult(result));
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (!task.isSuccessful() || auth.getCurrentUser() == null) {
                        callback.onResult(mapFirebaseLoginFailure(task.getException()));
                        return;
                    }

                    String uid = auth.getCurrentUser().getUid();
                    firestore.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                AuthOutcome outcome = mapProfileSnapshot(snapshot, password);
                                if (outcome.getStatus() == AuthOutcome.Status.SUCCESS) {
                                    callback.onResult(outcome);
                                } else {
                                    auth.signOut();
                                    callback.onResult(outcome);
                                }
                            })
                            .addOnFailureListener(exception -> {
                                auth.signOut();
                                callback.onResult(AuthOutcome.remoteError(exception.getMessage()));
                            });
                });
    }

    public static void createManagedAccount(
            Activity activity,
            String name,
            String email,
            String password,
            String role,
            String className,
            AuthCallback callback
    ) {
        if (!isFirebaseAvailable(activity)) {
            UserAccountRepository.RegistrationResult result = UserAccountRepository.registerAccount(
                    activity,
                    name,
                    email,
                    password,
                    role,
                    className
            );
            if (result.getStatus() == UserAccountRepository.RegistrationResult.Status.DUPLICATE) {
                callback.onResult(AuthOutcome.duplicate());
                return;
            }
            callback.onResult(AuthOutcome.success(result.getAccount(), false));
            return;
        }

        FirebaseApp defaultApp = FirebaseApp.getInstance();
        FirebaseOptions options = defaultApp.getOptions();
        String secondaryAppName = "managed-auth-" + System.currentTimeMillis();
        FirebaseApp secondaryApp = FirebaseApp.initializeApp(activity, options, secondaryAppName);
        if (secondaryApp == null) {
            callback.onResult(AuthOutcome.remoteError("Could not initialize a secondary Firebase Auth instance."));
            return;
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String normalizedEmail = email.trim().toLowerCase();
        secondaryAuth.createUserWithEmailAndPassword(normalizedEmail, password)
                .addOnCompleteListener(activity, task -> {
                    if (!task.isSuccessful() || secondaryAuth.getCurrentUser() == null) {
                        cleanupSecondaryApp(secondaryAuth, secondaryApp);
                        callback.onResult(mapFirebaseFailure(task.getException()));
                        return;
                    }

                    String uid = secondaryAuth.getCurrentUser().getUid();
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("name", name);
                    profile.put("email", normalizedEmail);
                    profile.put("role", role);
                    profile.put("className", className == null ? "" : className);
                    profile.put("createdAt", System.currentTimeMillis());

                    firestore.collection("users")
                            .document(uid)
                            .set(profile)
                            .addOnSuccessListener(unused -> {
                                UserAccount account = new UserAccount(name, normalizedEmail, password, role, className);
                                if ("Student".equals(role)) {
                                    StudentDirectoryRepository.upsertProfile(
                                            new StudentProfile(name, normalizedEmail, className)
                                    );
                                }
                                cleanupSecondaryApp(secondaryAuth, secondaryApp);
                                callback.onResult(AuthOutcome.success(account, true));
                            })
                            .addOnFailureListener(exception -> {
                                if (secondaryAuth.getCurrentUser() != null) {
                                    secondaryAuth.getCurrentUser().delete()
                                            .addOnCompleteListener(deleteTask -> {
                                                cleanupSecondaryApp(secondaryAuth, secondaryApp);
                                                callback.onResult(AuthOutcome.remoteError(exception.getMessage()));
                                            });
                                } else {
                                    cleanupSecondaryApp(secondaryAuth, secondaryApp);
                                    callback.onResult(AuthOutcome.remoteError(exception.getMessage()));
                                }
                            });
                });
    }

    private static AuthOutcome mapProfileSnapshot(DocumentSnapshot snapshot, String password) {
        if (!snapshot.exists()) {
            return AuthOutcome.notFound();
        }

        String name = snapshot.getString("name");
        String email = snapshot.getString("email");
        String role = snapshot.getString("role");
        String className = snapshot.getString("className");
        if (role == null || email == null) {
            return AuthOutcome.remoteError("Account profile is incomplete in Firestore.");
        }

        UserAccount account = new UserAccount(
                name == null ? email : name,
                email,
                password,
                role,
                className == null ? "" : className
        );
        if ("Student".equals(role)) {
            StudentDirectoryRepository.upsertProfile(
                    new StudentProfile(account.getName(), account.getEmail(), account.getClassName())
            );
        }
        return AuthOutcome.success(account, true);
    }

    private static AuthOutcome mapLocalAuthResult(UserAccountRepository.AuthResult result) {
        if (result.getStatus() == UserAccountRepository.AuthResult.Status.SUCCESS) {
            return AuthOutcome.success(result.getAccount(), false);
        }
        if (result.getStatus() == UserAccountRepository.AuthResult.Status.NOT_FOUND) {
            return AuthOutcome.notFound();
        }
        if (result.getStatus() == UserAccountRepository.AuthResult.Status.INVALID_PASSWORD) {
            return AuthOutcome.invalidPassword();
        }
        return AuthOutcome.remoteError("Account profile is incomplete.");
    }

    private static AuthOutcome mapFirebaseFailure(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return AuthOutcome.duplicate();
        }
        String message = exception == null ? "" : exception.getMessage();
        return AuthOutcome.remoteError(message);
    }

    private static AuthOutcome mapFirebaseLoginFailure(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return AuthOutcome.invalidPassword();
        }
        if (exception instanceof FirebaseAuthInvalidUserException) {
            return AuthOutcome.notFound();
        }
        String message = exception == null ? "" : exception.getMessage();
        return AuthOutcome.remoteError(message);
    }

    private static boolean isFirebaseAvailable(Context context) {
        List<FirebaseApp> apps = FirebaseApp.getApps(context);
        if (!apps.isEmpty()) {
            return true;
        }
        return FirebaseApp.initializeApp(context) != null;
    }

    private static void cleanupSecondaryApp(FirebaseAuth secondaryAuth, FirebaseApp secondaryApp) {
        try {
            secondaryAuth.signOut();
        } catch (Exception ignored) {
        }
        try {
            secondaryApp.delete();
        } catch (Exception ignored) {
        }
    }

    public interface AuthCallback {
        void onResult(AuthOutcome outcome);
    }

    public static final class AuthOutcome {
        public enum Status {
            SUCCESS,
            DUPLICATE,
            NOT_FOUND,
            INVALID_PASSWORD,
            REMOTE_ERROR
        }

        private final Status status;
        private final UserAccount account;
        private final boolean firebaseBacked;
        private final String message;

        private AuthOutcome(Status status, UserAccount account, boolean firebaseBacked, String message) {
            this.status = status;
            this.account = account;
            this.firebaseBacked = firebaseBacked;
            this.message = message;
        }

        public static AuthOutcome success(UserAccount account, boolean firebaseBacked) {
            return new AuthOutcome(Status.SUCCESS, account, firebaseBacked, "");
        }

        public static AuthOutcome duplicate() {
            return new AuthOutcome(Status.DUPLICATE, null, false, "");
        }

        public static AuthOutcome notFound() {
            return new AuthOutcome(Status.NOT_FOUND, null, false, "");
        }

        public static AuthOutcome invalidPassword() {
            return new AuthOutcome(Status.INVALID_PASSWORD, null, false, "");
        }

        public static AuthOutcome remoteError(String message) {
            return new AuthOutcome(Status.REMOTE_ERROR, null, true, message == null ? "" : message);
        }

        public Status getStatus() {
            return status;
        }

        public UserAccount getAccount() {
            return account;
        }

        public boolean isFirebaseBacked() {
            return firebaseBacked;
        }

        public String getMessage() {
            return message;
        }
    }
}
