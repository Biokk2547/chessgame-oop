package com.chessegame.logic;

import com.chessegame.ai.FENUtils;
import com.chessegame.model.*;

public class RegressionTests {
    private static int passed;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        passed++;
        System.out.println("[PASS] " + message);
    }

    public static void main(String[] args) {
        Board board = new Board();
        GameHistoryManager history = new GameHistoryManager();
        String initial = FENUtils.toPositionKey(board, Piece.Color.WHITE);
        history.setInitialPositionKey(initial);
        history.recordMove(board.moveRecord(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE, null), "future");
        history.undo(board);
        check(history.getHistory().isEmpty(), "Undo to start empties active history");
        check(history.getFormattedMoveHistory().isEmpty(), "Undo to start empties displayed history");
        check(history.getRepetitionCount("future") == 0, "Repetition ignores undone future moves");
        check(history.getRepetitionCount(initial) == 1, "Initial position counted once");
        check(history.getFullTrajectory().size() == 1 && history.canRedo(), "Undo preserves redo trajectory");
        history.redo(board);
        check(history.getHistory().size() == 1, "Redo restores active history");

        String[] positions = {
            "r3k2r/8/8/8/8/8/8/R3K2R w - - 17 42",
            "r3k2r/8/8/8/8/8/8/R3K2R b Kq - 3 9",
            "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 12",
            "4k3/8/8/8/3Pp3/8/8/4K3 b - d3 0 12"
        };
        for (String fen : positions) {
            Piece.Color turn = FENUtils.loadFromFEN(board, fen);
            check(fen.equals(FENUtils.toFEN(board, turn)), "FEN round trip: " + fen);
        }
        FENUtils.loadFromFEN(board, positions[0]);
        check(!ChessUtils.isLegalMove(board, new Position(7, 4), new Position(7, 6), Piece.Color.WHITE),
                "Imported position without castling rights cannot castle");
        FENUtils.loadFromFEN(board, positions[1]);
        check(ChessUtils.isLegalMove(board, new Position(7, 4), new Position(7, 6), Piece.Color.WHITE),
                "Imported kingside right permits castling");
        check(!ChessUtils.isLegalMove(board, new Position(7, 4), new Position(7, 2), Piece.Color.WHITE),
                "Imported missing queenside right prevents castling");
        for (int i = 2; i < positions.length; i++) {
            Piece.Color turn = FENUtils.loadFromFEN(board, positions[i]);
            Position from = new Position(i == 2 ? 3 : 4, 4);
            Position to = new Position(i == 2 ? 2 : 5, 3);
            check(ChessUtils.isLegalMove(board, from, to, turn), "Imported en-passant is legal for " + turn);
            MoveRecord move = board.moveRecord(from, to, turn, null);
            check(board.getPiece(new Position(from.row, to.col)) == null, "En-passant removes captured pawn");
            board.undoMoveRecord(move);
            check(positions[i].equals(FENUtils.toFEN(board, turn)), "En-passant undo restores imported position");
        }

        board = new Board();
        board.move(new Position(6, 4), new Position(4, 4), Piece.Color.WHITE);
        String fen = FENUtils.toFEN(board, Piece.Color.BLACK);
        String key = FENUtils.toPositionKey(board, Piece.Color.BLACK);
        FENUtils.loadFromFEN(board, fen.replace(" e3 ", " - "));
        check(key.equals(FENUtils.toPositionKey(board, Piece.Color.BLACK)), "Unavailable en-passant does not change repetition key");
        FENUtils.loadFromFEN(board, positions[2]);
        key = FENUtils.toPositionKey(board, Piece.Color.WHITE);
        FENUtils.loadFromFEN(board, positions[2].replace(" d6 ", " - "));
        check(!key.equals(FENUtils.toPositionKey(board, Piece.Color.WHITE)), "Legal en-passant changes repetition key");
        String pinned = "k3r3/8/8/3pP3/8/8/8/4K3 w - d6 0 1";
        FENUtils.loadFromFEN(board, pinned);
        key = FENUtils.toPositionKey(board, Piece.Color.WHITE);
        FENUtils.loadFromFEN(board, pinned.replace(" d6 ", " - "));
        check(key.equals(FENUtils.toPositionKey(board, Piece.Color.WHITE)), "Pinned en-passant does not change repetition key");

        board.clear();
        King king = new King(Piece.Color.WHITE);
        board.setPiece(new Position(7, 4), king);
        board.setPiece(new Position(7, 7), new Rook(Piece.Color.BLACK));
        check(!king.isValidMove(new Position(7, 4), new Position(7, 6), board), "Cannot castle with opponent rook");
        check(!FENUtils.toFEN(board, Piece.Color.WHITE).contains(" K"), "Opponent rook cannot grant FEN castling rights");
        board.clear();
        board.setPiece(new Position(4, 4), king);
        board.setPiece(new Position(4, 7), new Rook(Piece.Color.WHITE));
        check(!king.isValidMove(new Position(4, 4), new Position(4, 6), board), "Cannot castle outside home rank");

        board.clear();
        board.setPiece(new Position(6, 7), new Pawn(Piece.Color.BLACK));
        board.setPiece(new Position(7, 4), new King(Piece.Color.WHITE));
        board.moveRecord(new Position(6, 7), new Position(7, 7), Piece.Color.BLACK, new Rook(Piece.Color.BLACK));
        check(board.getPiece(new Position(7, 7)).hasMoved(), "Promoted rook cannot acquire castling rights");
        System.out.println("Passed " + passed + " regression checks.");
    }
}
