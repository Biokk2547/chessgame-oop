package com.chessegame.model;

public class Pawn extends Piece {
    public Pawn(Color color) { super(color, 'P'); }

    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int dir = (getColor() == Color.WHITE) ? -1 : 1; // white moves up (row-1)
        int startRow = (getColor() == Color.WHITE) ? 6 : 1;
        // Forward move
        if (from.col == to.col) {
            // one step
            if (to.row - from.row == dir && board.getPiece(to) == null) return true;
            // two steps from start
            if (from.row == startRow && to.row - from.row == 2*dir) {
                Position intermediate = new Position(from.row + dir, from.col);
                if (board.getPiece(intermediate) == null && board.getPiece(to) == null) return true;
            }
            return false;
        }
        // Capture
        if (Math.abs(from.col - to.col) == 1 && to.row - from.row == dir) {
            Piece target = board.getPiece(to);
            if (target != null && target.getColor() != getColor()) return true;

            // En Passant check
            if (target == null) {
                Position lastFrom = board.getLastMoveFrom();
                Position lastTo = board.getLastMoveTo();
                if (lastFrom != null && lastTo != null) {
                    Piece lastMoved = board.getPiece(lastTo);
                    if (lastMoved instanceof Pawn && lastMoved.getColor() != getColor() &&
                        lastTo.row == from.row && lastTo.col == to.col &&
                        Math.abs(lastFrom.row - lastTo.row) == 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
