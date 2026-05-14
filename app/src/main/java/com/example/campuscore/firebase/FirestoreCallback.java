package com.example.campuscore.firebase;

public interface FirestoreCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
