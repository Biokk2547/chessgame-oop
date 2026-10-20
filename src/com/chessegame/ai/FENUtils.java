package com.chessegame.ai;

import com.chessegame.model.Board;
import com.chessegame.model.King;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;
import com.chessegame.model.Rook;

/**
 * Utility for converting Board states into standard FEN (Forsyth–Edwards Notation) strings.
 */
public class FENUtils {

    public static String toFEN(Board board, Piece.Color activeColor) {
        StringBuilder sb = new StringBuilder();

        // 1. Piece placement (ranks 8 down to 1 -> row 0 to 7)
        for (int r = 0; r < 8; r++) {
            int emptyCount = 0;
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(new Position(r, c));
                if (p == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    char symbol = p.getSymbol();
                    if (p.getColor() == Piece.Color.WHITE) {
                        sb.append(Character.toUpperCase(symbol));
                    } else {
                        sb.append(Character.toLowerCase(symbol));
                    }
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            if (r < 7) {
                sb.append('/');
            }
        }

        // 2. Active color
        sb.append(' ').append(activeColor == Piece.Color.WHITE ? 'w' : 'b');

        // 3. Castling rights based on actual King/Rook positions
        StringBuilder castling = new StringBuilder();
        Piece wKing = board.getPiece(new Position(7, 4));
        if (wKing instanceof King && wKing.getColor() == Piece.Color.WHITE && !wKing.hasMoved()) {
            Piece wRookK = board.getPiece(new Position(7, 7));
            if (wRookK instanceof Rook && wRookK.getColor() == Piece.Color.WHITE && !wRookK.hasMoved()) castling.append('K');
            Piece wRookQ = board.getPiece(new Position(7, 0));
            if (wRookQ instanceof Rook && wRookQ.getColor() == Piece.Color.WHITE && !wRookQ.hasMoved()) castling.append('Q');
        }
        Piece bKing = board.getPiece(new Position(0, 4));
        if (bKing instanceof King && bKing.getColor() == Piece.Color.BLACK && !bKing.hasMoved()) {
            Piece bRookK = board.getPiece(new Position(0, 7));
            if (bRookK instanceof Rook && bRookK.getColor() == Piece.Color.BLACK && !bRookK.hasMoved()) castling.append('k');
            Piece bRookQ = board.getPiece(new Position(0, 0));
            if (bRookQ instanceof Rook && bRookQ.getColor() == Piece.Color.BLACK && !bRookQ.hasMoved()) castling.append('q');
        }
        if (castling.length() == 0) {
            sb.append(" -");
        } else {
            sb.append(' ').append(castling.toString());
        }

        // 4. En passant target square
        Position lastFrom = board.getLastMoveFrom();
        Position lastTo = board.getLastMoveTo();
        String epSquare = "-";
        if (lastFrom != null && lastTo != null) {
            Piece moved = board.getPiece(lastTo);
            if (moved instanceof com.chessegame.model.Pawn && Math.abs(lastTo.row - lastFrom.row) == 2) {
                int epRow = (lastFrom.row + lastTo.row) / 2;
                char epCol = (char) ('a' + lastTo.col);
                int epRank = 8 - epRow;
                epSquare = "" + epCol + epRank;
            }
        }
        sb.append(' ').append(epSquare);

        // 5. Halfmove clock and fullmove number
        sb.append(' ').append(board.getHalfMoveClock()).append(' ').append(board.getFullMoveNumber());

        return sb.toString();
    }

    /**
     * Converts the current board state and turn into a canonical position key
     * (piece placement + active turn + castling rights + en passant square)
     * used for detecting Threefold Repetition.
     */
    public static String toPositionKey(Board board, Piece.Color activeColor) {
        String fen = toFEN(board, activeColor);
        String[] parts = fen.split("\\s+");
        if (parts.length >= 4) {
            // An unavailable en-passant capture must not distinguish repetitions.
            if (!parts[3].equals("-")) {
                Position ep = fromUCISquare(parts[3]);
                int pawnRow = ep.row + (activeColor == Piece.Color.WHITE ? 1 : -1);
                boolean canCapture = false;
                for (int dc : new int[] {-1, 1}) {
                    int col = ep.col + dc;
                    if (col < 0 || col > 7) continue;
                    Position from = new Position(pawnRow, col);
                    Piece pawn = board.getPiece(from);
                    if (pawn instanceof com.chessegame.model.Pawn && pawn.getColor() == activeColor &&
                            com.chessegame.logic.ChessUtils.isLegalMove(board, from, ep, activeColor)) {
                        canCapture = true;
                        break;
                    }
                }
                if (!canCapture) parts[3] = "-";
            }
            return parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3];
        }
        return fen;
    }

