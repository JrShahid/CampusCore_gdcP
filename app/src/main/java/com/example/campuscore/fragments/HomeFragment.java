package com.example.campuscore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.campuscore.R;
import com.example.campuscore.adapters.FeatureCardAdapter;
import com.example.campuscore.databinding.FragmentHomeBinding;
import com.example.campuscore.firebase.FirebaseUserRepository;
import com.example.campuscore.models.FeatureCard;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String ARG_ROLE = "role";
    private FragmentHomeBinding binding;

    public static HomeFragment newInstance(String role) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String role = getArguments() == null
                ? FirebaseUserRepository.ROLE_STUDENT
                : getArguments().getString(ARG_ROLE, FirebaseUserRepository.ROLE_STUDENT);
        List<FeatureCard> cards = cardsFor(role);

        binding.headerText.setText(titleFor(role));
        binding.subtitleText.setText("Role-based tools for your campus workflow.");
        binding.featureRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.featureRecyclerView.setAdapter(new FeatureCardAdapter(cards));
        binding.emptyText.setVisibility(cards.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String titleFor(String role) {
        if (FirebaseUserRepository.ROLE_ADMIN.equals(role)) {
            return getString(R.string.admin_dashboard);
        }
        if (FirebaseUserRepository.ROLE_TEACHER.equals(role)) {
            return getString(R.string.teacher_dashboard);
        }
        return getString(R.string.student_dashboard);
    }

    private List<FeatureCard> cardsFor(String role) {
        List<FeatureCard> cards = new ArrayList<>();
        int icon = R.drawable.ic_feature;

        if (FirebaseUserRepository.ROLE_ADMIN.equals(role)) {
            cards.add(new FeatureCard(icon, "Manage Users", "Review and organize campus accounts.", false));
            cards.add(new FeatureCard(icon, "Announcements", "Prepare official campus updates.", false));
            cards.add(new FeatureCard(icon, "Reports", "Institution reports and summaries.", true));
            cards.add(new FeatureCard(icon, "System Analytics", "Campus-wide insights and usage patterns.", true));
            return cards;
        }

        if (FirebaseUserRepository.ROLE_TEACHER.equals(role)) {
            cards.add(new FeatureCard(icon, "Mark Attendance", "Capture and review class attendance.", false));
            cards.add(new FeatureCard(icon, "Upload Notes", "Share study material with students.", false));
            cards.add(new FeatureCard(icon, "Create Quiz", "Prepare quick assessments.", false));
            cards.add(new FeatureCard(icon, "Analytics", "Performance trends and engagement.", true));
            cards.add(new FeatureCard(icon, "Student Reports", "Individual academic reports.", true));
            return cards;
        }

        cards.add(new FeatureCard(icon, "Attendance", "Track your class attendance status.", false));
        cards.add(new FeatureCard(icon, "Notes", "Access shared academic notes.", false));
        cards.add(new FeatureCard(icon, "Quiz", "View upcoming and active quizzes.", false));
        cards.add(new FeatureCard(icon, "Assignments", "Submission tracking and deadlines.", true));
        cards.add(new FeatureCard(icon, "AI Assistant", "Personal academic assistance.", true));
        cards.add(new FeatureCard(icon, "Placements", "Career drives and placement updates.", true));
        return cards;
    }
}
