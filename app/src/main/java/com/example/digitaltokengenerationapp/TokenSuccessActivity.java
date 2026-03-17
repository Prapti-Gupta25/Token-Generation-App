package com.example.digitaltokengenerationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TokenSuccessActivity extends AppCompatActivity {

    TextView successTitle, successMessage;
    ImageView congratsImage;
    MaterialButton cancelTokenBtn;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_success);

        successTitle = findViewById(R.id.successTitle);
        successMessage = findViewById(R.id.successMessage);
        congratsImage = findViewById(R.id.congratsImage);
        cancelTokenBtn = findViewById(R.id.cancel_button); // ✅ Add this button in XML

        String selectedPlace = getIntent().getStringExtra("selectedPlace");
        String tokenNumber = getIntent().getStringExtra("tokenNumber");

        successTitle.setText("Congratulations 🎉");
        successMessage.setText("Your token " + tokenNumber + " for " + selectedPlace + " has been successfully generated!");

        // ✅ Animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(800);
        congratsImage.startAnimation(fadeIn);
        successTitle.startAnimation(fadeIn);
        successMessage.startAnimation(fadeIn);

        // ✅ Cancel token logic
        cancelTokenBtn.setOnClickListener(v -> {
            if (selectedPlace == null || tokenNumber == null) {
                Toast.makeText(this, "Error: Missing token info", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference placeRef = FirebaseDatabase.getInstance()
                    .getReference("places")
                    .child(selectedPlace);

            DatabaseReference requestsRef = placeRef.child("requests");

            // 🔍 Find the request with this token number
            requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean found = false;
                    for (DataSnapshot reqSnap : snapshot.getChildren()) {
                        String token = reqSnap.child("token").getValue(String.class);
                        if (token != null && token.equals(tokenNumber)) {
                            reqSnap.getRef().child("status").setValue("cancelled");
                            Toast.makeText(TokenSuccessActivity.this, "Token " + tokenNumber + " cancelled successfully.", Toast.LENGTH_SHORT).show();
                            found = true;

                            // Navigate back to booking or home
                            Intent intent = new Intent(TokenSuccessActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                    if (!found) {
                        Toast.makeText(TokenSuccessActivity.this, "Token not found or already processed.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TokenSuccessActivity.this, "Failed to cancel token: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
