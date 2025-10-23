package com.example.midiflux.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing pad configuration information
 */
public class PadInfo {
    private int padNumber;
    private String patchName;         // Previously title
    private boolean isMultiInstrument; // Whether this is a multi-instrument or single instrument
    private String effects;           // Effects for single instrument
    private Map<String, String> layerEffects; // Effects per layer for multi-instrument (layerName -> effects)
    private String overallEffects;    // Effects applied to the overall multi-instrument
    private String additionalNotes;   // Additional notes about knob values, etc. (previously description)
    private Date lastEdited;
    private String midiCommand;
    private int color; // Optional color for UI display
    
    public PadInfo() {
        this.lastEdited = new Date();
        this.isMultiInstrument = false;
        this.layerEffects = new HashMap<>(); // Initialize with empty HashMap
    }
    
    public PadInfo(int padNumber) {
        this();
        this.padNumber = padNumber;
    }
    
    public PadInfo(int padNumber, String patchName, String additionalNotes) {
        this(padNumber);
        this.patchName = patchName;
        this.additionalNotes = additionalNotes;
    }
    
    // Getters and setters
    public int getPadNumber() {
        return padNumber;
    }
    
    public void setPadNumber(int padNumber) {
        this.padNumber = padNumber;
    }
    
    public String getPatchName() {
        return patchName;
    }
    
    public void setPatchName(String patchName) {
        this.patchName = patchName;
        this.lastEdited = new Date();
    }
    
    public boolean isMultiInstrument() {
        return isMultiInstrument;
    }
    
    public void setMultiInstrument(boolean multiInstrument) {
        isMultiInstrument = multiInstrument;
        this.lastEdited = new Date();
    }
    
    public String getEffects() {
        return effects;
    }
    
    public void setEffects(String effects) {
        this.effects = effects;
        this.lastEdited = new Date();
    }
    
    public Map<String, String> getLayerEffects() {
        // Ensure we never return null
        if (layerEffects == null) {
            layerEffects = new HashMap<>();
        }
        return layerEffects;
    }
    
    public void setLayerEffects(Map<String, String> layerEffects) {
        this.layerEffects = layerEffects;
        this.lastEdited = new Date();
    }
    
    public void addLayerEffect(String layerName, String effects) {
        if (this.layerEffects == null) {
            this.layerEffects = new HashMap<>();
        }
        // Ensure layerName is not null or empty
        if (layerName == null || layerName.isEmpty()) {
            layerName = "Layer " + (this.layerEffects.size() + 1);
        }
        // Ensure effects is not null (empty string is ok)
        if (effects == null) {
            effects = "";
        }
        this.layerEffects.put(layerName, effects);
        this.lastEdited = new Date();
    }
    
    public String getOverallEffects() {
        return overallEffects;
    }
    
    public void setOverallEffects(String overallEffects) {
        this.overallEffects = overallEffects;
        this.lastEdited = new Date();
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
        this.lastEdited = new Date();
    }
    
    public Date getLastEdited() {
        return lastEdited;
    }
    
    public void setLastEdited(Date lastEdited) {
        this.lastEdited = lastEdited;
    }
    
    public String getMidiCommand() {
        return midiCommand;
    }
    
    public void setMidiCommand(String midiCommand) {
        this.midiCommand = midiCommand;
        this.lastEdited = new Date();
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    /**
     * Check if this pad has meaningful data
     */
    public boolean hasData() {
        return (patchName != null && !patchName.isEmpty()) || 
               (additionalNotes != null && !additionalNotes.isEmpty()) ||
               (effects != null && !effects.isEmpty()) ||
               (overallEffects != null && !overallEffects.isEmpty()) ||
               (layerEffects != null && !layerEffects.isEmpty()) ||
               (midiCommand != null && !midiCommand.isEmpty());
    }
    
    /**
     * Generate a display name for this pad
     */
    public String getDisplayName() {
        if (patchName != null && !patchName.isEmpty()) {
            return patchName;
        }
        return "Pad " + padNumber;
    }
}