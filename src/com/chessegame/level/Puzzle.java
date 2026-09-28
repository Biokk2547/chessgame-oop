package com.chessegame.level;

import com.chessegame.model.Piece;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Puzzle: Representation of a chess tactic/puzzle.
 * Supports single-move (Mate in 1) and multi-move tactical combinations (Mate in 2, Fork, Pin).
 */
public class Puzzle {
    private final int id;
    private final String title;
    private final String category;
    private final String description;
    private final String fen;
    private final Piece.Color playerColor;
    private final List<String> solutionMoves; // Alternating: [PlayerMove, OpponentResponse, PlayerMove, ...]
    private final String hint;
    private final String explanation;

    public Puzzle(int id, String title, String category, String description, String fen,
                  Piece.Color playerColor, List<String> solutionMoves, String hint, String explanation) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.fen = fen;
        this.playerColor = playerColor;
        this.solutionMoves = new ArrayList<>(solutionMoves);
        this.hint = hint;
        this.explanation = explanation;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getFen() { return fen; }
    public Piece.Color getPlayerColor() { return playerColor; }
    public List<String> getSolutionMoves() { return Collections.unmodifiableList(solutionMoves); }
    public String getHint() { return hint; }
    public String getExplanation() { return explanation; }

    /**
     * Total number of player steps required to solve this puzzle.
     */
    public int getTotalPlayerSteps() {
        return (solutionMoves.size() + 1) / 2;
    }
}
