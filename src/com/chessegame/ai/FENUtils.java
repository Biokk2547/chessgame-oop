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
        if (wKing instanceof King && !wKing.hasMoved()) {
            Piece wRookK = board.getPiece(new Position(7, 7));
            if (wRookK instanceof Rook && !wRookK.hasMoved()) castling.append('K');
            Piece wRookQ = board.getPiece(new Position(7, 0));
            if (wRookQ instanceof Rook && !wRookQ.hasMoved()) castling.append('Q');
        }
        Piece bKing = board.getPiece(new Position(0, 4));
        if (bKing instanceof King && !bKing.hasMoved()) {
            Piece bRookK = board.getPiece(new Position(0, 7));
            if (bRookK instanceof Rook && !bRookK.hasMoved()) castling.append('k');
            Piece bRookQ = board.getPiece(new Position(0, 0));
            if (bRookQ instanceof Rook && !bRookQ.hasMoved()) castling.append('q');
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
                    board.setPiece(new Position(row, col), piece);
                }
                col++;
            }
        }

        Piece.Color activeColor = Piece.Color.WHITE;
        if (parts.length > 1 && parts[1].equalsIgnoreCase("b")) {
            activeColor = Piece.Color.BLACK;
        }
        return activeColor;
    }
}
