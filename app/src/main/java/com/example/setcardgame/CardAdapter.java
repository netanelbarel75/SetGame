package com.example.setcardgame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.setcardgame.Card;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {
    
    private final List<Card> cards;
    private final Context context;
    private final OnCardClickListener listener;
    private final List<Card> selectedCards;
    
    public interface OnCardClickListener {
        void onCardClick(int position);
    }
    
    private boolean isValidSet = false;
    private boolean invalidSet = false;
    
    public CardAdapter(Context context, List<Card> cards, List<Card> selectedCards, OnCardClickListener listener) {
        this.context = context;
        this.cards = cards;
        this.selectedCards = selectedCards;
        this.listener = listener;
    }
    
    public void setValidSet(boolean isValid) {
        this.isValidSet = isValid;
        this.invalidSet = !isValid;
        notifyDataSetChanged();
    }
    
    public void resetSetValidation() {
        this.isValidSet = false;
        this.invalidSet = false;
    }
    
    /**
     * Get whether the current selection is a valid set
     * @return true if the current selection is a valid set
     */
    public boolean getIsValidSet() {
        return isValidSet;
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
        private final ImageView ivCardShape1;
        private final ImageView ivCardShape2;
        private final ImageView ivCardShape3;
        private final View vSelection;
        
        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBackground = itemView.findViewById(R.id.cardBackground);
            ivCardShape1 = itemView.findViewById(R.id.ivCardShape1);
            ivCardShape2 = itemView.findViewById(R.id.ivCardShape2);
            ivCardShape3 = itemView.findViewById(R.id.ivCardShape3);
            vSelection = itemView.findViewById(R.id.vSelection);
            
            itemView.setOnClickListener(this);
        }
        
        public void bind(Card card) {
            // Set up card appearance based on card properties
            setupCardShape(card);
            
            // Set selected state
            boolean isSelected = selectedCards.contains(card);
            
            if (isSelected) {
                // Always show selection highlight for selected cards
                vSelection.setVisibility(View.VISIBLE);
                
                // Set the appropriate background based on valid/invalid status
                if (selectedCards.size() == 3) {
                    if (isValidSet) {
                        vSelection.setBackgroundResource(R.drawable.card_valid);
                    } else if (invalidSet) {
                        vSelection.setBackgroundResource(R.drawable.card_invalid);
                    } else {
                        vSelection.setBackgroundResource(R.drawable.card_selected);
                    }
                } else {
                    // Regular selection for first two cards
                    vSelection.setBackgroundResource(R.drawable.card_selected);
                }
            } else {
                // Not selected, so hide the selection highlight
                vSelection.setVisibility(View.INVISIBLE);
            }
        }
        
        private void setupCardShape(Card card) {
            // Reset visibility
            ivCardShape1.setVisibility(View.GONE);
            ivCardShape2.setVisibility(View.GONE);
            ivCardShape3.setVisibility(View.GONE);
            
            // Get the appropriate shape drawable ID based on card properties
            int drawableId = getShapeDrawableId(card);
            
            // Apply color tint
            int colorId = getColorId(card.getColor());
            int colorTint = ContextCompat.getColor(context, colorId);
            
            // Set number of shapes based on card number
            switch (card.getNumber()) {
                case ONE:
                    ivCardShape1.setVisibility(View.VISIBLE);
                    ivCardShape1.setImageResource(drawableId);
                    ivCardShape1.setColorFilter(colorTint);
                    break;
                case TWO:
                    ivCardShape1.setVisibility(View.VISIBLE);
                    ivCardShape3.setVisibility(View.VISIBLE);
                    ivCardShape1.setImageResource(drawableId);
                    ivCardShape3.setImageResource(drawableId);
                    ivCardShape1.setColorFilter(colorTint);
                    ivCardShape3.setColorFilter(colorTint);
                    break;
                case THREE:
                    ivCardShape1.setVisibility(View.VISIBLE);
                    ivCardShape2.setVisibility(View.VISIBLE);
                    ivCardShape3.setVisibility(View.VISIBLE);
                    ivCardShape1.setImageResource(drawableId);
                    ivCardShape2.setImageResource(drawableId);
                    ivCardShape3.setImageResource(drawableId);
                    ivCardShape1.setColorFilter(colorTint);
                    ivCardShape2.setColorFilter(colorTint);
                    ivCardShape3.setColorFilter(colorTint);
                    break;
            }
            
            // Make sure the shapes are properly sized and positioned
            ivCardShape1.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivCardShape2.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivCardShape3.setScaleType(ImageView.ScaleType.FIT_CENTER);
            
            // Apply padding for better appearance
            int padding = 8;
            ivCardShape1.setPadding(padding, padding, padding, padding);
            ivCardShape2.setPadding(padding, padding, padding, padding);
            ivCardShape3.setPadding(padding, padding, padding, padding);
        }
        
        private int getShapeDrawableId(Card card) {
            // Determine the drawable ID based on shape and shading
            // Using the new SET card visuals from the uploaded image
            switch (card.getShape()) {
                case OVAL:
                    switch (card.getShading()) {
                        case SOLID:
                            return R.drawable.shape_solid_oval;
                        case STRIPED:
                            return R.drawable.shape_striped_oval;
                        case OUTLINE:
                            return R.drawable.shape_outline_oval;
                    }
                    break;
                case DIAMOND:
                    switch (card.getShading()) {
                        case SOLID:
                            return R.drawable.shape_solid_diamond;
                        case STRIPED:
                            return R.drawable.shape_striped_diamond;
                        case OUTLINE:
                            return R.drawable.shape_outline_diamond;
                    }
                    break;
                case SQUIGGLE:
                    switch (card.getShading()) {
                        case SOLID:
                            return R.drawable.shape_solid_squiggle;
                        case STRIPED:
                            return R.drawable.shape_striped_squiggle;
                        case OUTLINE:
                            return R.drawable.shape_outline_squiggle;
                    }
                    break;
            }
            
            // Default fallback
            return R.drawable.shape_solid_oval;
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
        
        private void applyColorFilter(ImageView imageView, int colorId) {
            // Apply the color filter to match the SET cards in the image
            imageView.setColorFilter(ContextCompat.getColor(context, colorId));
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