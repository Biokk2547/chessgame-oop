package com.chessegame.model;

public class Rook extends Piece {
    public Rook(Color color) { super(color, 'R'); }

    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        if (from.row != to.row && from.col != to.col) return false;
        return board.isPathClear(from, to);
    }
}
