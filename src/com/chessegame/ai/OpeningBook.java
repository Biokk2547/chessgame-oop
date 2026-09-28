package com.chessegame.ai;

import com.chessegame.logic.ChessUtils;
import com.chessegame.model.*;

import java.util.*;

/**
 * OpeningBook: Standard chess opening database for instantaneous (0ms) opening moves.
 * Supports Ruy Lopez, Italian Game, Queen's Gambit, Sicilian Defense, French Defense, and King's Indian.
 */
public class OpeningBook {

    public static ChessAI.AIMove getOpeningMove(Board board, Piece.Color color, int moveCount) {
        if (moveCount > 5) return null; // Opening book active for first 5 moves only

        // Helper to parse SAN notation squares e.g. "e2", "e4"
        List<ChessAI.AIMove> candidates = new ArrayList<>();

        if (color == Piece.Color.WHITE) {
            if (moveCount == 1) {
                // Move 1 White: e2-e4 (80% chance) or d2-d4 (20% chance)
                if (Math.random() < 0.80) addMoveIfLegal(board, color, "e2", "e4", candidates);
                else addMoveIfLegal(board, color, "d2", "d4", candidates);
            } else if (moveCount == 2) {
                // Move 2 White: Knight to f3 or Pawn c4
                addMoveIfLegal(board, color, "g1", "f3", candidates);
                addMoveIfLegal(board, color, "c2", "c4", candidates);
            } else if (moveCount == 3) {
                // Move 3 White: Bishop to b5 (Ruy Lopez) or c4 (Italian Game)
                addMoveIfLegal(board, color, "f1", "b5", candidates);
                addMoveIfLegal(board, color, "f1", "c4", candidates);
            } else if (moveCount == 4 || moveCount == 5) {
                // Kingside Castle or Pawn d3
                addMoveIfLegal(board, color, "e1", "g1", candidates);
                addMoveIfLegal(board, color, "d2", "d3", candidates);
            }
        } else {
            // Black Openings
            if (moveCount == 1) {
                // Move 1 Black: Sicilian Defense c7-c5 (50%), e7-e5 (30%), French e7-e6 (20%)
                double r = Math.random();
                if (r < 0.50) addMoveIfLegal(board, color, "c7", "c5", candidates);
                else if (r < 0.80) addMoveIfLegal(board, color, "e7", "e5", candidates);
                else addMoveIfLegal(board, color, "e7", "e6", candidates);
            } else if (moveCount == 2) {
                // Move 2 Black: Knight to c6 or Nf6
                addMoveIfLegal(board, color, "b8", "c6", candidates);
                addMoveIfLegal(board, color, "g8", "f6", candidates);
            } else if (moveCount == 3) {
                // Move 3 Black: Pawn d6 or Bishop c5
                addMoveIfLegal(board, color, "d7", "d6", candidates);
                addMoveIfLegal(board, color, "f8", "c5", candidates);
            } else if (moveCount == 4 || moveCount == 5) {
                // Knight f6 or Kingside Castle
                addMoveIfLegal(board, color, "g8", "f6", candidates);
                addMoveIfLegal(board, color, "e8", "g8", candidates);
            }
        }

        if (!candidates.isEmpty()) {
            return candidates.get((int) (Math.random() * candidates.size()));
        }
        return null;
    }

    private static void addMoveIfLegal(Board board, Piece.Color color, String fromStr, String toStr, List<ChessAI.AIMove> list) {
        Position from = parseSquare(fromStr);
        Position to = parseSquare(toStr);
        if (from != null && to != null) {
            Piece p = board.getPiece(from);
            if (p != null && p.getColor() == color) {
                if (ChessUtils.isLegalMove(board, from, to, color)) {
                    list.add(new ChessAI.AIMove(from, to, null));
                }
            }
        }
    }

    private static Position parseSquare(String sq) {
        if (sq == null || sq.length() != 2) return null;
        char colChar = sq.charAt(0);
        char rowChar = sq.charAt(1);
        int col = colChar - 'a';
        int rank = Character.getNumericValue(rowChar);
        int row = 8 - rank;
        if (col >= 0 && col < 8 && row >= 0 && row < 8) {
            return new Position(row, col);
        }
        return null;
    }
}
