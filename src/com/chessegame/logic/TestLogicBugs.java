package com.chessegame.logic;

import com.chessegame.ai.ChessAI;
import com.chessegame.ai.Evaluation;
import com.chessegame.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive Logic & Rules Test Suite for detecting chess logic bugs.
 */
public class TestLogicBugs {

    public static class TestResult {
        public final String testName;
        public final boolean passed;
        public final String details;

        public TestResult(String testName, boolean passed, String details) {
            this.testName = testName;
            this.passed = passed;
            this.details = details;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Starting Comprehensive Chess Logic & Bug Audit ===");
        List<TestResult> results = new ArrayList<>();

        // Test 1: Castling out of Check (FIDE rule violation check)
        results.add(testCastlingOutOfCheck());

        // Test 2: Castling through an attacked square (FIDE rule violation check)
        results.add(testCastlingThroughCheck());

        // Test 3: Normal Valid Castling (Kingside & Queenside)
        results.add(testValidCastling());

        // Test 4: Absolute Pin (Pinned piece cannot move and expose king)
        results.add(testAbsolutePin());

        // Test 5: En Passant Legality & Pin Safety
        results.add(testEnPassantPinSafety());

        // Test 6: Checkmate vs Stalemate Differentiation
        results.add(testCheckmateVsStalemate());

        // Test 7: AI Minimax Checkmate Evaluation Direction (Black AI must avoid checkmate)
        results.add(testAiMinimaxCheckmateAvoidance());

        // Test 8: Undo / Redo State Parity & Piece Restoration
        results.add(testUndoRedoParity());

        // Test 9: Insufficient Material (K vs K, KB vs K, KN vs K, KB vs KB same/diff colors)
        results.add(testInsufficientMaterial());

        // Test 10: Threefold Repetition (Standard Knight repetition sequence)
        results.add(testThreefoldRepetition());

        // Test 11: Fifty-Move Rule (Clock increments and pawn/capture resets)
        results.add(testFiftyMoveRule());

        // Test 12: FEN Dynamic En-Passant & Halfmove/Fullmove Clock Output
        results.add(testFENEnPassantAndClock());

        // Test 13: Last Move Tracking (Directional Arrow 1 Turn Back)
        results.add(testLastMoveTracking());

        // Summary
        int passedCount = 0;
        System.out.println("\n==========================================");
        System.out.println("           TEST SUITE SUMMARY             ");
        System.out.println("==========================================");
        for (TestResult r : results) {
            if (r.passed) {
                passedCount++;
                System.out.println("[PASS] " + r.testName);
            } else {
                System.out.println("[FAIL] " + r.testName + " -> " + r.details);
            }
        }
        System.out.println("Total: " + passedCount + " / " + results.size() + " passed.");
    }

    private static TestResult testCastlingOutOfCheck() {
        // Setup: White King at e1 (row 7, col 4), White Rook at h1 (row 7, col 7)
        // Black Rook at e8 (row 0, col 4) putting White King in check on e-file!
        Board b = new Board();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) b.setPiece(new Position(r, c), null);
        b.setPiece(new Position(7, 4), new King(Piece.Color.WHITE));
        b.setPiece(new Position(7, 7), new Rook(Piece.Color.WHITE));
        b.setPiece(new Position(0, 4), new Rook(Piece.Color.BLACK));
        b.setPiece(new Position(0, 0), new King(Piece.Color.BLACK));

        boolean inCheck = ChessUtils.isInCheck(b, Piece.Color.WHITE);
        boolean canCastle = ChessUtils.isLegalMove(b, new Position(7, 4), new Position(7, 6), Piece.Color.WHITE);

        if (!inCheck) {
            return new TestResult("Castling Out of Check", false, "White King was supposed to be in check but wasn't detected.");
        }
        if (canCastle) {
            return new TestResult("Castling Out of Check", false, "BUG: King was allowed to castle out of check (Illegal by FIDE rules)!");
        }
        return new TestResult("Castling Out of Check", true, "Correctly prevented King from castling while in check.");
    }

