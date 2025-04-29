package com.example.setcardgame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.setcardgame.firebase.FirebaseHelper;
import com.example.setcardgame.service.MusicManager;

public class MainActivity extends AppCompatActivity implements 
        MenuFragment.MenuFragmentListener,
        GameFragment.GameFragmentListener,
        LeaderboardFragment.LeaderboardFragmentListener {

    private static final String TAG = "MainActivity";
    private ImageButton btnToggleMusic;
    private MusicManager musicManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Set up music toggle button
        btnToggleMusic = findViewById(R.id.btnToggleMusic);
        musicManager = MusicManager.getInstance();
        
        // Initialize and start background music first
        initializeBackgroundMusic();
        
        // Then set up button click handler
        btnToggleMusic.setOnClickListener(v -> toggleMusic());
        
        // Set initial icon - default to ON since we're starting the music
        btnToggleMusic.setImageResource(R.drawable.ic_music_on);
        
        // Ensure leaderboard exists in Firebase when app starts
        initializeFirebase();
        
        if (savedInstanceState == null) {
            // Load the menu fragment initially
            loadFragment(new MenuFragment());
        }
    }
    
    /**
     * Toggle background music on/off
     */
    private void toggleMusic() {
        boolean newState = musicManager.toggleMusic();
        updateMusicToggleIcon();
    }
    
    /**
     * Update the music toggle button icon based on current state
     */
    private void updateMusicToggleIcon() {
        if (btnToggleMusic != null) {
            boolean isMusicEnabled = musicManager.isMusicEnabled();
            btnToggleMusic.setImageResource(isMusicEnabled ? 
                R.drawable.ic_music_on : R.drawable.ic_music_off);
        }
    }
    
    /**
     * Initialize background music service
     */
    private void initializeBackgroundMusic() {
        Log.d(TAG, "Initializing background music...");
        musicManager.init(this);
        musicManager.startMusic();
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
        // Ensure music is playing when starting a game
        musicManager.startMusic();
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Resume background music when activity comes to foreground
        musicManager.startMusic();
        
        // Update music icon to reflect current state
        updateMusicToggleIcon();
    }
    
    @Override
    protected void onPause() {
        // Don't pause the music when activity is in background - let it continue playing
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        // Disconnect from music service when activity is destroyed
        musicManager.disconnectFromService();
        super.onDestroy();
    }
}