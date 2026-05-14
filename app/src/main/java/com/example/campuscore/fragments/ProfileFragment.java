package com.example.campuscore.fragments;

public class ProfileFragment extends PlaceholderFragment {
    public static PlaceholderFragment create(String name, String email, String role) {
        return PlaceholderFragment.newInstance("Profile", name + "\n" + email + "\nRole: " + role);
    }
}
