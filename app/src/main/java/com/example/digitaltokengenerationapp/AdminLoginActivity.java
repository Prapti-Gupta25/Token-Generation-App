package com.example.digitaltokengenerationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    public Spinner placeSpinner;
    public EditText emailInput, passwordInput;
    public FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance();

        MaterialButton loginBtn = findViewById(R.id.loginBtn);
        MaterialButton backBtn = findViewById(R.id.backBtn);
        TextView registerText = findViewById(R.id.registerText);
        placeSpinner = findViewById(R.id.placeSpinner);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Spinner setup
        String[] places = {"RTO Office", "K.k.Wagh Canteen", "Trimbakeshwar Temple", "Cafe", "Sahyadri Hospital"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, places);
        placeSpinner.setAdapter(adapter);

        // 🌈 Stylish spinner background
        GradientDrawable spinnerBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        Color.parseColor("#9C27B0"),
                        Color.parseColor("#6A1B9A")
                });
        spinnerBg.setCornerRadius(24);
        spinnerBg.setStroke(3, Color.parseColor("#E6E6FA"));
        placeSpinner.setBackground(spinnerBg);
        placeSpinner.setPadding(22, 14, 22, 14);
        placeSpinner.setPopupBackgroundDrawable(spinnerBg);

        // 🔹 Register click
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, AdminRegisterActivity.class);
            startActivity(intent);
        });

        // 🔹 Back button click
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // 🔹 Login button click
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String selectedPlace = placeSpinner.getSelectedItem().toString();

            // Validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPlace.isEmpty()) {
                Toast.makeText(this, "Please select a place", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent multiple taps
            loginBtn.setEnabled(false);

            // Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful ✅", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                            intent.putExtra("Place Name", selectedPlace);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            finish();
                        } else {
                            loginBtn.setEnabled(true); // 🔥 important fix

                            Exception e = task.getException();
                            if (e != null && e.getMessage() != null) {
                                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}