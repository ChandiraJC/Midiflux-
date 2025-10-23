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
 * Adapter for displaying PadInfo items in a RecyclerView
 */
public class PadInfoAdapter extends RecyclerView.Adapter<PadInfoAdapter.PadViewHolder> {
    private List<PadInfo> padList;
    
    public PadInfoAdapter() {
        this.padList = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public PadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pad_info, parent, false);
        return new PadViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PadViewHolder holder, int position) {
        PadInfo pad = padList.get(position);
        holder.bind(pad);
    }
    
    @Override
    public int getItemCount() {
        return padList.size();
    }
    
    public void setPadInfoList(List<PadInfo> padList) {
        this.padList = padList;
        notifyDataSetChanged();
    }
    
    static class PadViewHolder extends RecyclerView.ViewHolder {
        private TextView textPadNumber;
        private TextView textPadTitle;
        private TextView textPadDescription;
        private TextView textLastEdited;
        
        public PadViewHolder(@NonNull View itemView) {
            super(itemView);
            textPadNumber = itemView.findViewById(R.id.textPadNumber);
            textPadTitle = itemView.findViewById(R.id.textPadTitle);
            textPadDescription = itemView.findViewById(R.id.textPadDescription);
            textLastEdited = itemView.findViewById(R.id.textLastEdited);
        }
        
        public void bind(PadInfo pad) {
            // Pad number
            String padNumberText = "PAD " + pad.getPadNumber();
            textPadNumber.setText(padNumberText);
            
            // Title (patch name)
            textPadTitle.setText(pad.getPatchName() != null ? pad.getPatchName() : "");

            // Description - prefer additional notes, otherwise synth/effects summary
            String description = "";
            if (pad.getAdditionalNotes() != null && !pad.getAdditionalNotes().isEmpty()) {
                description = pad.getAdditionalNotes();
            } else if (pad.isMultiInstrument()) {
                int layers = (pad.getLayerEffects() != null) ? pad.getLayerEffects().size() : 0;
                description = "Multi-instrument (" + layers + " layers)";
            } else if (pad.getEffects() != null && !pad.getEffects().isEmpty()) {
                description = "Effects: " + pad.getEffects();
            }

            if (!description.isEmpty()) {
                textPadDescription.setVisibility(View.VISIBLE);
                textPadDescription.setText(description);
            } else {
                textPadDescription.setVisibility(View.GONE);
            }
            
            // Last edited
            if (pad.getLastEdited() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                String dateText = "Last edited: " + dateFormat.format(pad.getLastEdited());
                textLastEdited.setText(dateText);
                textLastEdited.setVisibility(View.VISIBLE);
            } else {
                textLastEdited.setVisibility(View.GONE);
            }
        }
    }
}