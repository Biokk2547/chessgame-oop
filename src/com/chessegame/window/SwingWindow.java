package com.chessegame.window;

import com.chessegame.ui.swing.ChessSwingPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * Window manager implementation for Swing JFrame layout, properties, and window lifecycle.
 */
public class SwingWindow implements GameWindow {
    private String title = "Chess - Swing";
    private int width = 1000;
    private int height = 1000;
    private JFrame frame;
    private ChessSwingPanel mainPanel;

    @Override
    public void setTitle(String title) {
        this.title = title;
        if (frame != null) frame.setTitle(title);
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        if (frame != null) frame.setSize(width, height);
    }

    @Override
    public void launchWindow(String[] args) {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        mainPanel = new ChessSwingPanel(frame);
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new SwingWindow().launchWindow(args);
    }
}
