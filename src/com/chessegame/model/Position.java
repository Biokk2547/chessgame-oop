package com.chessegame.model;

public class Position {
    public final int row; // 0..7, 0 = rank 8
    public final int col; // 0..7, 0 = file a

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public static Position fromAlgebraic(String s) {
        if (s == null || s.length() != 2) throw new IllegalArgumentException("Invalid position: " + s);
        char file = s.charAt(0);
        char rank = s.charAt(1);
        int col = file - 'a';
        int r = rank - '1';
        if (col < 0 || col > 7 || r < 0 || r > 7) throw new IllegalArgumentException("Invalid position: " + s);
        int row = 7 - r; // rank 1 -> row 7, rank 8 -> row 0
        return new Position(row, col);
    }

    public String toAlgebraic() {
        char file = (char)('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    @Override
    public String toString() { return toAlgebraic(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position)o;
        return p.row == row && p.col == col;
    }
}
