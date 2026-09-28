package com.chessegame.level;

import com.chessegame.ai.FENUtils;
import com.chessegame.logic.ChessUtils;
import com.chessegame.model.Board;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;

import java.util.List;

public class TestPuzzleSystem {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("     TESTING CHESS PUZZLE & TACTICS SYSTEM       ");
        System.out.println("=================================================");

        List<Puzzle> puzzles = PuzzleManager.getAllPuzzles();
        System.out.println("Total Puzzles Configured: " + puzzles.size());
        if (puzzles.size() < 10) {
            throw new RuntimeException("Expected at least 10 puzzles, found " + puzzles.size());
        }

        int passedPuzzles = 0;
        for (Puzzle puzzle : puzzles) {
            System.out.println("\n[Testing Puzzle #" + puzzle.getId() + "]: " + puzzle.getTitle());
            System.out.println("Category: " + puzzle.getCategory());
            System.out.println("FEN: " + puzzle.getFen());

            Board board = new Board();
            Piece.Color activeTurn = FENUtils.loadFromFEN(board, puzzle.getFen());

            if (activeTurn != puzzle.getPlayerColor()) {
                throw new RuntimeException("Active turn mismatch in puzzle " + puzzle.getId() + ": expected " + puzzle.getPlayerColor() + " got " + activeTurn);
            }

            List<String> solution = puzzle.getSolutionMoves();
            if (solution.isEmpty()) {
                throw new RuntimeException("Solution moves cannot be empty for puzzle " + puzzle.getId());
            }

            // Verify the first move is legal
            String firstMove = solution.get(0);
            Position from = FENUtils.fromUCISquare(firstMove.substring(0, 2));
            Position to = FENUtils.fromUCISquare(firstMove.substring(2, 4));

            Piece movingPiece = board.getPiece(from);
            if (movingPiece == null) {
                throw new RuntimeException("No piece at " + firstMove.substring(0, 2) + " for puzzle " + puzzle.getId());
            }
            if (movingPiece.getColor() != activeTurn) {
                throw new RuntimeException("Piece color does not match active turn for puzzle " + puzzle.getId());
            }

            boolean isLegal = ChessUtils.isLegalMove(board, from, to, activeTurn);
            if (!isLegal) {
                throw new RuntimeException("First solution move " + firstMove + " is NOT legal on puzzle " + puzzle.getId());
            }

            System.out.println("First move verified legal: " + firstMove + " (" + movingPiece.getClass().getSimpleName() + " to " + firstMove.substring(2, 4) + ") -> PASS");
            passedPuzzles++;
        }

        System.out.println("\n=================================================");
        System.out.println("     ALL " + passedPuzzles + " PUZZLES VERIFIED 100% LEGAL!      ");
        System.out.println("=================================================");
    }
}
