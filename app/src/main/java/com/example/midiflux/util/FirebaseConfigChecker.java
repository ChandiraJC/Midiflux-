package com.example.midiflux.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for checking and fixing Firebase configuration.
 */
public class FirebaseConfigChecker {
    private static final String TAG = "FirebaseConfigChecker";

    /**
     * Check if Firebase is properly configured and working.
     * @param context The application context
     */
    public static void checkFirebaseConfig(Context context) {
        Log.d(TAG, "Checking Firebase configuration...");
        
        try {
            // Check if Firebase is initialized
            FirebaseApp app = FirebaseApp.getInstance();
            Log.d(TAG, "Firebase app name: " + app.getName());
            
            // Get Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            
            // Configure Firestore settings for better reliability
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
            db.setFirestoreSettings(settings);
            
            // Try to write a test document
            Map<String, Object> testData = new HashMap<>();
            testData.put("timestamp", System.currentTimeMillis());
            testData.put("test", true);
            
            db.collection("firebase_tests")
                .document("config_test")
                .set(testData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Firebase is properly configured and working!");
                    Toast.makeText(context, "Firebase connection successful", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firebase write test failed: " + e.getMessage());
                    Toast.makeText(context, "Firebase connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    
                    // Check for common issues
                    checkCommonFirebaseIssues(context, e);
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Firebase initialization error: " + e.getMessage());
            Toast.makeText(context, "Firebase initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * Check for common Firebase configuration issues.
     * @param context The application context
     * @param e The exception that occurred
     */
    private static void checkCommonFirebaseIssues(Context context, Exception e) {
        String errorMsg = e.getMessage();
        if (errorMsg == null) {
            errorMsg = "Unknown error";
        }
        
        if (errorMsg.contains("PERMISSION_DENIED")) {
            Log.e(TAG, "⚠️ Firebase permission denied. Check your Firestore security rules.");
            Toast.makeText(context, 
                "Firebase permission denied. Check Firestore security rules to allow writes.", 
                Toast.LENGTH_LONG).show();
        } else if (errorMsg.contains("NETWORK_ERROR") || errorMsg.contains("Failed to connect")) {
            Log.e(TAG, "⚠️ Network error. Check your internet connection.");
            Toast.makeText(context, 
                "Network error. Make sure you have an active internet connection.", 
                Toast.LENGTH_LONG).show();
        } else if (errorMsg.contains("API key")) {
            Log.e(TAG, "⚠️ Invalid API key. Check your google-services.json file.");
            Toast.makeText(context, 
                "Firebase API key issue. Check your google-services.json file.", 
                Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Reset Firebase caches to fix potential stale data issues.
     * @param context The application context
     */
    public static void resetFirebaseCaches(Context context) {
        Log.d(TAG, "Resetting Firebase caches...");
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.clearPersistence()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "✅ Firebase caches successfully cleared!");
                Toast.makeText(context, "Firebase caches cleared", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Failed to clear Firebase caches: " + e.getMessage());
                Toast.makeText(context, "Failed to clear Firebase caches", Toast.LENGTH_SHORT).show();
            });
    }
}