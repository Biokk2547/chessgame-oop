package com.chessegame.logic;

import com.chessegame.model.*;

/**
 * Utility class for checking game states (check, checkmate, stalemate).
 */
public class ChessUtils {

    /**
     * Checks if a king of given color is in check.
     */
    public static boolean isInCheck(Board board, Piece.Color kingColor) {
        Position kingPos = findKing(board, kingColor);
        if (kingPos == null) return false; // King not found (shouldn't happen in valid game)
        
        Piece.Color opponent = (kingColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        return isSquareAttacked(board, kingPos, opponent);
    }

    /**
     * Checks if a king is in checkmate.
     * Checkmate = king is in check AND has no legal moves.
     */
    public static boolean isCheckmate(Board board, Piece.Color kingColor) {
        if (!isInCheck(board, kingColor)) return false;
        return !hasAnyLegalMove(board, kingColor);
    }

    /**
     * Checks if a position is stalemate.
     * Stalemate = king is NOT in check AND has no legal moves.
     */
    public static boolean isStalemate(Board board, Piece.Color kingColor) {
        if (isInCheck(board, kingColor)) return false;
        return !hasAnyLegalMove(board, kingColor);
    }

    /**
     * Checks if a board position has insufficient material for checkmate (FIDE Article 9.6).
     * Conditions:
     * - King vs King
     * - King + Bishop vs King
     * - King + Knight vs King
     * - King + Bishop vs King + Bishop where both bishops are on the same color squares
     */
    public static boolean isInsufficientMaterial(Board board) {
        if (board == null) return false;
        int whiteKnights = 0, blackKnights = 0;
        int whiteBishops = 0, blackBishops = 0;
        Integer whiteBishopSquareColor = null;
        Integer blackBishopSquareColor = null;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p == null || p instanceof King) continue;

                // Any Pawn, Rook, or Queen guarantees mating material exists
                if (p instanceof Pawn || p instanceof Rook || p instanceof Queen) {
                    return false;
                } else if (p instanceof Knight) {
                    if (p.getColor() == Piece.Color.WHITE) whiteKnights++;
                    else blackKnights++;
                } else if (p instanceof Bishop) {
                    int squareColor = (r + c) % 2;
                    if (p.getColor() == Piece.Color.WHITE) {
                        whiteBishops++;
                        whiteBishopSquareColor = squareColor;
                    } else {
                        blackBishops++;
                        blackBishopSquareColor = squareColor;
                    }
                } else {
                    return false;
                }
            }
        }

        int whiteMinors = whiteKnights + whiteBishops;
        int blackMinors = blackKnights + blackBishops;

        // 1. King vs King
        if (whiteMinors == 0 && blackMinors == 0) return true;

        // 2. King + Knight vs King OR King + Bishop vs King
        if (whiteMinors == 1 && blackMinors == 0) return true;
        if (whiteMinors == 0 && blackMinors == 1) return true;

        // 3. King + Bishop vs King + Bishop (both bishops on same color square)
        if (whiteBishops == 1 && whiteKnights == 0 && blackBishops == 1 && blackKnights == 0) {
            if (whiteBishopSquareColor != null && whiteBishopSquareColor.equals(blackBishopSquareColor)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the 50-move rule has been reached (100 plies without a pawn move or capture).
     */
    public static boolean isFiftyMoveRule(Board board) {
        return board != null && board.getHalfMoveClock() >= 100;
    }

    /**
     * Checks if a specific move would be legal (doesn't leave king in check).
     */
    public static boolean isLegalMove(Board board, Position from, Position to, Piece.Color color) {
        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != color) return false;

        // Destination occupied by same-color piece is not allowed
        Piece target = board.getPiece(to);
        if (target != null && target.getColor() == color) return false;

        // Check basic piece movement
        if (!piece.isValidMove(from, to, board)) return false;

        // Special check for Castling: FIDE rules
        // 1. King cannot castle if currently in check
        // 2. King cannot castle through an attacked square
        // 3. King cannot castle into check
        if (piece instanceof King && Math.abs(to.col - from.col) == 2) {
            Piece.Color opponent = (color == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
            // Rule 1: Cannot castle out of check
            if (isInCheck(board, color)) return false;

            // Rule 2: Cannot castle through check (square King passes through)
            int stepCol = (to.col > from.col) ? (from.col + 1) : (from.col - 1);
            Position passingSquare = new Position(from.row, stepCol);
            if (isSquareAttacked(board, passingSquare, opponent)) return false;

            // Rule 3: Destination square cannot be attacked
            if (isSquareAttacked(board, to, opponent)) return false;
        }

        // Simulate the move
        Board testBoard = simulateMove(board, from, to);

        // Check if king is in check after the move
        boolean kingInCheck = isInCheck(testBoard, color);

        return !kingInCheck;
    }

    /**
     * Checks if a player has any legal move available.
     */
    private static boolean hasAnyLegalMove(Board board, Piece.Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position from = new Position(r, c);
                Piece piece = board.getPiece(from);
                if (piece == null || piece.getColor() != color) continue;

                // Try all possible destination squares
                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        Position to = new Position(toRow, toCol);
                        if (isLegalMove(board, from, to, color)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any piece of attacker color can attack the target square.
     * Uses direct attack rules rather than invoking generic movement logic so
     * blocking/capture/escape checks remain correct for check and checkmate.
     */
    public static boolean isSquareAttacked(Board board, Position target, Piece.Color attacker) {
        // Pawn attacks: one rank forward diagonally
        int pawnStep = (attacker == Piece.Color.WHITE) ? -1 : 1;
        int[][] pawnAttackOffsets = { { pawnStep, -1 }, { pawnStep, 1 } };
        for (int[] offset : pawnAttackOffsets) {
            Position pawnPos = new Position(target.row - offset[0], target.col + offset[1]);
            if (!isInside(pawnPos)) continue;
            Piece p = board.getPiece(pawnPos);
            if (p instanceof Pawn && p.getColor() == attacker) return true;
        }

        // Knight attacks
        int[][] knightOffsets = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        for (int[] offset : knightOffsets) {
            Position knightPos = new Position(target.row + offset[0], target.col + offset[1]);
            if (!isInside(knightPos)) continue;
            Piece p = board.getPiece(knightPos);
            if (p instanceof Knight && p.getColor() == attacker) return true;
        }

        // King attacks adjacent squares
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                Position kingPos = new Position(target.row + dr, target.col + dc);
                if (!isInside(kingPos)) continue;
                Piece p = board.getPiece(kingPos);
                if (p instanceof King && p.getColor() == attacker) return true;
            }
        }

        // Sliding pieces: rook/queen line attacks, bishop/queen diagonal attacks
        int[][] directions = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };
        for (int[] dir : directions) {
            int r = target.row + dir[0];
            int c = target.col + dir[1];
            while (isInside(new Position(r, c))) {
                Position pos = new Position(r, c);
                Piece p = board.getPiece(pos);
                if (p == null) {
                    r += dir[0];
                    c += dir[1];
                    continue;
                }
                if (p.getColor() != attacker) break;
                boolean isLinePiece = (Math.abs(dir[0]) + Math.abs(dir[1]) == 1 && (p instanceof Rook || p instanceof Queen))
                        || (Math.abs(dir[0]) == Math.abs(dir[1]) && (p instanceof Bishop || p instanceof Queen));
                if (isLinePiece) return true;
                break;
            }
        }

        return false;
    }

    private static boolean isInside(Position pos) {
        return pos != null && pos.row >= 0 && pos.row < 8 && pos.col >= 0 && pos.col < 8;
    }

    /**
     * Finds the king of given color on the board.
     */
    private static Position findKing(Board board, Piece.Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p instanceof King && p.getColor() == color) return new Position(r, c);
            }
        }
        return null;
    }

    /**
     * Creates a copy of the board and simulates a move.
     */
    private static Board simulateMove(Board board, Position from, Position to) {
        Board testBoard = new Board();
        // Copy all pieces (shallow copy of references is fine for simulation)
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                testBoard.setPiece(new Position(r, c), p);
            }
        }
        // Preserve last move info
        try {
            testBoard.getClass().getDeclaredField("lastMoveFrom").setAccessible(true);
            testBoard.getClass().getDeclaredField("lastMoveTo").setAccessible(true);
            testBoard.getClass().getDeclaredField("lastMoveFrom").set(testBoard, board.getLastMoveFrom());
            testBoard.getClass().getDeclaredField("lastMoveTo").set(testBoard, board.getLastMoveTo());
        } catch (Exception ignored) {}

        // Execute move on test board, handling en-passant and castling same as Board.move
        Piece piece = testBoard.getPiece(from);
        if (piece != null) {
            Piece target = testBoard.getPiece(to);
            // en-passant
            if (piece instanceof Pawn && from.col != to.col && target == null) {
                Position captured = new Position(from.row, to.col);
                Piece cap = testBoard.getPiece(captured);
                if (cap != null && cap instanceof Pawn) {
                    testBoard.setPiece(captured, null);
                }
            }
            // castling rook move
            if (piece instanceof King && Math.abs(to.col - from.col) == 2) {
                int row = from.row;
                if (to.col > from.col) {
                    Position rookFrom = new Position(row, 7);
                    Position rookTo = new Position(row, from.col + 1);
                    Piece rook = testBoard.getPiece(rookFrom);
                    if (rook instanceof Rook) {
                        testBoard.setPiece(rookTo, rook);
                        testBoard.setPiece(rookFrom, null);
                    }
                } else {
                    Position rookFrom = new Position(row, 0);
                    Position rookTo = new Position(row, from.col - 1);
                    Piece rook = testBoard.getPiece(rookFrom);
                    if (rook instanceof Rook) {
                        testBoard.setPiece(rookTo, rook);
                        testBoard.setPiece(rookFrom, null);
                    }
                }
            }

            testBoard.setPiece(to, piece);
            testBoard.setPiece(from, null);
            // pawn promotion
            if (piece instanceof Pawn) {
                if ((piece.getColor() == Piece.Color.WHITE && to.row == 0) ||
                    (piece.getColor() == Piece.Color.BLACK && to.row == 7)) {
                    testBoard.setPiece(to, new Queen(piece.getColor()));
                }
            }
        }
        return testBoard;
    }
}
