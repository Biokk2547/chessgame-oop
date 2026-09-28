package com.chessegame.ai;

import com.chessegame.model.MoveRecord;
import com.chessegame.model.Piece;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Data model representing the comprehensive report of a game review.
 */
public class GameReviewReport {

    public enum MoveQuality {
        BOOK("ตำรา", "Book", 0.0f),
        BRILLIANT("ยอดเยี่ยม", "!!", 1.0f),
        BEST("ดีที่สุด", "★", 1.0f),
        EXCELLENT("ยอดเยี่ยม", "+", 0.90f),
        GOOD("ดี", "+", 0.75f),
        INACCURACY("คลาดเคลื่อน", "?!", 0.50f),
        MISTAKE("ผิดพลาด", "?", 0.20f),
        BLUNDER("ผิดพลาดร้ายแรง", "??", 0.0f);

        private final String labelThai;
        private final String symbol;
        private final float weight;

        MoveQuality(String labelThai, String symbol, float weight) {
            this.labelThai = labelThai;
            this.symbol = symbol;
            this.weight = weight;
        }

        public String getLabelThai() { return labelThai; }
        public String getSymbol() { return symbol; }
        public float getWeight() { return weight; }
    }

    public static class ReviewedMove {
        private final int moveIndex; // 0-based
        private final int moveNumber; // 1, 2, 3...
        private final Piece.Color playerColor;
        private final MoveRecord moveRecord;
        private final MoveQuality quality;
        private final int evalBefore; // Centipawns from White perspective
        private final int evalAfter;  // Centipawns from White perspective
        private final int centipawnLoss; // Positive loss value for the player
        private final ChessAI.AIMove bestAlternative;
        private final String notation;
        private final String explanationThai;
        private final String characterReaction;

        public ReviewedMove(int moveIndex, int moveNumber, Piece.Color playerColor,
                            MoveRecord moveRecord, MoveQuality quality,
                            int evalBefore, int evalAfter, int centipawnLoss,
                            ChessAI.AIMove bestAlternative, String notation,
                            String explanationThai, String characterReaction) {
            this.moveIndex = moveIndex;
            this.moveNumber = moveNumber;
            this.playerColor = playerColor;
            this.moveRecord = moveRecord;
            this.quality = quality;
            this.evalBefore = evalBefore;
            this.evalAfter = evalAfter;
            this.centipawnLoss = centipawnLoss;
            this.bestAlternative = bestAlternative;
            this.notation = notation;
            this.explanationThai = explanationThai;
            this.characterReaction = characterReaction;
        }

        public int getMoveIndex() { return moveIndex; }
        public int getMoveNumber() { return moveNumber; }
        public Piece.Color getPlayerColor() { return playerColor; }
        public MoveRecord getMoveRecord() { return moveRecord; }
        public MoveQuality getQuality() { return quality; }
        public int getEvalBefore() { return evalBefore; }
        public int getEvalAfter() { return evalAfter; }
        public int getCentipawnLoss() { return centipawnLoss; }
        public ChessAI.AIMove getBestAlternative() { return bestAlternative; }
        public String getNotation() { return notation; }
        public String getExplanationThai() { return explanationThai; }
        public String getCharacterReaction() { return characterReaction; }
    }

    private final float whiteAccuracy;
    private final float blackAccuracy;
    private final Map<MoveQuality, Integer> whiteStats;
    private final Map<MoveQuality, Integer> blackStats;
    private final List<ReviewedMove> reviewedMoves;
    private final String whitePerformanceTitle;
    private final String blackPerformanceTitle;
    private final String matchSummaryThai;
    private final String engineName;

    public GameReviewReport(float whiteAccuracy, float blackAccuracy,
                            Map<MoveQuality, Integer> whiteStats,
                            Map<MoveQuality, Integer> blackStats,
                            List<ReviewedMove> reviewedMoves,
                            String whitePerformanceTitle,
                            String blackPerformanceTitle,
                            String matchSummaryThai,
                            String engineName) {
        this.whiteAccuracy = whiteAccuracy;
        this.blackAccuracy = blackAccuracy;
        this.whiteStats = (whiteStats != null) ? whiteStats : new EnumMap<>(MoveQuality.class);
        this.blackStats = (blackStats != null) ? blackStats : new EnumMap<>(MoveQuality.class);
        this.reviewedMoves = (reviewedMoves != null) ? reviewedMoves : new ArrayList<>();
        this.whitePerformanceTitle = whitePerformanceTitle;
        this.blackPerformanceTitle = blackPerformanceTitle;
        this.matchSummaryThai = matchSummaryThai;
        this.engineName = (engineName != null && !engineName.isEmpty()) ? engineName : "Master Engine (Built-in)";
    }

    public float getWhiteAccuracy() { return whiteAccuracy; }
    public float getBlackAccuracy() { return blackAccuracy; }
    public Map<MoveQuality, Integer> getWhiteStats() { return whiteStats; }
    public Map<MoveQuality, Integer> getBlackStats() { return blackStats; }
    public List<ReviewedMove> getReviewedMoves() { return reviewedMoves; }
    public String getWhitePerformanceTitle() { return whitePerformanceTitle; }
    public String getBlackPerformanceTitle() { return blackPerformanceTitle; }
    public String getMatchSummaryThai() { return matchSummaryThai; }
    public String getEngineName() { return engineName; }

    public int getWhiteQualityCount(MoveQuality q) {
        return whiteStats.getOrDefault(q, 0);
    }

    public int getBlackQualityCount(MoveQuality q) {
        return blackStats.getOrDefault(q, 0);
    }
}
