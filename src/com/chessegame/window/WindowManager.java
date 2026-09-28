package com.chessegame.window;

/**
 * Central Window Manager for launching and managing different game window UI implementations.
 */
public class WindowManager {
    public enum Mode {
        SWING,
        JAVAFX,
        LIBGDX,
        CONSOLE
    }

    public static void launchWindow(Mode mode, String[] args) {
        switch (mode) {
            case SWING:
                new SwingWindow().launchWindow(args);
                break;
            case JAVAFX:
                try {
                    Class<?> clazz = Class.forName("com.chessegame.window.fx.JavaFXWindow");
                    GameWindow window = (GameWindow) clazz.getDeclaredConstructor().newInstance();
                    window.launchWindow(args);
                } catch (Exception e) {
                    System.err.println("Failed to launch JavaFX Window: " + e.getMessage());
                }
                break;
            case LIBGDX:
                try {
                    Class<?> clazz = Class.forName("com.chessegame.window.gdx.LibGDXWindow");
                    GameWindow window = (GameWindow) clazz.getDeclaredConstructor().newInstance();
                    window.launchWindow(args);
                } catch (Exception e) {
                    System.err.println("Failed to launch LibGDX Window: " + e.getMessage());
                }
                break;
            case CONSOLE:
            default:
                new com.chessegame.logic.Game().start();
                break;
        }
    }
}
