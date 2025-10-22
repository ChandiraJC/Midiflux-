package com.example.midiflux.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.midiflux.model.PadInfo;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple storage manager for pad information using SharedPreferences
 */
public class PadStorageManager {
    private static final String PREFS_NAME = "PadInfoPrefs";
    private static final String KEY_PAD_DATA = "pad_data";
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public PadStorageManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Save pad information
     */
    public void savePadInfo(PadInfo padInfo) {
        Map<Integer, PadInfo> allPads = getAllPadInfo();
        allPads.put(padInfo.getPadNumber(), padInfo);
        
        String json = gson.toJson(allPads);
        prefs.edit().putString(KEY_PAD_DATA, json).apply();
    }
    
    /**
     * Get pad information for a specific pad number
     */
    public PadInfo getPadInfo(int padNumber) {
        Map<Integer, PadInfo> allPads = getAllPadInfo();
        return allPads.get(padNumber);
    }
    
    /**
     * Get all pad information
     */
    public Map<Integer, PadInfo> getAllPadInfo() {
        String json = prefs.getString(KEY_PAD_DATA, "{}");
        Type type = new TypeToken<Map<Integer, PadInfo>>(){}.getType();
        Map<Integer, PadInfo> result = gson.fromJson(json, type);
        return result != null ? result : new HashMap<>();
    }
    
    /**
     * Get list of pads that have data
     */
    public List<PadInfo> getPadsWithData() {
        Map<Integer, PadInfo> allPads = getAllPadInfo();
        List<PadInfo> padsWithData = new ArrayList<>();
        
        for (PadInfo padInfo : allPads.values()) {
            if (padInfo.hasData()) {
                padsWithData.add(padInfo);
            }
        }
        
        // Sort by pad number
        padsWithData.sort((p1, p2) -> Integer.compare(p1.getPadNumber(), p2.getPadNumber()));
        
        return padsWithData;
    }
    
    /**
     * Delete pad information
     */
    public void deletePadInfo(int padNumber) {
        Map<Integer, PadInfo> allPads = getAllPadInfo();
        allPads.remove(padNumber);
        
        String json = gson.toJson(allPads);
        prefs.edit().putString(KEY_PAD_DATA, json).apply();
    }
    
    /**
     * Clear all pad data
     */
    public void clearAllPadData() {
        prefs.edit().remove(KEY_PAD_DATA).apply();
    }
}