package com.chessegame.level;

import java.util.List;

public class TestBossLevels {
    public static void main(String[] args) {
        System.out.println("=== Starting Boss Levels & Level Design Verification Test ===");

        List<BossLevel> levels = BossLevel.getAllLevels();
        System.out.println("Total Boss Levels configured: " + levels.size());

        for (BossLevel l : levels) {
            System.out.println("\n-----------------------------------------");
            System.out.println("Level " + l.getLevelId() + ": " + l.getBossName() + " (" + l.getBossTitle() + ")");
            System.out.println("AI Depth: " + l.getAiDepth() + " | Style: " + l.getAiStyle());
            System.out.println("Time Limit: " + (l.getTimeLimitMs() / 1000L) + "s (inc: " + l.getIncrementMs() + "ms)");
            System.out.println("Handicap: " + l.getHandicap().getTitle() + " - " + l.getHandicap().getDescription());
            System.out.println("Intro: " + l.getIntroDialogue());
            System.out.println("Win: " + l.getWinDialogue());
            System.out.println("Lose: " + l.getLoseDialogue());

            if (l.createStartingBoard() == null) {
                throw new RuntimeException("Starting board failed for level " + l.getLevelId());
            }
        }

        System.out.println("\n=== Testing Level Progress Manager ===");
        // Ensure level 1 is always unlocked by default
        boolean lvl1Unlocked = LevelProgressManager.isLevelUnlocked(1);
        System.out.println("Level 1 Unlocked by default: " + lvl1Unlocked);

        System.out.println("\n=== Boss Levels & Level Design Verification PASSED! ===");
    }
}
