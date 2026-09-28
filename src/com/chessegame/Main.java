package com.chessegame;

import com.chessegame.window.WindowManager;

/**
 * Main application entry point supporting window selection (Swing, JavaFX, LibGDX, Console).
 */
public class Main {
    public static void main(String[] args) {
        try {
            java.io.File jarFile = new java.io.File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            java.io.File jarDir = jarFile.isDirectory() ? jarFile : jarFile.getParentFile();
            if (jarDir != null && jarDir.exists()) {
                java.io.File assetsInJarDir = new java.io.File(jarDir, "assets");
                if (assetsInJarDir.exists()) {
                    System.setProperty("user.dir", jarDir.getAbsolutePath());
                }
            }
        } catch (Throwable ignored) {}

        if (args.length > 0) {
            String modeStr = args[0].toUpperCase();
            try {
                WindowManager.Mode mode = WindowManager.Mode.valueOf(modeStr);
                WindowManager.launchWindow(mode, args);
                return;
            } catch (IllegalArgumentException ignored) {}
        }

        // Default launch LibGDX window if no args specified
        WindowManager.launchWindow(WindowManager.Mode.LIBGDX, args);
    }
}
