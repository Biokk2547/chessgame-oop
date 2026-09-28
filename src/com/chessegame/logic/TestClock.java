package com.chessegame.logic;

public class TestClock {
    public static void main(String[] args) throws Exception {
        Clock c = new Clock(1000L, 200L); // 1s + 200ms increment
        System.out.println("Start WHITE");
        c.startTurn(Clock.Side.WHITE);
        Thread.sleep(300);
        System.out.println("WHITE remaining (≈700): " + c.getRemaining(Clock.Side.WHITE));
        c.moveCompleted();
        System.out.println("After moveCompleted, WHITE remaining (≈900): " + c.getRemaining(Clock.Side.WHITE));
        System.out.println("BLACK remaining (≈1000): " + c.getRemaining(Clock.Side.BLACK));
        System.out.println("Sleeping 1200ms to timeout BLACK...");
        Thread.sleep(1200);
        System.out.println("BLACK remaining (<=0): " + c.getRemaining(Clock.Side.BLACK));
        System.out.println("BLACK flagged: " + c.isFlagged(Clock.Side.BLACK));
    }
}

