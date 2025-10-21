package com.example.midiflux;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirestoreTest {
    public static void sendTestMessage() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "Hello Firestore! Test message from FirestoreTest.java");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("testCollection").add(testData);
    }
}
