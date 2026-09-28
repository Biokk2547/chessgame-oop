package com.chessegame.model;

public abstract class Piece {
    public enum Color { WHITE, BLACK }
    protected final Color color;
    protected final char symbol; // uppercase for white, lowercase for black
    // Track whether this piece has moved (for castling, pawn double-step)
    protected boolean moved = false;

    protected Piece(Color color, char symbol) {
        this.color = color;
        this.symbol = color == Color.WHITE ? Character.toUpperCase(symbol) : Character.toLowerCase(symbol);
    }

    public Color getColor() { return color; }
    public char getSymbol() { return symbol; }

    public boolean hasMoved() { return moved; }
    public void setMoved(boolean v) { moved = v; }

    // Validate move ignoring checks, returns true if piece-specific movement rules allow it
    public abstract boolean isValidMove(Position from, Position to, Board board);
}
