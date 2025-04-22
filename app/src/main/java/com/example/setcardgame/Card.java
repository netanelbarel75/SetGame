package com.example.setcardgame;

import java.io.Serializable;

public class Card implements Serializable {
    public enum Color {
        RED, GREEN, PURPLE;
        
        public static Color fromInt(int value) {
            return Color.values()[value % 3];
        }
    }
    
    public enum Shape {
        OVAL, DIAMOND, SQUIGGLE;
        
        public static Shape fromInt(int value) {
            return Shape.values()[value % 3];
        }
    }
    
    public enum Shading {
        SOLID, STRIPED, OUTLINE;
        
        public static Shading fromInt(int value) {
            return Shading.values()[value % 3];
        }
    }
    
    public enum Number {
        ONE(1), TWO(2), THREE(3);
        
        private final int value;
        
        Number(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static Number fromInt(int value) {
            switch (value % 3) {
                case 0: return ONE;
                case 1: return TWO;
                case 2: return THREE;
                default: return ONE;
            }
        }
    }
    
    private final Color color;
    private final Shape shape;
    private final Shading shading;
    private final Number number;
    private final int id;
    
    public Card(Color color, Shape shape, Shading shading, Number number) {
        this.color = color;
        this.shape = shape;
        this.shading = shading;
        this.number = number;
        
        // Generate a unique ID for the card based on its properties
        this.id = color.ordinal() * 27 + shape.ordinal() * 9 + shading.ordinal() * 3 + number.ordinal();
    }
    
    public Color getColor() {
        return color;
    }
    
    public Shape getShape() {
        return shape;
    }
    
    public Shading getShading() {
        return shading;
    }
    
    public Number getNumber() {
        return number;
    }
    
    public int getId() {
        return id;
    }
    
    /**
     * Checks if three cards form a valid set
     * A valid set requires all properties to be either all the same or all different
     */
    public static boolean isValidSet(Card card1, Card card2, Card card3) {
        boolean colorValid = checkProperty(card1.color, card2.color, card3.color);
        boolean shapeValid = checkProperty(card1.shape, card2.shape, card3.shape);
        boolean shadingValid = checkProperty(card1.shading, card2.shading, card3.shading);
        boolean numberValid = checkProperty(card1.number, card2.number, card3.number);
        
        return colorValid && shapeValid && shadingValid && numberValid;
    }
    
    private static <T> boolean checkProperty(T prop1, T prop2, T prop3) {
        // All three properties are the same
        boolean allSame = prop1.equals(prop2) && prop2.equals(prop3);
        
        // All three properties are different
        boolean allDifferent = !prop1.equals(prop2) && !prop2.equals(prop3) && !prop1.equals(prop3);
        
        return allSame || allDifferent;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Card card = (Card) o;
        return id == card.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public String toString() {
        return "Card{" +
                "color=" + color +
                ", shape=" + shape +
                ", shading=" + shading +
                ", number=" + number +
                ", id=" + id +
                '}';
    }
}