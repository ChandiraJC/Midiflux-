package com.example.midiflux.storage;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.midiflux.model.PadInfo;
import com.example.midiflux.model.PadSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Firestore-backed session manager. Uses collection 'sessions'.
 * Methods are asynchronous and use callbacks.
 */
public class FirestoreSessionManager {
    private static final String TAG = "FirestoreSessionManager";
    private final FirebaseFirestore db;

    public interface SessionsCallback {
        void onSuccess(List<PadSession> sessions);
        void onFailure(Exception e);
    }

    public interface SessionCallback {
        void onSuccess(PadSession session);
        void onFailure(Exception e);
    }

    public interface SaveCallback {
        void onSuccess(String sessionId);
        void onFailure(Exception e);
    }

    public FirestoreSessionManager(android.content.Context context) {
        // Initialize Firebase if not already
        try { FirebaseApp.initializeApp(context); } catch (Exception ignored) {}
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Return the collection reference scoped to the currently signed-in user if available.
     * Falls back to the top-level "sessions" collection when no user is signed in.
     */
    private CollectionReference getSessionsCollection() {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                return db.collection("users").document(uid).collection("sessions");
            }
        } catch (Exception ignored) {}
        return db.collection("sessions");
    }

    public void getAllSessions(final SessionsCallback callback) {
    getSessionsCollection().get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PadSession> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        try {
                            // Log incoming document id and data for debugging
                            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAllSessions: docId=" + doc.getId() + " raw=" + doc.getData());

                            // Skip autosave documents - we only want to see explicitly saved sessions
                            if (doc.getId().startsWith("Autosave") || "autosave".equals(doc.getId()) || 
                                (doc.getString("sessionName") != null && 
                                 doc.getString("sessionName").startsWith("Autosave"))) {
                                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAllSessions: skipping autosave doc: " + doc.getId());
                                continue;
                            }

                            PadSession s = doc.toObject(PadSession.class);
                            if (s != null) {
                                s.setSessionId(doc.getId());
                                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAllSessions: parsed sessionName=" + s.getSessionName() + " id=" + s.getSessionId());
                                list.add(s);
                            } else {
                                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAllSessions: toObject returned null for docId=" + doc.getId());
                            }
                        } catch (Exception e) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAllSessions: parse error " + e.getMessage());
                        }
                    }
                    // Sort by createdDate (newest first) for predictable UI ordering
                    list.sort((a, b) -> {
                        if (a.getCreatedDate() == null && b.getCreatedDate() == null) return 0;
                        if (a.getCreatedDate() == null) return 1;
                        if (b.getCreatedDate() == null) return -1;
                        return b.getCreatedDate().compareTo(a.getCreatedDate());
                    });
                    if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getAllSessions: returning list size=" + list.size());
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getSession(String sessionId, final SessionCallback callback) {
    getSessionsCollection().document(sessionId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Debug log the raw data
                    if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getSession: Raw document data for sessionId=" + sessionId);
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data != null) {
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "  " + entry.getKey() + " = " + entry.getValue());
                        }
                        
                        // Debug log pads data specifically
                        Object padsObj = data.get("pads");
                        if (padsObj instanceof java.util.List) {
                            java.util.List<?> padsList = (java.util.List<?>) padsObj;
                            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "  Pads list size: " + padsList.size());
                            
                            for (Object padObj : padsList) {
                                if (padObj instanceof java.util.Map) {
                                    java.util.Map<?, ?> padMap = (java.util.Map<?, ?>) padObj;
                                    if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "    Pad: " + padMap);
                                    
                                    // Check for layerEffects specifically
                                    Object layerEffectsObj = padMap.get("layerEffects");
                                    if (layerEffectsObj != null) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "      layerEffects: " + layerEffectsObj + 
                                " (class: " + layerEffectsObj.getClass().getName() + ")");
                                        
                                        // Try to extract layer effect details
                                        if (layerEffectsObj instanceof java.util.Map) {
                                            @SuppressWarnings("unchecked")
                                            java.util.Map<String, Object> layerEffectsMap = 
                                                (java.util.Map<String, Object>) layerEffectsObj;
                                            
                                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                                for (java.util.Map.Entry<String, Object> layerEntry : 
                                                     layerEffectsMap.entrySet()) {
                                                    Log.d(TAG, "        Layer key: '" + layerEntry.getKey() + 
                                                              "' = '" + layerEntry.getValue() + "'");
                                                }
                                            }
                                        }
                                    } else {
                                        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "      layerEffects: null");
                                    }
                                }
                            }
                        }
                    }
                    
                    // Convert to PadSession as before
                    PadSession s = documentSnapshot.toObject(PadSession.class);
                    if (s != null) {
                        s.setSessionId(documentSnapshot.getId());
                        
                        // Log debug info about the deserialized session
                if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "getSession: Deserialized session name=" + s.getSessionName() +
                    ", padCount=" + s.getPadCount());
                        if (s.getPads() != null && Log.isLoggable(TAG, Log.DEBUG)) {
                            for (PadInfo p : s.getPads()) {
                                Log.d(TAG, "  Pad " + p.getPadNumber() + 
                                      ", isMulti=" + p.isMultiInstrument() +
                                      ", layerEffects=" + (p.getLayerEffects() != null ? 
                                                          p.getLayerEffects().size() : "null"));
                            }
                        }
                        
                        callback.onSuccess(s);
                    } else {
                        callback.onFailure(new Exception("Session not found"));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void saveSession(String name, Map<Integer, PadInfo> padsData, final SaveCallback callback) {
        PadSession session = new PadSession(name);
        List<PadInfo> padsList = new ArrayList<>();
        
        // Process and prepare each pad, making sure layer effects are properly initialized
        for (int i = 1; i <= 10; i++) {
            PadInfo p = padsData.get(i);
            if (p != null && p.hasData()) {
                // Ensure layer effects map is properly initialized
                if (p.isMultiInstrument() && p.getLayerEffects() == null) {
                    p.setLayerEffects(new HashMap<>());
                }
                
                // Debug log for layer effects
                if (p.isMultiInstrument() && Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "saveSession: pad " + p.getPadNumber() + " has " + 
                          (p.getLayerEffects() != null ? p.getLayerEffects().size() : 0) + 
                          " layer effects");
                    
                    if (p.getLayerEffects() != null) {
                        for (Map.Entry<String, String> entry : p.getLayerEffects().entrySet()) {
                            Log.d(TAG, "  Layer: " + entry.getKey() + " = " + entry.getValue());
                        }
                    }
                }
                
                padsList.add(p);
            }
        }
        session.setPads(padsList);

        // Write to Firestore
    if (Log.isLoggable(TAG, Log.DEBUG)) android.util.Log.d(TAG, "saveSession: saving name='" + name + "' padsCount=" + padsList.size());
    getSessionsCollection().add(session)
                .addOnSuccessListener(documentReference -> {
                    if (Log.isLoggable(TAG, Log.DEBUG)) android.util.Log.d(TAG, "saveSession: success id=" + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "saveSession: failure " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }

    // Create a new autosave document each time (with timestamp-based name) to preserve all autosaves
    public void upsertAutosave(Map<Integer, PadInfo> padsData, final SaveCallback callback) {
        // Generate timestamp-based autosave name
        String autosaveName = "Autosave " + new java.text.SimpleDateFormat("MMM dd HH:mm:ss", java.util.Locale.getDefault())
            .format(new java.util.Date());
        
        PadSession session = new PadSession(autosaveName);
        List<PadInfo> padsList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            PadInfo p = padsData.get(i);
            if (p != null && p.hasData()) padsList.add(p);
        }
        session.setPads(padsList);

        // Create a new document for each autosave (don't overwrite)
        Log.d(TAG, "upsertAutosave: creating new autosave with name: " + autosaveName);
    getSessionsCollection().add(session)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "upsertAutosave: success id=" + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "upsertAutosave: failure " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }
    
    /**
     * Delete a session from Firestore
     * @param sessionId the ID of the session to delete
     * @param callback callback for success/failure
     */
    public void deleteSession(String sessionId, final SaveCallback callback) {
        Log.d(TAG, "deleteSession: deleting session with id: " + sessionId);
        
    getSessionsCollection().document(sessionId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "deleteSession: success for id=" + sessionId);
                    callback.onSuccess(sessionId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteSession: failure " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }
    
    /**
     * Update an existing session in Firestore
     * @param session The session to update
     * @param callback Callback for success/failure
     */
    public void updateSession(PadSession session, final SaveCallback callback) {
        String sessionId = session.getSessionId();
        Log.d(TAG, "updateSession: updating session id=" + sessionId);
        
        // Update the lastModified date
        session.setLastModified(new java.util.Date());
        
        // Debug logging for pads and layer effects
        if (session.getPads() != null) {
            Log.d(TAG, "updateSession: session has " + session.getPads().size() + " pads");
            for (PadInfo pad : session.getPads()) {
                Log.d(TAG, "  Pad " + pad.getPadNumber() + 
                      ", isMulti=" + pad.isMultiInstrument() + 
                      ", hasLayerEffects=" + (pad.getLayerEffects() != null && !pad.getLayerEffects().isEmpty()));
                
                if (pad.isMultiInstrument() && pad.getLayerEffects() != null) {
                    for (Map.Entry<String, String> layerEntry : pad.getLayerEffects().entrySet()) {
                        Log.d(TAG, "    Layer: " + layerEntry.getKey() + " = " + layerEntry.getValue());
                    }
                }
            }
        }
        
    getSessionsCollection().document(sessionId).set(session)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "updateSession: success for id=" + sessionId);
                    callback.onSuccess(sessionId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateSession: failure " + e.getMessage(), e);
                    callback.onFailure(e);
                });
    }
}
