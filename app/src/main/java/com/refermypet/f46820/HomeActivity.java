package com.refermypet.f46820;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.refermypet.f46820.enums.UserType;
import com.refermypet.f46820.fragments.HotelHomeFragment;
import com.refermypet.f46820.fragments.HotelProfileFragment;
import com.refermypet.f46820.fragments.PersonHomeFragment;
import com.refermypet.f46820.viewmodel.UserViewModel;

/**
 * Main container for the application after login.
 * Acts as a host for Fragments and manages the top navigation bar.
 */
public class HomeActivity extends AppCompatActivity {
    private UserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the top navigation bar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportActionBar() != null) {
                // If there are fragments in the stack
                boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
                getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
            }
        });

        // Retrieve the logged-in user's ID passed from LoginActivity.
        int userId = getIntent().getIntExtra("USER_ID", -1);

        boolean openedFromNotification = getIntent().hasExtra("end_date");

        if (userId != -1) {
            viewModel.loadDashboardData(userId);
        }

        // Observe the user type to determine which fragment to load
        viewModel.getUserType().observe(this, type -> {
            // Check if fragment container is empty before adding a new fragment
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                if (openedFromNotification && type == UserType.PERSON) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PersonHomeFragment()) // Смени с PersonBookingsFragment ако имаш такъв
                            .commit();
                } else if (type == UserType.HOTEL) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HotelHomeFragment())
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PersonHomeFragment())
                            .commit();
                }
            }
        });

        // Start loading data only if a valid userId was provided
        if (userId != -1) {
            viewModel.loadDashboardData(userId);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_profile) {
            openProfile();
            return true;
        }

        if (itemId == R.id.action_logout) {
            logoutUser();
            return true;
        }
        if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openProfile() {
        UserType type = viewModel.getUserType().getValue();
        androidx.fragment.app.Fragment profileFragment;

        if (type == UserType.HOTEL) {
            profileFragment = new HotelProfileFragment();
        } else {
            profileFragment = new com.refermypet.f46820.fragments.PersonProfileFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Clears the current session and returns the user to the Login screen.
     * Uses Intent flags to clear the activity task stack for security.
     */
    private void logoutUser() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}