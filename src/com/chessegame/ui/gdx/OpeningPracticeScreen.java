package com.chessegame.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.chessegame.ai.Evaluation;
import com.chessegame.ai.FENUtils;
import com.chessegame.audio.MusicManager;
import com.chessegame.audio.SoundManager;
import com.chessegame.logic.ChessUtils;
import com.chessegame.model.*;
import com.chessegame.opening.Opening;
import com.chessegame.opening.OpeningManager;
import com.chessegame.particle.ParticleSystem;
import com.chessegame.particle.PieceGlideAnimation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpeningPracticeScreen: Premium chess opening repertoire practice arena.
 * Features a balanced 2-column layout with real-time strategic commentary,
 * visual hint arrows, interactive move timeline, and continuation against AI.
 */
public class OpeningPracticeScreen extends ScreenAdapter {

    private final LibGdxChessApp app;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont headerFont;
    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;
    private BitmapFont coordFont;
    private final GlyphLayout layout = new GlyphLayout();

    private ParticleSystem particleSystem;
    private PieceGlideAnimation activeGlideAnim = null;

    private Texture boardTex;
    private Texture backgroundTex;
    private final Map<String, Texture> pieceTex = new HashMap<>();

    private int currentOpeningId;
    private Opening currentOpening;
    private Board board;
    private Piece.Color currentTurn = Piece.Color.WHITE;
    private Position selected = null;
    private boolean[][] legalMoves = new boolean[8][8];

    // Drag & Drop State
    private boolean isDragging = false;
    private Position dragSource = null;
    private Piece draggedPiece = null;
    private float dragStartX = 0f;
    private float dragStartY = 0f;
    private float dragCurrentX = 0f;
    private float dragCurrentY = 0f;

    // Opening Practice Step State
    private int stepIndex = 0; // Current index in opening.getMovesUci()
    private boolean isCompleted = false;
    private boolean showHint = false;
    private String statusMessage = "";
    private Color statusColor = Color.WHITE;
    private float stateTime = 0f;

    // Last Move State
    private Position lastMoveFrom = null;
    private Position lastMoveTo = null;
    private Piece.Color lastMoveColor = Piece.Color.WHITE;

    private static final float TOP_BAR_HEIGHT = 64f;
    private final float boardInsetPercent = 6.0f / 142.0f;

    public OpeningPracticeScreen(LibGdxChessApp app, int openingId) {
        this.app = app;
        this.currentOpeningId = openingId;
        this.currentOpening = OpeningManager.getOpeningById(openingId);
    }

