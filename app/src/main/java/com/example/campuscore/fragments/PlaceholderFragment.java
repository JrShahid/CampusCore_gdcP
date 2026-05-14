package com.example.campuscore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campuscore.databinding.FragmentPlaceholderBinding;

public class PlaceholderFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";

    public static PlaceholderFragment newInstance(String title, String description) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPlaceholderBinding binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        Bundle args = getArguments();
        binding.titleText.setText(args == null ? "" : args.getString(ARG_TITLE, ""));
        binding.descriptionText.setText(args == null ? "" : args.getString(ARG_DESCRIPTION, ""));
        return binding.getRoot();
    }
}
