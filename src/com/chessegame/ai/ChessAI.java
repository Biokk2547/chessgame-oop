package com.chessegame.ai;

import com.chessegame.logic.ChessUtils;
import com.chessegame.model.*;

import java.util.*;

/**
 * Minimax + Alpha-Beta Pruning Chess Engine AI with Advanced Enhancements:
 * 1. Opening Book for 0ms instant opening moves (first 5 moves).
 * 2. MVV-LVA (Most Valuable Victim - Least Valuable Attacker) + Check Move
 * Ordering.
 * 3. Quiescence Search (QS) to prevent the Horizon Effect.
 * 4. Iterative Deepening Search (IDS) with 1,500ms Time Management.
 */
public class ChessAI {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    public static class AIMove {
        public final Position from;
        public final Position to;
        public final Piece promoChoice;

        public AIMove(Position from, Position to, Piece promoChoice) {
            this.from = from;
            this.to = to;
            this.promoChoice = promoChoice;
        }
    }

    private static final Random random = new Random();
    private static final long MAX_TIME_MS = 3000L; // 1.3 seconds maximum per turn
    private static boolean searchAborted = false;
    private static long searchDeadline = 0L;

    public static AIMove findBestMove(Board board, Piece.Color aiColor, int depth) {
        return findBestMove(board, aiColor, depth, Evaluation.AIStyle.MASTER);
    }

