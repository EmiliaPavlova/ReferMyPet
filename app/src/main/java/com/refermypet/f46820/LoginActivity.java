package com.refermypet.f46820;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.refermypet.f46820.dao.UserDao;
import com.refermypet.f46820.model.User;
import com.refermypet.f46820.utils.PasswordHasher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles user authentication, validating credentials against the local SQLite database.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private UserDao userDao;

    /**
     * ExecutorService used to perform database lookups off the main UI thread
     * to prevent interface freezing.
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database access object
        userDao = AppDatabase.getDatabase(this).userDao();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterLink = findViewById(R.id.tv_register_link);

        btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigation to registration screen using Intent
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Validates input and checks database for matching user credentials.
     */
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Run security-sensitive hashing and DB queries on a background thread
        executorService.execute(() -> {
            User user = userDao.findByEmail(email);

            if (user == null) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Wrong email or password.", Toast.LENGTH_LONG).show());
                return;
            }

            // Compare provided password with stored hash
            boolean passwordMatch = PasswordHasher.checkPassword(password, user.getPasswordHash());

            if (passwordMatch) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

                    // Pass the authenticated user's ID to the next activity
                    intent.putExtra("USER_ID", user.getId());

                    getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putInt("USER_ID", user.getId()).apply();

                    // Clear activity stack so user cannot go back to login via back button
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Email or password not valid.", Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the background worker when activity is destroyed
        executorService.shutdown();
    }
}