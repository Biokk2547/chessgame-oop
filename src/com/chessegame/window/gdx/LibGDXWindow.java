package com.chessegame.window.gdx;

import com.chessegame.window.GameWindow;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.chessegame.ui.gdx.LibGdxChessApp;

/**
 * Window manager implementation for LibGDX LWJGL3 Application Configuration and desktop launch.
 */
public class LibGDXWindow implements GameWindow {
    private String title = "Chess - libGDX";
    private int width = 960;
    private int height = 900;

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void launchWindow(String[] args) {
        System.out.println("user.dir=" + System.getProperty("user.dir"));
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle(title);
        cfg.setWindowedMode(width, height);
        cfg.setWindowPosition(-1, -1);
        cfg.useVsync(true);
        cfg.setResizable(true);
        cfg.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void created(Lwjgl3Window window) {
                try {
                    window.focusWindow();
                } catch (Throwable ignored) {}
            }
        });
        new Lwjgl3Application(new LibGdxChessApp(), cfg);
    }

    public static void main(String[] args) {
        new LibGDXWindow().launchWindow(args);
    }
}
