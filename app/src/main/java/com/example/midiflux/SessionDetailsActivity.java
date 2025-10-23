package com.example.midiflux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import com.example.midiflux.adapter.PadInfoAdapter;
import com.example.midiflux.model.PadSession;
import com.example.midiflux.storage.SessionStorageManager;
import com.example.midiflux.storage.FirestoreSessionManager;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SessionDetailsActivity extends AppCompatActivity {
    private static final String TAG = "SessionDetailsActivity";
    public static final String EXTRA_SESSION_ID = "session_id";
    
    private TextView textSessionTitle;
    private TextView textSessionInfo;
    private RecyclerView recyclerViewPads;
    private android.widget.ScrollView scrollSessionDetails;
    private TextView textSessionDetails;
    private LinearLayout layoutEmptyState;
    private ImageView buttonBack;
    
    private SessionStorageManager sessionManager;
    private PadInfoAdapter padAdapter;
    private PadSession currentSession;
    private FirestoreSessionManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);
        
        initViews();
        initManagers();
        loadSessionData();
        setupRecyclerView();
    }

    private void initViews() {
        textSessionTitle = findViewById(R.id.textSessionTitle);
        textSessionInfo = findViewById(R.id.textSessionInfo);
        recyclerViewPads = findViewById(R.id.recyclerViewSessionPads);
        scrollSessionDetails = findViewById(R.id.scrollSessionDetails);
        textSessionDetails = findViewById(R.id.textSessionDetails);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        buttonBack = findViewById(R.id.buttonBack);
        
        buttonBack.setOnClickListener(v -> finish());
    }

    private void initManagers() {
        sessionManager = new SessionStorageManager(this);
        firestoreManager = new FirestoreSessionManager(this);
    }

    private void loadSessionData() {
        String sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        if (sessionId == null) {
            finish();
            return;
        }
        // Try Firestore first
        firestoreManager.getSession(sessionId, new FirestoreSessionManager.SessionCallback() {
            @Override
            public void onSuccess(PadSession session) {
                currentSession = session;
                runOnUiThread(() -> updateUI());
            }

            @Override
            public void onFailure(Exception e) {
                // Fallback to local storage
                currentSession = sessionManager.getSession(sessionId);
                if (currentSession == null) {
                    finish();
                    return;
                }
                runOnUiThread(() -> updateUI());
            }
        });
    }

    private void updateUI() {
        // Session title
        String sessionName = currentSession.getSessionName();
        if (sessionName == null || sessionName.trim().isEmpty()) {
            sessionName = "Unnamed Session";
        }
        textSessionTitle.setText(sessionName);
        
        // Session info
        int padCount = currentSession.getPadCount();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentSession.getCreatedDate());
        String infoText = padCount + " Pad" + (padCount != 1 ? "s" : "") + " • Created " + dateText;
        textSessionInfo.setText(infoText);
        
        // Render structured text representation of the session
        StringBuilder sb = new StringBuilder();
        sb.append("Session: ").append(sessionName).append("\n");
        sb.append(infoText).append("\n\n");

        if (currentSession.getPads() == null || currentSession.getPads().isEmpty()) {
            sb.append("(No pads in this session)\n");
        } else {
            for (int i = 0; i < currentSession.getPads().size(); i++) {
                com.example.midiflux.model.PadInfo p = currentSession.getPads().get(i);
                sb.append("Pad ").append(p.getPadNumber()).append(":\n");
                sb.append("  Patch: ").append(p.getPatchName() == null ? "" : p.getPatchName()).append("\n");
                sb.append("  Multi-instrument: ").append(p.isMultiInstrument() ? "yes" : "no").append("\n");
                if (p.isMultiInstrument()) {
                    // Debug logging for layer effects
                    if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Pad " + p.getPadNumber() + 
                        " is multi-instrument, layerEffects=" + (p.getLayerEffects() != null ? p.getLayerEffects().size() : "null") +
                        ", hasLayerEffects=" + (p.getLayerEffects() != null && !p.getLayerEffects().isEmpty()));
                    
                    if (Log.isLoggable(TAG, Log.DEBUG) && p.getLayerEffects() != null) {
                        for (java.util.Map.Entry<String, String> en : p.getLayerEffects().entrySet()) {
                            Log.d(TAG, "  Layer: " + en.getKey() + " = " + en.getValue());
                        }
                    }

                    // Handle layer effects display, handle empty and null cases
                    Map<String, String> layerEffects = p.getLayerEffects();
                    
                    // Special handling in case Firebase deserialization issue
                    if (layerEffects == null) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Creating new empty layer effects map");
                        layerEffects = new HashMap<>(); // Ensure it's not null
                    }
                    
                    // Check if the map has any entries
                    boolean hasLayerEffects = !layerEffects.isEmpty();
                    
                    if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "hasLayerEffects=" + hasLayerEffects + 
                                      ", size=" + layerEffects.size());
                    
                    // Display layer effects
                    if (hasLayerEffects) {
                        for (Map.Entry<String, String> en : layerEffects.entrySet()) {
                            String layerName = en.getKey();
                            String effectsValue = en.getValue();
                            
                            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Displaying layer: " + layerName + 
                                             " = " + effectsValue);
                            
                            // Format the layer name consistently
                            String displayLayerName;
                            if (layerName.matches("\\d+")) {
                                // If it's just a number, format as "Layer X"
                                displayLayerName = "Layer " + layerName;
                            } else if (layerName.startsWith("Layer ")) {
                                // If it already starts with "Layer ", use as-is
                                displayLayerName = layerName;
                            } else {
                                // Otherwise, use the raw name
                                displayLayerName = layerName;
                            }
                            
                            // Add indentation and the formatted layer name
                            sb.append("  ").append(displayLayerName);
                            
                            // Always display the effects value after the layer name
                            // Include the colon even for empty effects for consistent formatting
                            sb.append(": ").append(effectsValue == null ? "" : effectsValue);
                            
                            // Only append the effects value if it's not null or empty
                            if (effectsValue != null && !effectsValue.isEmpty()) {
                                sb.append(": ").append(effectsValue);
                            }
                            
                            sb.append("\n");
                        }
                    } else {
                        sb.append("  Layer effects: none\n");
                    }
                    
                    sb.append("  Overall effects: ").append(p.getOverallEffects() == null ? "" : p.getOverallEffects()).append("\n");
                } else {
                    sb.append("  Effects: ").append(p.getEffects() == null ? "" : p.getEffects()).append("\n");
                }
                sb.append("  Notes: ").append(p.getAdditionalNotes() == null ? "" : p.getAdditionalNotes()).append("\n");
                sb.append("  Last edited: ").append(p.getLastEdited() == null ? "" : new SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(p.getLastEdited())).append("\n");
                sb.append("\n");
            }
        }

        // Set text and show the scroll view
        textSessionDetails.setText(sb.toString());
        scrollSessionDetails.setVisibility(View.VISIBLE);
        recyclerViewPads.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        padAdapter = new PadInfoAdapter();
        
        recyclerViewPads.setAdapter(padAdapter);
        recyclerViewPads.setLayoutManager(new LinearLayoutManager(this));
    }
}