package com.example.midiflux;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midiflux.adapter.SessionAdapter;
import com.example.midiflux.model.PadSession;
import com.example.midiflux.storage.SessionStorageManager;
import com.example.midiflux.storage.TempSessionManager;
import com.example.midiflux.storage.FirestoreSessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // UI and managers
    private RecyclerView recyclerView;
    private TextView textNoSessions;
    private SessionAdapter adapter;
    private SessionStorageManager storage;
    private TempSessionManager tempManager;
    private Button btnSaveSession;
    private ImageButton btnRefresh;
    private FirestoreSessionManager firestoreManager;
    private android.widget.ProgressBar progressLoading;

    public profileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment profileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewSessions);
        textNoSessions = view.findViewById(R.id.textNoSessions);
        btnSaveSession = view.findViewById(R.id.btnSaveSession);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        
        // Verify buttons were found in the layout
        if (btnSaveSession == null) {
            android.util.Log.e("profileFragment", "onCreateView: btnSaveSession not found in layout!");
        } else {
            android.util.Log.d("profileFragment", "onCreateView: btnSaveSession found in layout");
        }
        
        if (btnRefresh == null) {
            android.util.Log.e("profileFragment", "onCreateView: btnRefresh not found in layout!");
        } else {
            android.util.Log.d("profileFragment", "onCreateView: btnRefresh found in layout");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        storage = new SessionStorageManager(requireContext());
        tempManager = TempSessionManager.getInstance();
        firestoreManager = new FirestoreSessionManager(requireContext());

        // Start with empty list; Firestore will populate asynchronously
        adapter = new SessionAdapter(new ArrayList<>(), session -> {
            Intent intent = new Intent(requireContext(), SessionDetailsActivity.class);
            intent.putExtra(SessionDetailsActivity.EXTRA_SESSION_ID, session.getSessionId());
            startActivity(intent);
        });
        
        // Set up delete functionality
        adapter.setOnSessionDeleteListener((session, position) -> {
            // Show confirmation dialog before deleting
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Session")
                .setMessage("Are you sure you want to delete this session?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete from Firestore
                    firestoreManager.deleteSession(session.getSessionId(), new FirestoreSessionManager.SaveCallback() {
                        @Override
                        public void onSuccess(String sessionId) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Session deleted", Toast.LENGTH_SHORT).show();
                                adapter.removeSession(position);
                                
                                // If all sessions are deleted, show empty state
                                if (adapter.getItemCount() == 0) {
                                    recyclerView.setVisibility(View.GONE);
                                    textNoSessions.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Failed to delete: " + e.getMessage(), 
                                               Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        recyclerView.setAdapter(adapter);

        // Set up Save Session button
        btnSaveSession.setOnClickListener(v -> showSaveDialog());
        
        // Set up Refresh button
        btnRefresh.setOnClickListener(v -> {
            // Get count of pads before clearing for more informative toast
            int padCount = tempManager.getPadWithDataCount();
            
            // Clear temp manager data and refresh
            tempManager.clearAllTempData();
            
            // Show informative toast about what was cleared
            String message = (padCount > 0) 
                ? "Cleared " + padCount + " temporary pad" + (padCount > 1 ? "s" : "")
                : "No temporary pad data to clear";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            
            refreshSessionsAndFab();
        });

        progressLoading = view.findViewById(R.id.progressLoadingSessions);

        // Load sessions from Firestore first
        loadSessionsFromFirestore();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload sessions from Firestore when returning so UI shows all cloud-saved sessions
        // If Firestore is unavailable, loadSessionsFromFirestore() will fall back to local storage.
        loadSessionsFromFirestore();
        
        // Update save button visibility based on if there's temp data to save
        refreshSessionsAndFab();
    }

    public void refreshSessionsAndFab() {
        // Update save button visibility based on whether we have temp data
        if (btnSaveSession != null) {
            boolean hasTempData = tempManager != null && tempManager.hasAnyTempData();
            btnSaveSession.setVisibility(hasTempData ? View.VISIBLE : View.GONE);
            
            // Enable/disable button text for visual feedback
            btnSaveSession.setEnabled(hasTempData);
            if (hasTempData) {
                // Get details about which pads have data
                StringBuilder padNumbers = new StringBuilder();
                int count = 0;
                for (int i = 1; i <= 10; i++) {
                    if (tempManager.hasTempData(i)) {
                        if (count > 0) padNumbers.append(", ");
                        padNumbers.append(i);
                        count++;
                    }
                }
                
                btnSaveSession.setText("Save Current Pad Settings (" + count + " pads: " + padNumbers + ")");
                if (android.util.Log.isLoggable("profileFragment", android.util.Log.DEBUG)) android.util.Log.d("profileFragment", "Pads with data: " + padNumbers + ", totalCount=" + count);
            } else {
                btnSaveSession.setText("No Pad Settings To Save");
            }
            
            if (android.util.Log.isLoggable("profileFragment", android.util.Log.DEBUG)) android.util.Log.d("profileFragment", "refreshSessionsAndFab: hasTempData=" + hasTempData + 
                              " setting button visibility=" + (hasTempData ? "VISIBLE" : "GONE"));
        } else {
            if (android.util.Log.isLoggable("profileFragment", android.util.Log.DEBUG)) android.util.Log.d("profileFragment", "refreshSessionsAndFab: btnSaveSession is null!");
        }
        
        // The refresh button should always be visible for better UX
        if (btnRefresh != null) {
            // Always keep refresh button visible, just enable/disable based on temp data
            boolean hasTempData = tempManager != null && tempManager.hasAnyTempData();
            btnRefresh.setEnabled(hasTempData);
            btnRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void loadSessionsFromFirestore() {
        progressLoading.setVisibility(View.VISIBLE);
        firestoreManager.getAllSessions(new FirestoreSessionManager.SessionsCallback() {
            @Override
            public void onSuccess(List<com.example.midiflux.model.PadSession> sessions) {
                requireActivity().runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    android.util.Log.d("profileFragment", "loadSessionsFromFirestore: sessionsCount=" + (sessions == null ? 0 : sessions.size()));
                    if (sessions != null) {
                        for (com.example.midiflux.model.PadSession s : sessions) {
                            android.util.Log.d("profileFragment", "session returned id=" + s.getSessionId() + " name=" + s.getSessionName());
                        }
                    }
                    adapter.updateSessions(sessions);
                    if (sessions == null || sessions.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        textNoSessions.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        textNoSessions.setVisibility(View.GONE);
                    }
                    
                    // Always refresh save button visibility
                    refreshSessionsAndFab();
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Fallback to local storage
                requireActivity().runOnUiThread(() -> {
                    progressLoading.setVisibility(View.GONE);
                    // Load local sessions into the adapter so user still sees saved items
                    List<PadSession> local = storage.getAllSessions();
                    adapter.updateSessions(local);
                    if (local == null || local.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        textNoSessions.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        textNoSessions.setVisibility(View.GONE);
                    }
                    // Always refresh button visibility
                    refreshSessionsAndFab();
                });
            }
        });
    }

    private void showSaveDialog() {
        // First, show options dialog - new or update
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Save Pad Settings")
                .setMessage("Do you want to create a new session or update an existing one?")
                .setPositiveButton("New Session", (dialog, which) -> {
                    // Show dialog to create a new session
                    showNewSessionDialog();
                })
                .setNegativeButton("Update Existing Session", (dialog, which) -> {
                    // Show dialog to select existing session to update
                    showSelectExistingSessionDialog();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }
    
    /**
     * Show dialog to create a new session
     */
    private void showNewSessionDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("Session name");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Create New Session")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (name.isEmpty()) name = "Session " + System.currentTimeMillis();
                    final String sessionName = name;

                    // Save named session to Firestore, fallback to local storage on failure
                    java.util.Map<Integer, com.example.midiflux.model.PadInfo> padsMap = tempManager.getAllCurrentPads();
                    int padCount = 0; if (padsMap != null) {
                        for (int i=1;i<=10;i++) if (padsMap.get(i)!=null && padsMap.get(i).hasData()) padCount++;
                    }
                    android.util.Log.d("profileFragment", "showNewSessionDialog: saving sessionName='" + sessionName + "' padCount=" + padCount);
                    
                    // Show toast to confirm save is being attempted
                    Toast.makeText(requireContext(), "Saving session '" + sessionName + "'...", Toast.LENGTH_SHORT).show();

                    firestoreManager.saveSession(sessionName, tempManager.getAllCurrentPads(), new FirestoreSessionManager.SaveCallback() {
                        @Override
                        public void onSuccess(String sessionId) {
                            requireActivity().runOnUiThread(() -> {
                                // Clear temporary pad data after successful save
                                tempManager.clearAllTempData();
                                Toast.makeText(requireContext(), "Session saved", Toast.LENGTH_SHORT).show();
                                loadSessionsFromFirestore();
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Fallback to local storage
                            storage.saveSession(sessionName, tempManager.getAllCurrentPads());
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Saved locally (firestore failed)", Toast.LENGTH_SHORT).show();
                                // Load local sessions so the new local session appears immediately
                                List<PadSession> local = storage.getAllSessions();
                                adapter.updateSessions(local);
                                if (local == null || local.isEmpty()) {
                                    recyclerView.setVisibility(View.GONE);
                                    textNoSessions.setVisibility(View.VISIBLE);
                                } else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    textNoSessions.setVisibility(View.GONE);
                                }
                                refreshSessionsAndFab();
                            });
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Show dialog to select an existing session to update
     */
    private void showSelectExistingSessionDialog() {
        // Get the current list of sessions from the adapter
        List<PadSession> existingSessions = adapter.getSessions();
        
        // Check if there are any sessions to update
        if (existingSessions == null || existingSessions.isEmpty()) {
            Toast.makeText(requireContext(), "No existing sessions to update", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create session name array for the dialog
        String[] sessionNames = new String[existingSessions.size()];
        for (int i = 0; i < existingSessions.size(); i++) {
            PadSession session = existingSessions.get(i);
            sessionNames[i] = session.getSessionName() + " (" + session.getPadCount() + " pads)";
        }
        
        // Show dialog with session names
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Session to Update")
                .setItems(sessionNames, (dialog, which) -> {
                    PadSession selectedSession = existingSessions.get(which);
                    updateExistingSession(selectedSession);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Update an existing session with current pad settings
     * @param session The session to update
     */
    private void updateExistingSession(PadSession session) {
        // Get current pad settings
        java.util.Map<Integer, com.example.midiflux.model.PadInfo> currentPads = tempManager.getAllCurrentPads();
        
        // Confirm update
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Update Session")
                .setMessage("This will update session '" + session.getSessionName() + "' with your current pad settings.\n\n" +
                           "• Existing pads will be updated if they're in your current settings\n" +
                           "• New pads will be added\n\n" +
                           "Continue?")
                .setPositiveButton("Update", (dialog, which) -> {
                    // First get the full session to ensure we have all data
                    firestoreManager.getSession(session.getSessionId(), new FirestoreSessionManager.SessionCallback() {
                        @Override
                        public void onSuccess(PadSession fullSession) {
                            // Merge the current pads with the existing session
                            mergeAndUpdateSession(fullSession, currentPads);
                        }
                        
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Failed to retrieve session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Merge current pad settings with an existing session and update in Firestore
     * @param session Existing session to update
     * @param currentPads Current pad settings to merge
     */
    private void mergeAndUpdateSession(PadSession session, java.util.Map<Integer, com.example.midiflux.model.PadInfo> currentPads) {
        // Create a map of existing pads by pad number for easy lookup
        java.util.Map<Integer, com.example.midiflux.model.PadInfo> existingPads = new java.util.HashMap<>();
        if (session.getPads() != null) {
            for (com.example.midiflux.model.PadInfo pad : session.getPads()) {
                existingPads.put(pad.getPadNumber(), pad);
            }
        }
        
        // Merge current pads with existing pads
        for (java.util.Map.Entry<Integer, com.example.midiflux.model.PadInfo> entry : currentPads.entrySet()) {
            // Get the current pad with all its data
            com.example.midiflux.model.PadInfo currentPad = entry.getValue();
            
            // Ensure layerEffects is properly initialized for multi-instruments
            if (currentPad.isMultiInstrument() && currentPad.getLayerEffects() == null) {
                currentPad.setLayerEffects(new java.util.HashMap<>());
            }
            
            // Debug logging for layer effects
            if (currentPad.isMultiInstrument()) {
                android.util.Log.d("profileFragment", "Pad " + currentPad.getPadNumber() + 
                    " is multi-instrument with " + 
                    (currentPad.getLayerEffects() != null ? currentPad.getLayerEffects().size() : 0) + 
                    " layer effects");
            }
            
            existingPads.put(entry.getKey(), currentPad);
        }
        
        // Convert map back to list for the session
        java.util.List<com.example.midiflux.model.PadInfo> mergedPadsList = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, com.example.midiflux.model.PadInfo> entry : existingPads.entrySet()) {
            mergedPadsList.add(entry.getValue());
        }
        
        // Update the session with merged pads
        session.setPads(mergedPadsList);
        
        // Update the session in Firestore
        updateSessionInFirestore(session);
    }
    
    /**
     * Update an existing session in Firestore
     * @param session The session to update
     */
    private void updateSessionInFirestore(PadSession session) {
        // Show toast to indicate update is in progress
        Toast.makeText(requireContext(), "Updating session '" + session.getSessionName() + "'...", Toast.LENGTH_SHORT).show();
        
        // We'll update the session using the document ID
        String sessionId = session.getSessionId();
        
        // Check if we have a valid session ID
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Invalid session ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use set() to update the entire document
        firestoreManager.updateSession(session, new FirestoreSessionManager.SaveCallback() {
            @Override
            public void onSuccess(String updatedSessionId) {
                requireActivity().runOnUiThread(() -> {
                    // Clear temporary pad data after successful update
                    tempManager.clearAllTempData();
                    Toast.makeText(requireContext(), "Session updated successfully", Toast.LENGTH_SHORT).show();
                    // Reload the sessions list
                    loadSessionsFromFirestore();
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to update session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}