    public static AIMove findBestMove(Board board, Piece.Color aiColor, int targetDepth, Evaluation.AIStyle style) {
        if (targetDepth < 1)
            targetDepth = 1;
        if (targetDepth > 8)
            targetDepth = 8;

        // Clone working board to ensure thread-safety against live UI thread
        Board workingBoard = board.copy();

        // 1. Opening Book Lookup (0ms Instant Return)
        int moveCount = countTotalMoves(workingBoard);
        AIMove bookMove = OpeningBook.getOpeningMove(workingBoard, aiColor, moveCount);
        if (bookMove != null) {
            return bookMove;
        }

        List<AIMove> legalMoves = generateLegalMoves(workingBoard, aiColor);
        if (legalMoves.isEmpty())
            return null;

        // Shuffle candidate moves initially for equal evaluation variety
        Collections.shuffle(legalMoves, random);

        // Easy mode (Depth 1): 55% chance for random/suboptimal move (Rookie /
        // PirateCat / WitchKitty)
        if (targetDepth == 1 && random.nextFloat() < 0.55f) {
            return legalMoves.get(random.nextInt(legalMoves.size()));
        }

        // Casual mode (Depth 2): 35% chance to pick a secondary move (WitchKitty /
        // Berserker / Shadow Vampire)
        if (targetDepth == 2 && random.nextFloat() < 0.35f && legalMoves.size() > 1) {
            int pick = 1 + random.nextInt(Math.min(3, legalMoves.size() - 1));
            return legalMoves.get(pick);
        }

        // Balanced mode (Depth 3): 25% chance to pick a slightly suboptimal move
        if (targetDepth == 3 && random.nextFloat() < 0.25f && legalMoves.size() > 2) {
            int pick = 1 + random.nextInt(Math.min(3, legalMoves.size() - 1));
            return legalMoves.get(pick);
        }

        // 2. Iterative Deepening Search (IDS) with Adaptive Time Management
        long maxTimeForDepth = (targetDepth <= 2) ? 300L : (targetDepth <= 4) ? 600L : MAX_TIME_MS;
        searchAborted = false;
        searchDeadline = System.currentTimeMillis() + maxTimeForDepth;

        AIMove overallBestMove = legalMoves.get(0);

        for (int currentDepth = 1; currentDepth <= targetDepth; currentDepth++) {
            if (System.currentTimeMillis() >= searchDeadline) {
                break; // Stop if time budget exceeded
            }

            // MVV-LVA Move Ordering
            sortMoves(workingBoard, legalMoves, aiColor);

            AIMove depthBestMove = null;
            int bestScore = (aiColor == Piece.Color.WHITE) ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;

            Piece.Color nextTurn = (aiColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

            for (AIMove move : legalMoves) {
                if (System.currentTimeMillis() >= searchDeadline) {
                    searchAborted = true;
                    break;
                }

                MoveRecord record = workingBoard.moveRecord(move.from, move.to, aiColor, move.promoChoice);
                int score = minimax(workingBoard, currentDepth - 1, alpha, beta, nextTurn, aiColor, style);
                workingBoard.undoMoveRecord(record);

                if (aiColor == Piece.Color.WHITE) {
                    if (score > bestScore) {
                        bestScore = score;
                        depthBestMove = move;
                    }
                    alpha = Math.max(alpha, bestScore);
                } else {
                    if (score < bestScore) {
                        bestScore = score;
                        depthBestMove = move;
                    }
                    beta = Math.min(beta, bestScore);
                }

                if (beta <= alpha)
                    break; // Alpha-Beta Cutoff
            }

            // Only update overallBestMove if depth completed without time abort
            if (!searchAborted && depthBestMove != null) {
                overallBestMove = depthBestMove;
            }

            // Early Mate Cutoff: If forced checkmate is found, return immediately without
            // searching higher depths
            if (!searchAborted && Math.abs(bestScore) > 90000) {
                break;
            }
        }

        return overallBestMove;
    }

    // Minimax Search Algorithm with Alpha-Beta Pruning & Quiescence Search
    private static int minimax(Board board, int depth, int alpha, int beta, Piece.Color turn, Piece.Color aiColor,
            Evaluation.AIStyle style) {
        if (System.currentTimeMillis() >= searchDeadline) {
            searchAborted = true;
            return Evaluation.evaluateStyled(board, style);
        }

        if (depth == 0) {
            // Quiescence Search (QS) at leaf nodes (qDepth = 3 with Delta Pruning)
            return quiescence(board, alpha, beta, turn, aiColor, style, 3);
        }

        List<AIMove> legalMoves = generateLegalMoves(board, turn);
        if (legalMoves.isEmpty()) {
            if (ChessUtils.isInCheck(board, turn)) {
                // In minimax with White perspective: if White is checkmated -> -99999, if Black
                // is checkmated -> +99999
                return (turn == Piece.Color.WHITE) ? (-99999 - depth) : (99999 + depth);
            }
            return 0; // Stalemate
        }

        // Move Ordering: MVV-LVA
        sortMoves(board, legalMoves, turn);

        Piece.Color nextTurn = (turn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        boolean isMaximizing = (turn == Piece.Color.WHITE);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (AIMove move : legalMoves) {
                MoveRecord record = board.moveRecord(move.from, move.to, turn, move.promoChoice);
                int eval = minimax(board, depth - 1, alpha, beta, nextTurn, aiColor, style);
                board.undoMoveRecord(record);

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha)
                    break; // Alpha-Beta Cutoff
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (AIMove move : legalMoves) {
                MoveRecord record = board.moveRecord(move.from, move.to, turn, move.promoChoice);
                int eval = minimax(board, depth - 1, alpha, beta, nextTurn, aiColor, style);
                board.undoMoveRecord(record);

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha)
                    break; // Alpha-Beta Cutoff
            }
            return minEval;
        }
    }

