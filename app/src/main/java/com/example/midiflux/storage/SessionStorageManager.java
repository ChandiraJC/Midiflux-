package com.example.midiflux.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.midiflux.model.PadInfo;
import com.example.midiflux.model.PadSession;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Simple session storage manager
 */
public class SessionStorageManager {
    private static final String PREF_NAME = "pad_sessions";
    private static final String KEY_SESSIONS = "sessions";
    private SharedPreferences prefs;
    private Gson gson;
    
    public SessionStorageManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Save a new session with current pad configurations
     */
    public void saveSession(String sessionName, Map<Integer, PadInfo> padsData) {
        PadSession session = new PadSession(sessionName);
        
        // Convert map to list for storage
        List<PadInfo> padsList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            PadInfo padInfo = padsData.get(i);
            if (padInfo != null && padInfo.hasData()) {
                padsList.add(padInfo);
            }
        }
        
        session.setPads(padsList);
        
        List<PadSession> sessions = getAllSessions();
        sessions.add(session);
        
        String json = gson.toJson(sessions);
        prefs.edit().putString(KEY_SESSIONS, json).apply();
        android.util.Log.d("SessionStorageManager", "saveSession: wrote sessions json=" + json);
    }
    
    /**
     * Get all saved sessions
     */
    public List<PadSession> getAllSessions() {
        String json = prefs.getString(KEY_SESSIONS, "[]");
        Type type = new TypeToken<List<PadSession>>(){}.getType();
        List<PadSession> sessions = gson.fromJson(json, type);
        return sessions != null ? sessions : new ArrayList<>();
    }
    
    /**
     * Delete a session by ID
     */
    public void deleteSession(String sessionId) {
        List<PadSession> sessions = getAllSessions();
        sessions.removeIf(session -> session.getSessionId().equals(sessionId));
        
        String json = gson.toJson(sessions);
        prefs.edit().putString(KEY_SESSIONS, json).apply();
    }
    
    /**
     * Get session by ID
     */
    public PadSession getSession(String sessionId) {
        List<PadSession> sessions = getAllSessions();
        for (PadSession session : sessions) {
            if (session.getSessionId().equals(sessionId)) {
                return session;
            }
        }
        return null;
    }
}