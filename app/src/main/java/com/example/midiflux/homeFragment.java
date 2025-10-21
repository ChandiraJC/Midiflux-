






package com.example.midiflux;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class homeFragment extends Fragment {
    private MyMidiManager midiManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        midiManager = new MyMidiManager(requireContext());

        int[] padIds = {R.id.patch1, R.id.patch2, R.id.patch3, R.id.patch4, R.id.patch5,
                        R.id.patch6, R.id.patch7, R.id.patch8, R.id.patch9, R.id.patch10};

        for (int i = 0; i < padIds.length; i++) {
            final int cc = 20 + i; // CC#20 to CC#29
            View pad = view.findViewById(padIds[i]);
            pad.setOnClickListener(v -> midiManager.sendCC(0, cc, 127)); // channel 1, value 127
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (midiManager != null) {
            midiManager.close();
        }
    }
}