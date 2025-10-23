package com.example.midiflux.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple model class for storing a session of pad configurations
 */
public class PadSession {
    private String sessionId;
    private String sessionName;
    private Date createdDate;
    private Date lastModified;
    private List<PadInfo> pads;
    
    public PadSession() {
        this.pads = new ArrayList<>();
        this.createdDate = new Date();
        this.lastModified = new Date();
        this.sessionId = "session_" + System.currentTimeMillis();
    }
    
    public PadSession(String sessionName) {
        this();
        this.sessionName = sessionName;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getSessionName() {
        return sessionName;
    }
    
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
        this.lastModified = new Date();
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    public List<PadInfo> getPads() {
        return pads;
    }
    
    public void setPads(List<PadInfo> pads) {
        this.pads = pads;
        this.lastModified = new Date();
    }
    
    public void addPad(PadInfo padInfo) {
        if (this.pads == null) {
            this.pads = new ArrayList<>();
        }
        this.pads.add(padInfo);
        this.lastModified = new Date();
    }
    
    public int getPadCount() {
        return pads != null ? pads.size() : 0;
    }
    
    public boolean hasPads() {
        return pads != null && !pads.isEmpty();
    }
}