package com.example.setgame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    
    private final List<Card> cards;
    private final Context context;
    private final OnCardClickListener listener;
    private final List<Card> selectedCards;
    
    public interface OnCardClickListener {
        void onCardClick(int position);
    }
    
    public CardAdapter(Context context, List<Card> cards, List<Card> selectedCards, OnCardClickListener listener) {
        this.context = context;
        this.cards = cards;
        this.selectedCards = selectedCards;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.bind(card);
    }
    
    @Override
    public int getItemCount() {
        return cards.size();
    }
    
    class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        
        private final View cardBackground;
        private final ImageView ivCardShape;
        private final View vSelection;
        
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBackground = itemView.findViewById(R.id.cardBackground);
            ivCardShape = itemView.findViewById(R.id.ivCardShape);
            vSelection = itemView.findViewById(R.id.vSelection);
            
            itemView.setOnClickListener(this);
        }
        
        public void bind(Card card) {
            // Set up card appearance based on card properties
            setupCardShape(card);
            
            // Set selected state
            boolean isSelected = selectedCards.contains(card);
            vSelection.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
        
        private void setupCardShape(Card card) {
            // Get the appropriate shape drawable ID based on card properties
            int drawableId = getShapeDrawableId(card);
            
            // Apply the shape drawable
            ivCardShape.setImageResource(drawableId);
            
            // Apply color tint
            int colorId = getColorId(card.getColor());
            ivCardShape.setColorFilter(ContextCompat.getColor(context, colorId));
            
            // Set number of shapes based on card number
            setNumberOfShapes(card.getNumber());
        }
        
        private int getShapeDrawableId(Card card) {
            // Determine the drawable ID based on shape and shading
            switch (card.getShape()) {
                case OVAL:
                    switch (card.getShading()) {
                        case SOLID:
                            return R.drawable.shape_oval;
                        case STRIPED:
                            return R.drawable.shape_striped_oval;
                        case OUTLINE:
                            return R.drawable.shape_outline_oval;
                    }
                    break;
                case DIAMOND:
                    switch (card.getShading()) {
                        case SOLID:
                            return R.drawable.shape_diamond;
                        case STRIPED:
                            return R.drawable.shape_striped_diamond;
                        case OUTLINE:
                            return R.drawable.shape_outline_diamond;
                    }
                    break;
                case SQUIGGLE:
                    switch (card.getShading()) {
                        case SOLID:
                            return R.drawable.shape_squiggle;
                        case STRIPED:
                            return R.drawable.shape_striped_squiggle;
                        case OUTLINE:
                            return R.drawable.shape_outline_squiggle;
                    }
                    break;
            }
            
            // Default fallback
            return R.drawable.shape_oval;
        }
        
        private int getColorId(Card.Color color) {
            switch (color) {
                case RED:
                    return R.color.colorRed;
                case GREEN:
                    return R.color.colorGreen;
                case PURPLE:
                    return R.color.colorPurple;
                default:
                    return R.color.colorRed;
            }
        }
        
        private void setNumberOfShapes(Card.Number number) {
            // Handle multiple shapes for 2 or 3 cards
            // For simplicity in this implementation, we're just using a single shape image
            // In a real app, you'd create a compound drawable with multiple shapes
            // or use a custom view with multiple ImageViews
            
            // This is a placeholder for demonstration
            switch (number) {
                case ONE:
                    ivCardShape.setAlpha(1.0f);
                    break;
                case TWO:
                    ivCardShape.setAlpha(0.9f);
                    break;
                case THREE:
                    ivCardShape.setAlpha(0.8f);
                    break;
            }
        }
        
        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCardClick(position);
                }
            }
        }
    }
}