    private com.badlogic.gdx.files.FileHandle resolveAsset(String name) {
        String fileSep = System.getProperty("file.separator");
        String userDir = System.getProperty("user.dir");
        String[] candidates = new String[]{
                name,
                "assets/" + name,
                "desktop/assets/" + name,
                userDir + fileSep + "assets" + fileSep + name,
                userDir + fileSep + "desktop" + fileSep + "assets" + fileSep + name
        };
        for (String c : candidates) {
            try {
                com.badlogic.gdx.files.FileHandle fh = c.startsWith(userDir) ? Gdx.files.absolute(c) : Gdx.files.internal(c);
                if (fh != null && fh.exists()) return fh;
            } catch (Throwable ignored) {}
        }

        String alias = getPieceAlias(name);
        if (alias != null && !alias.equals(name)) {
            String[] aliasCandidates = new String[]{
                    alias,
                    "assets/" + alias,
                    "desktop/assets/" + alias,
                    userDir + fileSep + "assets" + fileSep + alias,
                    userDir + fileSep + "desktop" + fileSep + "assets" + fileSep + alias
            };
            for (String c : aliasCandidates) {
                try {
                    com.badlogic.gdx.files.FileHandle fh = c.startsWith(userDir) ? Gdx.files.absolute(c) : Gdx.files.internal(c);
                    if (fh != null && fh.exists()) return fh;
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    private static String getPieceAlias(String name) {
        switch (name) {
            case "w_K.png": return "W_King.png";
            case "w_Q.png": return "W_Queen.png";
            case "w_R.png": return "W_Rook.png";
            case "w_B.png": return "W_Bishop.png";
            case "w_N.png": return "W_Knight.png";
            case "w_P.png": return "W_Pawn.png";
            case "b_K.png": return "B_King.png";
            case "b_Q.png": return "B_Queen.png";
            case "b_R.png": return "B_Rook.png";
            case "b_B.png": return "B_Bishop.png";
            case "b_N.png": return "B_Knight.png";
            case "b_P.png": return "B_Pawn.png";
            default: return null;
        }
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        particleSystem = new ParticleSystem();

        // Load FreeType fonts
        try {
            com.badlogic.gdx.files.FileHandle ttfFile = resolveAsset("fonts/thai.ttf");
            if (ttfFile != null && ttfFile.exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttfFile);
                StringBuilder sb = new StringBuilder();
                for (char c = '\u0E01'; c <= '\u0E5B'; c++) sb.append(c);
                String extraChars = "•+-*!?:.()[]'\"%/#1234567890<>=~_@$&,;";

                // Header Font (24px)
                FreeTypeFontGenerator.FreeTypeFontParameter hp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                hp.size = 24;
                hp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                hp.minFilter = Texture.TextureFilter.Linear;
                hp.magFilter = Texture.TextureFilter.Linear;
                hp.borderWidth = 1.4f;
                hp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                headerFont = gen.generateFont(hp);

                // Title Font (19px)
                FreeTypeFontGenerator.FreeTypeFontParameter tp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                tp.size = 19;
                tp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                tp.minFilter = Texture.TextureFilter.Linear;
                tp.magFilter = Texture.TextureFilter.Linear;
                tp.borderWidth = 1.2f;
                tp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                titleFont = gen.generateFont(tp);

                // Body Font (15px)
                FreeTypeFontGenerator.FreeTypeFontParameter fp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                fp.size = 15;
                fp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                fp.minFilter = Texture.TextureFilter.Linear;
                fp.magFilter = Texture.TextureFilter.Linear;
                fp.borderWidth = 1.0f;
                fp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                font = gen.generateFont(fp);

                // Small Details Font (13px)
                FreeTypeFontGenerator.FreeTypeFontParameter sp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                sp.size = 13;
                sp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                sp.minFilter = Texture.TextureFilter.Linear;
                sp.magFilter = Texture.TextureFilter.Linear;
                sp.borderWidth = 0.8f;
                sp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                smallFont = gen.generateFont(sp);

                // Coordinate Font (14px)
                FreeTypeFontGenerator.FreeTypeFontParameter coordParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
                coordParam.size = 14;
                coordParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "abcdefgh12345678";
                coordParam.minFilter = Texture.TextureFilter.Linear;
                coordParam.magFilter = Texture.TextureFilter.Linear;
                coordFont = gen.generateFont(coordParam);

                gen.dispose();
            } else {
                headerFont = new BitmapFont();
                titleFont = new BitmapFont();
                font = new BitmapFont();
                smallFont = new BitmapFont();
                coordFont = new BitmapFont();
            }
        } catch (Throwable ignored) {
            headerFont = new BitmapFont();
            titleFont = new BitmapFont();
            font = new BitmapFont();
            smallFont = new BitmapFont();
            coordFont = new BitmapFont();
        }

        // Load Piece Textures
        String[][] pieceDefs = {
                {"K", "King"}, {"Q", "Queen"}, {"R", "Rook"},
                {"B", "Bishop"}, {"N", "Knight"}, {"P", "Pawn"}
        };
        for (String[] def : pieceDefs) {
            String k = def[0];
            String name = def[1];
            try {
                com.badlogic.gdx.files.FileHandle wFh = resolveAsset("w_" + k + ".png");
                if (wFh == null || !wFh.exists()) wFh = resolveAsset("W_" + name + ".png");
                if (wFh != null && wFh.exists()) pieceTex.put("w_" + k, new Texture(wFh));

                com.badlogic.gdx.files.FileHandle bFh = resolveAsset("b_" + k + ".png");
                if (bFh == null || !bFh.exists()) bFh = resolveAsset("B_" + name + ".png");
                if (bFh != null && bFh.exists()) pieceTex.put("b_" + k, new Texture(bFh));
            } catch (Throwable ignored) {}
        }

        // Load board & background
        try {
            com.badlogic.gdx.files.FileHandle bfh = resolveAsset("board.png");
            if (bfh != null && bfh.exists()) boardTex = new Texture(bfh);
        } catch (Throwable ignored) {}

        try {
            com.badlogic.gdx.files.FileHandle bgFh = resolveAsset("background.png");
            if (bgFh != null && bgFh.exists()) backgroundTex = new Texture(bgFh);
        } catch (Throwable ignored) {}

        resetOpening(currentOpeningId);
        setupInput();
    }

    private void resetOpening(int openingId) {
        this.currentOpeningId = openingId;
        this.currentOpening = OpeningManager.getOpeningById(openingId);
        this.board = new Board();
        this.currentTurn = Piece.Color.WHITE;
        this.selected = null;
        this.legalMoves = new boolean[8][8];
        this.stepIndex = 0;
        this.isCompleted = false;
        this.showHint = false;
        this.lastMoveFrom = null;
        this.lastMoveTo = null;
        this.activeGlideAnim = null;
        clearDrag();

        if (currentOpening.getPlayerColor() == Piece.Color.BLACK && stepIndex == 0) {
            statusMessage = "คู่ต่อสู้กำลังเดินเปิดเกม...";
            statusColor = new Color(0.40f, 0.85f, 1.0f, 1f);
            executeOpponentMoveWithDelay(600L);
        } else {
            statusMessage = "ตาเดินของคุณ! เลือกเดินตามแผนเปิดเกม";
            statusColor = new Color(0.95f, 0.85f, 0.35f, 1f);
        }
    }

    private void executeOpponentMoveWithDelay(long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {}

            Gdx.app.postRunnable(() -> {
                if (stepIndex < currentOpening.getMovesUci().size()) {
                    String moveUci = currentOpening.getMovesUci().get(stepIndex);
                    Position from = FENUtils.fromUCISquare(moveUci.substring(0, 2));
                    Position to = FENUtils.fromUCISquare(moveUci.substring(2, 4));

                    if (from != null && to != null) {
                        Piece moving = board.getPiece(from);
                        Piece captured = board.getPiece(to);
                        startGlide(moving, from, to, false);

                        board.moveRecord(from, to, currentTurn, null);
                        lastMoveFrom = from;
                        lastMoveTo = to;
                        lastMoveColor = currentTurn;

                        if (captured != null) {
                            SoundManager.getInstance().playCaptureSound();
                        } else {
                            SoundManager.getInstance().playMoveSound();
                        }

                        currentTurn = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                        stepIndex++;

                        statusMessage = "ตาเดินของคุณ! ดำเนินตามสายเปิดเกมต่อไป";
                        statusColor = new Color(0.95f, 0.85f, 0.35f, 1f);
                    }
                }
            });
        }).start();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.M) {
                    MusicManager.getInstance().toggleMute();
                    return true;
                }
                if (keycode == Input.Keys.ESCAPE) {
                    app.setScreen(new OpeningSelectScreen(app));
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;

                // 1. Check Victory Modal Clicks if completed
                if (isCompleted && handleVictoryModalClick(x, y)) return true;

                // 2. Check Top Bar Button Clicks
                if (handleTopBarClick(x, y)) return true;

                // 3. Check Action Button Clicks in Side Panel
                if (handleActionButtonsClick(x, y)) return true;

                // 4. Board Square Clicks & Drag Start
                float boardSize = getBoardSize();
                float boardX = getBoardX(boardSize);
                float boardY = getBoardY(boardSize);
                float destInset = boardInsetPercent * boardSize;
                float innerX = boardX + destInset;
                float innerY = boardY + destInset;
                float innerSquare = (boardSize - 2 * destInset) / 8f;

                if (x >= innerX && x < innerX + (innerSquare * 8) && y >= innerY && y < innerY + (innerSquare * 8)) {
                    int col = (int) Math.floor((x - innerX) / innerSquare);
                    int localRow = (int) Math.floor((y - innerY) / innerSquare);
                    int row = 7 - localRow;

                    Position clicked = new Position(row, col);
                    Piece p = board.getPiece(clicked);

                    if (currentTurn == currentOpening.getPlayerColor()) {
                        if (selected != null && legalMoves[row][col]) {
                            handlePlayerMove(selected, clicked, false);
                            selected = null;
                            legalMoves = new boolean[8][8];
                        } else if (p != null && p.getColor() == currentTurn) {
                            selected = clicked;
                            computeLegalMoves(selected);
                            isDragging = true;
                            dragSource = clicked;
                            draggedPiece = p;
                            dragStartX = x;
                            dragStartY = y;
                            dragCurrentX = x;
                            dragCurrentY = y;
                        } else {
                            selected = null;
                            legalMoves = new boolean[8][8];
                        }
                    }
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (isDragging) {
                    dragCurrentX = screenX;
                    dragCurrentY = Gdx.graphics.getHeight() - screenY;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (isDragging) {
                    float x = screenX;
                    float y = Gdx.graphics.getHeight() - screenY;

                    float boardSize = getBoardSize();
                    float boardX = getBoardX(boardSize);
                    float boardY = getBoardY(boardSize);
                    float destInset = boardInsetPercent * boardSize;
                    float innerX = boardX + destInset;
                    float innerY = boardY + destInset;
                    float innerSquare = (boardSize - 2 * destInset) / 8f;

                    if (x >= innerX && x < innerX + (innerSquare * 8) && y >= innerY && y < innerY + (innerSquare * 8)) {
                        int col = (int) Math.floor((x - innerX) / innerSquare);
                        int localRow = (int) Math.floor((y - innerY) / innerSquare);
                        int row = 7 - localRow;
                        Position target = new Position(row, col);

                        if (!target.equals(dragSource) && legalMoves[row][col]) {
                            handlePlayerMove(dragSource, target, true);
                            selected = null;
                            legalMoves = new boolean[8][8];
                        }
                    }
                    clearDrag();
                    return true;
                }
                return false;
            }
        });
    }

