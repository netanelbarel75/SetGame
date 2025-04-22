package com.example.setcardgame;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GameFragment extends Fragment implements CardAdapter.OnCardClickListener {
    
    private GameModel gameModel;
    private RecyclerView rvGameBoard;
    private CardAdapter cardAdapter;
    private TextView tvScore;
    private TextView tvRemainingCards;
    private TextView tvTimer;
    private TextView tvMessage;
    private Button btnNewGame;
    private Button btnHint;
    private Button btnAddCards;
    
    private Handler timerHandler;
    private Runnable timerRunnable;
    
    private GameFragmentListener listener;
    
    public interface GameFragmentListener {
        void onGameFinished(int score, long timeInSeconds);
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof GameFragmentListener) {
            listener = (GameFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement GameFragmentListener");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);
        
        try {
            // Initialize game model
            gameModel = new GameModel();
            
            // Initialize views
            rvGameBoard = view.findViewById(R.id.rvGameBoard);
            tvScore = view.findViewById(R.id.tvScore);
            tvRemainingCards = view.findViewById(R.id.tvRemainingCards);
            tvTimer = view.findViewById(R.id.tvTimer);
            tvMessage = view.findViewById(R.id.tvMessage);
            btnNewGame = view.findViewById(R.id.btnNewGame);
            btnHint = view.findViewById(R.id.btnHint);
            btnAddCards = view.findViewById(R.id.btnAddCards);
            
            // Set up RecyclerView
            int spanCount = 3; // Number of cards per row
            rvGameBoard.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
            cardAdapter = new CardAdapter(getContext(), gameModel.getBoard(), gameModel.getSelectedCards(), this);
            rvGameBoard.setAdapter(cardAdapter);
            
            // Set up button listeners
            btnNewGame.setOnClickListener(v -> startNewGame());
            btnHint.setOnClickListener(v -> giveHint());
            btnAddCards.setOnClickListener(v -> addCards());
            
            // Set up timer
            setupTimer();
            
            // Start a new game
            startNewGame();
        } catch (Exception e) {
            // Log the error
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error initializing game: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        
        return view;
    }
    
    private void setupTimer() {
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimerDisplay();
                if (!gameModel.isGameOver()) {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
    }
    
    private void startNewGame() {
        gameModel.startNewGame();
        updateUI();
        
        // Reset and start timer
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }
    
    private void giveHint() {
        List<Integer> setIndices = gameModel.findValidSet();
        if (!setIndices.isEmpty()) {
            // Flash the first card of a valid set as a hint
            int firstCardIndex = setIndices.get(0);
            flashCard(firstCardIndex);
        } else {
            showMessage(getString(R.string.no_sets_found));
            addCards();
        }
    }
    
    private void flashCard(int position) {
        // Implement a visual flash effect for the hint
        RecyclerView.ViewHolder viewHolder = rvGameBoard.findViewHolderForAdapterPosition(position);
        if (viewHolder == null) {
            // ViewHolder not found, may be outside visible area
            // Scroll to make it visible
            rvGameBoard.scrollToPosition(position);
            // Try again after a short delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) {
                    RecyclerView.ViewHolder vh = rvGameBoard.findViewHolderForAdapterPosition(position);
                    if (vh != null) {
                        View selectionHighlight = vh.itemView.findViewById(R.id.vSelection);
                        if (selectionHighlight != null) {
                            // Show highlight briefly
                            selectionHighlight.setVisibility(View.VISIBLE);
                            
                            // Hide highlight after delay
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (isAdded()) { // Check if fragment is still attached
                                    selectionHighlight.setVisibility(View.INVISIBLE);
                                }
                            }, 500);
                        }
                    }
                }
            }, 100);
            return;
        }
        
        View cardView = viewHolder.itemView;
        View selectionHighlight = cardView.findViewById(R.id.vSelection);
        
        if (selectionHighlight != null) {
            // Show highlight briefly
            selectionHighlight.setVisibility(View.VISIBLE);
            
            // Hide highlight after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) { // Check if fragment is still attached
                    selectionHighlight.setVisibility(View.INVISIBLE);
                }
            }, 500);
        }
    }
    
    private void addCards() {
        if (gameModel.addCards()) {
            updateUI();
        } else {
            Toast.makeText(getContext(), getString(R.string.deck_empty), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onCardClick(int position) {
        gameModel.selectCard(position);
        cardAdapter.notifyDataSetChanged();
        
        // Check if a set was just processed (selected cards will be cleared in this case)
        if (gameModel.getSelectedCards().isEmpty()) {
            updateUI();
            
            // Check if game is over
            if (gameModel.isGameOver()) {
                endGame();
            }
        }
    }
    
    private void updateUI() {
        // Update adapter with the current board state
        cardAdapter.notifyDataSetChanged();
        
        // Update UI elements
        tvScore.setText(getString(R.string.score, gameModel.getScore()));
        tvRemainingCards.setText(getString(R.string.cards_remaining, gameModel.getRemainingCards()));
        updateTimerDisplay();
    }
    
    private void updateTimerDisplay() {
        long seconds = gameModel.getElapsedTimeSeconds();
        String timeString = String.format(Locale.getDefault(), "%02d:%02d", 
                TimeUnit.SECONDS.toMinutes(seconds),
                seconds % 60);
        tvTimer.setText(getString(R.string.time, timeString));
    }
    
    private void showMessage(String message) {
        tvMessage.setText(message);
        tvMessage.setVisibility(View.VISIBLE);
        
        // Hide message after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) { // Check if fragment is still attached
                tvMessage.setVisibility(View.GONE);
            }
        }, 2000);
    }
    
    private void endGame() {
        // Stop timer
        timerHandler.removeCallbacks(timerRunnable);
        
        // Notify activity that game is finished
        if (listener != null) {
            listener.onGameFinished(gameModel.getScore(), gameModel.getElapsedTimeSeconds());
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Pause timer when fragment is paused
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Resume timer when fragment is resumed (if game is not over)
        if (timerHandler != null && timerRunnable != null && gameModel != null && !gameModel.isGameOver()) {
            timerHandler.post(timerRunnable);
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        listener = null;
    }
}