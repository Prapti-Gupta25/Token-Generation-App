package com.example.digitaltokengenerationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlaceSelectionActivity extends AppCompatActivity {

    private String selectedPlace = null;
    private final List<CardView> placeCards = new ArrayList<>();
    private final List<TextView> placeNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selection);

        Button continueBtn = findViewById(R.id.continueBtn);

        // 🔹 Step 1: Initialize all card and text references
        placeCards.add(findViewById(R.id.placeCard1));
        placeCards.add(findViewById(R.id.placeCard2));
        placeCards.add(findViewById(R.id.placeCard3));
        placeCards.add(findViewById(R.id.placeCard4));
        placeCards.add(findViewById(R.id.placeCard5));

        placeNames.add(findViewById(R.id.placeName1));
        placeNames.add(findViewById(R.id.placeName2));
        placeNames.add(findViewById(R.id.placeName3));
        placeNames.add(findViewById(R.id.placeName4));
        placeNames.add(findViewById(R.id.placeName5));

        // 🔹 Step 2: Hide all cards initially (they’ll be visible after Firebase data loads)
        for (CardView card : placeCards) {
            card.setVisibility(android.view.View.GONE);
        }

        // 🔹 Step 3: Load place names dynamically from Firebase
        DatabaseReference placesRef = FirebaseDatabase.getInstance().getReference("places");
        placesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    if (index >= placeNames.size()) break;

                    // Convert Firebase key to readable name (replace underscores with spaces)
                    String placeName = placeSnapshot.getKey().replace("_", " ");

                    // Set the text on UI and make the card visible
                    placeNames.get(index).setText(placeName);
                    placeCards.get(index).setVisibility(android.view.View.VISIBLE);
                    index++;
                }

                if (index == 0) {
                    Toast.makeText(PlaceSelectionActivity.this, "No places found in database.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaceSelectionActivity.this, "Failed to load places", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔹 Step 4: Handle place card selection
        for (int i = 0; i < placeCards.size(); i++) {
            int finalI = i;
            placeCards.get(i).setOnClickListener(v -> {
                selectedPlace = placeNames.get(finalI).getText().toString();

                // Reset all card highlights
                for (CardView card : placeCards) {
                    card.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));
                }

                // Highlight the selected card
                placeCards.get(finalI).setCardBackgroundColor(getResources().getColor(R.color.teal_700));
            });
        }

        // 🔹 Step 5: Continue button logic
        continueBtn.setOnClickListener(v -> {
            if (selectedPlace == null) {
                Toast.makeText(this, "Please select a place", Toast.LENGTH_SHORT).show();
            } else {
                // Pass selected place to TokenBookingActivity
                Intent intent = new Intent(PlaceSelectionActivity.this, TokenBookingActivity.class);
                intent.putExtra("selectedPlace", selectedPlace);
                startActivity(intent);
            }
        });
    }
}
