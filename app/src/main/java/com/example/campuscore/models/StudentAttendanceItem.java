package com.example.campuscore.models;

public class StudentAttendanceItem {
    private final UserModel user;
    private boolean present;

    public StudentAttendanceItem(UserModel user, boolean present) {
        this.user = user;
        this.present = present;
    }

    public UserModel getUser() {
        return user;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
