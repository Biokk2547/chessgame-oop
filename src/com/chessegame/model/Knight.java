package com.chessegame.model;

public class Knight extends Piece {
    public Knight(Color color) { super(color, 'N'); }

    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int dr = Math.abs(from.row - to.row);
        int dc = Math.abs(from.col - to.col);
        return (dr == 1 && dc == 2) || (dr == 2 && dc == 1);
    }
}
