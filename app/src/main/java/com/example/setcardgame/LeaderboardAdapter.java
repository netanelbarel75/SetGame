package com.example.setcardgame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    
    private final Context context;
    private final List<LeaderboardFragment.LeaderboardEntry> entries;
    
    public LeaderboardAdapter(Context context, List<LeaderboardFragment.LeaderboardEntry> entries) {
        this.context = context;
        this.entries = entries;
    }
    
    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardFragment.LeaderboardEntry entry = entries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return entries.size();
    }
    
    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView tvRank;
        private final TextView tvPlayerName;
        private final TextView tvScore;
        
        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
        
        public void bind(LeaderboardFragment.LeaderboardEntry entry) {
            tvRank.setText(String.valueOf(entry.getRank()));
            tvPlayerName.setText(entry.getPlayerName());
            tvScore.setText(String.valueOf(entry.getScore()));
        }
    }
}