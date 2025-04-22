package com.example.setgame;

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
        
        if (selectedCards.contains(card)) {
            selectedCards.remove(card);
            return true;
        } else if (selectedCards.size() < 3) {
            selectedCards.add(card);
            
            // Check if we have 3 cards selected
            if (selectedCards.size() == 3) {
                processSelectedSet();
            }
            return true;
        }
        
        return false;
    }
    
    private void processSelectedSet() {
        if (isSelectedSetValid()) {
            // Valid set found
            score++;
            
            // Remove the selected cards from the board
            board.removeAll(selectedCards);
            
            // Deal new cards if board size is less than initial size
            if (board.size() < INITIAL_BOARD_SIZE) {
                dealCards(Math.min(selectedCards.size(), deck.size()));
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
    
    public long getElapsedTimeSeconds() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000;
    }
}