package com.example.midiflux;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
                    java.net.InetAddress address = java.net.InetAddress.getByName("192.168.1.100"); // <-- Replace with your PC's IP
                    int port = 5005;
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

            view.findViewById(R.id.patch1).setOnClickListener(v -> sendPadMessage("PAD1"));
            view.findViewById(R.id.patch2).setOnClickListener(v -> sendPadMessage("PAD2"));
            view.findViewById(R.id.patch3).setOnClickListener(v -> sendPadMessage("PAD3"));
            view.findViewById(R.id.patch4).setOnClickListener(v -> sendPadMessage("PAD4"));
            view.findViewById(R.id.patch5).setOnClickListener(v -> sendPadMessage("PAD5"));
            view.findViewById(R.id.patch6).setOnClickListener(v -> sendPadMessage("PAD6"));
            view.findViewById(R.id.patch7).setOnClickListener(v -> sendPadMessage("PAD7"));
            view.findViewById(R.id.patch8).setOnClickListener(v -> sendPadMessage("PAD8"));
            view.findViewById(R.id.patch9).setOnClickListener(v -> sendPadMessage("PAD9"));
            view.findViewById(R.id.patch10).setOnClickListener(v -> sendPadMessage("PAD10"));
        }
    }
}