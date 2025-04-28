package com.example.setcardgame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.setcardgame.firebase.FirebaseHelper;
import com.example.setcardgame.service.MusicManager;

public class MenuFragment extends Fragment {
    
    private MenuFragmentListener listener;
    private TextView tvBestScore;
    private Button btnToggleMusic;
    private FirebaseHelper firebaseHelper;
    private MusicManager musicManager;
    
    public interface MenuFragmentListener {
        void onPlayGameClicked();
        void onLeaderboardClicked();
        void onRulesClicked();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseHelper = FirebaseHelper.getInstance();
        musicManager = MusicManager.getInstance();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MenuFragmentListener) {
            listener = (MenuFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MenuFragmentListener");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        
        Button btnPlay = view.findViewById(R.id.btnPlay);
        Button btnLeaderboard = view.findViewById(R.id.btnLeaderboard);
        Button btnRules = view.findViewById(R.id.btnRules);
        btnToggleMusic = view.findViewById(R.id.btnToggleMusic);
        tvBestScore = view.findViewById(R.id.tvBestScore);
        
        // Update the best score
        updateBestScore();
        
        btnPlay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayGameClicked();
            }
        });
        
        btnLeaderboard.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLeaderboardClicked();
            }
        });
        
        btnRules.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRulesClicked();
            }
        });
        
        // Set up music toggle button
        updateMusicToggleButton();
        btnToggleMusic.setOnClickListener(v -> toggleMusic());
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Update the best score when resuming to ensure it's current
        updateBestScore();
        
        // Make sure music toggle button reflects current state
        updateMusicToggleButton();
    }
    
    private void updateBestScore() {
        if (firebaseHelper != null && tvBestScore != null) {
            // If user is logged in, get their best score from Firebase
            if (firebaseHelper.isUserLoggedIn()) {
                firebaseHelper.getUserBestScore(new FirebaseHelper.BestScoreCallback() {
                    @Override
                    public void onSuccess(int bestScore) {
                        if (isAdded()) { // Check if fragment is still attached
                            String userName = firebaseHelper.getUserNameFromPrefs();
                            if (userName != null && !userName.isEmpty() && !"Guest".equals(userName)) {
                                tvBestScore.setText("Best Score: " + bestScore + " (" + userName + ")");
                            } else {
                                tvBestScore.setText("Best Score: " + bestScore);
                            }
                        }
                    }
                    
                    @Override
                    public void onError(String errorMessage) {
                        if (isAdded()) { // Check if fragment is still attached
                            // Just show the local score if there's an error
                            tvBestScore.setText("Best Score: " + firebaseHelper.getBestScoreFromPrefs());
                        }
                    }
                });
            } else {
                // For guest users, get the best score from shared preferences
                int bestScore = firebaseHelper.getBestScoreFromPrefs();
                tvBestScore.setText("Best Score: " + bestScore);
            }
        }
    }
    
    /**
     * Toggle background music on/off
     */
    private void toggleMusic() {
        boolean newState = musicManager.toggleMusic();
        updateMusicToggleButton();
    }
    
    /**
     * Update the music toggle button text based on current state
     */
    private void updateMusicToggleButton() {
        if (btnToggleMusic != null) {
            boolean isMusicEnabled = musicManager.isMusicEnabled();
            btnToggleMusic.setText(isMusicEnabled ? "Music: On" : "Music: Off");
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}