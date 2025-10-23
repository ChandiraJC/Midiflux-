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

import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOGIN = 1001;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up fragment switching
        ImageButton profileButton = findViewById(R.id.profilebutton);
        ImageButton homeButton = findViewById(R.id.homebutton);
        ImageButton settingsButton = findViewById(R.id.settingsbutton);
        ImageButton keypadButton = findViewById(R.id.keypadbutton);
        ImageButton knobsButton = findViewById(R.id.knobsbutton);

        // Show home fragment by default
        replaceFragment(new homeFragment());

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new profileFragment());
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new homeFragment());
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new settingsFragment());
            }
        });

        keypadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new keypadFragment());
            }
        });

        knobsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use knobs button as logout
                auth.signOut();
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(i, REQ_LOGIN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check auth state and show login if needed
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // Not signed in, launch LoginActivity
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, REQ_LOGIN);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        // Add a tag to the fragment based on its class name for later retrieval
        String tag = fragment.getClass().getSimpleName();
        
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment, tag);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        
        // If we're showing the profile fragment, make sure it refreshes its FAB visibility
        if (fragment instanceof profileFragment) {
            // Wait for the fragment transaction to complete
            fragmentManager.executePendingTransactions();
            ((profileFragment) fragment).refreshSessionsAndFab();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_LOGIN) {
            if (resultCode != RESULT_OK) {
                // User did not sign in; close the app or return to previous flow
                finish();
            }
            // else continue normally
        }
    }
}