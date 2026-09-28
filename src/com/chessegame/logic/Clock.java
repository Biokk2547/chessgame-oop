package com.chessegame.logic;

/**
 * Simple move timer for two sides (WHITE and BLACK).
 * - Construct with initial milliseconds per side and optional increment per move (ms).
 * - Call startTurn(side) to start timing a side's move.
 * - Call moveCompleted() when the current side finishes a move (applies increment and switches side).
 * - Call stop() to pause timers.
 * - getRemaining(side) returns remaining milliseconds.
 *
 * This implementation is lock-safe and does not use a background thread; it computes elapsed time on demand.
 */
public class Clock {
    
    public enum Side { WHITE, BLACK }

    private final Object lock = new Object();
    private long whiteRemainingMs;
    private long blackRemainingMs;
    private final long incrementMs;

    // which side is currently being timed (null = stopped)
    private Side runningSide = null;
    // last start time in nanoseconds (only valid when runningSide != null)
    private long lastStartNano = 0L;

    // Optional listener for timeout events
    public interface TimeoutListener { void onTimeout(Side side); }
    private TimeoutListener timeoutListener = null;
    
    public Clock(long initialMillisPerSide) {
        this(initialMillisPerSide, 0L);
    }

    public Clock(long initialMillisPerSide, long incrementPerMoveMillis) {
        this.whiteRemainingMs = initialMillisPerSide;
        this.blackRemainingMs = initialMillisPerSide;
        this.incrementMs = Math.max(0L, incrementPerMoveMillis);
    }

    public void setTimeoutListener(TimeoutListener l) {
        synchronized (lock) { this.timeoutListener = l; }
    }

    // Start timing for a side. If another side was running, its elapsed time is recorded.
    public void startTurn(Side side) {
        synchronized (lock) {
            if (side == null) return;
            if (runningSide != null && runningSide != side) {
                recordElapsedLocked();
            }
            runningSide = side;
            lastStartNano = System.nanoTime();
            checkTimeoutLocked();
        }
    }

    // Stop timing (pause). Records elapsed time for the running side.
    public void stop() {
        synchronized (lock) {
            recordElapsedLocked();
            runningSide = null;
            lastStartNano = 0L;
        }
    }

    // Call when the current side completes its move: apply increment, then switch to the opponent and start timing them.
    public void moveCompleted() {
        synchronized (lock) {
            if (runningSide == null) return; // nothing to complete
            // record elapsed for the side that just moved
            recordElapsedLocked();
            // apply increment to the side that just moved
            if (runningSide == Side.WHITE) {
                whiteRemainingMs += incrementMs;
            } else {
                blackRemainingMs += incrementMs;
            }
            // switch side and start timing the opponent
            runningSide = (runningSide == Side.WHITE) ? Side.BLACK : Side.WHITE;
            lastStartNano = System.nanoTime();
            checkTimeoutLocked();
        }
    }

    // Reset both clocks to the given initial milliseconds and stop.
    public void reset(long initialMillisPerSide) {
        synchronized (lock) {
            this.whiteRemainingMs = initialMillisPerSide;
            this.blackRemainingMs = initialMillisPerSide;
            this.runningSide = null;
            this.lastStartNano = 0L;
        }
    }

    // Add or deduct milliseconds from a side's clock
    public void addTime(Side side, long millis) {
        synchronized (lock) {
            recordElapsedLocked();
            if (side == Side.WHITE) {
                whiteRemainingMs += millis;
                if (whiteRemainingMs < 0) whiteRemainingMs = 0;
            } else {
                blackRemainingMs += millis;
                if (blackRemainingMs < 0) blackRemainingMs = 0;
            }
        }
    }

    // Get remaining milliseconds for a side (accounts for currently running side).
    public long getRemaining(Side side) {
        synchronized (lock) {
            long base = (side == Side.WHITE) ? whiteRemainingMs : blackRemainingMs;
            if (runningSide == side && lastStartNano != 0L) {
                long elapsed = (System.nanoTime() - lastStartNano) / 1_000_000L;
                long rem = base - elapsed;
                return Math.max(0L, rem);
            }
            return Math.max(0L, base);
        }
    }

    // Returns true if the side's time has reached zero (flagged).
    public boolean isFlagged(Side side) {
        return getRemaining(side) <= 0L;
    }

    // Human-friendly mm:ss or ss.ms format
    public static String formatMs(long ms) {
        if (ms <= 0) return "0:00";
        long totalSec = ms / 1000L;
        long minutes = totalSec / 60L;
        long seconds = totalSec % 60L;
        long remMs = ms % 1000L;
        if (minutes > 0) return String.format("%d:%02d", minutes, seconds);
        return String.format("%d.%03d", seconds, remMs);
    }

    // Internal: record elapsed time since lastStartNano into the running side's remaining time.
    private void recordElapsedLocked() {
        if (runningSide == null || lastStartNano == 0L) return;
        long elapsed = (System.nanoTime() - lastStartNano) / 1_000_000L;
        if (runningSide == Side.WHITE) {
            whiteRemainingMs -= elapsed;
            if (whiteRemainingMs < 0) whiteRemainingMs = 0;
        } else {
            blackRemainingMs -= elapsed;
            if (blackRemainingMs < 0) blackRemainingMs = 0;
        }
        lastStartNano = System.nanoTime();
    }

    // Internal: if running side has timed out, notify listener if present.
    private void checkTimeoutLocked() {
        if (runningSide == null) return;
        if (isFlagged(runningSide) && timeoutListener != null) {
            // call listener outside lock to avoid reentrancy concerns
            TimeoutListener l = timeoutListener;
            Side s = runningSide;
            // invoke listener in a new thread to avoid blocking caller.
            new Thread(() -> l.onTimeout(s)).start();
        }
    }
}

