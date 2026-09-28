package com.chessegame.ui.swing;

import com.chessegame.character.CharacterAnimator;
import com.chessegame.character.DialogManager;
import com.chessegame.model.*;
import com.chessegame.logic.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing View component representing the Chess board UI, animated character avatars,
 * real-time dialogue speech bubbles, panel layout, and interaction handlers.
 */
public class ChessSwingPanel extends JPanel {
    private JFrame parentFrame;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JLabel whiteTimeLabel;
    private JLabel blackTimeLabel;
    private JLabel vampireAvatarLabel;
    private JLabel evilBoxAvatarLabel;
    private JLabel dialogBannerLabel;
    private JButton[][] squares = new JButton[8][8];

    private Board board;
    private Piece.Color currentTurn = Piece.Color.WHITE;
    private Position selected = null;
    private final Border highlightBorder = BorderFactory.createLineBorder(Color.YELLOW, 4);
    private final Border emptyBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
    private final Border captureBorder = BorderFactory.createLineBorder(Color.RED, 3);
    private boolean[][] legalMoves = new boolean[8][8];

    // Clock settings
    private Clock clock;
    private Timer uiTimer;
    private final long INITIAL_MS = 5 * 60 * 1000L;
    private final long INCREMENT_MS = 2000L;

    // Character Animation & Dialogue System
    private ImageIcon[] vampireIcons = new ImageIcon[4];
    private ImageIcon[] evilBoxIcons = new ImageIcon[12];
    private CharacterAnimator vampireAnimator;
    private CharacterAnimator evilBoxAnimator;
    private DialogManager dialogManager;

