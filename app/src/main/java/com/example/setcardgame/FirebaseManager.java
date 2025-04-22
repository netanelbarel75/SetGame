package com.example.setcardgame;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to manage Firebase operations for the Set Card Game
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    
    // Firebase instances
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    
    // Singleton instance
    private static FirebaseManager instance;
    
    /**
     * Get the singleton instance of FirebaseManager
     */
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern
     */
    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }
    
    /**
     * Get the current authenticated user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    /**
     * Check if a user is currently logged in
     */
    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
    
    /**
     * Submit a new score to the leaderboard
     */
    public void submitScore(String playerName, int score, long timeInSeconds, int cardsFound) {
        if (!isUserLoggedIn()) {
            Log.w(TAG, "Cannot submit score: No user is logged in");
            return;
        }
        
        String userId = getCurrentUser().getUid();
        String email = getCurrentUser().getEmail();
        
        // Create score data
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("userId", userId);
        scoreData.put("email", email);
        scoreData.put("playerName", playerName);
        scoreData.put("score", score);
        scoreData.put("timeInSeconds", timeInSeconds);
        scoreData.put("cardsFound", cardsFound);
        scoreData.put("timestamp", System.currentTimeMillis());
        
        // Generate a unique key for this score entry
        String scoreKey = mDatabase.child("leaderboard").push().getKey();
        
        // Save the score to Firebase
        mDatabase.child("leaderboard").child(scoreKey).setValue(scoreData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Score submitted successfully");
                        } else {
                            Log.w(TAG, "Failed to submit score", task.getException());
                        }
                    }
                });
                
        // Also update the user's best score if better
        mDatabase.child("users").child(userId).child("scores").push().setValue(scoreData);
    }
    
    /**
     * Get the top scores from the leaderboard
     */
    public void getTopScores(final LeaderboardCallback callback) {
        mDatabase.child("leaderboard")
                .orderByChild("score")
                .limitToLast(20) // Get top 20 scores
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Map<String, Object>> scores = new ArrayList<>();
                        
                        // Process the scores in reverse order (highest first)
                        for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                            Map<String, Object> score = new HashMap<>();
                            score.put("id", scoreSnapshot.getKey());
                            
                            for (DataSnapshot child : scoreSnapshot.getChildren()) {
                                score.put(child.getKey(), child.getValue());
                            }
                            
                            scores.add(0, score); // Add to beginning to reverse order
                        }
                        
                        callback.onSuccess(scores);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getTopScores:onCancelled", databaseError.toException());
                        callback.onError(databaseError.getMessage());
                    }
                });
    }
    
    /**
     * Interface for leaderboard data callback
     */
    public interface LeaderboardCallback {
        void onSuccess(List<Map<String, Object>> scores);
        void onError(String errorMessage);
    }
}
