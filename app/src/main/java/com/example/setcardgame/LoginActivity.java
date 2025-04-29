package com.example.setcardgame;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.setcardgame.firebase.FirebaseHelper;
import com.example.setcardgame.service.MusicManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mFirestore;
    private FirebaseHelper mFirebaseHelper;
    
    private SignInButton signInButton;
    private Button btnPlayAsGuest;
    private ImageButton btnToggleMusic;
    private MusicManager musicManager;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Remove title from toolbar
        }
        
        // Set up music toggle button
        btnToggleMusic = findViewById(R.id.btnToggleMusic);
        musicManager = MusicManager.getInstance();
        
        // Initialize background music service first
        initializeBackgroundMusic();
        
        // Then set up button click handler
        btnToggleMusic.setOnClickListener(v -> toggleMusic());
        
        // Set initial icon - default to ON since we're starting the music
        btnToggleMusic.setImageResource(R.drawable.ic_music_on);
        
        // Debug: Print the SHA-1 hash to help with debugging Google Sign-In issues
        printKeyHash();
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();
        
        // Initialize FirebaseHelper
        mFirebaseHelper = FirebaseHelper.getInstance();
        mFirebaseHelper.setContext(getApplicationContext());
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        
        // Set up UI components
        signInButton = findViewById(R.id.btnSignIn);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(v -> signIn());
        
        btnPlayAsGuest = findViewById(R.id.btnPlayAsGuest);
        btnPlayAsGuest.setOnClickListener(v -> playAsGuest());
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
            // Make sure the icon reflects the actual state (both enabled and playing)
            boolean isMusicOn = musicManager.isMusicEnabled();
            btnToggleMusic.setImageResource(isMusicOn ? 
                R.drawable.ic_music_on : R.drawable.ic_music_off);
            
            // Log the current state for debugging
            Log.d(TAG, "Music enabled: " + isMusicOn);
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
     * Prints the SHA-1 fingerprint of the app's signing certificate to help with Google Sign-In debugging
     */
    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d(TAG, "KeyHash: " + keyHash);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting key hash", e);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
        
        // Ensure music is playing
        musicManager.startMusic();
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
        // Don't pause music when activity is in background
        super.onPause();
    }
    
    private void signIn() {
        // Check for network connectivity first
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please connect to the internet or play as a guest.", Toast.LENGTH_LONG).show();
            btnPlayAsGuest.setText("Play Offline");
            return;
        }
        
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    /**
     * Check if the device has an active network connection
     * @return true if network is available, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    private void playAsGuest() {
        // Proceed to main activity without signing in
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed
                Log.w(TAG, "Google sign in failed", e);
                
                // Provide more helpful error messages based on error code
                String errorMessage;
                switch (e.getStatusCode()) {
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        errorMessage = "Sign-in was cancelled.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS:
                        errorMessage = "Sign-in already in progress.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                        errorMessage = "Sign-in failed. Please try again.";
                        break;
                    case GoogleSignInStatusCodes.NETWORK_ERROR:
                        errorMessage = "Network error. Please check your internet connection and try again.";
                        break;
                    case 10: // DEVELOPER_ERROR
                        errorMessage = "Sign-in configuration error. Please contact app developer.";
                        break;
                    default:
                        errorMessage = "Google sign-in failed: " + e.getMessage();
                }
                
                // Show the guest button more prominently if there's a network error
                if (e.getStatusCode() == GoogleSignInStatusCodes.NETWORK_ERROR) {
                    btnPlayAsGuest.setText("Play Offline");
                    // You could also make the button more visible by changing its appearance
                }
                
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        
                        // Check if this is a new user
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            createUserDocument(user);
                        }
                        
                        // Save user info to SharedPreferences
                        if (user != null) {
                            String name = user.getDisplayName();
                            String email = user.getEmail();
                            mFirebaseHelper.saveUserInfoToPrefs(email, name, 0);
                        }
                        
                        updateUI(user);
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void createUserDocument(FirebaseUser user) {
        if (user != null) {
            // Create a new user document in Firestore
            Map<String, Object> userData = new HashMap<>();
            userData.put("displayName", user.getDisplayName());
            userData.put("email", user.getEmail());
            userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
            userData.put("highScore", 0);
            userData.put("gamesPlayed", 0);
            
            mFirestore.collection("users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "User document created"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error creating user document", e));
        }
    }
    
    private void updateUI(FirebaseUser user) {
        // Start MainActivity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}