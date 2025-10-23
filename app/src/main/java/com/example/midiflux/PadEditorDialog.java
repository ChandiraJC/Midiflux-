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
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.example.midiflux.storage.SessionStorageManager;
import com.example.midiflux.storage.TempSessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

/**
 * Dialog for editing pad properties with enhanced fields for instrument types and effects
 */
public class PadEditorDialog extends DialogFragment {
    
    // Interface for callbacks when pad settings are saved
    public interface OnPadSaveListener {
        void onPadSaved(int padNumber, PadInfo padInfo);
    }
    
    private int padNumber;
    private TextInputEditText patchNameEditText;
    private RadioGroup instrumentTypeGroup;
    private RadioButton singleInstrumentRadio;
    private RadioButton multiInstrumentRadio;
    private LinearLayout singleInstrumentSection;
    private LinearLayout multiInstrumentSection;
    private TextInputEditText effectsEditText;
    private TextInputEditText overallEffectsEditText;
    private TextInputEditText notesEditText;
    private Button addLayerButton;
    private LinearLayout layersContainer;
    
    private int layerCount = 1; // Start with one layer
    private TempSessionManager sessionManager;
    private PadInfo existingPadInfo;
    private OnPadSaveListener padSaveListener;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            padNumber = getArguments().getInt("padNumber", 0);
        }
        
        sessionManager = TempSessionManager.getInstance();
        existingPadInfo = sessionManager.getTempPad(padNumber);
        
        // Try to get the parent activity or fragment as a listener
        if (getParentFragment() instanceof OnPadSaveListener) {
            padSaveListener = (OnPadSaveListener) getParentFragment();
        } else if (getActivity() instanceof OnPadSaveListener) {
            padSaveListener = (OnPadSaveListener) getActivity();
        }
    }
    
    public void setOnPadSaveListener(OnPadSaveListener listener) {
        this.padSaveListener = listener;
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
        
        // Initialize all UI elements
        initializeUIElements(view);
        
        // Set up radio button listeners to show/hide sections
        instrumentTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_single_instrument) {
                singleInstrumentSection.setVisibility(View.VISIBLE);
                multiInstrumentSection.setVisibility(View.GONE);
            } else {
                singleInstrumentSection.setVisibility(View.GONE);
                multiInstrumentSection.setVisibility(View.VISIBLE);
            }
        });
        
        // Set up Add Layer button
        addLayerButton.setOnClickListener(v -> addNewLayer());
        
        // Set up save and cancel buttons
        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);
        
        saveButton.setOnClickListener(v -> {
            savePadSettings();
            dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dismiss());
        
        // If we have existing data, populate the form
        if (existingPadInfo != null) {
            populateFormWithExistingData();
        }
        
        // Create the dialog
        builder.setView(view);
        return builder.create();
    }
    
    /**
     * Initialize all UI elements
     */
    private void initializeUIElements(View view) {
        patchNameEditText = view.findViewById(R.id.patch_name_edit);
        instrumentTypeGroup = view.findViewById(R.id.instrument_type_group);
        singleInstrumentRadio = view.findViewById(R.id.radio_single_instrument);
        multiInstrumentRadio = view.findViewById(R.id.radio_multi_instrument);
        singleInstrumentSection = view.findViewById(R.id.single_instrument_section);
        multiInstrumentSection = view.findViewById(R.id.multi_instrument_section);
        effectsEditText = view.findViewById(R.id.effects_edit);
        overallEffectsEditText = view.findViewById(R.id.overall_effects_edit);
        notesEditText = view.findViewById(R.id.notes_edit);
        addLayerButton = view.findViewById(R.id.add_layer_button);
        layersContainer = view.findViewById(R.id.layers_container);
    }
    
    /**
     * Populate the form with existing data
     */
    private void populateFormWithExistingData() {
        if (existingPadInfo.getPatchName() != null) {
            patchNameEditText.setText(existingPadInfo.getPatchName());
        }
        
        if (existingPadInfo.isMultiInstrument()) {
            multiInstrumentRadio.setChecked(true);
            singleInstrumentSection.setVisibility(View.GONE);
            multiInstrumentSection.setVisibility(View.VISIBLE);
            
            // Load layers
            if (existingPadInfo.getLayerEffects() != null) {
                // Log what we have
                if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Populating form with " + existingPadInfo.getLayerEffects().size() + " existing layer effects");
                for (Map.Entry<String, String> entry : existingPadInfo.getLayerEffects().entrySet()) {
                    if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "  Layer: " + entry.getKey() + " = " + entry.getValue());
                }
                
                // Clear default layer
                layersContainer.removeAllViews();
                layerCount = 0;
                
                // Add each saved layer - even if the map is empty, ensure we have at least one layer
                if (!existingPadInfo.getLayerEffects().isEmpty()) {
                    for (Map.Entry<String, String> entry : existingPadInfo.getLayerEffects().entrySet()) {
                        addNewLayer();
                        
                        // Find the newly added layer and set its values
                        View layerView = layersContainer.getChildAt(layerCount - 1);
                        TextInputEditText layerNameEdit = layerView.findViewById(R.id.layer_name);
                        TextInputEditText layerEffectsEdit = layerView.findViewById(R.id.layer_effects);

                        // Process key for consistent display
                        String layerKey = entry.getKey();
                        
                        if (layerNameEdit != null) {
                            // If the key is already a number, use it directly
                            if (layerKey.matches("\\d+")) {
                                layerNameEdit.setText(layerKey);
                            } 
                            // If it starts with "Layer ", extract the number
                            else if (layerKey.startsWith("Layer ") && layerKey.substring(6).matches("\\d+")) {
                                layerNameEdit.setText(layerKey.substring(6));
                            }
                            // Otherwise use as-is
                            else {
                                layerNameEdit.setText(layerKey);
                            }
                        }
                        
                        // Set the effects value
                        if (layerEffectsEdit != null) {
                            layerEffectsEdit.setText(entry.getValue());
                        }
                        
                if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Populated layer " + layerCount + " with name=" + 
                    entry.getKey() + ", effects=" + entry.getValue());
                    }
                } else {
                    // Add default layers if none exist
                    addNewLayer(); // At least one layer for multi-instrument
                    if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Added default layer since no layers existed");
                }
            }
            
            // Set overall effects
            if (existingPadInfo.getOverallEffects() != null) {
                overallEffectsEditText.setText(existingPadInfo.getOverallEffects());
            }
        } else {
            singleInstrumentRadio.setChecked(true);
            if (existingPadInfo.getEffects() != null) {
                effectsEditText.setText(existingPadInfo.getEffects());
            }
        }
        
        if (existingPadInfo.getAdditionalNotes() != null) {
            notesEditText.setText(existingPadInfo.getAdditionalNotes());
        }
    }
    
    /**
     * Add a new layer to the multi-instrument section
     */
    private void addNewLayer() {
        layerCount++;
        
        // Inflate the layer layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View layerView = inflater.inflate(R.layout.item_layer, null);
        
        // Generate unique IDs for the layer's views
        int layerNameId = generateViewId();
        int layerEffectsId = generateViewId();
        
        // Find views in the inflated layout
        TextInputEditText layerNameEdit = layerView.findViewById(R.id.layer_name);
        TextInputEditText layerEffectsEdit = layerView.findViewById(R.id.layer_effects);
        TextView layerNumberText = layerView.findViewById(R.id.layer_number);
        
    // Do not reassign view IDs here. Keep the original R.id.* IDs so
    // layerView.findViewById(R.id.layer_name) and R.id.layer_effects work
    // correctly when saving. Update the visible header and default name.
    layerNumberText.setText("Layer " + layerCount);

    // Pre-fill the layer name input with the numeric index (user can edit)
    layerNameEdit.setText(Integer.toString(layerCount));
        
        // Add the layer to the container
        layersContainer.addView(layerView);
        
    if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Added new layer: " + layerCount);
    }
    
    /**
     * Get the ID for a layer name EditText based on the layer number
     */
    private int getLayerNameEditId(int layerNumber) {
        if (layerNumber == 1) {
            return R.id.layer_name_1;
        }
        // For dynamically added layers, we use the stored ID
        return layerNumber + 1000; // Just an offset to avoid conflicts
    }
    
    /**
     * Get the ID for a layer effects EditText based on the layer number
     */
    private int getLayerEffectsEditId(int layerNumber) {
        if (layerNumber == 1) {
            return R.id.layer_effects_1;
        }
        // For dynamically added layers, we use the stored ID
        return layerNumber + 2000; // Just an offset to avoid conflicts
    }
    
    /**
     * Generate a unique view ID
     */
    private int generateViewId() {
        return View.generateViewId();
    }
    
    /**
     * Save the pad settings
     */
    private void savePadSettings() {
        // Create a new PadInfo object or use the existing one
        PadInfo padInfo = existingPadInfo != null ? existingPadInfo : new PadInfo(padNumber);
        
        // Set basic info
        padInfo.setPatchName(patchNameEditText.getText().toString().trim());
        padInfo.setAdditionalNotes(notesEditText.getText().toString().trim());
        
        // Determine instrument type and set appropriate fields
        boolean isMultiInstrument = multiInstrumentRadio.isChecked();
        padInfo.setMultiInstrument(isMultiInstrument);
        
        if (isMultiInstrument) {
            // Create a new HashMap for layer effects
            Map<String, String> newLayerEffects = new HashMap<>();
            
            // Process each layer
            for (int i = 0; i < layersContainer.getChildCount(); i++) {
                View layerView = layersContainer.getChildAt(i);

                // Try to find the input fields for the layer. The default first
                // layer in the XML uses ids layer_name_1 / layer_effects_1 while
                // dynamically-inflated ones use layer_name / layer_effects. Try
                // both and fall back to scanning the child view for the first
                // TextInputEditText if necessary.
                TextInputEditText layerNameEdit = layerView.findViewById(R.id.layer_name);
                TextInputEditText layerEffectsEdit = layerView.findViewById(R.id.layer_effects);

                if (layerNameEdit == null) layerNameEdit = layerView.findViewById(R.id.layer_name_1);
                if (layerEffectsEdit == null) layerEffectsEdit = layerView.findViewById(R.id.layer_effects_1);

                // Final fallback: search for the first TextInputEditText inside this layerView
                if (layerNameEdit == null) layerNameEdit = findFirstTextInput(layerView);
                if (layerEffectsEdit == null) layerEffectsEdit = findFirstTextInputForEffects(layerView, layerNameEdit);

                // Read values directly from the inputs
                String rawLayerName = "";
                String layerEffects = "";
                if (layerNameEdit != null) rawLayerName = layerNameEdit.getText() == null ? "" : layerNameEdit.getText().toString().trim();
                if (layerEffectsEdit != null) layerEffects = layerEffectsEdit.getText() == null ? "" : layerEffectsEdit.getText().toString().trim();
                
                // Format the layer name consistently for storage
                String layerName;
                if (rawLayerName.isEmpty()) {
                    // Default to a numeric value if empty
                    layerName = Integer.toString(i + 1);
                } else if (rawLayerName.matches("\\d+")) {
                    // If it's just a number, use that number
                    layerName = rawLayerName;
                } else if (rawLayerName.startsWith("Layer ")) {
                    // If it already has "Layer " prefix, remove it to avoid duplication
                    layerName = rawLayerName.substring(6).trim();
                } else {
                    // Otherwise use as-is
                    layerName = rawLayerName;
                }
                
                // Add the layer effect to our new map - ALWAYS use the actual entered effects value
                newLayerEffects.put(layerName, layerEffects);
                
                // Log raw values to help verify what the user entered
                if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Saved layer raw values: index=" + i + ", key='" + layerName + "', value='" + layerEffects + "'");
                if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Added layer effect: " + layerName + " = " + layerEffects);
            }
            
            // Set the new layer effects map
            padInfo.setLayerEffects(newLayerEffects);
            
            // Debug log the resulting layer effects
                if (padInfo.getLayerEffects() != null) {
                    if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) {
                        Log.d("PadEditorDialog", "Final layer effects count: " + padInfo.getLayerEffects().size());
                        for (Map.Entry<String, String> entry : padInfo.getLayerEffects().entrySet()) {
                            Log.d("PadEditorDialog", "  Layer: " + entry.getKey() + " = " + entry.getValue());
                        }
                    }
                } else {
                    if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "Final layer effects is null!");
                }
            
            // Save overall effects
            padInfo.setOverallEffects(overallEffectsEditText.getText().toString().trim());
        } else {
            // Save single instrument effects
            padInfo.setEffects(effectsEditText.getText().toString().trim());
        }
        
        // Save the pad info to the temporary session manager only
        // Do NOT create autosave documents - user will explicitly save sessions later
        sessionManager.savePadTemporary(padNumber, padInfo);
        
      if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "savePadSettings: saved pad=" + padNumber + " patchName=" + padInfo.getPatchName() + 
          " to temporary manager. Session will be saved when user clicks 'Save Session'");
              
        // Notify any listeners that a pad was saved
        if (padSaveListener != null) {
            padSaveListener.onPadSaved(padNumber, padInfo);
            if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "savePadSettings: notified listener of pad save");
        } else {
            if (android.util.Log.isLoggable("PadEditorDialog", android.util.Log.DEBUG)) Log.d("PadEditorDialog", "savePadSettings: no listener to notify");
        }

        Toast.makeText(getContext(), "Pad " + padNumber + " settings saved", Toast.LENGTH_SHORT).show();
    }

    // Helper: find the first TextInputEditText inside a view (for fallback)
    private TextInputEditText findFirstTextInput(View root) {
        if (root == null) return null;
        if (root instanceof TextInputEditText) return (TextInputEditText) root;
        if (!(root instanceof ViewGroup)) return null;
        ViewGroup vg = (ViewGroup) root;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View c = vg.getChildAt(i);
            TextInputEditText found = findFirstTextInput(c);
            if (found != null) return found;
        }
        return null;
    }

    // Helper: find a likely effects TextInputEditText (the second text input in the layer)
    private TextInputEditText findFirstTextInputForEffects(View root, TextInputEditText nameField) {
        if (root == null) return null;
        if (!(root instanceof ViewGroup)) return null;
        ViewGroup vg = (ViewGroup) root;
        boolean sawName = false;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View c = vg.getChildAt(i);
            if (c == nameField) {
                sawName = true;
                continue;
            }
            TextInputEditText t = findFirstTextInput(c);
            if (t != null) {
                if (sawName) return t; // return the next text input after name
            }
        }
        // fallback to any text input
        return findFirstTextInput(root);
    }
}