package com.example.digitaltokengenerationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView currentTokenText, nextTokenText;
    private Spinner pendingTokenSpinner;
    private DatabaseReference databaseRef;
    private DatabaseReference requestsRef;

    private final List<String> pendingTokens = new ArrayList<>();
    private final Map<String, String> tokenKeyMap = new HashMap<>();
    private ArrayAdapter<String> spinnerAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize UI
        TextView placeNameText = findViewById(R.id.placeNameText);
        currentTokenText = findViewById(R.id.currentTokenText);
        nextTokenText = findViewById(R.id.nextTokenText);
        Button nextTokenBtn = findViewById(R.id.nextTokenBtn);
        Button markDoneBtn = findViewById(R.id.markDoneBtn);
        pendingTokenSpinner = findViewById(R.id.pendingTokenSpinner);
        FloatingActionButton resetTokenFab = findViewById(R.id.resetTokenFab);

        currentTokenText.setText("Current Token: --");
        nextTokenText.setText("Next Token: --");

        // Get selected place
        String selectedPlace = getIntent().getStringExtra("Place Name");
        if(selectedPlace == null || selectedPlace.isEmpty()){
            Toast.makeText(this, "Place not recieved", Toast.LENGTH_SHORT).show();
            selectedPlace = "Cafe";
        }
        placeNameText.setText("Place: " + selectedPlace);

        // Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("places").child(selectedPlace);
        requestsRef = databaseRef.child("requests");

        // Spinner setup
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pendingTokens);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pendingTokenSpinner.setAdapter(spinnerAdapter);

        pendingTokens.add("Loading...");
        spinnerAdapter.notifyDataSetChanged();

        // Load tokens
        loadPendingTokens();

        // Listen for token changes
        databaseRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentTokenValue = snapshot.child("currentToken").getValue(Integer.class);
                Integer nextTokenValue = snapshot.child("nextToken").getValue(Integer.class);

                int currentToken = currentTokenValue != null ? currentTokenValue : 0;
                int nextToken = nextTokenValue != null ? nextTokenValue : currentToken + 1;

                currentTokenText.setText("Current Token: " + currentToken);
                nextTokenText.setText("Next Token: " + nextToken);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to load token info", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Next Token Button logic improved
        nextTokenBtn.setOnClickListener(v -> databaseRef.child("currentToken").get().addOnSuccessListener(snapshot -> {
            Integer currentTokenVal = snapshot.getValue(Integer.class);
            int currentToken = currentTokenVal != null ? currentTokenVal : 0;

            // Save previous token as pending (if not already)
            if (currentToken > 0) {
                String prevTokenStr = String.valueOf(currentToken);
                requestsRef.orderByChild("token").equalTo(prevTokenStr)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    long timestamp = System.currentTimeMillis();
                                    String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                            .format(new Date(timestamp));

                                    Map<String, Object> tokenData = new HashMap<>();
                                    tokenData.put("token", prevTokenStr);
                                    tokenData.put("status", "pending");
                                    tokenData.put("timestamp", timestamp);
                                    tokenData.put("time", timeStr);

                                    requestsRef.push().setValue(tokenData);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }

            // Move to next token
            databaseRef.child("currentToken").setValue(currentToken + 1);
            databaseRef.child("nextToken").setValue(currentToken + 2);

            Toast.makeText(this, "Next token updated", Toast.LENGTH_SHORT).show();
        }));

        // ✅ Mark Done Button
        markDoneBtn.setOnClickListener(v -> {
            if (pendingTokens.isEmpty() || pendingTokens.get(0).equals("No pending tokens")) {
                Toast.makeText(this, "No pending tokens available", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedToken = (String) pendingTokenSpinner.getSelectedItem();
            if (selectedToken == null || selectedToken.equals("No pending tokens")) {
                Toast.makeText(this, "No valid token selected", Toast.LENGTH_SHORT).show();
                return;
            }

            String tokenKey = tokenKeyMap.get(selectedToken);
            if (tokenKey == null) {
                Toast.makeText(this, "Error finding token in database", Toast.LENGTH_SHORT).show();
                return;
            }

            requestsRef.child(tokenKey).child("status").setValue("served")
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Token " + selectedToken + " marked as done", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update token", Toast.LENGTH_SHORT).show());
        });

        // ✅ Floating Action Button (Reset)
        resetTokenFab.setOnClickListener(view -> {
            Intent intent = new Intent(AdminDashboardActivity.this, TokenResetActivity.class);
            intent.putExtra("placeName", placeNameText.getText().toString().replace("Place: ", "").trim());
            startActivity(intent);
        });


    }

    // Load pending tokens live
    private void loadPendingTokens() {
        requestsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingTokens.clear();
                tokenKeyMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String tokenNumber = ds.child("token").getValue(String.class);
                    String status = ds.child("status").getValue(String.class);
                    String key = ds.getKey();

                    if (tokenNumber != null && status != null &&
                            (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("generated"))) {
                        pendingTokens.add(tokenNumber);
                        tokenKeyMap.put(tokenNumber, key);
                    }
                }

                if (pendingTokens.isEmpty()) {
                    pendingTokens.add("No pending tokens");
                }

                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this, "Firebase Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}