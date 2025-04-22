package com.example.setcardgame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {
    
    private MenuFragmentListener listener;
    
    public interface MenuFragmentListener {
        void onPlayGameClicked();
        void onLeaderboardClicked();
        void onRulesClicked();
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
        
        return view;
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}