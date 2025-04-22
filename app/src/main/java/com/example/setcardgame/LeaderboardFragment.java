package com.example.setcardgame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setcardgame.firebase.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {
    
    private RecyclerView rvLeaderboard;
    private ProgressBar progressBar;
    private Button btnBack;
    
    private FirebaseHelper firebaseHelper;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> leaderboardEntries;
    
    private LeaderboardFragmentListener listener;
    
    public interface LeaderboardFragmentListener {
        void onBackToMenuClicked();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof LeaderboardFragmentListener) {
            listener = (LeaderboardFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LeaderboardFragmentListener");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        
        // Initialize Firebase Helper
        firebaseHelper = FirebaseHelper.getInstance();
        
        // Initialize views
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        progressBar = view.findViewById(R.id.progressBar);
        btnBack = view.findViewById(R.id.btnBack);
        
        // Set up RecyclerView
        leaderboardEntries = new ArrayList<>();
        adapter = new LeaderboardAdapter(getContext(), leaderboardEntries);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaderboard.setAdapter(adapter);
        
        // Set up button listener
        btnBack.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackToMenuClicked();
            }
        });
        
        // Load leaderboard data
        loadLeaderboard();
        
        return view;
    }
    
    private void loadLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);
        
        firebaseHelper.getTopScores(new FirebaseHelper.LeaderboardCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> scores) {
                progressBar.setVisibility(View.GONE);
                
                if (isAdded()) {
                    leaderboardEntries.clear();
                    
                    int rank = 1;
                    for (Map<String, Object> scoreData : scores) {
                        String playerName = (String) scoreData.get("playerName");
                        Object scoreObj = scoreData.get("score");
                        Object timeObj = scoreData.get("timeInSeconds");
                        
                        int score = 0;
                        long timeInSeconds = 0;
                        
                        if (scoreObj instanceof Long) {
                            score = ((Long) scoreObj).intValue();
                        } else if (scoreObj instanceof Integer) {
                            score = (Integer) scoreObj;
                        }
                        
                        if (timeObj instanceof Long) {
                            timeInSeconds = (Long) timeObj;
                        } else if (timeObj instanceof Integer) {
                            timeInSeconds = ((Integer) timeObj).longValue();
                        }
                        
                        if (playerName != null) {
                            leaderboardEntries.add(new LeaderboardEntry(
                                    rank++,
                                    playerName,
                                    score,
                                    timeInSeconds
                            ));
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                }
            }
            
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                
                if (isAdded()) {
                    Toast.makeText(getContext(), R.string.error_loading_leaderboard, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
    
    // Leaderboard entry data class
    public static class LeaderboardEntry {
        private final int rank;
        private final String playerName;
        private final int score;
        private final long timeInSeconds;
        
        public LeaderboardEntry(int rank, String playerName, int score, long timeInSeconds) {
            this.rank = rank;
            this.playerName = playerName;
            this.score = score;
            this.timeInSeconds = timeInSeconds;
        }
        
        public int getRank() {
            return rank;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public int getScore() {
            return score;
        }
        
        public long getTimeInSeconds() {
            return timeInSeconds;
        }
    }
}