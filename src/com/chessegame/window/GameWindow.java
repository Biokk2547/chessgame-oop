package com.chessegame.window;

/**
 * Common contract for managing game window properties and lifecycle.
 */
public interface GameWindow {
    void setTitle(String title);
    void setSize(int width, int height);
    void launchWindow(String[] args);
}
