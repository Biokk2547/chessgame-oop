package com.chessegame.model;

public class Queen extends Piece {
    public Queen(Color color) { super(color, 'Q'); }

    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        if (from.row == to.row || from.col == to.col) return board.isPathClear(from, to);
        if (Math.abs(from.row - to.row) == Math.abs(from.col - to.col)) return board.isPathClear(from, to);
        return false;
    }
}
