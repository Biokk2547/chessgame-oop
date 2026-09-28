package com.chessegame.model;

public class Bishop extends Piece {
    public Bishop(Color color) { super(color, 'B'); }

    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        if (Math.abs(from.row - to.row) != Math.abs(from.col - to.col)) return false;
        return board.isPathClear(from, to);
    }
}
