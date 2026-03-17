package com.example.digitaltokengenerationapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

public class UserLoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    MaterialButton loginBtn, backBtn;
    TextView registerText, loginTitle;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Initialize UI elements
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        backBtn = findViewById(R.id.backBtn);
        registerText = findViewById(R.id.registerText);
        loginTitle = findViewById(R.id.loginTitle);

        auth = FirebaseAuth.getInstance();

        // Handle login click
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, PlaceSelectionActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this,
                                    "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Back button
        backBtn.setOnClickListener(v -> finish());

        // Register text click
        registerText.setOnClickListener(v -> startActivity(new Intent(this, UserRegisterActivity.class)));
    }
}
