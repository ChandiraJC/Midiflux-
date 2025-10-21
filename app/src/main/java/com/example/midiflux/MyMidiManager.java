package com.example.midiflux;

import android.content.Context;
import android.media.midi.MidiManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.os.Handler;
import android.util.Log;
import java.io.IOException;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyMidiManager {
    private static final String TAG = "MyMidiManager";
    private MidiManager midiManager;
    private MidiDevice midiDevice;
    private MidiInputPort inputPort;
    private boolean isConnected = false;

    public MyMidiManager(Context context) {
        midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        openMidiDevice();
    }

    private void openMidiDevice() {
        if (midiManager == null) {
            Log.e(TAG, "MIDI service not available");
            return;
        }
        
        MidiDeviceInfo[] infos = midiManager.getDevices();
        if (infos.length > 0) {
            Log.d(TAG, "Found " + infos.length + " MIDI devices");
            
            midiManager.openDevice(infos[0], new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice device) {
                    if (device == null) {
                        Log.e(TAG, "Could not open MIDI device");
                        return;
                    }
                    
                    midiDevice = device;
                    inputPort = device.openInputPort(0);
                    if (inputPort != null) {
                        isConnected = true;
                        Log.d(TAG, "MIDI device connected and ready");
                    } else {
                        Log.e(TAG, "Could not open input port");
                    }
                }
            }, new Handler());
        } else {
            Log.w(TAG, "No MIDI devices found");
        }
    }

    public void sendCC(int channel, int cc, int value) {
        byte[] buffer = new byte[32];
        int numBytes = 0;
        long now = System.nanoTime();

        // Create a standard MIDI Control Change message
        buffer[numBytes++] = (byte)(0xB0 | channel); // CC on channel
        buffer[numBytes++] = (byte)cc;               // CC number
        buffer[numBytes++] = (byte)value;            // value

        // Log MIDI message to Firestore for testing
        Map<String, Object> midiData = new HashMap<>();
        midiData.put("bytes", Arrays.toString(Arrays.copyOf(buffer, numBytes)));
        midiData.put("channel", channel);
        midiData.put("cc", cc);
        midiData.put("value", value);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("midi_test").add(midiData);

        if (inputPort == null) {
            Log.e(TAG, "Cannot send MIDI: input port is null");
            return;
        }

        try {
            // Send via MidiInputPort (which extends OutputStream)
            inputPort.send(buffer, 0, numBytes, now);
            Log.d(TAG, "Sent MIDI CC: ch=" + channel + " cc=" + cc + " val=" + value);
        } catch (IOException e) {
            Log.e(TAG, "Error sending MIDI message: " + e.getMessage(), e);
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void close() {
        if (inputPort != null) {
            try {
                inputPort.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing input port: " + e.getMessage());
            }
            inputPort = null;
        }
        
        if (midiDevice != null) {
            try {
                midiDevice.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing MIDI device: " + e.getMessage());
            }
            midiDevice = null;
        }
        
        isConnected = false;
    }
}