    public static String toUCIMove(Position from, Position to, Piece promo) {
        if (from == null || to == null) return "";
        char fromCol = (char) ('a' + from.col);
        int fromRow = 8 - from.row;
        char toCol = (char) ('a' + to.col);
        int toRow = 8 - to.row;

        StringBuilder sb = new StringBuilder();
        sb.append(fromCol).append(fromRow).append(toCol).append(toRow);
        if (promo != null) {
            sb.append(Character.toLowerCase(promo.getSymbol()));
        }
        return sb.toString();
    }

    public static Position fromUCISquare(String sq) {
        if (sq == null || sq.length() < 2) return null;
        int col = sq.charAt(0) - 'a';
        int row = 8 - Character.getNumericValue(sq.charAt(1));
        if (col >= 0 && col < 8 && row >= 0 && row < 8) {
            return new Position(row, col);
        }
        return null;
    }

    /**
     * Loads a chess board position from a FEN string.
     * Returns the active player's turn (WHITE or BLACK).
     */
    public static Piece.Color loadFromFEN(Board board, String fen) {
        if (board == null || fen == null || fen.trim().isEmpty()) return Piece.Color.WHITE;
        board.clear();
        String[] parts = fen.trim().split("\\s+");
        String placement = parts[0];

        int row = 0;
        int col = 0;
        for (int i = 0; i < placement.length(); i++) {
            char ch = placement.charAt(i);
            if (ch == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(ch)) {
                col += Character.getNumericValue(ch);
            } else {
                Piece.Color color = Character.isUpperCase(ch) ? Piece.Color.WHITE : Piece.Color.BLACK;
                char lower = Character.toLowerCase(ch);
                Piece piece = null;
                switch (lower) {
                    case 'p': piece = new com.chessegame.model.Pawn(color); break;
                    case 'n': piece = new com.chessegame.model.Knight(color); break;
                    case 'b': piece = new com.chessegame.model.Bishop(color); break;
                    case 'r': piece = new com.chessegame.model.Rook(color); break;
                    case 'q': piece = new com.chessegame.model.Queen(color); break;
                    case 'k': piece = new com.chessegame.model.King(color); break;
                }
                if (piece != null && row < 8 && col < 8) {
                    piece.setMoved(true);
                    board.setPiece(new Position(row, col), piece);
                }
                col++;
            }
        }

        Piece.Color activeColor = Piece.Color.WHITE;
        if (parts.length > 1 && parts[1].equalsIgnoreCase("b")) {
            activeColor = Piece.Color.BLACK;
        }
        String rights = parts.length > 2 ? parts[2] : "-";
        restoreCastlingRight(board, rights, 'K', Piece.Color.WHITE, 7, 7);
        restoreCastlingRight(board, rights, 'Q', Piece.Color.WHITE, 7, 0);
        restoreCastlingRight(board, rights, 'k', Piece.Color.BLACK, 0, 7);
        restoreCastlingRight(board, rights, 'q', Piece.Color.BLACK, 0, 0);

        if (parts.length > 3 && !parts[3].equals("-")) {
            Position ep = fromUCISquare(parts[3]);
            int expectedRow = activeColor == Piece.Color.WHITE ? 2 : 5;
            if (ep != null && ep.row == expectedRow) {
                int direction = activeColor == Piece.Color.WHITE ? 1 : -1;
                Position lastTo = new Position(ep.row + direction, ep.col);
                Piece pawn = board.getPiece(lastTo);
                if (pawn instanceof com.chessegame.model.Pawn && pawn.getColor() != activeColor) {
                    board.setLastMove(new Position(ep.row - direction, ep.col), lastTo);
                }
            }
        }
        if (parts.length > 4) board.setHalfMoveClock(Integer.parseInt(parts[4]));
        if (parts.length > 5) board.setFullMoveNumber(Integer.parseInt(parts[5]));
        return activeColor;
    }

    private static void restoreCastlingRight(Board board, String rights, char right,
                                             Piece.Color color, int row, int rookCol) {
        if (rights.indexOf(right) < 0) return;
        Piece king = board.getPiece(new Position(row, 4));
        Piece rook = board.getPiece(new Position(row, rookCol));
        if (king instanceof King && king.getColor() == color &&
                rook instanceof Rook && rook.getColor() == color) {
            king.setMoved(false);
            rook.setMoved(false);
        }
    }
}
