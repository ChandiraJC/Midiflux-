package com.example.midiflux.storage;

import com.example.midiflux.model.PadInfo;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

/**
 * Temporary storage for current editing session
 * Holds pad data in memory until user saves as session
 */
public class TempSessionManager {
    private static final String TAG = "TempSessionManager";
    private static TempSessionManager instance;
    private Map<Integer, PadInfo> currentPads;
    
    private TempSessionManager() {
        currentPads = new HashMap<>();
    }
    
    public static TempSessionManager getInstance() {
        if (instance == null) {
            instance = new TempSessionManager();
        }
        return instance;
    }
    
    /**
     * Save pad data temporarily (in memory only)
     */
    public void savePadTemporary(int padNumber, PadInfo padInfo) {
        currentPads.put(padNumber, padInfo);
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "savePadTemporary: pad=" + padNumber + " hasData=" + (padInfo!=null && padInfo.hasData()));
    }
    
    /**
     * Get temporary pad data
     */
    public PadInfo getTempPad(int padNumber) {
        return currentPads.get(padNumber);
    }
    
    /**
     * Check if pad has temporary data
     */
    public boolean hasTempData(int padNumber) {
        PadInfo pad = currentPads.get(padNumber);
        return pad != null && pad.hasData();
    }
    
    /**
     * Get all current pads with data
     */
    public Map<Integer, PadInfo> getAllCurrentPads() {
        Map<Integer, PadInfo> padsWithData = new HashMap<>();
        for (Map.Entry<Integer, PadInfo> entry : currentPads.entrySet()) {
            if (entry.getValue() != null && entry.getValue().hasData()) {
                padsWithData.put(entry.getKey(), entry.getValue());
            }
        }
        return padsWithData;
    }
    
    /**
     * Get count of pads with data
     */
    public int getPadWithDataCount() {
        return getAllCurrentPads().size();
    }
    
    /**
     * Clear all temporary data
     */
    public void clearAllTempData() {
        int padCount = getPadWithDataCount(); // Record count before clearing
        currentPads.clear();
        if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "clearAllTempData: cleared " + padCount + " pads, hasAnyTempData=" + hasAnyTempData());
    }
    
    /**
     * Check if any pad has temporary data
     */
    public boolean hasAnyTempData() {
        return !getAllCurrentPads().isEmpty();
    }
}