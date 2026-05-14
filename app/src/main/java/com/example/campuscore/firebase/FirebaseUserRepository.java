package com.example.campuscore.firebase;

import com.example.campuscore.models.UserModel;
import com.example.campuscore.utils.AppRoles;
import com.example.campuscore.utils.FirestoreCollections;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class FirebaseUserRepository {
    public static final String ROLE_ADMIN = AppRoles.ADMIN;
    public static final String ROLE_TEACHER = AppRoles.TEACHER;
    public static final String ROLE_STUDENT = AppRoles.STUDENT;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public FirebaseUserRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void login(String email, String password, FirestoreCallback<UserModel> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> ensureUserProfile(callback))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void signup(String name, String email, String password, String department, String semester,
                       FirestoreCallback<UserModel> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError("Unable to create account. Please try again.");
                        return;
                    }

                    UserModel user = new UserModel(
                            firebaseUser.getUid(),
                            name,
                            email,
                            ROLE_STUDENT,
                            department,
                            semester,
                            ""
                    );

                    firestore.collection(FirestoreCollections.USERS)
                            .document(firebaseUser.getUid())
                            .set(user)
                            .addOnSuccessListener(unused -> callback.onSuccess(user))
                            .addOnFailureListener(error -> callback.onError(readableError(error)));
                })
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void sendPasswordReset(String email, FirestoreCallback<Void> callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void fetchCurrentUser(FirestoreCallback<UserModel> callback) {
        ensureUserProfile(callback);
    }

    private void ensureUserProfile(FirestoreCallback<UserModel> callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Session expired. Please login again.");
            return;
        }

        firestore.collection(FirestoreCollections.USERS)
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    UserModel user = snapshot.toObject(UserModel.class);
                    if (user == null) {
                        createMissingProfile(firebaseUser, callback);
                        return;
                    }
                    callback.onSuccess(user);
                })
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    private void createMissingProfile(FirebaseUser firebaseUser, FirestoreCallback<UserModel> callback) {
        String email = firebaseUser.getEmail() == null ? "" : firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName();
        if (name == null || name.trim().isEmpty()) {
            int atIndex = email.indexOf("@");
            name = atIndex > 0 ? email.substring(0, atIndex) : "Student";
        }

        UserModel fallbackUser = new UserModel(
                firebaseUser.getUid(),
                name,
                email,
                ROLE_STUDENT,
                "",
                "",
                ""
        );

        firestore.collection(FirestoreCollections.USERS)
                .document(firebaseUser.getUid())
                .set(fallbackUser)
                .addOnSuccessListener(unused -> callback.onSuccess(fallbackUser))
                .addOnFailureListener(error -> callback.onError(readableError(error)));
    }

    public void logout() {
        auth.signOut();
    }

    private String readableError(Exception error) {
        if (error instanceof FirebaseAuthInvalidUserException) {
            return "No account found for this email. Please sign up first.";
        }
        if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return "Incorrect email or password.";
        }
        if (error instanceof FirebaseAuthUserCollisionException) {
            return "This email is already registered. Please login instead.";
        }
        if (error instanceof FirebaseNetworkException) {
            return "Network error while contacting Firebase. Please check your internet connection.";
        }
        if (error instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) error;
            if (firestoreException.getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                return "Firestore access was denied. Publish the CampusCore Firestore rules in Firebase Console.";
            }
        }

        String message = error.getMessage();
        if (message != null && message.contains("CONFIGURATION_NOT_FOUND")) {
            return "Firebase configuration was not found for this app. Please verify the Firebase project setup.";
        }
        if (message != null && message.contains("PASSWORD_LOGIN_DISABLED")) {
            return "Email/Password sign-in is disabled in Firebase Authentication. Enable it in the Firebase Console.";
        }
        return message == null || message.trim().isEmpty()
                ? "Firebase request failed. Please try again."
                : message;
    }
}
