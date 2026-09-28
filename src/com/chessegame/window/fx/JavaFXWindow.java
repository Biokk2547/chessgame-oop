package com.chessegame.window.fx;

import com.chessegame.window.GameWindow;
import com.chessegame.ui.fx.ChessPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Window manager implementation for JavaFX Stage lifecycle and properties.
 */
public class JavaFXWindow extends Application implements GameWindow {
    private static String windowTitle = "Chess - JavaFX";
    private static int windowWidth = 1100;
    private static int windowHeight = 1200;

    public static void main(String[] args) {
        Application.launch(JavaFXWindow.class, args);
    }

    @Override
    public void setTitle(String title) {
        windowTitle = title;
    }

    @Override
    public void setSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    @Override
    public void launchWindow(String[] args) {
        Application.launch(JavaFXWindow.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        ChessPage page = new ChessPage();
        primaryStage.setTitle(windowTitle);
        primaryStage.setScene(new Scene(page, windowWidth, windowHeight));
        primaryStage.show();
    }
}