    private void handlePlayerMove(Position from, Position to, boolean fromDrag) {
        if (stepIndex >= currentOpening.getMovesUci().size()) return;

        String expectedUci = currentOpening.getMovesUci().get(stepIndex);
        String playerMoveUci = FENUtils.toUCIMove(from, to, null);

        if (playerMoveUci.equalsIgnoreCase(expectedUci)) {
            Piece moving = board.getPiece(from);
            Piece captured = board.getPiece(to);
            startGlide(moving, from, to, fromDrag);

            board.moveRecord(from, to, currentTurn, null);
            lastMoveFrom = from;
            lastMoveTo = to;
            lastMoveColor = currentTurn;

            if (captured != null) {
                SoundManager.getInstance().playCaptureSound();
            } else {
                SoundManager.getInstance().playMoveSound();
            }

            stepIndex++;
            showHint = false;
            currentTurn = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

            if (stepIndex >= currentOpening.getMovesUci().size()) {
                isCompleted = true;
                statusMessage = "สำเร็จ! คุณเชี่ยวชาญสายเปิดเกมนี้แล้ว!";
                statusColor = new Color(0.25f, 0.95f, 0.45f, 1f);
                OpeningManager.recordOpeningCompleted(currentOpening.getId(), 3);
                SoundManager.getInstance().playVictorySound();
                particleSystem.emitVictoryConfetti(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            } else {
                statusMessage = "ถูกต้อง! คู่ต่อสู้กำลังเดินตามสาย...";
                statusColor = new Color(0.40f, 0.85f, 1.0f, 1f);
                executeOpponentMoveWithDelay(450L);
            }
        } else {
            statusMessage = "ยังไม่ใช่ตาเดินของสายนี้! (ลองคิดดูใหม่อีกครั้ง หรือกดดูคำใบ้)";
            statusColor = new Color(0.95f, 0.35f, 0.35f, 1f);
            SoundManager.getInstance().playClickSound();
        }
    }

    private boolean handleTopBarClick(float x, float y) {
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        // Top Left: Back Button
        float backX = 20f;
        float backY = screenH - 50f;
        float backW = 150f;
        float backH = 36f;
        if (x >= backX && x <= backX + backW && y >= backY && y <= backY + backH) {
            SoundManager.getInstance().playClickSound();
            app.setScreen(new OpeningSelectScreen(app));
            return true;
        }

        // Top Right: Music Toggle
        float musicW = 150f;
        float musicH = 36f;
        float musicX = screenW - musicW - 20f;
        float musicY = screenH - 50f;
        if (x >= musicX && x <= musicX + musicW && y >= musicY && y <= musicY + musicH) {
            MusicManager.getInstance().toggleMute();
            return true;
        }

        return false;
    }

    private boolean handleActionButtonsClick(float x, float y) {
        float boardSize = getBoardSize();
        float boardX = getBoardX(boardSize);
        float boardY = getBoardY(boardSize);

        float panelX = boardX + boardSize + 24f;
        float panelW = Math.max(340f, Gdx.graphics.getWidth() - panelX - 24f);

        float btnY1 = boardY + 54f;
        float btnY2 = boardY + 10f;
        float btnH = 38f;

        // Row 1: 3 Buttons (Hint, Restart, Play AI)
        float gap = 8f;
        float b1W = (panelW - 24f - 2 * gap) / 3f;
        float b1X = panelX + 12f;
        float b2X = b1X + b1W + gap;
        float b3X = b2X + b1W + gap;

        // Button 1: Hint
        if (x >= b1X && x <= b1X + b1W && y >= btnY1 && y <= btnY1 + btnH) {
            showHint = !showHint;
            SoundManager.getInstance().playClickSound();
            return true;
        }
        // Button 2: Restart
        if (x >= b2X && x <= b2X + b1W && y >= btnY1 && y <= btnY1 + btnH) {
            resetOpening(currentOpeningId);
            SoundManager.getInstance().playClickSound();
            return true;
        }
        // Button 3: Play vs AI
        if (x >= b3X && x <= b3X + b1W && y >= btnY1 && y <= btnY1 + btnH) {
            SoundManager.getInstance().playClickSound();
            transitionToPlayVsAi();
            return true;
        }

        // Row 2: 2 Buttons (Select Opening, Main Menu)
        float b2W = (panelW - 24f - gap) / 2f;
        float b4X = panelX + 12f;
        float b5X = b4X + b2W + gap;

        // Button 4: Select Opening
        if (x >= b4X && x <= b4X + b2W && y >= btnY2 && y <= btnY2 + btnH) {
            SoundManager.getInstance().playClickSound();
            app.setScreen(new OpeningSelectScreen(app));
            return true;
        }
        // Button 5: Main Menu
        if (x >= b5X && x <= b5X + b2W && y >= btnY2 && y <= btnY2 + btnH) {
            SoundManager.getInstance().playClickSound();
            app.setScreen(new MainMenuScreen(app));
            return true;
        }

        return false;
    }

    private boolean handleVictoryModalClick(float x, float y) {
        float modalW = 500f;
        float modalH = 280f;
        float modalX = (Gdx.graphics.getWidth() - modalW) / 2f;
        float modalY = (Gdx.graphics.getHeight() - modalH) / 2f;

        float btnW = 210f;
        float btnH = 42f;
        float btnY1 = modalY + 70f;
        float btnY2 = modalY + 18f;

        float b1X = modalX + 28f;
        float b2X = modalX + modalW - btnW - 28f;

        // Button 1: Play vs AI
        if (x >= b1X && x <= b1X + btnW && y >= btnY1 && y <= btnY1 + btnH) {
            transitionToPlayVsAi();
            return true;
        }

        // Button 2: Next Opening
        if (x >= b2X && x <= b2X + btnW && y >= btnY1 && y <= btnY1 + btnH) {
            int nextId = (currentOpeningId % 10) + 1;
            resetOpening(nextId);
            return true;
        }

        // Button 3: Retry
        if (x >= b1X && x <= b1X + btnW && y >= btnY2 && y <= btnY2 + btnH) {
            resetOpening(currentOpeningId);
            return true;
        }

        // Button 4: Select Opening
        if (x >= b2X && x <= b2X + btnW && y >= btnY2 && y <= btnY2 + btnH) {
            app.setScreen(new OpeningSelectScreen(app));
            return true;
        }

        return false;
    }

    private void transitionToPlayVsAi() {
        app.setScreen(new GameScreen(app, board.copy(), currentTurn, currentOpening.getPlayerColor(), 3, Evaluation.AIStyle.MASTER));
    }

    private void computeLegalMoves(Position from) {
        legalMoves = new boolean[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                legalMoves[r][c] = ChessUtils.isLegalMove(board, from, new Position(r, c), currentTurn);
            }
        }
    }

