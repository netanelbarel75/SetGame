package com.example.setcardgame;

import com.example.setcardgame.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameModel {
    private static final int INITIAL_BOARD_SIZE = 12;
    private static final int ADD_CARDS_COUNT = 3;
    
    private List<Card> deck;
    private List<Card> board;
    private List<Card> selectedCards;
    private int score;
    private long startTimeMillis;
    private boolean gameOver;
    private Random random;
    
    public GameModel() {
        deck = new ArrayList<>();
        board = new ArrayList<>();
        selectedCards = new ArrayList<>();
        score = 0;
        gameOver = false;
        random = new Random();
        
        startTimeMillis = System.currentTimeMillis();
        initializeDeck();
    }
    
    private void initializeDeck() {
        deck.clear();
        // Create a complete deck of 81 unique cards (3^4 combinations)
        for (Card.Color color : Card.Color.values()) {
            for (Card.Shape shape : Card.Shape.values()) {
                for (Card.Shading shading : Card.Shading.values()) {
                    for (Card.Number number : Card.Number.values()) {
                        deck.add(new Card(color, shape, shading, number));
                    }
                }
            }
        }
        
        // Shuffle the deck
        Collections.shuffle(deck, random);
    }
    
    public void startNewGame() {
        initializeDeck();
        board.clear();
        selectedCards.clear();
        score = 0;
        gameOver = false;
        startTimeMillis = System.currentTimeMillis();
        
        // Deal initial cards
        dealCards(INITIAL_BOARD_SIZE);
        
        // Make sure there's at least one valid set on the board
        ensureValidSetExists();
    }
    
    private void dealCards(int count) {
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            board.add(deck.remove(0));
        }
    }
    
    public boolean addCards() {
        if (deck.isEmpty()) {
            return false;
        }
        
        dealCards(ADD_CARDS_COUNT);
        return true;
    }
    
    public boolean selectCard(int position) {
        if (position < 0 || position >= board.size()) {
            return false;
        }
        
        Card card = board.get(position);
        
        // If card is already selected, deselect it (only if we haven't selected 3 cards yet)
        if (selectedCards.contains(card)) {
            // Only allow deselection if we don't have 3 cards yet (prevent changing while validating)
            if (selectedCards.size() < 3) {
                selectedCards.remove(card);
                return true;
            }
            return false;
        } 
        // Only allow selection of new cards if we haven't reached 3 cards yet
        else if (selectedCards.size() < 3) {
            selectedCards.add(card);
            return true;
        }
        
        return false;
    }
    
    /**
     * Clears the selected cards without processing them
     */
    public void clearSelectedCards() {
        selectedCards.clear();
    }
    
    public void processSelectedSet() {
        if (selectedCards.size() != 3) {
            return;
        }
        
        if (isSelectedSetValid()) {
            // Valid set found - increase score based on sets found
            score++; // Each set found adds one to the score
            
            List<Card> selectedCopy = new ArrayList<>(selectedCards);
            
            // Deal new cards to replace the selected ones
            if (!deck.isEmpty()) {
                // For each selected card, replace it in the same position
                for (Card card : selectedCopy) {
                    int index = board.indexOf(card);
                    if (index != -1 && !deck.isEmpty()) {
                        board.set(index, deck.remove(0));
                    } else {
                        // If we can't replace (deck empty), remove the card
                        board.remove(card);
                    }
                }
            } else {
                // If deck is empty, just remove the cards
                board.removeAll(selectedCopy);
            }
            
            // Check if game is over
            checkGameOver();
        }
        
        // Clear the selected cards
        selectedCards.clear();
    }
    
    private boolean isSelectedSetValid() {
        if (selectedCards.size() != 3) {
            return false;
        }
        
        return Card.isValidSet(
                selectedCards.get(0),
                selectedCards.get(1),
                selectedCards.get(2)
        );
    }
    
    private void ensureValidSetExists() {
        while (!hasValidSet() && !deck.isEmpty()) {
            addCards();
        }
        
        // If still no valid set and deck is empty, game is over
        if (!hasValidSet() && deck.isEmpty()) {
            gameOver = true;
        }
    }
    
    public boolean hasValidSet() {
        int size = board.size();
        
        for (int i = 0; i < size - 2; i++) {
            for (int j = i + 1; j < size - 1; j++) {
                for (int k = j + 1; k < size; k++) {
                    if (Card.isValidSet(board.get(i), board.get(j), board.get(k))) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    public List<Integer> findValidSet() {
        int size = board.size();
        List<Integer> setIndices = new ArrayList<>();
        
        for (int i = 0; i < size - 2; i++) {
            for (int j = i + 1; j < size - 1; j++) {
                for (int k = j + 1; k < size; k++) {
                    if (Card.isValidSet(board.get(i), board.get(j), board.get(k))) {
                        setIndices.add(i);
                        setIndices.add(j);
                        setIndices.add(k);
                        return setIndices;
                    }
                }
            }
        }
        
        return setIndices; // Empty if no set found
    }
    
    private void checkGameOver() {
        // Game is over if deck is empty and there are no valid sets on the board
        if (deck.isEmpty() && !hasValidSet()) {
            gameOver = true;
        }
    }
    
    public List<Card> getBoard() {
        return board;
    }
    
    public List<Card> getSelectedCards() {
        return selectedCards;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getRemainingCards() {
        return deck.size();
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * Manually set the game over state
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    public long getElapsedTimeSeconds() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000;
    }
}