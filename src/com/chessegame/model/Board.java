package com.chessegame.model;

public class Board {
    private final Piece[][] board = new Piece[8][8];
    // Track last move (for en-passant)
    private Position lastMoveFrom = null;
    private Position lastMoveTo = null;
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;

    public Board() { init(); }

    public void reset() { init(); }

    public void clear() {
        lastMoveFrom = null;
        lastMoveTo = null;
        halfMoveClock = 0;
        fullMoveNumber = 1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = null;
            }
        }
    }

    public void init() {
        lastMoveFrom = null;
        lastMoveTo = null;
        halfMoveClock = 0;
        fullMoveNumber = 1;
        // Clear
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) board[r][c] = null;
        // Black major pieces (row 0)
        board[0][0] = new Rook(Piece.Color.BLACK);
        board[0][1] = new Knight(Piece.Color.BLACK);
        board[0][2] = new Bishop(Piece.Color.BLACK);
        board[0][3] = new Queen(Piece.Color.BLACK);
        board[0][4] = new King(Piece.Color.BLACK);
        board[0][5] = new Bishop(Piece.Color.BLACK);
        board[0][6] = new Knight(Piece.Color.BLACK);
        board[0][7] = new Rook(Piece.Color.BLACK);
        // Black pawns (row1)
        for (int c = 0; c < 8; c++) board[1][c] = new Pawn(Piece.Color.BLACK);
        // White pawns (row6)
        for (int c = 0; c < 8; c++) board[6][c] = new Pawn(Piece.Color.WHITE);
        // White major pieces (row7)
        board[7][0] = new Rook(Piece.Color.WHITE);
        board[7][1] = new Knight(Piece.Color.WHITE);
        board[7][2] = new Bishop(Piece.Color.WHITE);
        board[7][3] = new Queen(Piece.Color.WHITE);
        board[7][4] = new King(Piece.Color.WHITE);
        board[7][5] = new Bishop(Piece.Color.WHITE);
        board[7][6] = new Knight(Piece.Color.WHITE);
        board[7][7] = new Rook(Piece.Color.WHITE);
    }

    public Piece getPiece(Position p) { return board[p.row][p.col]; }
    public void setPiece(Position p, Piece piece) { board[p.row][p.col] = piece; }

    public Position getLastMoveFrom() { return lastMoveFrom; }
    public Position getLastMoveTo() { return lastMoveTo; }
    public int getHalfMoveClock() { return halfMoveClock; }
    public void setHalfMoveClock(int halfMoveClock) { this.halfMoveClock = halfMoveClock; }
    public int getFullMoveNumber() { return fullMoveNumber; }
    public void setFullMoveNumber(int fullMoveNumber) { this.fullMoveNumber = fullMoveNumber; }

    private boolean isInside(Position p) { return p.row >= 0 && p.row < 8 && p.col >= 0 && p.col < 8; }

    // Attempts to move piece; throws IllegalArgumentException on invalid move
    public void move(Position from, Position to, Piece.Color turn) {
        moveRecord(from, to, turn, null);
    }

    public MoveRecord moveRecord(Position from, Position to, Piece.Color turn, Piece promotionChoice) {
        if (!isInside(from) || !isInside(to)) throw new IllegalArgumentException("Position out of bounds");
        Piece p = getPiece(from);
        if (p == null) throw new IllegalArgumentException("No piece at " + from);
        if (p.getColor() != turn) throw new IllegalArgumentException("Not " + turn + "'s piece at " + from);
        Piece target = getPiece(to);
        if (target != null && target.getColor() == p.getColor()) throw new IllegalArgumentException("Cannot capture your own piece at " + to);
        if (!p.isValidMove(from, to, this)) throw new IllegalArgumentException("Illegal move for " + p.getClass().getSimpleName());

        MoveRecord.MoveType type = MoveRecord.MoveType.NORMAL;
        Piece captured = target;
        Position enPassantCapPos = null;
        Piece promotedPiece = null;
        boolean movedPrevHasMoved = p.hasMoved();
        boolean capPrevHasMoved = target != null ? target.hasMoved() : false;
        Position prevLastFrom = lastMoveFrom;
        Position prevLastTo = lastMoveTo;

        int prevHalfMoveClock = halfMoveClock;
        int prevFullMoveNumber = fullMoveNumber;

        // Handle special moves: en-passant and castling
        if (p instanceof Pawn && from.col != to.col && target == null) {
            enPassantCapPos = new Position(from.row, to.col);
            captured = getPiece(enPassantCapPos);
            if (captured != null) {
                capPrevHasMoved = captured.hasMoved();
                setPiece(enPassantCapPos, null);
                type = MoveRecord.MoveType.EN_PASSANT;
            }
        }

        // Castling: king moves two squares horizontally; move the rook accordingly
        if (p instanceof King && Math.abs(to.col - from.col) == 2) {
            int row = from.row;
            if (to.col > from.col) {
                type = MoveRecord.MoveType.CASTLING_KINGSIDE;
                Position rookFrom = new Position(row, 7);
                Position rookTo = new Position(row, from.col + 1);
                Piece rook = getPiece(rookFrom);
                if (rook instanceof Rook) {
                    setPiece(rookTo, rook);
                    setPiece(rookFrom, null);
                    rook.setMoved(true);
                }
            } else {
                type = MoveRecord.MoveType.CASTLING_QUEENSIDE;
                Position rookFrom = new Position(row, 0);
                Position rookTo = new Position(row, from.col - 1);
                Piece rook = getPiece(rookFrom);
                if (rook instanceof Rook) {
                    setPiece(rookTo, rook);
                    setPiece(rookFrom, null);
                    rook.setMoved(true);
                }
            }
        }

        // Execute move
        setPiece(to, p);
        setPiece(from, null);
        p.setMoved(true);

        // Fifty-move rule tracking: resets on pawn moves or captures, increments otherwise
        if (p instanceof Pawn || captured != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        if (turn == Piece.Color.BLACK) {
            fullMoveNumber++;
        }

        // Pawn promotion
        if (p instanceof Pawn && (to.row == 0 || to.row == 7)) {
            type = MoveRecord.MoveType.PROMOTION;
            promotedPiece = (promotionChoice != null) ? promotionChoice : new Queen(p.getColor());
            setPiece(to, promotedPiece);
        }

        // Build Algebraic Notation
        String notation = buildNotation(from, to, p, captured, type, promotedPiece);

        // Record last move
        lastMoveFrom = from;
        lastMoveTo = to;

        return new MoveRecord(from, to, p, captured, type, promotedPiece, enPassantCapPos,
                movedPrevHasMoved, capPrevHasMoved, prevLastFrom, prevLastTo, notation,
                prevHalfMoveClock, prevFullMoveNumber);
    }

    public void undoMoveRecord(MoveRecord move) {
        if (move == null) return;
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece moved = move.getMovedPiece();

        // Restore moved piece to original position & previous moved state
        setPiece(from, moved);
        moved.setMoved(move.isMovedPiecePreviousHasMoved());

        // Clear 'to' square or restore normal captured piece
        if (move.getMoveType() == MoveRecord.MoveType.EN_PASSANT) {
            setPiece(to, null);
            Position capPos = move.getEnPassantCapturedPos();
            if (capPos != null) {
                Piece cap = move.getCapturedPiece();
                setPiece(capPos, cap);
                if (cap != null) cap.setMoved(move.isCapturedPiecePreviousHasMoved());
            }
        } else {
            Piece cap = move.getCapturedPiece();
            setPiece(to, cap);
            if (cap != null) cap.setMoved(move.isCapturedPiecePreviousHasMoved());
        }

        // Undo Castling rook move
        if (move.getMoveType() == MoveRecord.MoveType.CASTLING_KINGSIDE) {
            int row = from.row;
            Position rookFrom = new Position(row, 7);
            Position rookTo = new Position(row, from.col + 1);
            Piece rook = getPiece(rookTo);
            setPiece(rookFrom, rook);
            setPiece(rookTo, null);
            if (rook != null) rook.setMoved(false);
        } else if (move.getMoveType() == MoveRecord.MoveType.CASTLING_QUEENSIDE) {
            int row = from.row;
            Position rookFrom = new Position(row, 0);
            Position rookTo = new Position(row, from.col - 1);
            Piece rook = getPiece(rookTo);
            setPiece(rookFrom, rook);
            setPiece(rookTo, null);
            if (rook != null) rook.setMoved(false);
        }

        // Restore previous last move positions and clocks
        lastMoveFrom = move.getPreviousLastMoveFrom();
        lastMoveTo = move.getPreviousLastMoveTo();
        halfMoveClock = move.getPreviousHalfMoveClock();
        fullMoveNumber = move.getPreviousFullMoveNumber();
    }

    private String buildNotation(Position from, Position to, Piece p, Piece cap, MoveRecord.MoveType type, Piece promoted) {
        if (type == MoveRecord.MoveType.CASTLING_KINGSIDE) return "O-O";
        if (type == MoveRecord.MoveType.CASTLING_QUEENSIDE) return "O-O-O";

        StringBuilder sb = new StringBuilder();
        if (p instanceof Pawn) {
            if (cap != null) {
                sb.append((char) ('a' + from.col)).append("x");
            }
            sb.append((char) ('a' + to.col)).append(8 - to.row);
            if (promoted != null) {
                sb.append("=").append(Character.toUpperCase(promoted.getSymbol()));
            }
        } else {
            sb.append(Character.toUpperCase(p.getSymbol()));
            if (cap != null) sb.append("x");
            sb.append((char) ('a' + to.col)).append(8 - to.row);
        }
        return sb.toString();
    }

    public boolean hasKing(Piece.Color color) {
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
            Piece p = board[r][c];
            if (p instanceof King && p.getColor() == color) return true;
        }
        return false;
    }

    public void print() {
        System.out.println("  a b c d e f g h");
        for (int r = 0; r < 8; r++) {
            System.out.print((8 - r) + " ");
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                System.out.print((p == null ? '.' : p.getSymbol()) + " ");
            }
            System.out.println((8 - r));
        }
        System.out.println("  a b c d e f g h");
    }

    // Deep Copy Board for thread-safe AI simulation without UI flickering
    public Board copy() {
        Board clone = new Board();
        clone.lastMoveFrom = (this.lastMoveFrom != null) ? new Position(this.lastMoveFrom.row, this.lastMoveFrom.col) : null;
        clone.lastMoveTo = (this.lastMoveTo != null) ? new Position(this.lastMoveTo.row, this.lastMoveTo.col) : null;
        clone.halfMoveClock = this.halfMoveClock;
        clone.fullMoveNumber = this.fullMoveNumber;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece orig = this.board[r][c];
                if (orig != null) {
                    Piece copyP = copyPiece(orig);
                    clone.board[r][c] = copyP;
                } else {
                    clone.board[r][c] = null;
                }
            }
        }
        return clone;
    }

    private Piece copyPiece(Piece p) {
        Piece copy;
        if (p instanceof Pawn) copy = new Pawn(p.getColor());
        else if (p instanceof Knight) copy = new Knight(p.getColor());
        else if (p instanceof Bishop) copy = new Bishop(p.getColor());
        else if (p instanceof Rook) copy = new Rook(p.getColor());
        else if (p instanceof Queen) copy = new Queen(p.getColor());
        else if (p instanceof King) copy = new King(p.getColor());
        else return null;

        copy.setMoved(p.hasMoved());
        return copy;
    }

    // Utility: check clear path for straight or diagonal moves
    public boolean isPathClear(Position from, Position to) {
        int dr = Integer.signum(to.row - from.row);
        int dc = Integer.signum(to.col - from.col);
        int r = from.row + dr;
        int c = from.col + dc;
        while (r != to.row || c != to.col) {
            if (board[r][c] != null) return false;
            r += dr; c += dc;
        }
        return true;
    }
}
