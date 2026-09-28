package com.chessegame.character;

import java.util.List;

/**
 * Handles frame timing and frame indexing for animated character sprites.
 */
public class CharacterAnimator {
    private final String characterName;
    private final List<String> framePaths;
    private final float frameDuration; // seconds per frame
    private float stateTime;
    private int currentFrameIndex;

    public CharacterAnimator(int frameCount, float frameDuration) {
        this.characterName = "character";
        java.util.List<String> dummy = new java.util.ArrayList<>();
        for (int i = 0; i < frameCount; i++) dummy.add("" + i);
        this.framePaths = dummy;
        this.frameDuration = Math.max(0.01f, frameDuration);
        this.stateTime = 0f;
        this.currentFrameIndex = 0;
    }

    public CharacterAnimator(String characterName, List<String> framePaths, float frameDuration) {
        this.characterName = characterName;
        this.framePaths = framePaths;
        this.frameDuration = Math.max(0.01f, frameDuration);
        this.stateTime = 0f;
        this.currentFrameIndex = 0;
    }

    public void update(float delta) {
        if (framePaths == null || framePaths.isEmpty()) return;
        stateTime += delta;
        currentFrameIndex = (int) (stateTime / frameDuration) % framePaths.size();
    }

    public int getCurrentFrameIndex() {
        if (framePaths == null || framePaths.isEmpty()) return 0;
        return currentFrameIndex % framePaths.size();
    }

    public String getCurrentFramePath() {
        if (framePaths == null || framePaths.isEmpty()) return "";
        return framePaths.get(getCurrentFrameIndex());
    }

    public String getCharacterName() {
        return characterName;
    }

    public List<String> getFramePaths() {
        return framePaths;
    }

    public void reset() {
        stateTime = 0f;
        currentFrameIndex = 0;
    }
}