    // Quiescence Search (QS): Resolves ongoing captures at leaf nodes with Delta
    // Pruning
    private static int quiescence(Board board, int alpha, int beta, Piece.Color turn, Piece.Color aiColor,
            Evaluation.AIStyle style, int qDepth) {
        int standPat = Evaluation.evaluateStyled(board, style);

        if (qDepth == 0)
            return standPat;

        boolean isMaximizing = (turn == Piece.Color.WHITE);

        if (isMaximizing) {
            if (standPat >= beta)
                return beta;
            if (standPat > alpha)
                alpha = standPat;

            List<AIMove> captureMoves = generateCaptureMoves(board, turn);
            sortMoves(board, captureMoves, turn);

            Piece.Color nextTurn = Piece.Color.BLACK;
            for (AIMove move : captureMoves) {
                // Delta Pruning: If standPat + victimVal + 200 < alpha, skip move
                Piece victim = board.getPiece(move.to);
                if (victim != null) {
                    int victimVal = Evaluation.getPieceValue(victim);
                    if (standPat + victimVal + 200 < alpha)
                        continue;
                }

                MoveRecord record = board.moveRecord(move.from, move.to, turn, move.promoChoice);
                int score = quiescence(board, alpha, beta, nextTurn, aiColor, style, qDepth - 1);
                board.undoMoveRecord(record);

                if (score >= beta)
                    return beta;
                if (score > alpha)
                    alpha = score;
            }
            return alpha;
        } else {
            if (standPat <= alpha)
                return alpha;
            if (standPat < beta)
                beta = standPat;

            List<AIMove> captureMoves = generateCaptureMoves(board, turn);
            sortMoves(board, captureMoves, turn);

            Piece.Color nextTurn = Piece.Color.WHITE;
            for (AIMove move : captureMoves) {
                // Delta Pruning: If standPat - victimVal - 200 > beta, skip move
                Piece victim = board.getPiece(move.to);
                if (victim != null) {
                    int victimVal = Evaluation.getPieceValue(victim);
                    if (standPat - victimVal - 200 > beta)
                        continue;
                }

                MoveRecord record = board.moveRecord(move.from, move.to, turn, move.promoChoice);
                int score = quiescence(board, alpha, beta, nextTurn, aiColor, style, qDepth - 1);
                board.undoMoveRecord(record);

                if (score <= alpha)
                    return alpha;
                if (score < beta)
                    beta = score;
            }
            return beta;
        }
    }

    // MVV-LVA (Most Valuable Victim - Least Valuable Attacker) Move Scoring (Pure
    // function)
    private static void sortMoves(Board board, List<AIMove> moves, Piece.Color turn) {
        moves.sort((m1, m2) -> Integer.compare(scoreMove(board, m2), scoreMove(board, m1)));
    }

    private static int scoreMove(Board board, AIMove move) {
        int score = 0;
        Piece attacker = board.getPiece(move.from);
        Piece victim = board.getPiece(move.to);

        // 1. MVV-LVA Capture Scoring (Pure lookup, zero board mutation)
        if (victim != null) {
            int victimVal = Evaluation.getPieceValue(victim);
            int attackerVal = (attacker != null) ? Evaluation.getPieceValue(attacker) : 100;
            score += 10000 + (victimVal * 10) - attackerVal; // e.g. Pawn taking Queen = 10000 + (9000) - 100 = 18900
        }

        // 2. Pawn Promotion Priority Bonus
        if (move.promoChoice != null) {
            score += 8000;
        }

        return score;
    }

    private static List<AIMove> generateLegalMoves(Board board, Piece.Color color) {
        List<AIMove> moves = new ArrayList<>();
        for (int r1 = 0; r1 < 8; r1++) {
            for (int c1 = 0; c1 < 8; c1++) {
                Position from = new Position(r1, c1);
                Piece p = board.getPiece(from);
                if (p != null && p.getColor() == color) {
                    for (int r2 = 0; r2 < 8; r2++) {
                        for (int c2 = 0; c2 < 8; c2++) {
                            Position to = new Position(r2, c2);
                            if (ChessUtils.isLegalMove(board, from, to, color)) {
                                if (p instanceof Pawn && (to.row == 0 || to.row == 7)) {
                                    moves.add(new AIMove(from, to, new Queen(color)));
                                } else {
                                    moves.add(new AIMove(from, to, null));
                                }
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    private static List<AIMove> generateCaptureMoves(Board board, Piece.Color color) {
        List<AIMove> captures = new ArrayList<>();
        List<AIMove> allMoves = generateLegalMoves(board, color);
        for (AIMove m : allMoves) {
            if (board.getPiece(m.to) != null) {
                captures.add(m);
            }
        }
        return captures;
    }

    private static int countTotalMoves(Board board) {
        int count = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p != null)
                    count++;
            }
        }
        // Approximate fullmove count: 32 - pieceCount
        return Math.max(1, (32 - count) / 2 + 1);
    }
}
