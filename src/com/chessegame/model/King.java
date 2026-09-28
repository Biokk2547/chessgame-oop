package com.chessegame.model;

public class King extends Piece {
    public King(Color color) { super(color, 'K'); }

    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int dr = Math.abs(from.row - to.row);
        int dc = Math.abs(from.col - to.col);
        if (dr <= 1 && dc <= 1) return !(dr == 0 && dc == 0);

        // Castling: 2 steps horizontally
        if (dr == 0 && dc == 2 && !hasMoved()) {
            int row = from.row;
            if (to.col > from.col) {
                // Kingside castling
                Position rookPos = new Position(row, 7);
                Piece r = board.getPiece(rookPos);
                if (r instanceof Rook && !r.hasMoved() && board.isPathClear(from, rookPos)) {
                    return true;
                }
            } else {
                // Queenside castling
                Position rookPos = new Position(row, 0);
                Piece r = board.getPiece(rookPos);
                if (r instanceof Rook && !r.hasMoved() && board.isPathClear(from, rookPos)) {
                    return true;
                }
            }
        }
        return false;
    }
}
