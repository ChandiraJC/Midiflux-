package com.example.midiflux;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.midiflux.model.PadInfo;
import com.example.midiflux.storage.PadStorageManager;

/**
 * Comprehensive dialog for editing pad properties with detailed instrument and effects information
 */
public class PadEditorDialog extends DialogFragment {
    
    private int padNumber;
    private PadStorageManager storageManager;
    
    // Basic pad info
    private EditText patchNameEditText;
    private RadioGroup instrumentTypeRadioGroup;
    private RadioButton singleInstrumentRadio;
    private RadioButton multiInstrumentRadio;
    
    // Single instrument views
    private LinearLayout singleInstrumentLayout;
    private EditText singleInstrumentNameEditText;
    private EditText singleInstrumentEffectsEditText;
    
    // Multi instrument views
    private LinearLayout multiInstrumentLayout;
    private EditText multiInstrumentNamesEditText;
    private EditText multiInstrumentLayerEffectsEditText;
    private EditText multiInstrumentMasterEffectsEditText;
    
    // Additional notes
    private EditText additionalNotesEditText;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            padNumber = getArguments().getInt("padNumber", 0);
        }
        
        storageManager = new PadStorageManager(getContext());
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_comprehensive_pad_editor, null);
        
        initializeViews(view);
        setupListeners();
        loadExistingData();
        
        // Create the dialog
        builder.setView(view);
        Dialog dialog = builder.create();
        
        // Make dialog larger
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        return dialog;
    }
    
    private void initializeViews(View view) {
        // Set up the dialog title
        TextView titleTextView = view.findViewById(R.id.dialog_title);
        titleTextView.setText("Edit Pad " + padNumber);
        
        // Basic pad info
        patchNameEditText = view.findViewById(R.id.patch_name_edit);
        instrumentTypeRadioGroup = view.findViewById(R.id.instrument_type_radio_group);
        singleInstrumentRadio = view.findViewById(R.id.single_instrument_radio);
        multiInstrumentRadio = view.findViewById(R.id.multi_instrument_radio);
        
        // Single instrument views
        singleInstrumentLayout = view.findViewById(R.id.single_instrument_layout);
        singleInstrumentNameEditText = view.findViewById(R.id.single_instrument_name_edit);
        singleInstrumentEffectsEditText = view.findViewById(R.id.single_instrument_effects_edit);
        
        // Multi instrument views
        multiInstrumentLayout = view.findViewById(R.id.multi_instrument_layout);
        multiInstrumentNamesEditText = view.findViewById(R.id.multi_instrument_names_edit);
        multiInstrumentLayerEffectsEditText = view.findViewById(R.id.multi_instrument_layer_effects_edit);
        multiInstrumentMasterEffectsEditText = view.findViewById(R.id.multi_instrument_master_effects_edit);
        
        // Additional notes
        additionalNotesEditText = view.findViewById(R.id.additional_notes_edit);
        
        // Buttons
        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        
        saveButton.setOnClickListener(v -> {
            savePadSettings();
            dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dismiss());
    }
    
    private void setupListeners() {
        // Set up radio group listener to show/hide appropriate layouts
        instrumentTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.single_instrument_radio) {
                singleInstrumentLayout.setVisibility(View.VISIBLE);
                multiInstrumentLayout.setVisibility(View.GONE);
            } else if (checkedId == R.id.multi_instrument_radio) {
                singleInstrumentLayout.setVisibility(View.GONE);
                multiInstrumentLayout.setVisibility(View.VISIBLE);
            }
        });
        
        // Set default selection
        singleInstrumentRadio.setChecked(true);
        singleInstrumentLayout.setVisibility(View.VISIBLE);
        multiInstrumentLayout.setVisibility(View.GONE);
    }
    
    private void loadExistingData() {
        PadInfo existingPadInfo = storageManager.getPadInfo(padNumber);
        if (existingPadInfo != null) {
            // Load basic info
            patchNameEditText.setText(existingPadInfo.getPatchName());
            
            // Set instrument type
            if (existingPadInfo.isMultiInstrument()) {
                multiInstrumentRadio.setChecked(true);
                singleInstrumentLayout.setVisibility(View.GONE);
                multiInstrumentLayout.setVisibility(View.VISIBLE);
                
                // Load multi instrument data
                if (existingPadInfo.getMultiInstrumentNames() != null) {
                    multiInstrumentNamesEditText.setText(existingPadInfo.getMultiInstrumentNames());
                }
                if (existingPadInfo.getMultiInstrumentLayerEffects() != null) {
                    multiInstrumentLayerEffectsEditText.setText(existingPadInfo.getMultiInstrumentLayerEffects());
                }
                if (existingPadInfo.getMultiInstrumentMasterEffects() != null) {
                    multiInstrumentMasterEffectsEditText.setText(existingPadInfo.getMultiInstrumentMasterEffects());
                }
            } else {
                singleInstrumentRadio.setChecked(true);
                singleInstrumentLayout.setVisibility(View.VISIBLE);
                multiInstrumentLayout.setVisibility(View.GONE);
                
                // Load single instrument data
                if (existingPadInfo.getSingleInstrumentName() != null) {
                    singleInstrumentNameEditText.setText(existingPadInfo.getSingleInstrumentName());
                }
                if (existingPadInfo.getSingleInstrumentEffects() != null) {
                    singleInstrumentEffectsEditText.setText(existingPadInfo.getSingleInstrumentEffects());
                }
            }
            
            // Load additional notes
            if (existingPadInfo.getAdditionalNotes() != null) {
                additionalNotesEditText.setText(existingPadInfo.getAdditionalNotes());
            }
        }
    }
    
    /**
     * Save the comprehensive pad settings
     */
    private void savePadSettings() {
        String patchName = patchNameEditText.getText().toString().trim();
        boolean isSingleInstrument = singleInstrumentRadio.isChecked();
        String additionalNotes = additionalNotesEditText.getText().toString().trim();
        
        // Create PadInfo object
        PadInfo padInfo = new PadInfo(padNumber);
        padInfo.setPatchName(patchName.isEmpty() ? "Patch " + padNumber : patchName);
        padInfo.setMultiInstrument(!isSingleInstrument);
        padInfo.setAdditionalNotes(additionalNotes);
        
        if (isSingleInstrument) {
            String instrumentName = singleInstrumentNameEditText.getText().toString().trim();
            String effects = singleInstrumentEffectsEditText.getText().toString().trim();
            
            padInfo.setSingleInstrumentName(instrumentName);
            padInfo.setSingleInstrumentEffects(effects);
        } else {
            String instrumentNames = multiInstrumentNamesEditText.getText().toString().trim();
            String layerEffects = multiInstrumentLayerEffectsEditText.getText().toString().trim();
            String masterEffects = multiInstrumentMasterEffectsEditText.getText().toString().trim();
            
            padInfo.setMultiInstrumentNames(instrumentNames);
            padInfo.setMultiInstrumentLayerEffects(layerEffects);
            padInfo.setMultiInstrumentMasterEffects(masterEffects);
        }
        
        // Save to storage
        storageManager.savePadInfo(padInfo);
        
        Toast.makeText(getContext(), "Pad " + padNumber + " settings saved successfully!", Toast.LENGTH_SHORT).show();
    }
}