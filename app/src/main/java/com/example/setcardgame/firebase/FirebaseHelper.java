package com.example.setcardgame.firebase;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.firebase.database.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to manage Firebase operations for the Set Card Game
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String PREFS_NAME = "SetGamePrefs";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    
    // Firebase instances
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private Context mContext;
    
    // Singleton instance
    private static FirebaseHelper instance;
    private static boolean isInitializing = false;
    private static boolean isInitialized = false;
    
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
     * Set the application context for shared preferences
     */
    public void setContext(Context context) {
        mContext = context;
    }
    
    /**
     * Private constructor for singleton pattern
     */
    private FirebaseHelper() {
        // Initialize the fields outside try-catch to ensure they're always initialized
        FirebaseAuth auth = null;
        DatabaseReference database = null;
        
        try {
            // Configure Firebase Database with optimized settings
            FirebaseDatabase firebaseDb = FirebaseDatabase.getInstance();
            
            // Disable persistence to reduce memory usage (must be called before any other Firebase calls)
            try {
                firebaseDb.setPersistenceEnabled(false);
            } catch (Exception e) {
                Log.w(TAG, "Could not disable persistence, it may already be set: " + e.getMessage());
            }
            
            // Set log level to reduce debug messages
            try {
                firebaseDb.setLogLevel(Logger.Level.WARN);
            } catch (Exception e) {
                Log.w(TAG, "Could not set log level: " + e.getMessage());
            }
            
            // Initialize Firebase components
            auth = FirebaseAuth.getInstance();
            database = firebaseDb.getReference();
            
            // Make sure we're connected
            try {
                firebaseDb.goOnline();
            } catch (Exception e) {
                Log.w(TAG, "Could not go online: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
        
        // If initialization failed, create empty instances
        if (auth == null) {
            Log.w(TAG, "Firebase Auth initialization failed, using fallback");
            auth = FirebaseAuth.getInstance(); // Last attempt to initialize
        }
        
        if (database == null) {
            Log.w(TAG, "Firebase Database initialization failed, using fallback");
            database = FirebaseDatabase.getInstance().getReference(); // Last attempt to initialize
        }
        
        // Assign to final fields
        mAuth = auth;
        mDatabase = database;
    }
    
    /**
     * Save user info to SharedPreferences
     */
    public void saveUserInfoToPrefs(String email, String name, int bestScore) {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_USER_NAME, name);
            editor.putInt(KEY_BEST_SCORE, bestScore);
            editor.apply();
        }
    }
    
    /**
     * Get user's best score from SharedPreferences
     */
    public int getBestScoreFromPrefs() {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getInt(KEY_BEST_SCORE, 0);
        }
        return 0;
    }
    
    /**
     * Get user name from SharedPreferences
     */
    public String getUserNameFromPrefs() {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getString(KEY_USER_NAME, "Guest");
        }
        return "Guest";
    }
    
    /**
     * Debug method to check Firebase database connection
     */
    public void debugDatabaseConnection() {
        Log.d(TAG, "Checking database connection...");
        try {
            // Get the database URL
            String databaseUrl = FirebaseDatabase.getInstance().getReference().toString();
            Log.d(TAG, "Database URL: " + databaseUrl);
            
            // Check if leaderboard exists - but don't create it
            mDatabase.child("leaderboard").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Leaderboard exists: " + dataSnapshot.exists());
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "Leaderboard has children: " + dataSnapshot.hasChildren());
                        long childCount = 0;
                        
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            childCount++;
                            if (childCount <= 3) { // Only log the first 3 to avoid excessive logging
                                Log.d(TAG, "Child key: " + child.getKey());
                                try {
                                    Log.d(TAG, "Sample value: " + child.getValue());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error getting child value", e);
                                }
                            }
                        }
                        Log.d(TAG, "Total number of leaderboard entries: " + childCount);
                    } else {
                        Log.d(TAG, "Leaderboard doesn't exist yet. It will be created when needed.");
                        // We no longer create it here - this is just for debugging
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error checking database", e);
        }
    }
    
    /**
     * Safe check and create of leaderboard - will not overwrite existing data
     */
    private void checkAndCreateLeaderboardIfNeeded() {
        try {
            mDatabase.child("leaderboard").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Log.d(TAG, "Leaderboard does not exist. Creating it now...");
                        // Create the leaderboard node with an initial empty object
                        mDatabase.child("leaderboard").setValue(new HashMap<>())
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Created new leaderboard node");
                                // Only add dummy data if the node is completely new
                                addInitialDummyData();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to create leaderboard node", e);
                            });
                    } else {
                        Log.d(TAG, "Leaderboard exists with " + dataSnapshot.getChildrenCount() + " entries");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "checkAndCreateLeaderboardIfNeeded:onCancelled", databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in checkAndCreateLeaderboardIfNeeded", e);
        }
    }
    
    /**
     * Add initial dummy data to a completely new leaderboard
     */
    private void addInitialDummyData() {
        try {
            // Add 5 initial example records
            addDummyScore("Champion Player", 15, 180, 15);
            addDummyScore("Expert Player", 12, 240, 12);
            addDummyScore("Advanced Player", 10, 300, 10);
            addDummyScore("Intermediate Player", 8, 330, 8);
            addDummyScore("Beginner Player", 5, 420, 5);
            Log.d(TAG, "Added initial dummy data to new leaderboard");
        } catch (Exception e) {
            Log.e(TAG, "Error adding initial dummy data", e);
        }
    }
    
    /**
     * Get the current authenticated user
     */
    public FirebaseUser getCurrentUser() {
        try {
            return mAuth.getCurrentUser();
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user", e);
            return null;
        }
    }
    
    /**
     * Check if a user is currently logged in
     */
    public boolean isUserLoggedIn() {
        try {
            return mAuth != null && mAuth.getCurrentUser() != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user is logged in", e);
            return false;
        }
    }

    /**
     * Get the user's best score
     * @param callback Callback to receive the best score
     */
    public void getUserBestScore(final BestScoreCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            // If not logged in, return the local best score from preferences
            callback.onSuccess(getBestScoreFromPrefs());
            return;
        }
        
        // Query the user's scores, ordered by score, and get only the highest one
        mDatabase.child("users").child(user.getUid()).child("scores")
                .orderByChild("score")
                .limitToLast(1) // Only get the highest score
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int bestScore = 0;
                        
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            // Get the highest score
                            for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                                // There should only be one child, but iterate just in case
                                Object scoreObj = scoreSnapshot.child("score").getValue();
                                if (scoreObj != null) {
                                    if (scoreObj instanceof Long) {
                                        bestScore = ((Long) scoreObj).intValue();
                                    } else if (scoreObj instanceof Integer) {
                                        bestScore = (Integer) scoreObj;
                                    } else if (scoreObj instanceof Double) {
                                        bestScore = ((Double) scoreObj).intValue();
                                    }
                                }
                                break; // Just take the first one, as it should be the highest
                            }
                        }
                        
                        // Save to preferences
                        saveUserInfoToPrefs(user.getEmail(), user.getDisplayName(), bestScore);
                        
                        callback.onSuccess(bestScore);
                    }
                    
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "getUserBestScore:onCancelled", databaseError.toException());
                        callback.onError(databaseError.getMessage());
                    }
                });
    }
    
    /**
     * Submit a new score to the leaderboard
     */
    public void submitScore(String playerName, int score, long timeInSeconds, int cardsFound) {
        try {
            // First, make sure the leaderboard exists without overwriting it
            // We check first - only create if it does not exist
            checkAndCreateLeaderboardIfNeeded();
            
            // Create score data
            Map<String, Object> scoreData = new HashMap<>();
            
            // Add user ID if signed in
            FirebaseUser user = getCurrentUser();
            if (user != null) {
                scoreData.put("userId", user.getUid());
                scoreData.put("email", user.getEmail());
                
                // Update local best score if needed
                if (score > getBestScoreFromPrefs()) {
                    saveUserInfoToPrefs(user.getEmail(), user.getDisplayName(), score);
                }
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
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error submitting score", e);
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception submitting score", e);
        }
    }
    
    /**
     * Add a dummy score to the leaderboard
     */
    private void addDummyScore(String playerName, int score, long timeInSeconds, int cardsFound) {
        // Create score data
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("playerName", playerName);
        scoreData.put("score", score);
        scoreData.put("timeInSeconds", timeInSeconds);
        scoreData.put("cardsFound", cardsFound);
        scoreData.put("timestamp", System.currentTimeMillis());
        
        // Generate a unique key for this score entry
        String scoreKey = mDatabase.child("leaderboard").push().getKey();
        
        if (scoreKey != null) {
            // Save the score to Firebase
            mDatabase.child("leaderboard").child(scoreKey).setValue(scoreData);
        }
    }
    
    /**
     * Get the top scores from the leaderboard
     */
    public void getTopScores(final LeaderboardCallback callback) {
        try {
            Log.d(TAG, "Starting to fetch top scores");
            
            // Debug the database reference
            Log.d(TAG, "Database reference: " + mDatabase.toString());
            
            // IMPORTANT: We no longer call ensureLeaderboardExists() here to avoid potential data loss
            // Instead, we just make sure we can read from the leaderboard
            
            Query query = mDatabase.child("leaderboard")
                    .orderByChild("score")
                    .limitToLast(20); // Get top 20 scores
                    
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Leaderboard data received, exists: " + dataSnapshot.exists());
                    Log.d(TAG, "Leaderboard child count: " + dataSnapshot.getChildrenCount());
                    
                    List<Map<String, Object>> scores = new ArrayList<>();
                    
                    // Process the scores in reverse order (highest first)
                    for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "Processing score with key: " + scoreSnapshot.getKey());
                        
                        Map<String, Object> score = new HashMap<>();
                        score.put("id", scoreSnapshot.getKey());
                        
                        for (DataSnapshot child : scoreSnapshot.getChildren()) {
                            score.put(child.getKey(), child.getValue());
                        }
                        
                        scores.add(0, score); // Add to beginning to reverse order
                    }
                    
                    Log.d(TAG, "Processed " + scores.size() + " scores");
                    callback.onSuccess(scores);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "getTopScores:onCancelled", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting top scores", e);
            callback.onError("Failed to load leaderboard: " + e.getMessage());
        }
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
    
    /**
     * Interface for best score callback
     */
    public interface BestScoreCallback {
        void onSuccess(int bestScore);
        void onError(String errorMessage);
    }
}