package com.example.midiflux.model;

import java.util.Date;

/**
 * Model class for storing comprehensive pad information
 */
public class PadInfo {
    private int padNumber;
    private String patchName;
    private boolean isMultiInstrument;
    
    // Single instrument fields
    private String singleInstrumentName;
    private String singleInstrumentEffects;
    
    // Multi instrument fields
    private String multiInstrumentNames;
    private String multiInstrumentLayerEffects;
    private String multiInstrumentMasterEffects;
    
    // Additional notes
    private String additionalNotes;
    private Date lastModified;
    
    public PadInfo() {
        this.lastModified = new Date();
    }
    
    public PadInfo(int padNumber) {
        this();
        this.padNumber = padNumber;
        this.patchName = "Patch " + padNumber;
    }
    
    // Getters and Setters
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
        this.lastModified = new Date();
    }
    
    public boolean isMultiInstrument() {
        return isMultiInstrument;
    }
    
    public void setMultiInstrument(boolean multiInstrument) {
        isMultiInstrument = multiInstrument;
        this.lastModified = new Date();
    }
    
    public String getSingleInstrumentName() {
        return singleInstrumentName;
    }
    
    public void setSingleInstrumentName(String singleInstrumentName) {
        this.singleInstrumentName = singleInstrumentName;
        this.lastModified = new Date();
    }
    
    public String getSingleInstrumentEffects() {
        return singleInstrumentEffects;
    }
    
    public void setSingleInstrumentEffects(String singleInstrumentEffects) {
        this.singleInstrumentEffects = singleInstrumentEffects;
        this.lastModified = new Date();
    }
    
    public String getMultiInstrumentNames() {
        return multiInstrumentNames;
    }
    
    public void setMultiInstrumentNames(String multiInstrumentNames) {
        this.multiInstrumentNames = multiInstrumentNames;
        this.lastModified = new Date();
    }
    
    public String getMultiInstrumentLayerEffects() {
        return multiInstrumentLayerEffects;
    }
    
    public void setMultiInstrumentLayerEffects(String multiInstrumentLayerEffects) {
        this.multiInstrumentLayerEffects = multiInstrumentLayerEffects;
        this.lastModified = new Date();
    }
    
    public String getMultiInstrumentMasterEffects() {
        return multiInstrumentMasterEffects;
    }
    
    public void setMultiInstrumentMasterEffects(String multiInstrumentMasterEffects) {
        this.multiInstrumentMasterEffects = multiInstrumentMasterEffects;
        this.lastModified = new Date();
    }
    
    public String getAdditionalNotes() {
        return additionalNotes;
    }
    
    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
        this.lastModified = new Date();
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    /**
     * Check if this pad has any meaningful data entered
     */
    public boolean hasData() {
        return (patchName != null && !patchName.trim().isEmpty() && !patchName.equals("Patch " + padNumber)) ||
               (singleInstrumentName != null && !singleInstrumentName.trim().isEmpty()) ||
               (singleInstrumentEffects != null && !singleInstrumentEffects.trim().isEmpty()) ||
               (multiInstrumentNames != null && !multiInstrumentNames.trim().isEmpty()) ||
               (multiInstrumentLayerEffects != null && !multiInstrumentLayerEffects.trim().isEmpty()) ||
               (multiInstrumentMasterEffects != null && !multiInstrumentMasterEffects.trim().isEmpty()) ||
               (additionalNotes != null && !additionalNotes.trim().isEmpty());
    }
}