package com.chessegame.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * LevelProgressManager: Handles local persistence for unlocked boss levels and earned stars (1-3 stars).
 */
public class LevelProgressManager {

    private static final String PREF_NAME = "chessgame_boss_rush_progress";
    private static final String KEY_UNLOCKED = "max_unlocked_level";
    private static final String KEY_STARS_PREFIX = "level_stars_";

    private static Preferences getPrefs() {
        try {
            return Gdx.app.getPreferences(PREF_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getMaxUnlockedLevel() {
        Preferences p = getPrefs();
        if (p == null) return 1;
        return Math.max(1, Math.min(5, p.getInteger(KEY_UNLOCKED, 1)));
    }

    public static boolean isLevelUnlocked(int levelId) {
        if (levelId <= 1) return true;
        return levelId <= getMaxUnlockedLevel();
    }

    public static int getStarsForLevel(int levelId) {
        Preferences p = getPrefs();
        if (p == null) return 0;
        return p.getInteger(KEY_STARS_PREFIX + levelId, 0);
    }

    /**
     * Records completion of a level, updating max stars and unlocking the next level.
     */
    public static void recordLevelCompletion(int levelId, int starsEarned) {
        Preferences p = getPrefs();
        if (p == null) return;

        int currentBest = p.getInteger(KEY_STARS_PREFIX + levelId, 0);
        if (starsEarned > currentBest) {
            p.putInteger(KEY_STARS_PREFIX + levelId, Math.min(3, starsEarned));
        }

        int currentMax = p.getInteger(KEY_UNLOCKED, 1);
        if (levelId >= currentMax && levelId < 5) {
            p.putInteger(KEY_UNLOCKED, levelId + 1);
        }

        p.flush();
    }

    public static int getTotalStars() {
        int total = 0;
        for (int i = 1; i <= 5; i++) {
            total += getStarsForLevel(i);
        }
        return total;
    }

    public static void resetProgress() {
        Preferences p = getPrefs();
        if (p != null) {
            p.clear();
            p.putInteger(KEY_UNLOCKED, 1);
            p.flush();
        }
    }
}
