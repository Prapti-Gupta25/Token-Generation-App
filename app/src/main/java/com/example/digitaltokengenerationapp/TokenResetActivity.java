package com.example.digitaltokengenerationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TokenResetActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_reset);
        Button resetButton = findViewById(R.id.resetTokenBtn);


        // Get place name from intent
        String selectedPlace = getIntent().getStringExtra("placeName");
        if (selectedPlace == null) selectedPlace = "None";

        // Firebase reference
        databaseRef = FirebaseDatabase.getInstance()
                .getReference("places")
                .child(selectedPlace);

        String finalSelectedPlace = selectedPlace;
        resetButton.setOnClickListener(v -> {
            int baseToken = getBaseToken(finalSelectedPlace);

            // Step 1: Reset tokens according to the place range
            databaseRef.child("currentToken").setValue(baseToken);
            databaseRef.child("nextToken").setValue(baseToken + 1);

            databaseRef.child("current");

            // Step 2: Clear requests
            databaseRef.child("requests").removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this,
                            finalSelectedPlace + " tokens reset to " + baseToken,
                            Toast.LENGTH_SHORT).show();

                    // ✅ Step 3: Go back to Admin Dashboard after reset
                    Intent intent = new Intent(TokenResetActivity.this, AdminDashboardActivity.class);
                    intent.putExtra("placeName", finalSelectedPlace);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // close this activity
                } else {
                    Toast.makeText(this,
                            "Failed to reset: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            });
        });

    }

    // ✅ Base token ranges for each place
    private int getBaseToken(String placeName) {
        switch (placeName) {
            case "RTO Office":
                return 100;
            case "K_K_Wagh_Canteen":
                return 200;
            case "Trimbakeshwar Temple":
                return 300;
            case "Cafe":
                return 400;
            case "Sahyadri Hospital":
                return 500;
            default:
                return 0; // fallback if not found
        }
    }
}
