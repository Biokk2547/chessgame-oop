package com.chessegame.model;

public class Player {
    private final String name;
    private final Piece.Color color;

    public Player(String name, Piece.Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public Piece.Color getColor() { return color; }
}