    private void clearDrag() {
        isDragging = false;
        dragSource = null;
        draggedPiece = null;
    }

    private void startGlide(Piece piece, Position from, Position to, boolean fromDrag) {
        if (piece == null || from == null || to == null) return;
        float boardSize = getBoardSize();
        float boardX = getBoardX(boardSize);
        float boardY = getBoardY(boardSize);
        float destInset = boardInsetPercent * boardSize;
        float innerSquare = (boardSize - 2 * destInset) / 8f;
        float pieceBaseSize = innerSquare * 0.84f;

        float fromX = fromDrag ? dragCurrentX - pieceBaseSize / 2f : boardX + destInset + (from.col * innerSquare) + (innerSquare - pieceBaseSize) / 2f;
        float fromY = fromDrag ? dragCurrentY - pieceBaseSize / 2f : boardY + destInset + ((7 - from.row) * innerSquare) + (innerSquare - pieceBaseSize) / 2f;
        float toX = boardX + destInset + (to.col * innerSquare) + (innerSquare - pieceBaseSize) / 2f;
        float toY = boardY + destInset + ((7 - to.row) * innerSquare) + (innerSquare - pieceBaseSize) / 2f;

        boolean isCap = (board.getPiece(to) != null);
        activeGlideAnim = new PieceGlideAnimation(piece, from, to, fromX, fromY, toX, toY, isCap, fromDrag ? 0.08f : 0.22f);
    }

    private float getBoardSize() {
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        float availH = screenH - TOP_BAR_HEIGHT - 36f;
        float maxW = screenW * 0.50f;
        return Math.max(340f, Math.min(availH, Math.min(680f, maxW)));
    }

    private float getBoardX(float boardSize) {
        return 34f;
    }

    private float getBoardY(float boardSize) {
        float availH = Gdx.graphics.getHeight() - TOP_BAR_HEIGHT - 24f;
        return 12f + (availH - boardSize) / 2f;
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.setToOrtho(false, width, height);
            camera.update();
        }
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        particleSystem.update(delta);
        if (activeGlideAnim != null) {
            activeGlideAnim.update(delta, particleSystem);
            if (activeGlideAnim.isFinished()) activeGlideAnim = null;
        }

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        float mx = Gdx.input.getX();
        float my = screenH - Gdx.input.getY();

