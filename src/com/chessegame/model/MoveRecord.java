package com.chessegame.model;

public class MoveRecord {
    public enum MoveType {
        NORMAL,
        CASTLING_KINGSIDE,
        CASTLING_QUEENSIDE,
        EN_PASSANT,
        PROMOTION
    }

    private final Position from;
    private final Position to;
    private final Piece movedPiece;
    private final Piece capturedPiece;
    private final MoveType moveType;
    private final Piece promotedPiece;
    private final Position enPassantCapturedPos;
    private final boolean movedPiecePreviousHasMoved;
    private final boolean capturedPiecePreviousHasMoved;
    private final Position previousLastMoveFrom;
    private final Position previousLastMoveTo;
    private final String notation;
    private final int previousHalfMoveClock;
    private final int previousFullMoveNumber;

    public MoveRecord(Position from, Position to, Piece movedPiece, Piece capturedPiece,
                      MoveType moveType, Piece promotedPiece, Position enPassantCapturedPos,
                      boolean movedPiecePreviousHasMoved, boolean capturedPiecePreviousHasMoved,
                      Position previousLastMoveFrom, Position previousLastMoveTo, String notation) {
        this(from, to, movedPiece, capturedPiece, moveType, promotedPiece, enPassantCapturedPos,
             movedPiecePreviousHasMoved, capturedPiecePreviousHasMoved, previousLastMoveFrom, previousLastMoveTo,
             notation, 0, 1);
    }

    public MoveRecord(Position from, Position to, Piece movedPiece, Piece capturedPiece,
                      MoveType moveType, Piece promotedPiece, Position enPassantCapturedPos,
                      boolean movedPiecePreviousHasMoved, boolean capturedPiecePreviousHasMoved,
                      Position previousLastMoveFrom, Position previousLastMoveTo, String notation,
                      int previousHalfMoveClock, int previousFullMoveNumber) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.moveType = moveType;
        this.promotedPiece = promotedPiece;
        this.enPassantCapturedPos = enPassantCapturedPos;
        this.movedPiecePreviousHasMoved = movedPiecePreviousHasMoved;
        this.capturedPiecePreviousHasMoved = capturedPiecePreviousHasMoved;
        this.previousLastMoveFrom = previousLastMoveFrom;
        this.previousLastMoveTo = previousLastMoveTo;
        this.notation = notation;
        this.previousHalfMoveClock = previousHalfMoveClock;
        this.previousFullMoveNumber = previousFullMoveNumber;
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Piece getMovedPiece() { return movedPiece; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public MoveType getMoveType() { return moveType; }
    public Piece getPromotedPiece() { return promotedPiece; }
    public Position getEnPassantCapturedPos() { return enPassantCapturedPos; }
    public boolean isMovedPiecePreviousHasMoved() { return movedPiecePreviousHasMoved; }
    public boolean isCapturedPiecePreviousHasMoved() { return capturedPiecePreviousHasMoved; }
    public Position getPreviousEnPassantTarget() { return previousLastMoveFrom; }
    public Position getPreviousLastMoveFrom() { return previousLastMoveFrom; }
    public Position getPreviousLastMoveTo() { return previousLastMoveTo; }
    public String getNotation() { return notation; }
    public int getPreviousHalfMoveClock() { return previousHalfMoveClock; }
    public int getPreviousFullMoveNumber() { return previousFullMoveNumber; }
}
