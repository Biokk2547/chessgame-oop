package com.chessegame.logic;

import com.chessegame.model.*;
import java.util.Scanner;

public class Game {
    private Scanner scanner = new Scanner(System.in);
    private Board board;
    private Piece.Color currentTurn = Piece.Color.WHITE;

    // Clock integration: 5 minutes per side, 2s increment
    private Clock clock;
    private Thread timeoutMonitor;
    private final long INITIAL_MS = 5 * 60 * 1000L;
    private final long INCREMENT_MS = 2000L;

    public void start() {
        System.out.println("Welcome to Console Chess");
        board = new Board();

        // initialize clock and start White's turn timer
        clock = new Clock(INITIAL_MS, INCREMENT_MS);
        clock.startTurn(Clock.Side.WHITE);

        // background monitor to detect timeouts while waiting for input
        timeoutMonitor = new Thread(() -> {
            while (true) {
                if (clock.isFlagged(Clock.Side.WHITE)) {
                    System.out.println("White ran out of time. Black wins!");
                    System.exit(0);
                }
                if (clock.isFlagged(Clock.Side.BLACK)) {
                    System.out.println("Black ran out of time. White wins!");
                    System.exit(0);
                }
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
            }
        });
        timeoutMonitor.setDaemon(true);
        timeoutMonitor.start();

        loop();
    }

    private void loop() {
        while (true) {
            board.print();
            // show remaining time for both sides
            System.out.println("Time - White: " + Clock.formatMs(clock.getRemaining(Clock.Side.WHITE))
                    + "  Black: " + Clock.formatMs(clock.getRemaining(Clock.Side.BLACK)));
            System.out.println((currentTurn == Piece.Color.WHITE ? "White" : "Black") + " to move. Enter move (e2 e4), 'resign', 'draw', or 'exit':");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye");
                timeoutMonitor.interrupt();
                break;
            }
            if (line.equalsIgnoreCase("resign")) {
                Piece.Color winner = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                System.out.println((currentTurn == Piece.Color.WHITE ? "White" : "Black") + " resigned. " +
                        (winner == Piece.Color.WHITE ? "White" : "Black") + " wins!");
                timeoutMonitor.interrupt();
                break;
            }
            if (line.equalsIgnoreCase("draw")) {
                System.out.println("Game drawn by mutual agreement.");
                timeoutMonitor.interrupt();
                break;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Invalid input. Use format: e2 e4, 'resign', 'draw', or 'exit'");
                continue;
            }
            try {
                Position from = Position.fromAlgebraic(parts[0]);
                Position to = Position.fromAlgebraic(parts[1]);
                
                // Validate move is legal (doesn't leave king in check)
                if (!ChessUtils.isLegalMove(board, from, to, currentTurn)) {
                    System.out.println("Illegal move: move leaves king in check");
                    continue;
                }
                
                board.move(from, to, currentTurn);
                
                // Determine which side just moved and switch currentTurn immediately so UI/state is consistent
                Piece.Color moved = currentTurn;
                Piece.Color opponent = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                currentTurn = opponent;
                
                // apply clock move completion (adds increment and starts opponent's timer)
                if (clock != null) clock.moveCompleted();
                
                // Check game end conditions (use opponent as the side that will move next)
                if (!board.hasKing(opponent)) {
                    board.print();
                    System.out.println((moved == Piece.Color.WHITE ? "White" : "Black") + " wins! King captured.");
                    timeoutMonitor.interrupt();
                    break;
                }
                
                if (ChessUtils.isCheckmate(board, opponent)) {
                    board.print();
                    System.out.println((moved == Piece.Color.WHITE ? "White" : "Black") + " wins! Checkmate!");
                    timeoutMonitor.interrupt();
                    break;
                }
                
                if (ChessUtils.isStalemate(board, opponent)) {
                    board.print();
                    System.out.println("Stalemate! Game is a draw.");
                    timeoutMonitor.interrupt();
                    break;
                }

                if (ChessUtils.isInsufficientMaterial(board)) {
                    board.print();
                    System.out.println("Draw! Insufficient material to checkmate.");
                    timeoutMonitor.interrupt();
                    break;
                }

                if (ChessUtils.isFiftyMoveRule(board)) {
                    board.print();
                    System.out.println("Draw! Fifty-move rule reached (50 moves without pawn move or capture).");
                    timeoutMonitor.interrupt();
                    break;
                }
                
                if (ChessUtils.isInCheck(board, opponent)) {
                    System.out.println((opponent == Piece.Color.WHITE ? "White" : "Black") + " is in check!");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Move failed: " + ex.getMessage());
            }
        }
    }
}