        if (camera == null) camera = new OrthographicCamera();
        camera.setToOrtho(false, screenW, screenH);
        camera.update();

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.08f, 0.09f, 0.13f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Background image
        if (backgroundTex != null) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(backgroundTex, 0f, 0f, (float) screenW, (float) screenH);
            batch.end();
        }

        // 2. Dark Atmospheric Tint
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.07f, 0.12f, backgroundTex != null ? 0.75f : 1.0f);
        shapes.rect(0f, 0f, (float) screenW, (float) screenH);

        // Top Header Bar
        shapes.setColor(0.10f, 0.12f, 0.18f, 0.96f);
        shapes.rect(0f, screenH - TOP_BAR_HEIGHT, screenW, TOP_BAR_HEIGHT);
        shapes.setColor(0.95f, 0.75f, 0.25f, 0.90f);
        shapes.rect(0f, screenH - TOP_BAR_HEIGHT, screenW, 3f);

        // Top Left Button: Back
        float backX = 20f;
        float backY = screenH - 50f;
        float backW = 150f;
        float backH = 36f;
        boolean backHover = (mx >= backX && mx <= backX + backW && my >= backY && my <= backY + backH);
        shapes.setColor(backHover ? new Color(0.32f, 0.40f, 0.55f, 1f) : new Color(0.22f, 0.28f, 0.40f, 0.95f));
        shapes.rect(backX, backY, backW, backH);

        // Top Right Button: Music
        float musicW = 150f;
        float musicH = 36f;
        float musicX = screenW - musicW - 20f;
        float musicY = screenH - 50f;
        boolean musicMuted = MusicManager.getInstance().isMuted();
        boolean musicHover = (mx >= musicX && mx <= musicX + musicW && my >= musicY && my <= musicY + musicH);
        if (musicMuted) {
            shapes.setColor(musicHover ? new Color(0.55f, 0.22f, 0.22f, 1f) : new Color(0.42f, 0.18f, 0.18f, 0.95f));
        } else {
            shapes.setColor(musicHover ? new Color(0.22f, 0.65f, 0.40f, 1f) : new Color(0.18f, 0.55f, 0.32f, 0.95f));
        }
        shapes.rect(musicX, musicY, musicW, musicH);

        // Layout Dimensions
        float boardSize = getBoardSize();
        float boardX = getBoardX(boardSize);
        float boardY = getBoardY(boardSize);
        float destInset = boardInsetPercent * boardSize;
        float innerX = boardX + destInset;
        float innerY = boardY + destInset;
        float innerSquare = (boardSize - 2 * destInset) / 8f;

        float panelX = boardX + boardSize + 24f;
        float panelW = Math.max(340f, screenW - panelX - 24f);
        float panelY = boardY;
        float panelH = boardSize;

        // 3. Side Panel Background Card
        shapes.setColor(0.10f, 0.12f, 0.18f, 0.96f);
        shapes.rect(panelX, panelY, panelW, panelH);

        // Section Cards inside Side Panel
        float cardW = panelW - 24f;
        float cardX = panelX + 12f;

        float headerCardH = 78f;
        float headerCardY = panelY + panelH - headerCardH - 12f;

        float goalCardH = 76f;
        float goalCardY = headerCardY - goalCardH - 10f;

        float timelineCardH = 76f;
        float timelineCardY = goalCardY - timelineCardH - 10f;

        float theoryCardH = 88f;
        float theoryCardY = timelineCardY - theoryCardH - 10f;

        float statusBannerH = 44f;
        float statusBannerY = theoryCardY - statusBannerH - 10f;

        // Card 1: Header Card
        shapes.setColor(0.13f, 0.15f, 0.23f, 0.95f);
        shapes.rect(cardX, headerCardY, cardW, headerCardH);
        shapes.setColor(getCategoryColor(currentOpening.getCategory()));
        shapes.rect(cardX, headerCardY, 5f, headerCardH);

        // Card 2: Tactical Goal Card
        shapes.setColor(0.12f, 0.14f, 0.21f, 0.95f);
        shapes.rect(cardX, goalCardY, cardW, goalCardH);
        shapes.setColor(0.20f, 0.70f, 0.90f, 0.90f);
        shapes.rect(cardX, goalCardY, 4f, goalCardH);

        // Card 3: Timeline Card
        shapes.setColor(0.12f, 0.14f, 0.21f, 0.95f);
        shapes.rect(cardX, timelineCardY, cardW, timelineCardH);

        // Timeline Progress Bar Background & Fill
        float pbX = cardX + 12f;
        float pbY = timelineCardY + timelineCardH - 32f;
        float pbW = cardW - 24f;
        float pbH = 6f;
        shapes.setColor(0.20f, 0.22f, 0.30f, 1f);
        shapes.rect(pbX, pbY, pbW, pbH);

        float progressRatio = (float) stepIndex / (float) currentOpening.getTotalSteps();
        progressRatio = Math.max(0f, Math.min(1f, progressRatio));
        shapes.setColor(0.25f, 0.85f, 0.45f, 1f);
        shapes.rect(pbX, pbY, pbW * progressRatio, pbH);

        // Timeline Move Pills
        int totalSteps = currentOpening.getTotalSteps();
        float pillGap = 6f;
        float pillW = Math.min(90f, (cardW - 24f - (totalSteps - 1) * pillGap) / (float) totalSteps);
        float pillH = 24f;
        float pillY = timelineCardY + 10f;

        for (int i = 0; i < totalSteps; i++) {
            float px = cardX + 12f + i * (pillW + pillGap);
            if (i < stepIndex) {
                shapes.setColor(0.15f, 0.52f, 0.32f, 0.95f); // Completed (Green)
            } else if (i == stepIndex) {
                float pulse = 0.75f + 0.25f * (float) Math.sin(stateTime * 6f);
                shapes.setColor(0.90f, 0.68f, 0.15f, pulse); // Current (Amber)
            } else {
                shapes.setColor(0.17f, 0.19f, 0.26f, 0.90f); // Upcoming (Muted Slate)
            }
            shapes.rect(px, pillY, pillW, pillH);
        }

        // Card 4: Current Theory Card
        shapes.setColor(0.13f, 0.15f, 0.23f, 0.95f);
        shapes.rect(cardX, theoryCardY, cardW, theoryCardH);
        shapes.setColor(0.95f, 0.75f, 0.20f, 0.95f);
        shapes.rect(cardX, theoryCardY, 4f, theoryCardH);

        // Card 5: Live Status Banner
        if (isCompleted) {
            shapes.setColor(0.12f, 0.55f, 0.30f, 0.95f);
        } else if (statusColor.equals(new Color(0.95f, 0.35f, 0.35f, 1f))) {
            shapes.setColor(0.55f, 0.16f, 0.16f, 0.95f);
        } else if (currentTurn != currentOpening.getPlayerColor()) {
            shapes.setColor(0.14f, 0.35f, 0.52f, 0.95f);
        } else {
            shapes.setColor(0.18f, 0.24f, 0.38f, 0.95f);
        }
        shapes.rect(cardX, statusBannerY, cardW, statusBannerH);

        // 4. Action Buttons (Row 1 & Row 2)
        float btnY1 = boardY + 54f;
        float btnY2 = boardY + 10f;
        float btnH = 38f;
        float gap = 8f;

        // Row 1 Buttons: Hint, Restart, Play AI
        float b1W = (panelW - 24f - 2 * gap) / 3f;
        float b1X = panelX + 12f;
        float b2X = b1X + b1W + gap;
        float b3X = b2X + b1W + gap;

        boolean h1 = (mx >= b1X && mx <= b1X + b1W && my >= btnY1 && my <= btnY1 + btnH);
        shapes.setColor(showHint ? (h1 ? new Color(0.95f, 0.75f, 0.25f, 1f) : new Color(0.85f, 0.62f, 0.15f, 1f))
                : (h1 ? new Color(0.28f, 0.55f, 0.85f, 1f) : new Color(0.22f, 0.45f, 0.72f, 1f)));
        shapes.rect(b1X, btnY1, b1W, btnH); // Hint

        boolean h2 = (mx >= b2X && mx <= b2X + b1W && my >= btnY1 && my <= btnY1 + btnH);
        shapes.setColor(h2 ? new Color(0.42f, 0.48f, 0.60f, 1f) : new Color(0.32f, 0.36f, 0.46f, 1f));
        shapes.rect(b2X, btnY1, b1W, btnH); // Restart

        boolean h3 = (mx >= b3X && mx <= b3X + b1W && my >= btnY1 && my <= btnY1 + btnH);
        shapes.setColor(h3 ? new Color(0.22f, 0.75f, 0.48f, 1f) : new Color(0.16f, 0.60f, 0.38f, 1f));
        shapes.rect(b3X, btnY1, b1W, btnH); // Play vs AI

        // Row 2 Buttons: Select Opening, Main Menu
        float b2W = (panelW - 24f - gap) / 2f;
        float b4X = panelX + 12f;
        float b5X = b4X + b2W + gap;

        boolean h4 = (mx >= b4X && mx <= b4X + b2W && my >= btnY2 && my <= btnY2 + btnH);
        shapes.setColor(h4 ? new Color(0.55f, 0.36f, 0.78f, 1f) : new Color(0.42f, 0.28f, 0.62f, 1f));
        shapes.rect(b4X, btnY2, b2W, btnH); // Select Opening

        boolean h5 = (mx >= b5X && mx <= b5X + b2W && my >= btnY2 && my <= btnY2 + btnH);
        shapes.setColor(h5 ? new Color(0.48f, 0.22f, 0.22f, 1f) : new Color(0.38f, 0.18f, 0.18f, 1f));
        shapes.rect(b5X, btnY2, b2W, btnH); // Main Menu

        shapes.end();

        // 5. Card Borders
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.28f, 0.34f, 0.46f, 0.80f);
        shapes.rect(panelX, panelY, panelW, panelH);
        shapes.rect(cardX, headerCardY, cardW, headerCardH);
        shapes.rect(cardX, goalCardY, cardW, goalCardH);
        shapes.rect(cardX, timelineCardY, cardW, timelineCardH);
        shapes.rect(cardX, theoryCardY, cardW, theoryCardH);
        shapes.rect(cardX, statusBannerY, cardW, statusBannerH);
        shapes.end();

        // 6. Ambience Particles
        particleSystem.render(shapes);

        // 7. Render Board Texture
        if (boardTex != null) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(boardTex, boardX, boardY, boardSize, boardSize);
            batch.end();
        }

        // 8. Board Highlights, Legal Moves & Hint Neon Arrow
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Highlight Last Move
        if (lastMoveFrom != null && lastMoveTo != null) {
            shapes.setColor(0.95f, 0.85f, 0.20f, 0.28f);
            shapes.rect(innerX + (lastMoveFrom.col * innerSquare),
                    innerY + ((7 - lastMoveFrom.row) * innerSquare), innerSquare, innerSquare);
            shapes.rect(innerX + (lastMoveTo.col * innerSquare),
                    innerY + ((7 - lastMoveTo.row) * innerSquare), innerSquare, innerSquare);
        }

        // Highlight Selected Square
        if (selected != null) {
            shapes.setColor(0.20f, 0.70f, 0.95f, 0.45f);
            shapes.rect(innerX + (selected.col * innerSquare),
                    innerY + ((7 - selected.row) * innerSquare), innerSquare, innerSquare);
        }

        // Highlight Hint Squares
        Position hintFrom = null;
        Position hintTo = null;
        if (showHint && stepIndex < currentOpening.getMovesUci().size()) {
            String hintUci = currentOpening.getMovesUci().get(stepIndex);
            hintFrom = FENUtils.fromUCISquare(hintUci.substring(0, 2));
            hintTo = FENUtils.fromUCISquare(hintUci.substring(2, 4));

            if (hintFrom != null && hintTo != null) {
                float pulse = 0.50f + 0.35f * (float) Math.sin(stateTime * 6f);
                shapes.setColor(0.95f, 0.75f, 0.15f, pulse);
                shapes.rect(innerX + (hintFrom.col * innerSquare),
                        innerY + ((7 - hintFrom.row) * innerSquare), innerSquare, innerSquare);

                shapes.setColor(0.20f, 0.85f, 0.35f, pulse);
                shapes.rect(innerX + (hintTo.col * innerSquare),
                        innerY + ((7 - hintTo.row) * innerSquare), innerSquare, innerSquare);
            }
        }

        // Legal Move Dots
        if (selected != null) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    if (legalMoves[r][c]) {
                        float cx = innerX + (c * innerSquare) + (innerSquare / 2f);
                        float cy = innerY + ((7 - r) * innerSquare) + (innerSquare / 2f);
                        Piece destP = board.getPiece(new Position(r, c));
                        if (destP != null) {
                            shapes.setColor(0.95f, 0.25f, 0.25f, 0.65f);
                            shapes.circle(cx, cy, innerSquare * 0.36f);
                        } else {
                            shapes.setColor(0.20f, 0.80f, 0.40f, 0.70f);
                            shapes.circle(cx, cy, innerSquare * 0.16f);
                        }
                    }
                }
            }
        }

        // Draw Hint Neon Arrow
        if (showHint && hintFrom != null && hintTo != null) {
            float fX = innerX + (hintFrom.col + 0.5f) * innerSquare;
            float fY = innerY + (7 - hintFrom.row + 0.5f) * innerSquare;
            float tX = innerX + (hintTo.col + 0.5f) * innerSquare;
            float tY = innerY + (7 - hintTo.row + 0.5f) * innerSquare;
            float pulse = 0.75f + 0.25f * (float) Math.sin(stateTime * 6f);
            Color arrowColor = new Color(0.18f, 0.95f, 0.45f, 0.88f * pulse);
            drawArrow(fX, fY, tX, tY, 7f, 18f, 20f, arrowColor);
        }

        // Draw Role Indicator Circle on Header Card
        boolean isWhite = (currentOpening.getPlayerColor() == Piece.Color.WHITE);
        float roleCircleX = cardX + 18f;
        float roleCircleY = headerCardY + 18f;
        shapes.setColor(isWhite ? Color.WHITE : new Color(0.18f, 0.18f, 0.22f, 1f));
        shapes.circle(roleCircleX, roleCircleY, 7f);
        if (!isWhite) {
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.WHITE);
            shapes.circle(roleCircleX, roleCircleY, 7f);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
        }

        shapes.end();

        // 9. Draw Board Pieces
        batch.begin();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (isDragging && dragSource != null && dragSource.row == r && dragSource.col == c) {
                    continue;
                }
                Piece p = board.getPiece(new Position(r, c));
                if (p != null) {
                    Texture tex = getPieceTexture(p);
                    if (tex != null) {
                        float pieceBaseSize = innerSquare * 0.84f;
                        float textureRatio = (float) tex.getHeight() / tex.getWidth();
                        float drawWidth = pieceBaseSize;
                        float drawHeight = pieceBaseSize * textureRatio;
                        float px = innerX + (c * innerSquare) + (innerSquare - drawWidth) / 2f;
                        float py = innerY + ((7 - r) * innerSquare) + (innerSquare - pieceBaseSize) / 2f;
                        batch.draw(tex, px, py, drawWidth, drawHeight);
                    }
                }
            }
        }

        // Gliding Piece Animation
        if (activeGlideAnim != null) {
            Texture tex = getPieceTexture(activeGlideAnim.getPiece());
            if (tex != null) {
                float pieceBaseSize = innerSquare * 0.84f;
                float textureRatio = (float) tex.getHeight() / tex.getWidth();
                float drawWidth = pieceBaseSize;
                float drawHeight = pieceBaseSize * textureRatio;
                batch.draw(tex, activeGlideAnim.getCurrentX(), activeGlideAnim.getCurrentY(), drawWidth, drawHeight);
            }
        }

        // Dragged Piece under mouse
        if (isDragging && draggedPiece != null) {
            Texture tex = getPieceTexture(draggedPiece);
            if (tex != null) {
                float pieceBaseSize = innerSquare * 0.90f;
                float textureRatio = (float) tex.getHeight() / tex.getWidth();
                float drawWidth = pieceBaseSize;
                float drawHeight = pieceBaseSize * textureRatio;
                batch.draw(tex, dragCurrentX - drawWidth / 2f, dragCurrentY - drawHeight / 2f, drawWidth, drawHeight);
            }
        }

        // Board Coordinates (a-h and 1-8)
        coordFont.setColor(new Color(0.85f, 0.88f, 0.95f, 0.85f));
        for (int i = 0; i < 8; i++) {
            char colChar = (char) ('a' + i);
            coordFont.draw(batch, String.valueOf(colChar),
                    innerX + (i * innerSquare) + (innerSquare / 2f) - 4f,
                    boardY + destInset - 6f);

            int rankNum = 8 - i;
            coordFont.draw(batch, String.valueOf(rankNum),
                    boardX + destInset - 16f,
                    innerY + (i * innerSquare) + (innerSquare / 2f) + 5f);
        }

        // 10. Top Bar Text Labels
        headerFont.setColor(Color.GOLD);
        layout.setText(headerFont, "โหมดฝึกซ้อมเปิดเกม (Opening Practice)");
        headerFont.draw(batch, "โหมดฝึกซ้อมเปิดเกม (Opening Practice)", (screenW - layout.width) / 2f, screenH - 22f);

        // Top Left Button Label
        font.setColor(Color.WHITE);
        drawButtonText(batch, "<- เลือกสายอื่น", backX, backY, backW, backH);

        // Top Right Music Label
        String musicLabel = musicMuted ? "เพลง: ปิด [M]" : "เพลง: เปิด [M]";
        drawButtonText(batch, musicLabel, musicX, musicY, musicW, musicH);

        // 11. Side Panel Card Text Labels
        // --- Card 1: Header Text ---
        titleFont.setColor(Color.GOLD);
        String thTitle = currentOpening.getNameTh();
        titleFont.draw(batch, thTitle, cardX + 16f, headerCardY + headerCardH - 14f);

        smallFont.setColor(new Color(0.80f, 0.86f, 0.96f, 1f));
        String categoryTag = "[" + currentOpening.getCategory().toUpperCase() + "]  " + currentOpening.getNameEn();
        smallFont.draw(batch, categoryTag, cardX + 16f, headerCardY + headerCardH - 38f);

        String roleStr = isWhite ? "คุณเล่น: หมากขาว (White)" : "คุณเล่น: หมากดำ (Black)";
        smallFont.setColor(isWhite ? new Color(0.95f, 0.92f, 0.70f, 1f) : new Color(0.70f, 0.85f, 1.0f, 1f));
        smallFont.draw(batch, roleStr, roleCircleX + 14f, roleCircleY + 5f);

        String diffStr = "ระดับ: " + currentOpening.getDifficulty();
        layout.setText(smallFont, diffStr);
        smallFont.draw(batch, diffStr, cardX + cardW - layout.width - 14f, roleCircleY + 5f);

        // --- Card 2: Tactical Goal Text ---
        smallFont.setColor(new Color(0.35f, 0.85f, 1.0f, 1f));
        smallFont.draw(batch, "เป้าหมายยุทธวิธีของสายนี้:", cardX + 14f, goalCardY + goalCardH - 12f);

        smallFont.setColor(new Color(0.88f, 0.90f, 0.95f, 1f));
        layout.setText(smallFont, currentOpening.getTacticalGoal(), new Color(0.88f, 0.90f, 0.95f, 1f), cardW - 28f, Align.left, true);
        smallFont.draw(batch, layout, cardX + 14f, goalCardY + goalCardH - 34f);

        // --- Card 3: Move Sequence Timeline Text ---
        smallFont.setColor(Color.WHITE);
        smallFont.draw(batch, "ลำดับตาเดินตามสาย", cardX + 12f, timelineCardY + timelineCardH - 12f);

        int dispStep = Math.min(stepIndex + 1, totalSteps);
        String stepCounterStr = "ความคืบหน้า: ขั้นที่ " + dispStep + " / " + totalSteps;
        layout.setText(smallFont, stepCounterStr);
        smallFont.setColor(new Color(0.95f, 0.85f, 0.35f, 1f));
        smallFont.draw(batch, stepCounterStr, cardX + cardW - layout.width - 12f, timelineCardY + timelineCardH - 12f);

        // Render Pills Text
        for (int i = 0; i < totalSteps; i++) {
            float px = cardX + 12f + i * (pillW + pillGap);
            String moveNot = (i < currentOpening.getMoveNotations().size()) ? currentOpening.getMoveNotations().get(i) : ("Step " + (i + 1));
            smallFont.setColor((i == stepIndex) ? new Color(0.10f, 0.10f, 0.12f, 1f) : Color.WHITE);
            drawButtonText(batch, moveNot, px, pillY, pillW, pillH);
        }

        // --- Card 4: Current Theory Text ---
        String currentMoveNotation = (stepIndex < currentOpening.getMoveNotations().size()) ? currentOpening.getMoveNotations().get(stepIndex) : "";
        String theoryHeader = (stepIndex < totalSteps)
                ? ("คำอธิบายตาเดิน: " + currentMoveNotation)
                : "จบสายเปิดเกมอย่างสมบูรณ์แบบ!";
        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, theoryHeader, cardX + 14f, theoryCardY + theoryCardH - 14f);

        String currentTheory = "";
        if (stepIndex < currentOpening.getMoveExplanations().size()) {
            currentTheory = currentOpening.getMoveExplanations().get(stepIndex);
        } else {
            currentTheory = "เข้าสู่ช่วงกลางเกม (Middlegame): หมากทุกตัวได้รับการพัฒนาอย่างมีประสิทธิภาพ พร้อมเปิดฉากสร้างความได้เปรียบ!";
        }
        font.setColor(new Color(0.90f, 0.92f, 0.98f, 1f));
        layout.setText(font, currentTheory, new Color(0.90f, 0.92f, 0.98f, 1f), cardW - 28f, Align.left, true);
        font.draw(batch, layout, cardX + 14f, theoryCardY + theoryCardH - 40f);

        // --- Card 5: Live Status Banner Text ---
        font.setColor(Color.WHITE);
        layout.setText(font, statusMessage);
        font.draw(batch, statusMessage, cardX + (cardW - layout.width) / 2f, statusBannerY + (statusBannerH + layout.height) / 2f);

        // --- Action Buttons Text ---
        font.setColor(Color.WHITE);
        drawButtonText(batch, showHint ? "ปิดคำใบ้" : "ดูคำใบ้", b1X, btnY1, b1W, btnH);
        drawButtonText(batch, "เริ่มใหม่", b2X, btnY1, b1W, btnH);
        drawButtonText(batch, "เล่นต่อกับ AI", b3X, btnY1, b1W, btnH);

        drawButtonText(batch, "เลือกสายเปิดเกม", b4X, btnY2, b2W, btnH);
        drawButtonText(batch, "กลับหน้าหลัก", b5X, btnY2, b2W, btnH);

        batch.end();

        // 12. Victory Modal Overlay
        if (isCompleted) {
            renderVictoryModal(screenW, screenH);
        }
    }

    private void renderVictoryModal(int screenW, int screenH) {
        float modalW = 500f;
        float modalH = 280f;
        float modalX = (screenW - modalW) / 2f;
        float modalY = (screenH - modalH) / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Dark dim backdrop
        shapes.setColor(0f, 0f, 0f, 0.65f);
        shapes.rect(0f, 0f, screenW, screenH);

        // Modal Box
        shapes.setColor(0.12f, 0.15f, 0.22f, 0.98f);
        shapes.rect(modalX, modalY, modalW, modalH);

        // Golden Accent Top Border
        shapes.setColor(0.95f, 0.75f, 0.20f, 1f);
        shapes.rect(modalX, modalY + modalH - 4f, modalW, 4f);

        // Action Buttons
        float btnW = 210f;
        float btnH = 42f;
        float btnY1 = modalY + 70f;
        float btnY2 = modalY + 18f;
        float b1X = modalX + 28f;
        float b2X = modalX + modalW - btnW - 28f;

        shapes.setColor(0.18f, 0.65f, 0.42f, 1f); // Play vs AI
        shapes.rect(b1X, btnY1, btnW, btnH);

        shapes.setColor(0.25f, 0.48f, 0.85f, 1f); // Next Opening
        shapes.rect(b2X, btnY1, btnW, btnH);

        shapes.setColor(0.35f, 0.40f, 0.50f, 1f); // Retry
        shapes.rect(b1X, btnY2, btnW, btnH);

        shapes.setColor(0.45f, 0.30f, 0.65f, 1f); // Select Opening
        shapes.rect(b2X, btnY2, btnW, btnH);

        // Draw 3 Golden Stars
        float starCenterY = modalY + modalH - 78f;
        drawStar(shapes, modalX + modalW / 2f - 36f, starCenterY, 14f, Color.GOLD);
        drawStar(shapes, modalX + modalW / 2f, starCenterY + 4f, 17f, Color.GOLD);
        drawStar(shapes, modalX + modalW / 2f + 36f, starCenterY, 14f, Color.GOLD);

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.35f, 0.42f, 0.58f, 0.90f);
        shapes.rect(modalX, modalY, modalW, modalH);
        shapes.end();

        // Modal Texts
        batch.begin();

        titleFont.setColor(Color.GOLD);
        String vTitle = "ยินดีด้วย! คุณเชี่ยวชาญสายนี้แล้ว";
        layout.setText(titleFont, vTitle);
        titleFont.draw(batch, vTitle, modalX + (modalW - layout.width) / 2f, modalY + modalH - 24f);

        font.setColor(Color.WHITE);
        String sub = currentOpening.getNameTh() + " (" + currentOpening.getNameEn() + ")";
        layout.setText(font, sub);
        font.draw(batch, sub, modalX + (modalW - layout.width) / 2f, modalY + modalH - 52f);

        smallFont.setColor(new Color(0.85f, 0.90f, 1.0f, 1f));
        String praise = "คุณได้เรียนรู้ขั้นตอนการเปิดเกมและเป้าหมายยุทธวิธีอย่างสมบูรณ์แบบ\nพร้อมนำไปใช้ลุยคว้าชัยชนะในกระดานจริง!";
        layout.setText(smallFont, praise, new Color(0.85f, 0.90f, 1.0f, 1f), modalW - 40f, Align.center, true);
        smallFont.draw(batch, layout, modalX + 20f, modalY + modalH - 105f);

        // Button Labels
        font.setColor(Color.WHITE);
        drawButtonText(batch, "เล่นต่อกับ AI", b1X, btnY1, btnW, btnH);
        drawButtonText(batch, "ฝึกสายถัดไป", b2X, btnY1, btnW, btnH);
        drawButtonText(batch, "ซ้อมใหม่อีกครั้ง", b1X, btnY2, btnW, btnH);
        drawButtonText(batch, "เลือกสายอื่น", b2X, btnY2, btnW, btnH);

        batch.end();
    }

    private void drawArrow(float fromX, float fromY, float toX, float toY, float shaftWidth, float headLength, float headWidth, Color color) {
        float dx = toX - fromX;
        float dy = toY - fromY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0.001f) return;

        float nx = dx / len;
        float ny = dy / len;

        float actualHeadLen = Math.min(headLength, len * 0.5f);
        float shaftLen = len - actualHeadLen;

        float shaftEndX = fromX + nx * shaftLen;
        float shaftEndY = fromY + ny * shaftLen;

        float px = -ny;
        float py = nx;

        float h1X = shaftEndX + px * (headWidth / 2f);
        float h1Y = shaftEndY + py * (headWidth / 2f);
        float h2X = shaftEndX - px * (headWidth / 2f);
        float h2Y = shaftEndY - py * (headWidth / 2f);

        shapes.setColor(color);
        shapes.rectLine(fromX, fromY, shaftEndX, shaftEndY, shaftWidth);
        shapes.triangle(toX, toY, h1X, h1Y, h2X, h2Y);
    }

    private void drawStar(ShapeRenderer sr, float cx, float cy, float radius, Color color) {
        sr.setColor(color);
        int points = 5;
        float innerRadius = radius * 0.42f;
        float[] vertices = new float[points * 2 * 2];
        for (int i = 0; i < points * 2; i++) {
            float r = (i % 2 == 0) ? radius : innerRadius;
            float angle = (float) (Math.PI / 2.0 - i * Math.PI / points);
            vertices[i * 2] = cx + (float) Math.cos(angle) * r;
            vertices[i * 2 + 1] = cy + (float) Math.sin(angle) * r;
        }
        for (int i = 0; i < points * 2; i++) {
            int next = (i + 1) % (points * 2);
            sr.triangle(cx, cy, vertices[i * 2], vertices[i * 2 + 1], vertices[next * 2], vertices[next * 2 + 1]);
        }
    }

    private Color getCategoryColor(String cat) {
        switch (cat) {
            case "Open Game": return new Color(0.18f, 0.68f, 0.38f, 1f);
            case "Semi-Open": return new Color(0.85f, 0.65f, 0.15f, 1f);
            case "Closed Game": return new Color(0.20f, 0.52f, 0.85f, 1f);
            case "Gambit": return new Color(0.88f, 0.42f, 0.20f, 1f);
            case "System": return new Color(0.55f, 0.35f, 0.80f, 1f);
            case "Flank": return new Color(0.18f, 0.65f, 0.68f, 1f);
            default: return new Color(0.40f, 0.45f, 0.55f, 1f);
        }
    }

    private void drawButtonText(SpriteBatch batch, String text, float x, float y, float w, float h) {
        layout.setText(font, text);
        float tx = x + (w - layout.width) / 2f;
        float ty = y + (h + layout.height) / 2f;
        font.draw(batch, text, tx, ty);
    }

    private Texture getPieceTexture(Piece p) {
        if (p == null) return null;
        String colorPrefix = (p.getColor() == Piece.Color.WHITE) ? "w_" : "b_";
        String typeKey = "P";
        if (p instanceof King) typeKey = "K";
        else if (p instanceof Queen) typeKey = "Q";
        else if (p instanceof Rook) typeKey = "R";
        else if (p instanceof Bishop) typeKey = "B";
        else if (p instanceof Knight) typeKey = "N";
        return pieceTex.get(colorPrefix + typeKey);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (headerFont != null) headerFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (coordFont != null) coordFont.dispose();
        if (boardTex != null) boardTex.dispose();
        if (backgroundTex != null) backgroundTex.dispose();
        for (Texture t : pieceTex.values()) {
            if (t != null) t.dispose();
        }
        pieceTex.clear();
    }
}
