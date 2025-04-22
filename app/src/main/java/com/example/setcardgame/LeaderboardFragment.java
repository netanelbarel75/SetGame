package com.example.setgame;

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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    
    private RecyclerView rvLeaderboard;
    private ProgressBar progressBar;
    private Button btnBack;
    
    private FirebaseFirestore firestore;
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
        
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        
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
        
        firestore.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(20) // Get top 20 scores
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful() && isAdded()) {
                        leaderboardEntries.clear();
                        
                        int rank = 1;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String playerName = document.getString("playerName");
                            Long score = document.getLong("score");
                            Long timeInSeconds = document.getLong("timeInSeconds");
                            
                            if (playerName != null && score != null) {
                                leaderboardEntries.add(new LeaderboardEntry(
                                        rank++,
                                        playerName,
                                        score.intValue(),
                                        timeInSeconds != null ? timeInSeconds : 0
                                ));
                            }
                        }
                        
                        adapter.notifyDataSetChanged();
                    } else if (isAdded()) {
                        Toast.makeText(getContext(), R.string.error_loading_leaderboard, Toast.LENGTH_SHORT).show();
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