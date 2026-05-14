package com.example.campuscore.activities.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.example.campuscore.R;
import com.example.campuscore.databinding.ActivityAdminDashboardBinding;
import com.example.campuscore.firebase.FirebaseUserRepository;
import com.example.campuscore.fragments.AttendanceFragment;
import com.example.campuscore.fragments.HomeFragment;
import com.example.campuscore.fragments.NotesFragment;
import com.example.campuscore.fragments.ProfileFragment;
import com.example.campuscore.fragments.QuizFragment;
import com.example.campuscore.utils.IntentConstants;
import com.example.campuscore.utils.NavigationUtils;

public class AdminDashboardActivity extends AppCompatActivity {
    private ActivityAdminDashboardBinding binding;
    private FirebaseUserRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new FirebaseUserRepository();

        binding.toolbar.setNavigationOnClickListener(view -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.navigationView.setCheckedItem(R.id.nav_home);
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                repository.logout();
                NavigationUtils.openLoginAndClear(this);
                return true;
            }

            Fragment fragment;
            if (id == R.id.nav_attendance) {
                fragment = AttendanceFragment.create();
            } else if (id == R.id.nav_notes) {
                fragment = NotesFragment.create();
            } else if (id == R.id.nav_quiz) {
                fragment = QuizFragment.create();
            } else if (id == R.id.nav_profile) {
                fragment = ProfileFragment.create(userName(), userEmail(), FirebaseUserRepository.ROLE_ADMIN);
            } else {
                fragment = HomeFragment.newInstance(FirebaseUserRepository.ROLE_ADMIN);
            }
            show(fragment);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        if (savedInstanceState == null) {
            show(HomeFragment.newInstance(FirebaseUserRepository.ROLE_ADMIN));
        }
    }

    private void show(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private String userName() {
        return getIntent().getStringExtra(IntentConstants.EXTRA_USER_NAME) == null
                ? "Admin"
                : getIntent().getStringExtra(IntentConstants.EXTRA_USER_NAME);
    }

    private String userEmail() {
        return getIntent().getStringExtra(IntentConstants.EXTRA_USER_EMAIL) == null
                ? ""
                : getIntent().getStringExtra(IntentConstants.EXTRA_USER_EMAIL);
    }
}
