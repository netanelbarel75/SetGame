package com.example.setcardgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.setcardgame.firebase.FirebaseHelper;

public class MainActivity extends AppCompatActivity implements 
        MenuFragment.MenuFragmentListener,
        GameFragment.GameFragmentListener,
        LeaderboardFragment.LeaderboardFragmentListener {

    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Ensure leaderboard exists in Firebase when app starts
        initializeFirebase();
        
        if (savedInstanceState == null) {
            // Load the menu fragment initially
            loadFragment(new MenuFragment());
        }
    }
    
    /**
     * Initialize Firebase components
     */
    private void initializeFirebase() {
        Log.d(TAG, "Initializing Firebase components...");
        // Only use FirebaseHelper for consistency
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        
        // Set the context for SharedPreferences access
        firebaseHelper.setContext(getApplicationContext());
        
        // Just check the database connection, don't create any data
        new Thread(() -> {
            try {
                // Only debug the connection - don't modify data
                firebaseHelper.debugDatabaseConnection();
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to Firebase", e);
            }
        }).start();
    }
    
    private void loadFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();  // Use commitAllowingStateLoss instead of commit
        } catch (Exception e) {
            // Handle or log the error
            Toast.makeText(this, "Error loading fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onPlayGameClicked() {
        loadFragment(new GameFragment());
    }
    
    @Override
    public void onLeaderboardClicked() {
        loadFragment(new LeaderboardFragment());
    }
    
    @Override
    public void onRulesClicked() {
        // Show game rules dialog
        RulesDialogFragment rulesDialog = new RulesDialogFragment();
        rulesDialog.show(getSupportFragmentManager(), "RulesDialogFragment");
    }
    
    @Override
    public void onGameFinished(int score, long timeInSeconds) {
        // Show score submission dialog
        ScoreSubmissionDialogFragment scoreDialog = ScoreSubmissionDialogFragment.newInstance(score, timeInSeconds);
        scoreDialog.show(getSupportFragmentManager(), "ScoreSubmissionDialogFragment");
    }
    
    @Override
    public void onBackToMenuClicked() {
        loadFragment(new MenuFragment());
    }
}