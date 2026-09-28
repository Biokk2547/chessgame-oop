package com.chessegame.opening;

import com.chessegame.model.Piece;

import java.util.Collections;
import java.util.List;

/**
 * Opening: Data model representing a curated chess opening repertoire line.
 */
public class Opening {
    private final int id;
    private final String nameEn;
    private final String nameTh;
    private final String category; // Open Game, Semi-Open, Closed, Gambit, System, Flank
    private final Piece.Color playerColor; // The color player practices (WHITE or BLACK)
    private final List<String> movesUci; // List of UCI moves e.g. "e2e4", "e7e5", "g1f3"
    private final List<String> moveNotations; // SAN notations e.g. "1. e4", "1... e5"
    private final List<String> moveExplanations; // Explanations for each ply in Thai
    private final String tacticalGoal; // Strategic concept & objective in Thai
    private final String difficulty; // "ง่าย", "ปานกลาง", "เชี่ยวชาญ"

    public Opening(int id, String nameEn, String nameTh, String category,
                   Piece.Color playerColor, List<String> movesUci,
                   List<String> moveNotations, List<String> moveExplanations,
                   String tacticalGoal, String difficulty) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameTh = nameTh;
        this.category = category;
        this.playerColor = playerColor;
        this.movesUci = Collections.unmodifiableList(movesUci);
        this.moveNotations = Collections.unmodifiableList(moveNotations);
        this.moveExplanations = Collections.unmodifiableList(moveExplanations);
        this.tacticalGoal = tacticalGoal;
        this.difficulty = difficulty;
    }

    public int getId() { return id; }
    public String getNameEn() { return nameEn; }
    public String getNameTh() { return nameTh; }
    public String getCategory() { return category; }
    public Piece.Color getPlayerColor() { return playerColor; }
    public List<String> getMovesUci() { return movesUci; }
    public List<String> getMoveNotations() { return moveNotations; }
    public List<String> getMoveExplanations() { return moveExplanations; }
    public String getTacticalGoal() { return tacticalGoal; }
    public String getDifficulty() { return difficulty; }

    public int getTotalSteps() {
        return movesUci.size();
    }
}
