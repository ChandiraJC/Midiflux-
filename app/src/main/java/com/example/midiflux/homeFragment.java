package com.example.midiflux;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

        // --- UDP send logic ---
        private void sendPadMessage(String pad) {
            new Thread(() -> {
                try {
                    String message = pad; // e.g., "PAD1"
                    java.net.DatagramSocket socket = new java.net.DatagramSocket();
                    java.net.InetAddress address = java.net.InetAddress.getByName("172.20.10.5"); // <-- Replace with your PC's IP
                    int port = 6000;
                    byte[] buf = message.getBytes();
                    java.net.DatagramPacket packet = new java.net.DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

    public homeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment homeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_home, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Set up click and long-press listeners for each pad
            setupPad(view.findViewById(R.id.patch1), "PAD1", 1);
            setupPad(view.findViewById(R.id.patch2), "PAD2", 2);
            setupPad(view.findViewById(R.id.patch3), "PAD3", 3);
            setupPad(view.findViewById(R.id.patch4), "PAD4", 4);
            setupPad(view.findViewById(R.id.patch5), "PAD5", 5);
            setupPad(view.findViewById(R.id.patch6), "PAD6", 6);
            setupPad(view.findViewById(R.id.patch7), "PAD7", 7);
            setupPad(view.findViewById(R.id.patch8), "PAD8", 8);
            setupPad(view.findViewById(R.id.patch9), "PAD9", 9);
            setupPad(view.findViewById(R.id.patch10), "PAD10", 10);
        }
        
        /**
         * Set up click and long-press listeners for a pad
         * @param padView The pad view
         * @param padMessage The message to send on click
         * @param padNumber The pad number
         */
        private void setupPad(View padView, String padMessage, int padNumber) {
            // Set up click listener
            padView.setOnClickListener(v -> sendPadMessage(padMessage));
            
            // Set up long-press listener
            padView.setOnLongClickListener(v -> {
                showPadEditorDialog(padNumber);
                return true; // Return true to indicate the long-press was handled
            });
        }
        
        /**
         * Show the pad editor dialog
         * @param padNumber The pad number
         */
        private void showPadEditorDialog(int padNumber) {
            // Create and show the pad editor dialog
            PadEditorDialog dialog = new PadEditorDialog();
            Bundle args = new Bundle();
            args.putInt("padNumber", padNumber);
            dialog.setArguments(args);
            dialog.show(getChildFragmentManager(), "PadEditorDialog");
        }
}