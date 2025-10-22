package com.example.midiflux;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midiflux.adapter.PadInfoAdapter;
import com.example.midiflux.model.PadInfo;
import com.example.midiflux.storage.PadStorageManager;

import java.util.List;

/**
 * Fragment for displaying saved pad information
 */
public class profileFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private PadInfoAdapter adapter;
    private PadStorageManager storageManager;

    public profileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageManager = new PadStorageManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.pads_recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        
        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PadInfoAdapter();
        recyclerView.setAdapter(adapter);
        
        // Load and display pad data
        loadPadData();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadPadData();
    }
    
    private void loadPadData() {
        List<PadInfo> padsWithData = storageManager.getPadsWithData();
        
        if (padsWithData.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No pad configurations saved yet.\n\nLong press on any pad in the Home tab to start configuring your pads!");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.setPadInfoList(padsWithData);
        }
    }
}