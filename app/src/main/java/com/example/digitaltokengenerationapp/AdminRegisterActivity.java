package com.example.digitaltokengenerationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminRegisterActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passwordInput;
    MaterialButton registerBtn;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create account in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Save admin info in Firestore
                            String userId = auth.getCurrentUser().getUid();
                            Map<String, Object> adminData = new HashMap<>();
                            adminData.put("name", name);
                            adminData.put("email", email);
                            adminData.put("role", "admin");

                            firestore.collection("admins").document(userId)
                                    .set(adminData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Admin registered successfully 🎉", Toast.LENGTH_SHORT).show();

                                        // ✅ Sign out after successful registration (to force login flow)
                                        FirebaseAuth.getInstance().signOut();

                                        // Go to login screen
                                        startActivity(new Intent(this, AdminLoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save admin data: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        } else {
                            // ✅ Improved error handling
                            Exception e = task.getException();
                            if (e != null && e.getMessage() != null) {
                                if (e.getMessage().contains("The email address is already in use")) {
                                    Toast.makeText(this, "This email is already registered. Try logging in.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        });
    }
}
