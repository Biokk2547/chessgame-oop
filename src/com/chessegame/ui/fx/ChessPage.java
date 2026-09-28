package com.chessegame.ui.fx;

import com.chessegame.character.CharacterAnimator;
import com.chessegame.character.DialogManager;
import com.chessegame.model.Board;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;
import com.chessegame.logic.ChessUtils;
import com.chessegame.logic.Clock;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX View component representing the Chess board UI, header with animated character avatars,
 * real-time dialogue speech bubbles, and clock.
 */
public class ChessPage extends BorderPane {
    private final Board board;
    private Piece.Color currentTurn = Piece.Color.WHITE;
    private Position selected = null;
    private final Button[][] squares = new Button[8][8];
    private final boolean[][] legalMoves = new boolean[8][8];

    private final Label statusLabel = new Label();
    private final Label whiteTimeLabel = new Label();
    private final Label blackTimeLabel = new Label();
    private final Label dialogBannerLabel = new Label();

    private final ImageView vampireImageView = new ImageView();
    private final ImageView evilBoxImageView = new ImageView();

    private Clock clock;
    private AnimationTimer uiTimer;
    private long lastNanoTime = 0;
    private static final long INITIAL_MS = 5 * 60 * 1000L;
    private static final long INCREMENT_MS = 2000L;

    // Character Animation & Dialogue System
    private final Image[] vampireImages = new Image[4];
    private final Image[] evilBoxImages = new Image[12];
    private CharacterAnimator vampireAnimator;
    private CharacterAnimator evilBoxAnimator;
    private DialogManager dialogManager;

    public ChessPage() {
        this.board = new Board();
        loadCharacterImages();
        initializeClock();
        buildLayout();
        updateUI();
    }

    private Image loadImageFromFile(String relPath) {
        String[] candidates = new String[] {
            relPath,
            "assets/" + relPath,
            "desktop/assets/" + relPath,
            relPath.replace("charactor/", "characters/"),
            "assets/" + relPath.replace("charactor/", "characters/"),
            "desktop/assets/" + relPath.replace("charactor/", "characters/")
        };
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    return new Image(fis, 60, 60, true, true);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private void loadCharacterImages() {
        List<String> vampirePaths = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String relPath = "charactor/Vampire/VampireAngryframe" + i + ".png";
            vampirePaths.add(relPath);
            vampireImages[i - 1] = loadImageFromFile(relPath);
        }
        vampireAnimator = new CharacterAnimator("Vampire", vampirePaths, 0.20f);

        List<String> evilBoxPaths = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String relPath = "charactor/evilbox/EvilBox1_f" + i + ".png";
            evilBoxPaths.add(relPath);
            evilBoxImages[i - 1] = loadImageFromFile(relPath);
        }
        evilBoxAnimator = new CharacterAnimator("EvilBox", evilBoxPaths, 0.12f);

        dialogManager = new DialogManager();

