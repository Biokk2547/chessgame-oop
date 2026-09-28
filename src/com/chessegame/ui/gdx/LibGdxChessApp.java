package com.chessegame.ui.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.chessegame.audio.MusicManager;

/**
 * LibGDX Game entry delegate. Initializes the MainMenuScreen and Background Music.
 */
public class LibGdxChessApp extends Game {
    @Override
    public void create() {
        System.out.println("[DEBUG] LibGdxChessApp.create() STARTED");
        try {
            System.out.println("Gdx.files.internal('W_King.png').exists(): " + Gdx.files.internal("W_King.png").exists());
        } catch (Throwable t) {
            System.out.println("Gdx.files check failed: " + t.getMessage());
        }

        // Start relaxing ambient background music
        try {
            MusicManager.getInstance().playBGM();
        } catch (Throwable t) {
            System.out.println("BGM start failed: " + t.getMessage());
        }

        System.out.println("[DEBUG] Calling setScreen(new MainMenuScreen)");
        setScreen(new MainMenuScreen(this));
        System.out.println("[DEBUG] LibGdxChessApp.create() FINISHED");
    }

    @Override
    public void dispose() {
        try {
            MusicManager.getInstance().dispose();
        } catch (Throwable ignored) {}
        super.dispose();
    }
}
