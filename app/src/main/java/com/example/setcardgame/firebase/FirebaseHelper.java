package com.example.setcardgame.firebase;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to manage Firebase operations for the Set Card Game
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    
    // Firebase instances
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    
    // Singleton instance
    private static FirebaseHelper instance;
    
    /**
     * Get the singleton instance of FirebaseHelper
     */
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }
    
    /**
     * Private constructor for singleton pattern
     */
    private FirebaseHelper() {
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
        // Create score data
        Map<String, Object> scoreData = new HashMap<>();
        
        // Add user ID if signed in
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            scoreData.put("userId", user.getUid());
            scoreData.put("email", user.getEmail());
        }
        
        scoreData.put("playerName", playerName);
        scoreData.put("score", score);
        scoreData.put("timeInSeconds", timeInSeconds);
        scoreData.put("cardsFound", cardsFound);
        scoreData.put("timestamp", System.currentTimeMillis());
        
        // Generate a unique key for this score entry
        String scoreKey = mDatabase.child("leaderboard").push().getKey();
        
        if (scoreKey != null) {
            // Save the score to Firebase
            mDatabase.child("leaderboard").child(scoreKey).setValue(scoreData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Score submitted successfully");
                            
                            // If user is logged in, also update their personal scores
                            if (user != null) {
                                mDatabase.child("users").child(user.getUid())
                                        .child("scores").child(scoreKey).setValue(scoreData);
                            }
                        } else {
                            Log.w(TAG, "Failed to submit score", task.getException());
                        }
                    });
        }
    }
    
    /**
     * Get the top scores from the leaderboard
     */
    public void getTopScores(final LeaderboardCallback callback) {
        Query query = mDatabase.child("leaderboard")
                .orderByChild("score")
                .limitToLast(20); // Get top 20 scores
                
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
     * Get a user's personal best scores
     */
    public void getUserScores(final LeaderboardCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            callback.onError("User not logged in");
            return;
        }
        
        mDatabase.child("users").child(user.getUid()).child("scores")
                .orderByChild("score")
                .limitToLast(10) // Get top 10 scores
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
                        Log.w(TAG, "getUserScores:onCancelled", databaseError.toException());
                        callback.onError(databaseError.getMessage());
                    }
                });
    }
    
    /**
     * Update user profile information
     */
    public void updateUserProfile(String displayName, final UserProfileCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        
        mDatabase.child("users").child(user.getUid()).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
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
    
    /**
     * Interface for user profile operations callback
     */
    public interface UserProfileCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
