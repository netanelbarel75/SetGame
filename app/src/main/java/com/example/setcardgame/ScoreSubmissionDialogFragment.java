package com.example.setgame;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ScoreSubmissionDialogFragment extends DialogFragment {
    
    private static final String ARG_SCORE = "score";
    private static final String ARG_TIME = "time";
    
    private int score;
    private long timeInSeconds;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    public static ScoreSubmissionDialogFragment newInstance(int score, long timeInSeconds) {
        ScoreSubmissionDialogFragment fragment = new ScoreSubmissionDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCORE, score);
        args.putLong(ARG_TIME, timeInSeconds);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            score = getArguments().getInt(ARG_SCORE);
            timeInSeconds = getArguments().getLong(ARG_TIME);
        }
        
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        // Inflate and set the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_score_submission, null);
        builder.setView(view);
        
        // Set up the views
        TextView tvScore = view.findViewById(R.id.tvScore);
        TextView tvTime = view.findViewById(R.id.tvTime);
        EditText etPlayerName = view.findViewById(R.id.etPlayerName);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        
        // Format time
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", 
                TimeUnit.SECONDS.toMinutes(timeInSeconds),
                timeInSeconds % 60);
        
        // Set initial values
        tvScore.setText(getString(R.string.your_score, score));
        tvTime.setText(getString(R.string.your_time, timeFormatted));
        
        // Check if user is signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            etPlayerName.setText(currentUser.getDisplayName());
        } else {
            etPlayerName.setText(R.string.guest_player);
        }
        
        // Set up button listeners
        btnSubmit.setOnClickListener(v -> {
            String playerName = etPlayerName.getText().toString().trim();
            if (!playerName.isEmpty()) {
                submitScore(playerName);
                dismiss();
            } else {
                etPlayerName.setError("Please enter a name");
            }
        });
        
        btnCancel.setOnClickListener(v -> dismiss());
        
        return builder.create();
    }
    
    private void submitScore(String playerName) {
        // Create score data
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("playerName", playerName);
        scoreData.put("score", score);
        scoreData.put("timeInSeconds", timeInSeconds);
        scoreData.put("timestamp", System.currentTimeMillis());
        
        // Add user ID if signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            scoreData.put("userId", currentUser.getUid());
        }
        
        // Save score to Firestore
        firestore.collection("scores")
                .add(scoreData)
                .addOnSuccessListener(documentReference -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Score submitted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to submit score: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.setCancelable(false);
        }
    }
}