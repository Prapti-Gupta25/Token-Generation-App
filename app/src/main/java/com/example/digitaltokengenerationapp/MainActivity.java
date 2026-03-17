package com.example.digitaltokengenerationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    MaterialButton userBtn, adminBtn;
    TextView appTitle, tagline;
    LinearLayout rootLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);
        appTitle = findViewById(R.id.appTitle);
        tagline = findViewById(R.id.tagline);
        userBtn = findViewById(R.id.userBtn);
        adminBtn = findViewById(R.id.adminBtn);
        adminBtn = findViewById((R.id.adminBtn));

        // Hide all initially (for splash animation)
        tagline.setAlpha(0f);
        userBtn.setAlpha(0f);
        adminBtn.setAlpha(0f);

        // Start splash-style animation for app title
        startSplashAnimation();

        // Add button press scaling
        addButtonTouchAnimation(userBtn);
        addButtonTouchAnimation(adminBtn);

        // Navigation
        userBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(intent);
        });

        adminBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }

    private void startSplashAnimation() {
        // Reset title properties
        appTitle.setAlpha(0f);
        appTitle.setScaleX(0.8f);
        appTitle.setScaleY(0.8f);

        // Fade + scale up the app title
        appTitle.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(900)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Delay other elements until title finishes
        new Handler().postDelayed(() -> {
            tagline.setTranslationY(40f);
            tagline.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            userBtn.setTranslationY(60f);
            userBtn.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(200)
                    .setDuration(700)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            adminBtn.setTranslationY(70f);
            adminBtn.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(400)
                    .setDuration(700)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

        }, 900); // starts after title finishes
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addButtonTouchAnimation(MaterialButton button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    break;
            }
            return false;
        });
    }
}
