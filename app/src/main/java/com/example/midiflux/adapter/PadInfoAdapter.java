package com.example.midiflux.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midiflux.R;
import com.example.midiflux.model.PadInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying pad information in RecyclerView
 */
public class PadInfoAdapter extends RecyclerView.Adapter<PadInfoAdapter.PadInfoViewHolder> {
    
    private List<PadInfo> padInfoList;
    private SimpleDateFormat dateFormat;
    
    public PadInfoAdapter() {
        this.padInfoList = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
    }
    
    public void setPadInfoList(List<PadInfo> padInfoList) {
        this.padInfoList = padInfoList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PadInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pad_info, parent, false);
        return new PadInfoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PadInfoViewHolder holder, int position) {
        PadInfo padInfo = padInfoList.get(position);
        holder.bind(padInfo);
    }
    
    @Override
    public int getItemCount() {
        return padInfoList.size();
    }
    
    class PadInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView padNumberTextView;
        private TextView patchNameTextView;
        private TextView instrumentTypeTextView;
        private TextView instrumentDetailsTextView;
        private TextView effectsTextView;
        private TextView additionalNotesTextView;
        private TextView lastModifiedTextView;
        
        public PadInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            padNumberTextView = itemView.findViewById(R.id.pad_number);
            patchNameTextView = itemView.findViewById(R.id.patch_name);
            instrumentTypeTextView = itemView.findViewById(R.id.instrument_type);
            instrumentDetailsTextView = itemView.findViewById(R.id.instrument_details);
            effectsTextView = itemView.findViewById(R.id.effects);
            additionalNotesTextView = itemView.findViewById(R.id.additional_notes);
            lastModifiedTextView = itemView.findViewById(R.id.last_modified);
        }
        
        public void bind(PadInfo padInfo) {
            padNumberTextView.setText("Pad " + padInfo.getPadNumber());
            patchNameTextView.setText(padInfo.getPatchName());
            
            if (padInfo.isMultiInstrument()) {
                instrumentTypeTextView.setText("Multi/Layered Instruments");
                
                // Show multi-instrument details
                StringBuilder instrumentDetails = new StringBuilder();
                if (padInfo.getMultiInstrumentNames() != null && !padInfo.getMultiInstrumentNames().trim().isEmpty()) {
                    instrumentDetails.append("Layers: ").append(padInfo.getMultiInstrumentNames());
                    instrumentDetailsTextView.setVisibility(View.VISIBLE);
                } else {
                    instrumentDetailsTextView.setVisibility(View.GONE);
                }
                instrumentDetailsTextView.setText(instrumentDetails.toString());
                
                // Show multi-instrument effects
                StringBuilder effects = new StringBuilder();
                if (padInfo.getMultiInstrumentLayerEffects() != null && !padInfo.getMultiInstrumentLayerEffects().trim().isEmpty()) {
                    effects.append("Layer Effects: ").append(padInfo.getMultiInstrumentLayerEffects()).append("\n");
                }
                if (padInfo.getMultiInstrumentMasterEffects() != null && !padInfo.getMultiInstrumentMasterEffects().trim().isEmpty()) {
                    effects.append("Master Effects: ").append(padInfo.getMultiInstrumentMasterEffects());
                }
                
                if (effects.length() > 0) {
                    effectsTextView.setVisibility(View.VISIBLE);
                    effectsTextView.setText(effects.toString().trim());
                } else {
                    effectsTextView.setVisibility(View.GONE);
                }
                
            } else {
                instrumentTypeTextView.setText("Single Instrument");
                
                // Show single instrument details
                String instrumentName = padInfo.getSingleInstrumentName();
                if (instrumentName != null && !instrumentName.trim().isEmpty()) {
                    instrumentDetailsTextView.setVisibility(View.VISIBLE);
                    instrumentDetailsTextView.setText("Instrument: " + instrumentName);
                } else {
                    instrumentDetailsTextView.setVisibility(View.GONE);
                }
                
                // Show single instrument effects
                String effects = padInfo.getSingleInstrumentEffects();
                if (effects != null && !effects.trim().isEmpty()) {
                    effectsTextView.setVisibility(View.VISIBLE);
                    effectsTextView.setText("Effects: " + effects);
                } else {
                    effectsTextView.setVisibility(View.GONE);
                }
            }
            
            // Show additional notes
            String additionalNotes = padInfo.getAdditionalNotes();
            if (additionalNotes != null && !additionalNotes.trim().isEmpty()) {
                additionalNotesTextView.setVisibility(View.VISIBLE);
                additionalNotesTextView.setText("Notes: " + additionalNotes);
            } else {
                additionalNotesTextView.setVisibility(View.GONE);
            }
            
            // Show last modified date
            lastModifiedTextView.setText("Last modified: " + dateFormat.format(padInfo.getLastModified()));
        }
    }
}