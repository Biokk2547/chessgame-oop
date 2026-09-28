package com.chessegame.logic;

import com.chessegame.model.Piece;

public class SimClock {
    public static void main(String[] args) throws Exception {
        Clock c = new Clock(2000L, 500L); // 2s + 0.5s increment
        c.startTurn(Clock.Side.WHITE);
        Thread.sleep(300);
        // White moves
        Piece.Color moved = Piece.Color.WHITE;
        Piece.Color opponent = Piece.Color.BLACK;
        c.moveCompleted();
        System.out.println("After White move: White=" + c.getRemaining(Clock.Side.WHITE) + " Black=" + c.getRemaining(Clock.Side.BLACK));
        // Black moves after 400ms
        Thread.sleep(400);
        c.moveCompleted();
        System.out.println("After Black move: White=" + c.getRemaining(Clock.Side.WHITE) + " Black=" + c.getRemaining(Clock.Side.BLACK));
    }
}