        vampireImageView.setFitWidth(60);
        vampireImageView.setFitHeight(60);
        evilBoxImageView.setFitWidth(60);
        evilBoxImageView.setFitHeight(60);
    }

    private void initializeClock() {
        clock = new Clock(INITIAL_MS, INCREMENT_MS);
        clock.startTurn(Clock.Side.WHITE);
        uiTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNanoTime == 0) {
                    lastNanoTime = now;
                    return;
                }
                float delta = (now - lastNanoTime) / 1_000_000_000.0f;
                lastNanoTime = now;

                updateCharacterAnimations(delta);
                updateTimeLabels();

                if (clock.isFlagged(Clock.Side.WHITE)) {
                    showAlert("Time", "White ran out of time. EvilBox wins!", Alert.AlertType.INFORMATION);
                    resetGame();
                } else if (clock.isFlagged(Clock.Side.BLACK)) {
                    showAlert("Time", "Black ran out of time. Vampire wins!", Alert.AlertType.INFORMATION);
                    resetGame();
                }
            }
        };
        uiTimer.start();
    }

    private void updateCharacterAnimations(float delta) {
        if (vampireAnimator != null) {
            vampireAnimator.update(delta);
            int vIdx = vampireAnimator.getCurrentFrameIndex();
            if (vampireImages[vIdx] != null) vampireImageView.setImage(vampireImages[vIdx]);
        }
        if (evilBoxAnimator != null) {
            evilBoxAnimator.update(delta);
            int eIdx = evilBoxAnimator.getCurrentFrameIndex();
            if (evilBoxImages[eIdx] != null) evilBoxImageView.setImage(evilBoxImages[eIdx]);
        }
        if (dialogManager != null) {
            dialogManager.update(delta);
            if (dialogManager.isDialogueActive()) {
                dialogBannerLabel.setText("💬 " + dialogManager.getCurrentDialogue());
                if (dialogManager.getCurrentSpeaker() == DialogManager.CharacterType.VAMPIRE) {
                    dialogBannerLabel.setTextFill(Color.web("#FF7043"));
                } else {
                    dialogBannerLabel.setTextFill(Color.web("#00E5FF"));
                }
            } else {
                dialogBannerLabel.setText("");
            }
        }
    }

    private void buildLayout() {
        setTop(createHeaderSection());
        setCenter(createBoardSection());
        setBottom(createFooterSection());
    }

    private VBox createHeaderSection() {
        VBox topSection = new VBox(8);
        topSection.setStyle("-fx-background-color: #222226; -fx-padding: 12px;");
        topSection.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("♟ Chess Game ♟");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);

        HBox avatarBox = new HBox(15);
        avatarBox.setAlignment(Pos.CENTER);

        whiteTimeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        whiteTimeLabel.setTextFill(Color.WHITE);

        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        statusLabel.setTextFill(Color.web("#FFD700"));

        blackTimeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        blackTimeLabel.setTextFill(Color.WHITE);

        avatarBox.getChildren().addAll(vampireImageView, whiteTimeLabel, statusLabel, blackTimeLabel, evilBoxImageView);

        dialogBannerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dialogBannerLabel.setTextFill(Color.web("#00E5FF"));

        topSection.getChildren().addAll(titleLabel, avatarBox, dialogBannerLabel);
        return topSection;
    }

    private GridPane createBoardSection() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Button button = new Button();
                button.setPrefSize(100, 100);
                button.setFont(Font.font(132));
                final int rowIndex = row;
                final int colIndex = col;
                button.setOnAction(e -> onClick(rowIndex, colIndex));

                button.setOnDragDetected(event -> {
                    Position pos = new Position(rowIndex, colIndex);
                    Piece p = board.getPiece(pos);
                    if (p != null && p.getColor() == currentTurn) {
                        Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(rowIndex + "," + colIndex);
                        db.setContent(content);
                        if (selected != null) {
                            highlightSelection(selected, false);
                        }
                        selected = pos;
                        computeLegalMoves(selected);
                        highlightSelection(selected, true);
                        event.consume();
                    }
                });

                button.setOnDragOver(event -> {
                    if (event.getGestureSource() != button && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                button.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String[] parts = db.getString().split(",");
                        int fromR = Integer.parseInt(parts[0]);
                        int fromC = Integer.parseInt(parts[1]);
                        if (fromR != rowIndex || fromC != colIndex) {
                            onClick(rowIndex, colIndex);
                            success = true;
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                squares[row][col] = button;
                grid.add(button, col, row);
            }
        }
        return grid;
    }

    private VBox createFooterSection() {
        VBox bottomSection = new VBox();
        bottomSection.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px;");
        bottomSection.setAlignment(Pos.CENTER);

        Label infoLabel = new Label("Click or drag a piece (Drag & Drop) to the destination square.");
        infoLabel.setFont(Font.font("Arial", 12));
        bottomSection.getChildren().add(infoLabel);
        return bottomSection;
    }

    private void onClick(int row, int col) {
        Position pos = new Position(row, col);
        Piece piece = board.getPiece(pos);

        if (selected == null) {
            if (piece != null && piece.getColor() == currentTurn) {
                selected = pos;
                computeLegalMoves(selected);
                highlightSelection(selected, true);
            }
            return;
        }

        try {
            if (piece != null && piece.getColor() == currentTurn) {
                highlightSelection(selected, false);
                selected = pos;
                computeLegalMoves(selected);
                highlightSelection(selected, true);
                return;
            }

            if (!ChessUtils.isLegalMove(board, selected, pos, currentTurn)) {
                showAlert("Invalid Move", "Illegal move: this move leaves your king in check!", Alert.AlertType.ERROR);
                highlightSelection(selected, false);
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
                updateUI();
                dialogManager.triggerCheckmate(moved);
                showAlert("Game Over", moved == Piece.Color.WHITE ? "Vampire wins! King captured." : "EvilBox wins! King captured.", Alert.AlertType.INFORMATION);
                resetGame();
                return;
            }

            if (ChessUtils.isCheckmate(board, opponent)) {
                updateUI();
                dialogManager.triggerCheckmate(moved);
                showAlert("Game Over - Checkmate", currentTurn == Piece.Color.WHITE ? "Vampire wins! Checkmate!" : "EvilBox wins! Checkmate!", Alert.AlertType.INFORMATION);
                resetGame();
                return;
            }

            if (ChessUtils.isStalemate(board, opponent)) {
                updateUI();
                dialogManager.triggerStalemate();
                showAlert("Game Over - Stalemate", "Stalemate! Game is a draw.", Alert.AlertType.INFORMATION);
                resetGame();
                return;
            }

            if (ChessUtils.isInCheck(board, opponent)) {
                dialogManager.triggerCheck(opponent);
                showAlert("Check!", opponent == Piece.Color.WHITE ? "Vampire is in check!" : "EvilBox is in check!", Alert.AlertType.WARNING);
            } else {
                dialogManager.triggerMove(moved, isCapture);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Error", "Move failed: " + ex.getMessage(), Alert.AlertType.ERROR);
        } finally {
            if (selected != null) {
                highlightSelection(selected, false);
                selected = null;
            }
            clearLegalMoves();
        }

        updateUI();
    }

    private void resetGame() {
        board.reset();
        currentTurn = Piece.Color.WHITE;
        selected = null;
        clearLegalMoves();
        clock = new Clock(INITIAL_MS, INCREMENT_MS);
        clock.startTurn(Clock.Side.WHITE);
        if (dialogManager != null) dialogManager.triggerGameStart();
        updateUI();
    }

    private void computeLegalMoves(Position from) {
        clearLegalMoves();
        if (from == null) return;

        Piece piece = board.getPiece(from);
        if (piece == null || piece.getColor() != currentTurn) return;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position to = new Position(row, col);
                if (ChessUtils.isLegalMove(board, from, to, currentTurn)) {
                    legalMoves[row][col] = true;
                }
            }
        }
    }

    private void clearLegalMoves() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                legalMoves[row][col] = false;
            }
        }
    }

    private void highlightSelection(Position position, boolean active) {
        if (position == null) return;
        Button button = squares[position.row][position.col];
        if (button == null) return;

        String style = buildSquareStyle(position.row, position.col, active);
        button.setStyle(style);
    }

    private void updateUI() {
        statusLabel.setText((currentTurn == Piece.Color.WHITE ? "Vampire" : "EvilBox") + "'s turn");

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Button button = squares[row][col];
                Piece piece = board.getPiece(new Position(row, col));
                button.setText(legalMoves[row][col] && piece == null ? "•" : getUnicodeFor(piece));
                button.setTextFill(piece != null && piece.getColor() == Piece.Color.WHITE ? Color.BLACK : Color.WHITE);

                if (legalMoves[row][col] && piece == null) {
                    button.setFont(Font.font(28));
                    button.setTextFill(Color.web("#666666"));
                } else {
                    button.setFont(Font.font(56));
                    if (piece != null) {
                        button.setTextFill(piece.getColor() == Piece.Color.WHITE ? Color.BLACK : Color.WHITE);
                    }
                }

                boolean selectedSquare = selected != null && selected.row == row && selected.col == col;
                button.setStyle(buildSquareStyle(row, col, selectedSquare));
            }
        }

        updateTimeLabels();
    }

    private String buildSquareStyle(int row, int col, boolean selectedSquare) {
        boolean light = ((row + col) % 2) == 0;
        String baseColor = light ? "#EEEED2" : "#769656";
        String borderColor = selectedSquare ? "#FFD700" : "transparent";
        String borderWidth = selectedSquare ? "4px" : "0px";
        return "-fx-background-color: " + baseColor + "; -fx-border-color: " + borderColor + "; -fx-border-width: " + borderWidth + "; -fx-cursor: hand;";
    }

    private void updateTimeLabels() {
        if (clock == null) return;
        whiteTimeLabel.setText("Vampire: " + Clock.formatMs(clock.getRemaining(Clock.Side.WHITE)));
        blackTimeLabel.setText("EvilBox: " + Clock.formatMs(clock.getRemaining(Clock.Side.BLACK)));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private String getUnicodeFor(Piece piece) {
        if (piece == null) return "";
        char symbol = piece.getSymbol();
        switch (Character.toUpperCase(symbol)) {
            case 'K': return piece.getColor() == Piece.Color.WHITE ? "♔" : "♚";
            case 'Q': return piece.getColor() == Piece.Color.WHITE ? "♕" : "♛";
            case 'R': return piece.getColor() == Piece.Color.WHITE ? "♖" : "♜";
            case 'B': return piece.getColor() == Piece.Color.WHITE ? "♗" : "♝";
            case 'N': return piece.getColor() == Piece.Color.WHITE ? "♘" : "♞";
            case 'P': return piece.getColor() == Piece.Color.WHITE ? "♙" : "♟";
            default: return String.valueOf(symbol);
        }
    }
}