    private static TestResult testCastlingThroughCheck() {
        // Setup: White King at e1 (row 7, col 4), White Rook at h1 (row 7, col 7)
        // Black Bishop at a6 (row 2, col 0) attacking f1 (row 7, col 5)!
        Board b = new Board();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) b.setPiece(new Position(r, c), null);
        b.setPiece(new Position(7, 4), new King(Piece.Color.WHITE));
        b.setPiece(new Position(7, 7), new Rook(Piece.Color.WHITE));
        b.setPiece(new Position(2, 0), new Bishop(Piece.Color.BLACK)); // Attacks (3,1), (4,2), (5,3), (6,4), (7,5 = f1)
        b.setPiece(new Position(0, 0), new King(Piece.Color.BLACK));

        boolean f1Attacked = ChessUtils.isSquareAttacked(b, new Position(7, 5), Piece.Color.BLACK);
        boolean canCastle = ChessUtils.isLegalMove(b, new Position(7, 4), new Position(7, 6), Piece.Color.WHITE);

        if (!f1Attacked) {
            return new TestResult("Castling Through Check", false, "Square f1 was supposed to be attacked by Bishop at a6.");
        }
        if (canCastle) {
            return new TestResult("Castling Through Check", false, "BUG: King was allowed to castle through attacked square f1!");
        }
        return new TestResult("Castling Through Check", true, "Correctly prevented King from castling through attacked square.");
    }

    private static TestResult testValidCastling() {
        Board b = new Board();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) b.setPiece(new Position(r, c), null);
        b.setPiece(new Position(7, 4), new King(Piece.Color.WHITE));
        b.setPiece(new Position(7, 7), new Rook(Piece.Color.WHITE));
        b.setPiece(new Position(7, 0), new Rook(Piece.Color.WHITE));
        b.setPiece(new Position(0, 4), new King(Piece.Color.BLACK));

        boolean kingside = ChessUtils.isLegalMove(b, new Position(7, 4), new Position(7, 6), Piece.Color.WHITE);
        boolean queenside = ChessUtils.isLegalMove(b, new Position(7, 4), new Position(7, 2), Piece.Color.WHITE);

        if (!kingside || !queenside) {
            return new TestResult("Valid Castling", false, "Valid castling was incorrectly rejected (Kingside=" + kingside + ", Queenside=" + queenside + ")");
        }
        return new TestResult("Valid Castling", true, "Both Kingside and Queenside castling allowed when legal.");
    }

    private static TestResult testAbsolutePin() {
        // Setup: White King at e1 (row 7, col 4), White Bishop at e4 (row 4, col 4), Black Rook at e8 (row 0, col 4)
        // White Bishop is pinned on the e-file and cannot move off the e-file!
        Board b = new Board();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) b.setPiece(new Position(r, c), null);
        b.setPiece(new Position(7, 4), new King(Piece.Color.WHITE));
        b.setPiece(new Position(4, 4), new Bishop(Piece.Color.WHITE));
        b.setPiece(new Position(0, 4), new Rook(Piece.Color.BLACK));
        b.setPiece(new Position(0, 0), new King(Piece.Color.BLACK));

        boolean moveOffPin = ChessUtils.isLegalMove(b, new Position(4, 4), new Position(3, 3), Piece.Color.WHITE);
        if (moveOffPin) {
            return new TestResult("Absolute Pin", false, "BUG: Pinned Bishop was allowed to move off the pin and expose king!");
        }
        return new TestResult("Absolute Pin", true, "Pinned piece correctly prevented from exposing king to check.");
    }

    private static TestResult testEnPassantPinSafety() {
        // White King at e5, White Pawn at f5, Black Pawn at g5 (just moved g7->g5), Black Rook at h5
        // If White plays fxg6 e.p., the f5 and g5 pawns disappear, exposing White King at e5 to Rook at h5 on the 5th rank!
        Board b = new Board();
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) b.setPiece(new Position(r, c), null);
        b.setPiece(new Position(3, 4), new King(Piece.Color.WHITE)); // e5
        b.setPiece(new Position(3, 5), new Pawn(Piece.Color.WHITE)); // f5
        Pawn blackPawn = new Pawn(Piece.Color.BLACK);
        b.setPiece(new Position(3, 6), blackPawn); // g5
        b.setPiece(new Position(3, 7), new Rook(Piece.Color.BLACK)); // h5
        b.setPiece(new Position(0, 0), new King(Piece.Color.BLACK));

        // Simulate Black just moved g7 -> g5 (row 1, col 6 -> row 3, col 6)
        try {
            java.lang.reflect.Field f1 = Board.class.getDeclaredField("lastMoveFrom");
            java.lang.reflect.Field f2 = Board.class.getDeclaredField("lastMoveTo");
            f1.setAccessible(true);
            f2.setAccessible(true);
            f1.set(b, new Position(1, 6));
            f2.set(b, new Position(3, 6));
        } catch (Exception ignored) {}

        boolean legalEP = ChessUtils.isLegalMove(b, new Position(3, 5), new Position(2, 6), Piece.Color.WHITE);
        if (legalEP) {
            return new TestResult("En Passant Pin Safety", false, "BUG: En-passant was allowed even though it exposed King to rank check!");
        }
        return new TestResult("En Passant Pin Safety", true, "Illegal En-passant correctly blocked when exposing King.");
    }

    private static TestResult testCheckmateVsStalemate() {
        // Scholar's Mate check
        Board b = new Board();
        b.move(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE); // 1. e4
        b.move(new Position(1, 4), new Position(3, 4), Piece.Color.BLACK); // 1... e5
        b.move(new Position(7, 5), new Position(4, 2), Piece.Color.WHITE); // 2. Bc4
        b.move(new Position(0, 1), new Position(2, 2), Piece.Color.BLACK); // 2... Nc6
        b.move(new Position(7, 3), new Position(3, 7), Piece.Color.WHITE); // 3. Qh5
        b.move(new Position(0, 6), new Position(2, 5), Piece.Color.BLACK); // 3... Nf6
        b.move(new Position(3, 7), new Position(1, 5), Piece.Color.WHITE); // 4. Qxf7#

        boolean isMate = ChessUtils.isCheckmate(b, Piece.Color.BLACK);
        boolean isStale = ChessUtils.isStalemate(b, Piece.Color.BLACK);

        if (!isMate) return new TestResult("Checkmate vs Stalemate", false, "Scholar's mate was not recognized as checkmate.");
        if (isStale) return new TestResult("Checkmate vs Stalemate", false, "Scholar's mate was incorrectly recognized as stalemate.");
        return new TestResult("Checkmate vs Stalemate", true, "Checkmate accurately recognized and distinct from stalemate.");
    }

    private static TestResult testAiMinimaxCheckmateAvoidance() {
        // Setup: Black is facing mate in 1 if Black doesn't defend!
        // White Queen at h5, Bishop at c4. If Black plays a random move, White mates with Qxf7#.
        // Black has a move that blocks (e.g. g6 or Qe7). Black AI must choose a move that prevents Qxf7#!
        Board b = new Board();
        b.move(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE); // e4
        b.move(new Position(1, 4), new Position(3, 4), Piece.Color.BLACK); // e5
        b.move(new Position(7, 5), new Position(4, 2), Piece.Color.WHITE); // Bc4
        b.move(new Position(0, 1), new Position(2, 2), Piece.Color.BLACK); // Nc6
        b.move(new Position(7, 3), new Position(3, 7), Piece.Color.WHITE); // Qh5

        // Black AI turn
        ChessAI.AIMove best = ChessAI.findBestMove(b, Piece.Color.BLACK, 3, Evaluation.AIStyle.MASTER);
        if (best == null) return new TestResult("AI Checkmate Avoidance", false, "AI returned null move.");

        // If Black plays Nf6 (which defends against Qxf7 and attacks Queen) or g6 or Qe7 -> Good!
        // If Black plays a suicidal move like a6 or h6, Qxf7# is immediate!
        Board testB = b.copy();
        testB.moveRecord(best.from, best.to, Piece.Color.BLACK, best.promoChoice);
        boolean whiteCanMateNext = ChessUtils.isLegalMove(testB, new Position(3, 7), new Position(1, 5), Piece.Color.WHITE) &&
                                   ChessUtils.isCheckmate(testB.copy(), Piece.Color.BLACK);

        if (whiteCanMateNext) {
            return new TestResult("AI Checkmate Avoidance", false, "BUG: Black AI failed to defend against mate in 1 threat!");
        }
        return new TestResult("AI Checkmate Avoidance", true, "AI accurately recognized and defended against checkmate threat.");
    }

    private static TestResult testUndoRedoParity() {
        Board b = new Board();
        GameHistoryManager mgr = new GameHistoryManager();

        MoveRecord m1 = b.moveRecord(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE, null);
        mgr.recordMove(m1);
        MoveRecord m2 = b.moveRecord(new Position(1, 4), new Position(3, 4), Piece.Color.BLACK, null);
        mgr.recordMove(m2);

        // Undo move 2
        mgr.undo(b);
        if (b.getPiece(new Position(3, 4)) != null || b.getPiece(new Position(1, 4)) == null) {
            return new TestResult("Undo/Redo Parity", false, "Undo failed to restore Black pawn to e7.");
        }

        // Redo move 2
        mgr.redo(b);
        if (b.getPiece(new Position(3, 4)) == null || b.getPiece(new Position(1, 4)) != null) {
            return new TestResult("Undo/Redo Parity", false, "Redo failed to move Black pawn to e5.");
        }

        return new TestResult("Undo/Redo Parity", true, "Undo/Redo fully preserves board state consistency.");
    }

    private static TestResult testInsufficientMaterial() {
        // 1. King vs King
        Board b = new Board();
        b.clear();
        b.setPiece(new Position(7, 4), new King(Piece.Color.WHITE));
        b.setPiece(new Position(0, 4), new King(Piece.Color.BLACK));
        if (!ChessUtils.isInsufficientMaterial(b)) {
            return new TestResult("Insufficient Material", false, "K vs K should be insufficient material!");
        }

        // 2. King + Knight vs King
        b.setPiece(new Position(7, 1), new Knight(Piece.Color.WHITE));
        if (!ChessUtils.isInsufficientMaterial(b)) {
            return new TestResult("Insufficient Material", false, "K+N vs K should be insufficient material!");
        }
        b.setPiece(new Position(7, 1), null);

        // 3. King + Bishop vs King
        b.setPiece(new Position(7, 2), new Bishop(Piece.Color.WHITE));
        if (!ChessUtils.isInsufficientMaterial(b)) {
            return new TestResult("Insufficient Material", false, "K+B vs K should be insufficient material!");
        }

        // 4. King + Bishop vs King + Bishop on same color square (e.g. c1=7,2 sum 9; f8=0,5 sum 5: both odd=light)
        b.setPiece(new Position(0, 5), new Bishop(Piece.Color.BLACK));
        if (!ChessUtils.isInsufficientMaterial(b)) {
            return new TestResult("Insufficient Material", false, "K+B vs K+B on same colored squares should be insufficient material!");
        }

        // 5. King + Bishop vs King + Bishop on different color square (c1=7,2 sum 9 odd; c8=0,2 sum 2 even)
        b.setPiece(new Position(0, 5), null);
        b.setPiece(new Position(0, 2), new Bishop(Piece.Color.BLACK));
        if (ChessUtils.isInsufficientMaterial(b)) {
            return new TestResult("Insufficient Material", false, "K+B vs K+B on different colored squares is NOT dead position by FIDE!");
        }

        // 6. Pawn exists -> never insufficient
        b.setPiece(new Position(0, 2), null);
        b.setPiece(new Position(6, 4), new Pawn(Piece.Color.WHITE));
        if (ChessUtils.isInsufficientMaterial(b)) {
            return new TestResult("Insufficient Material", false, "Position with pawn should NOT be insufficient material!");
        }

        return new TestResult("Insufficient Material", true, "FIDE Article 9.6 dead positions accurately recognized.");
    }

    private static TestResult testThreefoldRepetition() {
        Board b = new Board();
        GameHistoryManager mgr = new GameHistoryManager();
        mgr.setInitialPositionKey(com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.WHITE));

        // Sequence of repeated knight moves:
        // Position 1: Initial position (count = 1)
        // 1. Nf3 Nf6
        MoveRecord m1 = b.moveRecord(new Position(7, 6), new Position(5, 5), Piece.Color.WHITE, null);
        mgr.recordMove(m1, com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.BLACK));
        MoveRecord m2 = b.moveRecord(new Position(0, 6), new Position(2, 5), Piece.Color.BLACK, null);
        mgr.recordMove(m2, com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.WHITE));

        // 2. Ng1 Ng8 -> Returns to Initial position! (count = 2)
        MoveRecord m3 = b.moveRecord(new Position(5, 5), new Position(7, 6), Piece.Color.WHITE, null);
        mgr.recordMove(m3, com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.BLACK));
        MoveRecord m4 = b.moveRecord(new Position(2, 5), new Position(0, 6), Piece.Color.BLACK, null);
        String repKey2 = com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.WHITE);
        mgr.recordMove(m4, repKey2);

        if (mgr.isThreefoldRepetition(repKey2)) {
            return new TestResult("Threefold Repetition", false, "Position occurred only twice, should not be threefold yet!");
        }

        // 3. Nf3 Nf6
        MoveRecord m5 = b.moveRecord(new Position(7, 6), new Position(5, 5), Piece.Color.WHITE, null);
        mgr.recordMove(m5, com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.BLACK));
        MoveRecord m6 = b.moveRecord(new Position(0, 6), new Position(2, 5), Piece.Color.BLACK, null);
        mgr.recordMove(m6, com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.WHITE));

        // 4. Ng1 Ng8 -> Returns to Initial position 3rd time! (count = 3)
        MoveRecord m7 = b.moveRecord(new Position(5, 5), new Position(7, 6), Piece.Color.WHITE, null);
        mgr.recordMove(m7, com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.BLACK));
        MoveRecord m8 = b.moveRecord(new Position(2, 5), new Position(0, 6), Piece.Color.BLACK, null);
        String repKey3 = com.chessegame.ai.FENUtils.toPositionKey(b, Piece.Color.WHITE);
        mgr.recordMove(m8, repKey3);

        if (!mgr.isThreefoldRepetition(repKey3)) {
            return new TestResult("Threefold Repetition", false, "Position occurred 3 times but was not detected as Threefold Repetition!");
        }

        return new TestResult("Threefold Repetition", true, "Threefold position repetition accurately tracked and detected.");
    }

    private static TestResult testFiftyMoveRule() {
        Board b = new Board();
        if (b.getHalfMoveClock() != 0 || b.getFullMoveNumber() != 1) {
            return new TestResult("Fifty-Move Rule", false, "Initial board clock should be 0 and move number 1.");
        }

        // White knight moves (not pawn, no capture): clock becomes 1
        b.moveRecord(new Position(7, 6), new Position(5, 5), Piece.Color.WHITE, null);
        if (b.getHalfMoveClock() != 1) {
            return new TestResult("Fifty-Move Rule", false, "Clock should be 1 after knight move.");
        }

        // Black knight moves: clock becomes 2, fullMoveNumber becomes 2
        b.moveRecord(new Position(0, 6), new Position(2, 5), Piece.Color.BLACK, null);
        if (b.getHalfMoveClock() != 2 || b.getFullMoveNumber() != 2) {
            return new TestResult("Fifty-Move Rule", false, "Clock should be 2 and fullMoveNumber 2 after Black move.");
        }

        // White pawn move: clock must reset to 0!
        b.moveRecord(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE, null);
        if (b.getHalfMoveClock() != 0) {
            return new TestResult("Fifty-Move Rule", false, "Clock must reset to 0 on pawn move!");
        }

        // Test Fifty-Move Rule trigger at 100 plies
        b.setHalfMoveClock(99);
        if (ChessUtils.isFiftyMoveRule(b)) {
            return new TestResult("Fifty-Move Rule", false, "99 plies should not trigger Fifty-Move Rule yet.");
        }
        b.setHalfMoveClock(100);
        if (!ChessUtils.isFiftyMoveRule(b)) {
            return new TestResult("Fifty-Move Rule", false, "100 plies (50 full moves) must trigger Fifty-Move Rule!");
        }

        return new TestResult("Fifty-Move Rule", true, "Fifty-move halfmove clock and rule triggers accurately verified.");
    }

    private static TestResult testFENEnPassantAndClock() {
        Board b = new Board();
        // White plays e4 (row 6 col 4 to row 4 col 4)
        b.moveRecord(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE, null);
        String fen1 = com.chessegame.ai.FENUtils.toFEN(b, Piece.Color.BLACK);
        // Expect en passant square 'e3' and clock '0 1'
        if (!fen1.contains(" e3 0 1")) {
            return new TestResult("FEN En-Passant & Clock", false, "FEN after e4 should end with 'e3 0 1', got: " + fen1);
        }

        // Black plays Nf6 (row 0 col 6 to row 2 col 5)
        b.moveRecord(new Position(0, 6), new Position(2, 5), Piece.Color.BLACK, null);
        String fen2 = com.chessegame.ai.FENUtils.toFEN(b, Piece.Color.WHITE);
        // Expect en passant square '-' and clock '1 2'
        if (!fen2.contains(" - 1 2")) {
            return new TestResult("FEN En-Passant & Clock", false, "FEN after Nf6 should end with '- 1 2', got: " + fen2);
        }

        return new TestResult("FEN En-Passant & Clock", true, "Dynamic En-Passant square and halfmove/fullmove clocks verified.");
    }

    private static TestResult testLastMoveTracking() {
        Board b = new Board();
        GameHistoryManager history = new GameHistoryManager();

        // 1. Initial State: last move must be null
        if (history.getLastMove() != null) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Initial state last move should be null!");
        }

        // 2. White plays e4 (6,4 -> 4,4)
        Position from1 = new Position(6, 4);
        Position to1 = new Position(4, 4);
        MoveRecord m1 = b.moveRecord(from1, to1, Piece.Color.WHITE, null);
        history.recordMove(m1);

        MoveRecord last1 = history.getLastMove();
        if (last1 == null || !last1.getFrom().equals(from1) || !last1.getTo().equals(to1)) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Last move after e4 mismatch!");
        }

        // 3. Black plays e5 (1,4 -> 3,4)
        Position from2 = new Position(1, 4);
        Position to2 = new Position(3, 4);
        MoveRecord m2 = b.moveRecord(from2, to2, Piece.Color.BLACK, null);
        history.recordMove(m2);

        MoveRecord last2 = history.getLastMove();
        if (last2 == null || !last2.getFrom().equals(from2) || !last2.getTo().equals(to2)) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Last move after e5 mismatch!");
        }

        // 4. Undo: steps back 1 turn -> last move should now be e4 (1 move back)
        history.undo(b);
        MoveRecord lastAfterUndo = history.getLastMove();
        if (lastAfterUndo == null || !lastAfterUndo.getFrom().equals(from1) || !lastAfterUndo.getTo().equals(to1)) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Last move after undo should point to e4!");
        }

        // 5. Undo again: steps back to initial board -> last move should be null
        history.undo(b);
        if (history.getLastMove() != null) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Last move after 2 undos should be null!");
        }

        // 6. Redo: should point to e4 again
        history.redo(b);
        MoveRecord lastAfterRedo = history.getLastMove();
        if (lastAfterRedo == null || !lastAfterRedo.getFrom().equals(from1) || !lastAfterRedo.getTo().equals(to1)) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Last move after redo should point to e4!");
        }

        // 7. Clear: should be null
        history.clear();
        if (history.getLastMove() != null) {
            return new TestResult("Last Move Tracking (1 Turn Back)", false, "Last move after clear should be null!");
        }

        return new TestResult("Last Move Tracking (1 Turn Back)", true, "Last move tracked accurately across moves, undos, and redos.");
    }
}
