package com.example.midiflux;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog for editing pad properties
 */
public class PadEditorDialog extends DialogFragment {
    
    private int padNumber;
    private EditText patchNameEditText;
    private EditText instrumentEditText;
    private EditText notesEditText;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            padNumber = getArguments().getInt("padNumber", 0);
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pad_editor, null);
        
        // Set up the dialog title and views
        TextView titleTextView = view.findViewById(R.id.dialog_title);
        titleTextView.setText("Edit Pad " + padNumber);
        
        patchNameEditText = view.findViewById(R.id.patch_name_edit);
        instrumentEditText = view.findViewById(R.id.instrument_edit);
        notesEditText = view.findViewById(R.id.notes_edit);
        
        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        
        // Set up listeners
        saveButton.setOnClickListener(v -> {
            savePadSettings();
            dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dismiss());
        
        // Create the dialog
        builder.setView(view);
        return builder.create();
    }
    
    /**
     * Save the pad settings
     */
    private void savePadSettings() {
        String patchName = patchNameEditText.getText().toString().trim();
        String instrument = instrumentEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();
        
        // TODO: Implement actual saving to Firestore or local storage
        
        Toast.makeText(getContext(), "Pad " + padNumber + " settings saved", Toast.LENGTH_SHORT).show();
    }
}