package com.chessegame.ai;

import com.chessegame.logic.GameHistoryManager;
import com.chessegame.model.Board;
import com.chessegame.model.MoveRecord;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;

import java.util.List;

/**
 * Verification test for GameReviewer & GameReviewReport.
 */
public class TestGameReview {

    public static void main(String[] args) {
        System.out.println("=== Starting Game Review Engine Verification Test ===");

        Board board = new Board();
        GameHistoryManager history = new GameHistoryManager();

        // 1. e4 (White: 6,4 to 4,4)
        MoveRecord m1 = board.moveRecord(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE, null);
        history.recordMove(m1);

        // 1... e5 (Black: 1,4 to 3,4)
        MoveRecord m2 = board.moveRecord(new Position(1, 4), new Position(3, 4), Piece.Color.BLACK, null);
        history.recordMove(m2);

        // 2. Nf3 (White: 7,6 to 5,5)
        MoveRecord m3 = board.moveRecord(new Position(7, 6), new Position(5, 5), Piece.Color.WHITE, null);
        history.recordMove(m3);

        // 2... Nc6 (Black: 0,1 to 2,2)
        MoveRecord m4 = board.moveRecord(new Position(0, 1), new Position(2, 2), Piece.Color.BLACK, null);
        history.recordMove(m4);

        // 3. Bc4 (White: 7,5 to 4,2 - Italian opening)
        MoveRecord m5 = board.moveRecord(new Position(7, 5), new Position(4, 2), Piece.Color.WHITE, null);
        history.recordMove(m5);

        // 3... h6 (Black: 1,7 to 2,7)
        MoveRecord m6 = board.moveRecord(new Position(1, 7), new Position(2, 7), Piece.Color.BLACK, null);
        history.recordMove(m6);

        // 4. d4 (White: 6,3 to 4,3)
        MoveRecord m7 = board.moveRecord(new Position(6, 3), new Position(4, 3), Piece.Color.WHITE, null);
        history.recordMove(m7);

        // 4... exd4 (Black: 3,4 to 4,3)
        MoveRecord m8 = board.moveRecord(new Position(3, 4), new Position(4, 3), Piece.Color.BLACK, null);
        history.recordMove(m8);

        // 5. Nxd4 (White: 5,5 to 4,3)
        MoveRecord m9 = board.moveRecord(new Position(5, 5), new Position(4, 3), Piece.Color.WHITE, null);
        history.recordMove(m9);

        List<MoveRecord> trajectory = history.getFullTrajectory();
        System.out.println("Total moves in match: " + trajectory.size());

        GameReviewReport report = GameReviewer.analyzeGame(trajectory, (cur, tot) -> {
            System.out.println("Analyzing move " + cur + "/" + tot + "...");
        });

        System.out.println("\n--- 📊 GAME REVIEW REPORT SUMMARY ---");
        System.out.printf("White Accuracy: %.1f%% (%s)\n", report.getWhiteAccuracy(), report.getWhitePerformanceTitle());
        System.out.printf("Black Accuracy: %.1f%% (%s)\n", report.getBlackAccuracy(), report.getBlackPerformanceTitle());
        System.out.println("Match Summary: " + report.getMatchSummaryThai());

        System.out.println("\n--- 📝 MOVE-BY-MOVE DETAILED BREAKDOWN ---");
        for (GameReviewReport.ReviewedMove rm : report.getReviewedMoves()) {
            System.out.printf("Move %d (%s) - %s | Quality: %s [%s] | Loss: %d cp | Eval: %+d\n",
                    rm.getMoveNumber(),
                    (rm.getPlayerColor() == Piece.Color.WHITE ? "White" : "Black"),
                    rm.getNotation(),
                    rm.getQuality().getSymbol(),
                    rm.getQuality().getLabelThai(),
                    rm.getCentipawnLoss(),
                    rm.getEvalAfter()
            );
            System.out.println("   Explanation: " + rm.getExplanationThai());
            System.out.println("   Reaction: " + rm.getCharacterReaction());
        }

        System.out.println("\n=== Verification Test Completed Successfully! ===");
    }
}
