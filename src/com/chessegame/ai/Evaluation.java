package com.chessegame.ai;

import com.chessegame.model.*;

/**
 * Evaluation class providing material weights and Piece-Square Positional Tables (PST)
 * for scoring chess board states.
 */
public class Evaluation {

    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 320;
    public static final int BISHOP_VALUE = 330;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 20000;

    // Piece-Square Tables (PST) - 8x8 matrices encouraging positional play
    private static final int[] PAWN_TABLE = {
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_TABLE = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_TABLE = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_TABLE = {
          0,  0,  0,  0,  0,  0,  0,  0,
          5, 10, 10, 10, 10, 10, 10,  5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
         -5,  0,  0,  0,  0,  0,  0, -5,
          0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] QUEEN_TABLE = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_TABLE = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    };

    public enum AIStyle {
        MASTER,
        AGGRESSIVE,
        DEFENSIVE
    }

    /**
     * Evaluates total board score. Positive score favors WHITE, negative score favors BLACK.
     */
    public static int evaluate(Board board) {
        return evaluateStyled(board, AIStyle.MASTER);
    }

    public static int evaluateStyled(Board board, AIStyle style) {
        int total = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p != null) {
                    int val = getPieceValue(p);
                    int pst = getPstValue(p, r, c);

                    if (style == AIStyle.AGGRESSIVE) {
                        // Aggressive style: values offensive forward positioning & attacking material higher
                        if (p instanceof Queen || p instanceof Rook || p instanceof Knight) {
                            pst = (int) (pst * 1.35f);
                        }
                    } else if (style == AIStyle.DEFENSIVE) {
                        // Defensive style: values King safety & Pawn structure protection higher
                        if (p instanceof King || p instanceof Pawn) {
                            pst = (int) (pst * 1.40f);
                        }
                    }

                    int score = val + pst;
                    if (p.getColor() == Piece.Color.WHITE) {
                        total += score;
                    } else {
                        total -= score;
                    }
                }
            }
        }
        return total;
    }

    public static int getPieceValue(Piece p) {
        if (p instanceof Pawn) return PAWN_VALUE;
        if (p instanceof Knight) return KNIGHT_VALUE;
        if (p instanceof Bishop) return BISHOP_VALUE;
        if (p instanceof Rook) return ROOK_VALUE;
        if (p instanceof Queen) return QUEEN_VALUE;
        if (p instanceof King) return KING_VALUE;
        return 0;
    }

    private static int getPstValue(Piece p, int row, int col) {
        int idx = (p.getColor() == Piece.Color.WHITE) ? (row * 8 + col) : ((7 - row) * 8 + col);
        if (p instanceof Pawn) return PAWN_TABLE[idx];
        if (p instanceof Knight) return KNIGHT_TABLE[idx];
        if (p instanceof Bishop) return BISHOP_TABLE[idx];
        if (p instanceof Rook) return ROOK_TABLE[idx];
        if (p instanceof Queen) return QUEEN_TABLE[idx];
        if (p instanceof King) return KING_TABLE[idx];
        return 0;
    }
}
