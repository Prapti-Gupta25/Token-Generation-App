package com.example.digitaltokengenerationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TokenBookingActivity extends AppCompatActivity {

    TextView placeNameText, tokenDisplay;
    MaterialButton generateTokenBtn;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_booking);

        placeNameText = findViewById(R.id.placeNameText);
        tokenDisplay = findViewById(R.id.tokenDisplay);
        generateTokenBtn = findViewById(R.id.generateTokenBtn);

        // ✅ Get selected place from previous activity
        String selectedPlace = getIntent().getStringExtra("selectedPlace");
        if (selectedPlace != null && !selectedPlace.isEmpty()) {
            placeNameText.setText(selectedPlace);
        } else {
            selectedPlace = "Unknown";
            placeNameText.setText("Selected Place: Not Available");
        }

        String finalSelectedPlace = selectedPlace;

        // ✅ Firebase reference
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        // ✅ Button click to generate token
        generateTokenBtn.setOnClickListener(v -> {
            String placeKey = finalSelectedPlace.replace(".", "_");
            DatabaseReference placeRef = rootRef.child("places").child(placeKey);
            DatabaseReference nextTokenRef = placeRef.child("nextToken");



            // 🔁 Transaction ensures atomic token increment
            nextTokenRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer next = currentData.getValue(Integer.class);
                    if (next == null) next = 1;
                    currentData.setValue(next + 1);
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                    if (error != null) {
                        Toast.makeText(TokenBookingActivity.this, "Failed to generate token. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!committed) {
                        Toast.makeText(TokenBookingActivity.this, "Transaction failed. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Generate token string
                    Integer updatedNext = currentData.getValue(Integer.class);
                    int reserved = (updatedNext != null ? updatedNext : 1) - 1;
                    String tokenString = "T" + reserved;

                    // ✅ Add time
                    String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                    // ✅ Update "currentToken" for admin monitoring
                    placeRef.child("currentToken").setValue(reserved);

                    // ✅ Save request info
                    DatabaseReference requestsRef = placeRef.child("requests").push();
                    Map<String, Object> request = new HashMap<>();
                    request.put("token", tokenString);
                    request.put("status", "pending");
                    request.put("time", currentTime);
                    request.put("timestamp", System.currentTimeMillis());

                    requestsRef.setValue(request).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // ✅ Show token and animation
                            tokenDisplay.setText("Your Token: " + tokenString);

                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setDuration(400);

                            Animation scaleUp = new ScaleAnimation(
                                    0.8f, 1.05f, 0.8f, 1.05f,
                                    Animation.RELATIVE_TO_SELF, 0.5f,
                                    Animation.RELATIVE_TO_SELF, 0.5f);
                            scaleUp.setDuration(300);

                            tokenDisplay.startAnimation(fadeIn);
                            tokenDisplay.startAnimation(scaleUp);

                            Toast.makeText(TokenBookingActivity.this, "Token Generated Successfully!", Toast.LENGTH_SHORT).show();

                            // ✅ Go to Success Activity
                            Intent intent = new Intent(TokenBookingActivity.this, TokenSuccessActivity.class);
                            intent.putExtra("selectedPlace", finalSelectedPlace);
                            intent.putExtra("tokenNumber", tokenString);
                            startActivity(intent);
                        } else {
                            Toast.makeText(TokenBookingActivity.this, "Failed to save token. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });


    }
}
