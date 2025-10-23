package com.example.midiflux.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.midiflux.R;
import com.example.midiflux.model.PadSession;
import com.example.midiflux.model.PadInfo;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {
    private List<PadSession> sessions;
    private OnSessionClickListener listener;
    private OnSessionDeleteListener deleteListener;

    public interface OnSessionClickListener {
        void onSessionClick(PadSession session);
    }
    
    public interface OnSessionDeleteListener {
        void onSessionDelete(PadSession session, int position);
    }

    public SessionAdapter(List<PadSession> sessions, OnSessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }
    
    public void setOnSessionDeleteListener(OnSessionDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_card, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        PadSession session = sessions.get(position);
        holder.bind(session, position);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void updateSessions(List<PadSession> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }
    
    public void removeSession(int position) {
        if (position >= 0 && position < sessions.size()) {
            sessions.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public List<PadSession> getSessions() {
        return this.sessions;
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private TextView textSessionName;
        private TextView textSessionDate;
        private TextView textPadCount;
        private TextView textPadPreview;
        private ImageButton buttonDeleteSession;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            textSessionName = itemView.findViewById(R.id.textSessionName);
            textSessionDate = itemView.findViewById(R.id.textSessionDate);
            textPadCount = itemView.findViewById(R.id.textPadCount);
            textPadPreview = itemView.findViewById(R.id.textPadPreview);
            buttonDeleteSession = itemView.findViewById(R.id.buttonDeleteSession);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onSessionClick(sessions.get(position));
                    }
                }
            });
        }

        public void bind(PadSession session, int position) {
            // Session name
            textSessionName.setText(session.getSessionName() != null ? 
                session.getSessionName() : "Unnamed Session");

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateText = "Created: " + dateFormat.format(session.getCreatedDate());
            textSessionDate.setText(dateText);

            // Pad count
            int padCount = session.getPadCount();
            String padCountText = padCount + " Pad" + (padCount != 1 ? "s" : "") + " Configured";
            textPadCount.setText(padCountText);

            // Pad preview - show which pads are configured
            StringBuilder preview = new StringBuilder("Pads: ");
            List<PadInfo> pads = session.getPads();
            if (pads != null && !pads.isEmpty()) {
                for (int i = 0; i < pads.size(); i++) {
                    if (i > 0) preview.append(", ");
                    preview.append(pads.get(i).getPadNumber());
                    // Limit preview to avoid too long text
                    if (i >= 6) {
                        preview.append("...");
                        break;
                    }
                }
            } else {
                preview.append("None");
            }
            textPadPreview.setText(preview.toString());
            
            // Set up delete button
            buttonDeleteSession.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onSessionDelete(session, position);
                }
            });
        }
    }
}