    public ChessSwingPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        loadCharacterIcons();
        initComponent();
    }

    private ImageIcon loadIconFromFile(String path, int width, int height) {
        String[] candidates = new String[] {
            path,
            "assets/" + path,
            "desktop/assets/" + path,
            path.replace("charactor/", "characters/"),
            "assets/" + path.replace("charactor/", "characters/"),
            "desktop/assets/" + path.replace("charactor/", "characters/")
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        }
        return null;
    }

    private void loadCharacterIcons() {
        List<String> vampirePaths = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String relPath = "charactor/Vampire/VampireAngryframe" + i + ".png";
            vampirePaths.add(relPath);
            vampireIcons[i - 1] = loadIconFromFile(relPath, 56, 56);
        }
        vampireAnimator = new CharacterAnimator("Vampire", vampirePaths, 0.20f);

        List<String> evilBoxPaths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String relPath = "charactor/evilbox/EvilBox1_f" + i + ".png";
            evilBoxPaths.add(relPath);
            evilBoxIcons[i - 1] = loadIconFromFile(relPath, 56, 56);
        }
        evilBoxAnimator = new CharacterAnimator("EvilBox", evilBoxPaths, 0.12f);

        dialogManager = new DialogManager();
    }

    private void initComponent() {
        board = new Board();

        // Top Control Container
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(0x22, 0x22, 0x26));

        // Header Panel with Avatars and Clocks
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        headerPanel.setOpaque(false);

        vampireAvatarLabel = new JLabel(vampireIcons[0]);
        headerPanel.add(vampireAvatarLabel);

        whiteTimeLabel = new JLabel("Vampire: 5:00");
        whiteTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        whiteTimeLabel.setForeground(Color.WHITE);
        headerPanel.add(whiteTimeLabel);

        statusLabel = new JLabel(" | White's turn | ");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusLabel.setForeground(new Color(0xFF, 0xD7, 0x00));
        headerPanel.add(statusLabel);

        blackTimeLabel = new JLabel("EvilBox: 5:00");
        blackTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        blackTimeLabel.setForeground(Color.WHITE);
        headerPanel.add(blackTimeLabel);

        evilBoxAvatarLabel = new JLabel(evilBoxIcons[0]);
        headerPanel.add(evilBoxAvatarLabel);

        topContainer.add(headerPanel);

        // Real-Time Dialogue Speech Banner
        JPanel dialogPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dialogPanel.setBackground(new Color(0x18, 0x18, 0x1E));
        dialogBannerLabel = new JLabel("💬 " + dialogManager.getCurrentDialogue());
        dialogBannerLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        dialogBannerLabel.setForeground(new Color(0x00, 0xE5, 0xFF));
        dialogPanel.add(dialogBannerLabel);

        topContainer.add(dialogPanel);
        add(topContainer, BorderLayout.NORTH);

        // Chess Board Panel
        boardPanel = new JPanel(new GridLayout(8, 8));
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(100, 100));
                btn.setFont(new Font("SansSerif", Font.PLAIN, 56));
                btn.setFocusPainted(false);
                btn.setOpaque(true);
                btn.setBorder(emptyBorder);
                final int row = r, col = c;
                btn.addActionListener(e -> onClick(row, col));
                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    private Point pressPoint;

                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        pressPoint = e.getLocationOnScreen();
                    }

                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) {
                        if (pressPoint == null) return;
                        Point releasePoint = e.getLocationOnScreen();
                        if (releasePoint.distance(pressPoint) > 12.0) {
                            Point panelPoint = new Point(releasePoint);
                            SwingUtilities.convertPointFromScreen(panelPoint, boardPanel);
                            Component targetComp = boardPanel.getComponentAt(panelPoint);
                            if (targetComp instanceof JButton) {
                                for (int tr = 0; tr < 8; tr++) {
                                    for (int tc = 0; tc < 8; tc++) {
                                        if (squares[tr][tc] == targetComp) {
                                            if (tr != row || tc != col) {
                                                handleDragMove(row, col, tr, tc);
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
                squares[r][c] = btn;
                boardPanel.add(btn);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        // Bottom control bar with instructions and action buttons
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(0xDD, 0xDD, 0xDD));
        JLabel hintLabel = new JLabel(" Click or drag a piece (Drag & Drop) to the destination square.");
        bottom.add(hintLabel, BorderLayout.WEST);

        JPanel actionBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actionBtnPanel.setOpaque(false);
        JButton drawBtn = new JButton("ขอเสมอ (Draw)");
        drawBtn.setFocusPainted(false);
        drawBtn.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(parentFrame,
                    (currentTurn == Piece.Color.WHITE ? "White" : "Black") + " offers a draw. Do you accept?",
                    "Offer Draw", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(parentFrame, "Game drawn by mutual agreement.", "Draw", JOptionPane.INFORMATION_MESSAGE);
                resetGame();
            }
        });
        JButton resignBtn = new JButton("ยอมแพ้ (Resign)");
        resignBtn.setFocusPainted(false);
        resignBtn.setForeground(new Color(0xB7, 0x1C, 0x1C));
        resignBtn.addActionListener(e -> {
            Piece.Color winner = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
            JOptionPane.showMessageDialog(parentFrame,
                    (currentTurn == Piece.Color.WHITE ? "White" : "Black") + " resigned! " +
                    (winner == Piece.Color.WHITE ? "Vampire (White)" : "EvilBox (Black)") + " wins!",
                    "Resigned", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
        });
        actionBtnPanel.add(drawBtn);
        actionBtnPanel.add(resignBtn);
        bottom.add(actionBtnPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        // Initialize clock and UI timer
        clock = new Clock(INITIAL_MS, INCREMENT_MS);
        clock.startTurn(Clock.Side.WHITE);

        uiTimer = new Timer(100, e -> {
            updateTimeLabels();
            updateCharacterAnimations(0.1f);
            if (clock.isFlagged(Clock.Side.WHITE)) {
                JOptionPane.showMessageDialog(parentFrame, "White ran out of time. EvilBox wins!", "Time", JOptionPane.INFORMATION_MESSAGE);
                resetGame();
            } else if (clock.isFlagged(Clock.Side.BLACK)) {
                JOptionPane.showMessageDialog(parentFrame, "Black ran out of time. Vampire wins!", "Time", JOptionPane.INFORMATION_MESSAGE);
                resetGame();
            }
        });
        uiTimer.start();

        refreshBoardUI();
    }

    private void updateCharacterAnimations(float delta) {
        if (vampireAnimator != null) {
            vampireAnimator.update(delta);
            int vIdx = vampireAnimator.getCurrentFrameIndex();
            if (vampireIcons[vIdx] != null) vampireAvatarLabel.setIcon(vampireIcons[vIdx]);
        }
        if (evilBoxAnimator != null) {
            evilBoxAnimator.update(delta);
            int eIdx = evilBoxAnimator.getCurrentFrameIndex();
            if (evilBoxIcons[eIdx] != null) evilBoxAvatarLabel.setIcon(evilBoxIcons[eIdx]);
        }
        if (dialogManager != null) {
            dialogManager.update(delta);
            if (dialogManager.isDialogueActive()) {
                dialogBannerLabel.setText("💬 " + dialogManager.getCurrentDialogue());
                if (dialogManager.getCurrentSpeaker() == DialogManager.CharacterType.VAMPIRE) {
                    dialogBannerLabel.setForeground(new Color(0xFF, 0x70, 0x43));
                } else {
                    dialogBannerLabel.setForeground(new Color(0x00, 0xE5, 0xFF));
                }
            } else {
                dialogBannerLabel.setText("");
            }
        }
    }

    private void handleDragMove(int fromRow, int fromCol, int toRow, int toCol) {
        Position from = new Position(fromRow, fromCol);
        Piece p = board.getPiece(from);
        if (p != null && p.getColor() == currentTurn) {
            if (selected != null) {
                highlight(selected, false);
            }
            selected = from;
            computeLegalMoves(selected);
            highlight(selected, true);
            onClick(toRow, toCol);
        }
    }

    private void onClick(int row, int col) {
        Position pos = new Position(row, col);
        Piece p = board.getPiece(pos);
        if (selected == null) {
            if (p != null && p.getColor() == currentTurn) {
                selected = pos;
                computeLegalMoves(selected);
                highlight(selected, true);
            }
        } else {
            try {
                if (p != null && p.getColor() == currentTurn) {
                    highlight(selected, false);
                    selected = pos;
                    computeLegalMoves(selected);
                    highlight(selected, true);
                    return;
                }
                if (!ChessUtils.isLegalMove(board, selected, pos, currentTurn)) {
                    JOptionPane.showMessageDialog(parentFrame, "Illegal move: this move leaves your king in check!", "Invalid Move", JOptionPane.ERROR_MESSAGE);
                    highlight(selected, false);
                    clearLegalMoves();
                    selected = null;
                    return;
                }
                boolean isCapture = (board.getPiece(pos) != null);
                board.move(selected, pos, currentTurn);
                Piece.Color moved = currentTurn;
                Piece.Color opponent = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                currentTurn = opponent;

                if (clock != null) clock.moveCompleted();

                if (!board.hasKing(opponent)) {
                    refreshBoardUI();
                    dialogManager.triggerCheckmate(moved);
                    JOptionPane.showMessageDialog(parentFrame, (moved == Piece.Color.WHITE ? "Vampire" : "EvilBox") + " wins! King captured.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    resetGame();
                    return;
                }

                if (ChessUtils.isCheckmate(board, opponent)) {
                    refreshBoardUI();
                    dialogManager.triggerCheckmate(moved);
                    JOptionPane.showMessageDialog(parentFrame, (moved == Piece.Color.WHITE ? "Vampire" : "EvilBox") + " wins! Checkmate!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    resetGame();
                    return;
                }

                if (ChessUtils.isStalemate(board, opponent)) {
                    refreshBoardUI();
                    dialogManager.triggerStalemate();
                    JOptionPane.showMessageDialog(parentFrame, "Stalemate! Game is a draw.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    resetGame();
                    return;
                }

                if (ChessUtils.isInsufficientMaterial(board)) {
                    refreshBoardUI();
                    dialogManager.triggerStalemate();
                    JOptionPane.showMessageDialog(parentFrame, "Draw! Insufficient material to checkmate.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    resetGame();
                    return;
                }

                if (ChessUtils.isFiftyMoveRule(board)) {
                    refreshBoardUI();
                    dialogManager.triggerStalemate();
                    JOptionPane.showMessageDialog(parentFrame, "Draw! Fifty-move rule reached.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    resetGame();
                    return;
                }

                if (ChessUtils.isInCheck(board, opponent)) {
                    dialogManager.triggerCheck(opponent);
                    JOptionPane.showMessageDialog(parentFrame, (opponent == Piece.Color.WHITE ? "Vampire" : "EvilBox") + " is in check!", "Check", JOptionPane.WARNING_MESSAGE);
                } else {
                    dialogManager.triggerMove(moved, isCapture);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Move failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (selected != null) { highlight(selected, false); selected = null; }
                clearLegalMoves();
            }
            refreshBoardUI();
        }
    }

    private void resetGame() {
        board = new Board();
        currentTurn = Piece.Color.WHITE;
        selected = null;
        clearLegalMoves();
        clock = new Clock(INITIAL_MS, INCREMENT_MS);
        clock.startTurn(Clock.Side.WHITE);
        if (dialogManager != null) dialogManager.triggerGameStart();
        refreshBoardUI();
    }

    private void highlight(Position p, boolean on) {
        JButton b = squares[p.row][p.col];
        if (on) b.setBorder(highlightBorder);
        else b.setBorder(emptyBorder);
    }

    private void refreshBoardUI() {
        statusLabel.setText(currentTurn == Piece.Color.WHITE ? "Vampire's turn" : "EvilBox's turn");
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton b = squares[r][c];
                Piece p = board.getPiece(new Position(r, c));
                if (legalMoves[r][c] && p == null) {
                    b.setText("\u2022");
                    b.setFont(new Font("SansSerif", Font.PLAIN, 28));
                    b.setForeground(new Color(0x66, 0x66, 0x66));
                } else {
                    if (p == null) b.setText(""); else b.setText(getUnicodeFor(p));
                    b.setFont(new Font("SansSerif", Font.PLAIN, 56));
                    b.setForeground(p != null && p.getColor() == Piece.Color.WHITE ? Color.BLACK : Color.WHITE);
                }
                boolean light = ((r + c) % 2) == 0;
                Color bg = light ? new Color(0xEE, 0xEE, 0xD2) : new Color(0x76, 0x95, 0x56);
                b.setBackground(bg);

                Piece pAt = board.getPiece(new Position(r, c));
                if (legalMoves[r][c] && pAt != null && pAt.getColor() != currentTurn) {
                    b.setBorder(captureBorder);
                } else {
                    b.setBorder(emptyBorder);
                }
                b.setHorizontalTextPosition(SwingConstants.CENTER);
            }
        }
        updateTimeLabels();
    }

    private void updateTimeLabels() {
        whiteTimeLabel.setText("Vampire: " + Clock.formatMs(clock.getRemaining(Clock.Side.WHITE)));
        blackTimeLabel.setText("EvilBox: " + Clock.formatMs(clock.getRemaining(Clock.Side.BLACK)));
    }

    private void computeLegalMoves(Position from) {
        clearLegalMoves();
        if (from == null) return;
        Piece p = board.getPiece(from);
        if (p == null || p.getColor() != currentTurn) return;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position to = new Position(r, c);
                if (ChessUtils.isLegalMove(board, from, to, currentTurn)) legalMoves[r][c] = true;
            }
        }
    }

    private void clearLegalMoves() {
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) legalMoves[r][c] = false;
    }

    private String getUnicodeFor(Piece p) {
        if (p == null) return "";
        char s = p.getSymbol();
        switch (Character.toUpperCase(s)) {
            case 'K': return p.getColor() == Piece.Color.WHITE ? "\u2654" : "\u265A";
            case 'Q': return p.getColor() == Piece.Color.WHITE ? "\u2655" : "\u265B";
            case 'R': return p.getColor() == Piece.Color.WHITE ? "\u2656" : "\u265C";
            case 'B': return p.getColor() == Piece.Color.WHITE ? "\u2657" : "\u265D";
            case 'N': return p.getColor() == Piece.Color.WHITE ? "\u2658" : "\u265E";
            case 'P': return p.getColor() == Piece.Color.WHITE ? "\u2659" : "\u265F";
            default: return String.valueOf(s);
        }
    }
}
