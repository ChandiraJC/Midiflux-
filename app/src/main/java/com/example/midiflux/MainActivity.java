package com.example.midiflux;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import android.util.Log;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Write a test document
            DocumentReference docRef = db.collection("testCollection").document("testDocument");
            HashMap<String, Object> data = new HashMap<>();
            data.put("message", "Hellooooo Firestore!");
            docRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreTest", "Document written successfully!");
                    // Read the test document
                    docRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d("FirestoreTest", "Read from Firestore: " + documentSnapshot.getString("message"));
                        } else {
                            Log.d("FirestoreTest", "No such document!");
                        }
                    }).addOnFailureListener(e -> Log.e("FirestoreTest", "Read failed", e));
                })
                .addOnFailureListener(e -> Log.e("FirestoreTest", "Write failed", e));
    }
}