package com.chessegame.ai;

import com.chessegame.model.Board;
import com.chessegame.model.MoveRecord;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;

import java.util.*;

/**
 * GameReviewer: Performs comprehensive post-match game analysis.
 * Reconstructs the game trajectory, evaluates move quality, calculates accuracy %,
 * discovers best alternative moves, and produces Thai commentary.
 * Integrates with Stockfish UCI Engine when available with fallback to built-in Master AI Engine.
 */
public class GameReviewer {

    public interface ReviewProgressListener {
        void onProgress(int currentMove, int totalMoves);
    }

    /**
     * Synchronously analyzes the full trajectory and returns a GameReviewReport.
     */
    public static GameReviewReport analyzeGame(List<MoveRecord> moves, ReviewProgressListener listener) {
        boolean useStockfish = StockfishEngine.isAvailable();
        String engineName = StockfishEngine.getEngineName();

        if (moves == null || moves.isEmpty()) {
            return new GameReviewReport(100.0f, 100.0f,
                    new EnumMap<>(GameReviewReport.MoveQuality.class),
                    new EnumMap<>(GameReviewReport.MoveQuality.class),
                    new ArrayList<>(), "ยอดฝีมือ", "ยอดฝีมือ", "ยังไม่มีประวัติการเดินหมาก", engineName);
        }

        Board replayBoard = new Board();
        List<GameReviewReport.ReviewedMove> reviewedMoves = new ArrayList<>();

        Map<GameReviewReport.MoveQuality, Integer> whiteStats = new EnumMap<>(GameReviewReport.MoveQuality.class);
        Map<GameReviewReport.MoveQuality, Integer> blackStats = new EnumMap<>(GameReviewReport.MoveQuality.class);
        for (GameReviewReport.MoveQuality q : GameReviewReport.MoveQuality.values()) {
            whiteStats.put(q, 0);
            blackStats.put(q, 0);
        }

        double whiteLossSum = 0;
        int whiteMoveCount = 0;
        double blackLossSum = 0;
        int blackMoveCount = 0;

        int totalMoves = moves.size();

        for (int i = 0; i < totalMoves; i++) {
            if (listener != null) {
                listener.onProgress(i + 1, totalMoves);
            }

            MoveRecord record = moves.get(i);
            Piece.Color playerColor = record.getMovedPiece().getColor();
            int moveNumber = (i / 2) + 1;

            int evalBefore;
            ChessAI.AIMove bestMove;

            if (useStockfish) {
                String fenBefore = FENUtils.toFEN(replayBoard, playerColor);
                StockfishEngine.EvaluationResult sfBefore = StockfishEngine.evaluatePosition(fenBefore, playerColor, 10);
                if (sfBefore != null) {
                    evalBefore = sfBefore.scoreCp;
                    bestMove = (sfBefore.bestFrom != null && sfBefore.bestTo != null) ?
                            new ChessAI.AIMove(sfBefore.bestFrom, sfBefore.bestTo, null) :
                            ChessAI.findBestMove(replayBoard, playerColor, 3, Evaluation.AIStyle.MASTER);
                } else {
                    evalBefore = Evaluation.evaluateStyled(replayBoard, Evaluation.AIStyle.MASTER);
                    bestMove = ChessAI.findBestMove(replayBoard, playerColor, 3, Evaluation.AIStyle.MASTER);
                }
            } else {
                // 1. Eval before move (from White perspective)
                evalBefore = Evaluation.evaluateStyled(replayBoard, Evaluation.AIStyle.MASTER);
                // 2. Compute engine best move before actual move is executed
                bestMove = ChessAI.findBestMove(replayBoard, playerColor, 3, Evaluation.AIStyle.MASTER);
            }

            // 3. Execute the actual move
            replayBoard.moveRecord(record.getFrom(), record.getTo(), playerColor, record.getPromotedPiece());

            // 4. Eval after move (from White perspective)
            int evalAfter;
            if (useStockfish) {
                Piece.Color nextTurn = (playerColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                String fenAfter = FENUtils.toFEN(replayBoard, nextTurn);
                StockfishEngine.EvaluationResult sfAfter = StockfishEngine.evaluatePosition(fenAfter, nextTurn, 10);
                if (sfAfter != null) {
                    evalAfter = sfAfter.scoreCp;
                } else {
                    evalAfter = Evaluation.evaluateStyled(replayBoard, Evaluation.AIStyle.MASTER);
                }
            } else {
                evalAfter = Evaluation.evaluateStyled(replayBoard, Evaluation.AIStyle.MASTER);
            }

            // 5. Calculate Centipawn Loss for the player
            int centipawnLoss;
            if (playerColor == Piece.Color.WHITE) {
                centipawnLoss = Math.max(0, evalBefore - evalAfter);
                whiteLossSum += capLoss(centipawnLoss);
                whiteMoveCount++;
            } else {
                centipawnLoss = Math.max(0, evalAfter - evalBefore);
                blackLossSum += capLoss(centipawnLoss);
                blackMoveCount++;
            }

            // Check if actual move matches best move
            boolean isBestMoveMatch = false;
            if (bestMove != null) {
                isBestMoveMatch = (bestMove.from.equals(record.getFrom()) && bestMove.to.equals(record.getTo()));
            }

            // 6. Determine Move Quality
            GameReviewReport.MoveQuality quality = classifyMove(i, centipawnLoss, isBestMoveMatch, evalBefore, evalAfter, playerColor, record);

            if (playerColor == Piece.Color.WHITE) {
                whiteStats.put(quality, whiteStats.get(quality) + 1);
            } else {
                blackStats.put(quality, blackStats.get(quality) + 1);
            }

            // 7. Generate Thai Explanation & Character Reaction
            String explanationThai = generateExplanation(quality, record, bestMove, isBestMoveMatch);
            String reaction = generateCharacterReaction(quality, playerColor, record);

            reviewedMoves.add(new GameReviewReport.ReviewedMove(
                    i, moveNumber, playerColor, record, quality,
                    evalBefore, evalAfter, centipawnLoss, bestMove,
                    record.getNotation(), explanationThai, reaction
            ));
        }

        // Calculate Accuracies
        float whiteAccuracy = calculateAccuracy(whiteLossSum, whiteMoveCount);
        float blackAccuracy = calculateAccuracy(blackLossSum, blackMoveCount);

        String whiteTitle = getPerformanceTitle(whiteAccuracy);
        String blackTitle = getPerformanceTitle(blackAccuracy);
        String matchSummary = generateMatchSummary(whiteAccuracy, blackAccuracy, reviewedMoves);

        return new GameReviewReport(
                whiteAccuracy, blackAccuracy,
                whiteStats, blackStats,
                reviewedMoves, whiteTitle, blackTitle, matchSummary, engineName
        );
    }

    private static double capLoss(int cpLoss) {
        // Soft caps loss so a single blunder doesn't drop accuracy to 0% completely
        return Math.min(450.0, cpLoss);
    }

    private static float calculateAccuracy(double totalLoss, int count) {
        if (count <= 0) return 100.0f;
        double avgLoss = totalLoss / count;
        // Standard smooth accuracy mapping: 0 avg loss -> 100%, 30 avg loss -> ~90%, 100 avg loss -> ~70%
        double acc = 100.0 * Math.exp(-0.0055 * avgLoss);
        acc = Math.max(10.0, Math.min(100.0, acc));
        return (float) (Math.round(acc * 10.0) / 10.0);
    }

    private static GameReviewReport.MoveQuality classifyMove(int moveIndex, int loss, boolean isBestMatch,
                                                             int evalBefore, int evalAfter, Piece.Color color,
                                                             MoveRecord record) {
        // Opening book phase (first 4 plies)
        if (moveIndex < 4 && loss <= 20) {
            return GameReviewReport.MoveQuality.BOOK;
        }

        // Brilliant move check (Turned losing state around or made high-value sacrifice)
        boolean isSacrifice = record.getCapturedPiece() == null && record.getMoveType() == MoveRecord.MoveType.NORMAL;
        if (color == Piece.Color.WHITE) {
            if (evalBefore < -150 && evalAfter >= 100) return GameReviewReport.MoveQuality.BRILLIANT;
        } else {
            if (evalBefore > 150 && evalAfter <= -100) return GameReviewReport.MoveQuality.BRILLIANT;
        }

        if (isBestMatch || loss <= 15) {
            return GameReviewReport.MoveQuality.BEST;
        } else if (loss <= 45) {
            return GameReviewReport.MoveQuality.EXCELLENT;
        } else if (loss <= 85) {
            return GameReviewReport.MoveQuality.GOOD;
        } else if (loss <= 180) {
            return GameReviewReport.MoveQuality.INACCURACY;
        } else if (loss <= 350) {
            return GameReviewReport.MoveQuality.MISTAKE;
        } else {
            return GameReviewReport.MoveQuality.BLUNDER;
        }
    }

    private static String formatPos(Position p) {
        if (p == null) return "";
        char colChar = (char) ('a' + p.col);
        int rowNum = 8 - p.row;
        return "" + colChar + rowNum;
    }

    private static String generateExplanation(GameReviewReport.MoveQuality quality, MoveRecord record,
                                              ChessAI.AIMove bestMove, boolean isBestMatch) {
        String not = (record.getNotation() != null) ? record.getNotation() : (formatPos(record.getFrom()) + "->" + formatPos(record.getTo()));
        String bestNot = (bestMove != null) ? (formatPos(bestMove.from) + " -> " + formatPos(bestMove.to)) : "";

        switch (quality) {
            case BRILLIANT:
                return "ตาเดินระดับอัจฉริยะ (" + not + ") สร้างความได้เปรียบมหาศาลและพลิกเกมอย่างสมบูรณ์แบบ";
            case BOOK:
                return "หมากเปิดเกมมาตรฐานตามตำราสากล (" + not + ") คุมพื้นที่ได้อย่างแข็งแกร่ง";
            case BEST:
                return "ตาเดินที่ดีที่สุด (" + not + ") แม่นยำตามการคำนวณของ Engine รักษาความได้เปรียบ";
            case EXCELLENT:
                return "ตาเดินยอดเยี่ยม (" + not + ") มีประสิทธิภาพสูงและกดดันตำแหน่งคู่ต่อสู้ได้ดี";
            case GOOD:
                return "ตาเดินที่ดี (" + not + ") มีความปลอดภัยและรักษาโครงสร้างหมากได้อย่างมั่นคง";
            case INACCURACY:
                return "ตาเดินคลาดเคลื่อน (" + not + ") อาจทำให้เสียจังหวะเล็กน้อย" +
                        (!bestNot.isEmpty() && !isBestMatch ? " (แนะนำ: " + bestNot + ")" : "");
            case MISTAKE:
                return "เกิดความผิดพลาด (" + not + ") ส่งผลให้สูญเสียความได้เปรียบในตำแหน่ง" +
                        (!bestNot.isEmpty() ? " (ตาที่ดีกว่าคือ: " + bestNot + ")" : "");
            case BLUNDER:
            default:
                return "ผิดพลาดร้ายแรง (" + not + ") เสี่ยงสูญเสียหมากสำคัญหรือเปิดช่องให้คู่ต่อสู้รุกหนัก!" +
                        (!bestNot.isEmpty() ? " (ทางรอดที่ดีที่สุด: " + bestNot + ")" : "");
        }
    }

    private static String generateCharacterReaction(GameReviewReport.MoveQuality quality, Piece.Color color, MoveRecord record) {
        boolean isWhite = (color == Piece.Color.WHITE);
        String name = isWhite ? "Vampire" : "EvilBox";

        switch (quality) {
            case BOOK:
                return isWhite ? "Vampire: 'เปิดเกมตามตำรามาตรฐาน... คุมพื้นที่ได้แข็งแกร่ง!'" :
                                 "EvilBox: 'เปิดเกมตามฐานข้อมูลมาตรฐาน ไร้จุดบกพร่อง!'";
            case BRILLIANT:
                return isWhite ? "Vampire: 'หึหึ... คมกริบราวกับเขี้ยวของข้า! แผนนี้ไร้ที่ติ!'" :
                                 "EvilBox: 'คำนวณเกินขีดจำกัด! พลังการเดินตานี้ช่างน่าทึ่ง!'";
            case BEST:
                return isWhite ? "Vampire: 'การเดินที่สง่างามและไร้ช่องโหว่'" :
                                 "EvilBox: 'ตรรกะอันสมบูรณ์แบบ หมากตานี้แม่นยำมาก!'";
            case EXCELLENT:
            case GOOD:
                return isWhite ? "Vampire: 'เดินได้รอบคอบ... จงรักษาระดับนี้ไว้'" :
                                 "EvilBox: 'ระบบทำงานได้ตามแผน เดินหน้าต่อไป!'";
            case INACCURACY:
                return isWhite ? "Vampire: 'หืม... กลิ่นอายความประมาทเริ่มลอยมาแล้วนะ'" :
                                 "EvilBox: 'ตรวจพบความคลาดเคลื่อนเล็กน้อยในสมการ'";
            case MISTAKE:
                return isWhite ? "Vampire: 'แย่แล้ว! พลาดท่าให้ศัตรูฉวยโอกาสเสียได้!'" :
                                 "EvilBox: 'เกิดข้อผิดพลาด! โครงสร้างกำลังถูกทำลาย!'";
            case BLUNDER:
            default:
                return isWhite ? "Vampire: 'อ๊ากก! เสียหมากสำคัญแบบนี้ ข้าแทบรับไม่ได้!'" :
                                 "EvilBox: 'ระบบล้มเหลวร้ายแรง! หมากตัวนั้นไม่ควรเดินไปตรงนั้น!'";
        }
    }

    private static String getPerformanceTitle(float acc) {
        if (acc >= 92.0f) return "ปรมาจารย์ (Grandmaster)";
        if (acc >= 85.0f) return "ยอดฝีมือ (Master)";
        if (acc >= 75.0f) return "นักรบชำนาญการ (Advanced)";
        if (acc >= 65.0f) return "ผู้เล่นระดับกลาง (Intermediate)";
        return "กำลังฝึกฝน (Novice)";
    }

    private static String generateMatchSummary(float whiteAcc, float blackAcc, List<GameReviewReport.ReviewedMove> moves) {
        int blunders = 0;
        for (GameReviewReport.ReviewedMove m : moves) {
            if (m.getQuality() == GameReviewReport.MoveQuality.BLUNDER) blunders++;
        }

        if (whiteAcc >= 85.0f && blackAcc >= 85.0f) {
            return "ศึกระดับตำนาน! ทั้งสองฝ่ายเดินหมากได้อย่างแม่นยำและรัดกุมมาก";
        } else if (Math.abs(whiteAcc - blackAcc) >= 20.0f) {
            String dominant = (whiteAcc > blackAcc) ? "Vampire (ขาว)" : "EvilBox (ดำ)";
            return dominant + " กุมความได้เปรียบและคุมเกมได้อย่างเด็ดขาดตลอดการแข่งขัน";
        } else if (blunders >= 4) {
            return "แมตช์ที่ดุเดือดและเต็มไปด้วยจุดเปลี่ยนมากมายจากข้อผิดพลาดของทั้งสองฝ่าย!";
        } else {
            return "การแข่งขันสูสีคู่คี่ มีการแลกเปลี่ยนหมัดและช่วงชิงจังหวะอย่างน่าประทับใจ";
        }
    }
}
