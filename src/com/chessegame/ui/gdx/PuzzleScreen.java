package com.chessegame.ui.gdx;

import com.badlogic.gdx.Gdx;
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
import com.chessegame.ai.FENUtils;
import com.chessegame.audio.SoundManager;
import com.chessegame.level.Puzzle;
import com.chessegame.level.PuzzleManager;
import com.chessegame.logic.ChessUtils;
import com.chessegame.model.*;
import com.chessegame.particle.ParticleSystem;
import com.chessegame.particle.PieceGlideAnimation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PuzzleScreen: Interactive tactical chess puzzle training arena.
 * Displays curated puzzles (Mate in 1, Mate in 2, Forks, Pins), step-by-step move verification,
 * hint reveals, celebration confetti, and puzzle progress persistence.
 */
public class PuzzleScreen extends ScreenAdapter {

    private final LibGdxChessApp app;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont headerFont;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
    private BitmapFont coordFont;
    private final GlyphLayout layout = new GlyphLayout();

    private ParticleSystem particleSystem;
    private PieceGlideAnimation activeGlideAnim = null;

    private Texture boardTex;
    private Texture backgroundTex;
    private final Map<String, Texture> pieceTex = new HashMap<>();

    private int currentPuzzleIndex = 0;
    private Puzzle currentPuzzle;
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

    // Puzzle Solving State
    private int solutionStepIndex = 0; // Tracks which move in solutionMoves is expected
    private boolean isSolved = false;
    private boolean isFailed = false;
    private boolean showHint = false;
    private boolean hintUsed = false;
    private String statusMessage = "";
    private float statusTimer = 0f;
    private float stateTime = 0f;

    // Last Move Directional Arrow State
    private Position lastMoveFrom = null;
    private Position lastMoveTo = null;
    private Piece.Color lastMoveColor = Piece.Color.WHITE;

    private static final int HEADER_BAR_HEIGHT = 70;
    private static final int BUTTON_BAR_HEIGHT = 70;
    private static final int HORIZONTAL_MARGIN = 20;
    private static final int VERTICAL_MARGIN = 20;
    private static final int MAX_BOARD_SIZE = 720;
    private final float boardInsetPercent = 6.0f / 142.0f;

    public PuzzleScreen(LibGdxChessApp app) {
        this(app, 0);
    }

    public PuzzleScreen(LibGdxChessApp app, int initialPuzzleIndex) {
        this.app = app;
        this.currentPuzzleIndex = Math.max(0, Math.min(PuzzleManager.getAllPuzzles().size() - 1, initialPuzzleIndex));
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
            case "W_King.png": return "w_K.png";
            case "W_Queen.png": return "w_Q.png";
            case "W_Rook.png": return "w_R.png";
            case "W_Bishop.png": return "w_B.png";
            case "W_Knight.png": return "w_N.png";
            case "W_Pawn.png": return "w_P.png";
            case "B_King.png": return "b_K.png";
            case "B_Queen.png": return "b_Q.png";
            case "B_Rook.png": return "b_R.png";
            case "B_Bishop.png": return "b_B.png";
            case "B_Knight.png": return "b_N.png";
            case "B_Pawn.png": return "b_P.png";
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

        // Load Fonts
        try {
            com.badlogic.gdx.files.FileHandle ttfFile = resolveAsset("fonts/thai.ttf");
            if (ttfFile != null && ttfFile.exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttfFile);
                StringBuilder sb = new StringBuilder();
                for (char c = '\u0E01'; c <= '\u0E5B'; c++) sb.append(c);
                String extraChars = "•+-*!?:.()[]'\"%/#1234567890<>";

                // Header Font (26px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter hp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                hp.size = 26;
                hp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                hp.minFilter = Texture.TextureFilter.Linear;
                hp.magFilter = Texture.TextureFilter.Linear;
                hp.borderWidth = 1.4f;
                hp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                headerFont = gen.generateFont(hp);

                // Title Font (20px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter tp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                tp.size = 20;
                tp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                tp.minFilter = Texture.TextureFilter.Linear;
                tp.magFilter = Texture.TextureFilter.Linear;
                tp.borderWidth = 1.2f;
                tp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                titleFont = gen.generateFont(tp);

                // Body / Button Font (17px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter fp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                fp.size = 17;
                fp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                fp.minFilter = Texture.TextureFilter.Linear;
                fp.magFilter = Texture.TextureFilter.Linear;
                fp.borderWidth = 1.0f;
                fp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                font = gen.generateFont(fp);

                // Small Details Font (14px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter sp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                sp.size = 14;
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
                font = new BitmapFont();
                titleFont = new BitmapFont();
                smallFont = new BitmapFont();
                coordFont = new BitmapFont();
            }
        } catch (Exception e) {
            headerFont = new BitmapFont();
            font = new BitmapFont();
            titleFont = new BitmapFont();
            smallFont = new BitmapFont();
            coordFont = new BitmapFont();
        }

        String[][] pieceDefs = {
            {"K", "King"},
            {"Q", "Queen"},
            {"R", "Rook"},
            {"B", "Bishop"},
            {"N", "Knight"},
            {"P", "Pawn"}
        };
        for (String[] def : pieceDefs) {
            String k = def[0];
            String name = def[1];
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset("W_" + name + ".png");
                if (fh == null || !fh.exists()) fh = resolveAsset("w_" + k + ".png");
                if (fh != null && fh.exists()) {
                    Texture tex = new Texture(fh);
                    pieceTex.put("w_" + k, tex);
                    pieceTex.put("w_" + k.toLowerCase(), tex);
                }
            } catch (Exception ignored) {}
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset("B_" + name + ".png");
                if (fh == null || !fh.exists()) fh = resolveAsset("b_" + k + ".png");
                if (fh != null && fh.exists()) {
                    Texture tex = new Texture(fh);
                    pieceTex.put("b_" + k, tex);
                    pieceTex.put("b_" + k.toLowerCase(), tex);
                }
            } catch (Exception ignored) {}
        }
        try {
            com.badlogic.gdx.files.FileHandle fh = resolveAsset("board.png");
            if (fh != null) boardTex = new Texture(fh);
            com.badlogic.gdx.files.FileHandle bfh = resolveAsset("background.png");
            if (bfh != null) backgroundTex = new Texture(bfh);
        } catch (Exception ignored) {}

        loadPuzzle(currentPuzzleIndex);
        setupInput();
    }

    private void loadPuzzle(int index) {
        List<Puzzle> list = PuzzleManager.getAllPuzzles();
        if (index < 0) index = 0;
        if (index >= list.size()) index = list.size() - 1;
        currentPuzzleIndex = index;
        currentPuzzle = list.get(index);

        board = new Board();
        currentTurn = FENUtils.loadFromFEN(board, currentPuzzle.getFen());
        selected = null;
        legalMoves = new boolean[8][8];
        activeGlideAnim = null;
        clearDrag();
        lastMoveFrom = null;
        lastMoveTo = null;

        solutionStepIndex = 0;
        isSolved = false;
        isFailed = false;
        showHint = false;
        hintUsed = false;
        statusMessage = "ฝ่าย " + (currentPuzzle.getPlayerColor() == Piece.Color.WHITE ? "ขาว" : "ดำ") + " เป็นฝ่ายเริ่มเดิน!";
        statusTimer = 2.0f;
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.M) {
                    com.chessegame.audio.MusicManager.getInstance().toggleMute();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == com.badlogic.gdx.Input.Buttons.RIGHT) {
                    selected = null;
                    legalMoves = new boolean[8][8];
                    clearDrag();
                    return true;
                }

                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;

                int screenW = Gdx.graphics.getWidth();
                int screenH = Gdx.graphics.getHeight();

                // Top Header Buttons
                // Back Button (Left)
                if (x >= 20f && x <= 150f && y >= screenH - 54f && y <= screenH - 16f) {
                    app.setScreen(new MainMenuScreen(app));
                    return true;
                }

                // Bottom Control Buttons
                float bY = 16f;
                float bH = 40f;

                // Prev Puzzle Button
                if (x >= 20f && x <= 140f && y >= bY && y <= bY + bH) {
                    if (currentPuzzleIndex > 0) {
                        loadPuzzle(currentPuzzleIndex - 1);
                    }
                    return true;
                }

                // Next Puzzle Button
                if (x >= 150f && x <= 270f && y >= bY && y <= bY + bH) {
                    if (currentPuzzleIndex < PuzzleManager.getAllPuzzles().size() - 1) {
                        loadPuzzle(currentPuzzleIndex + 1);
                    }
                    return true;
                }

                // Reset Button
                if (x >= 280f && x <= 410f && y >= bY && y <= bY + bH) {
                    loadPuzzle(currentPuzzleIndex);
                    return true;
                }

                // Hint Button
                if (x >= 420f && x <= 560f && y >= bY && y <= bY + bH) {
                    showHint = !showHint;
                    hintUsed = true;
                    return true;
                }

                if (isSolved || (activeGlideAnim != null && !activeGlideAnim.isFinished())) {
                    return false;
                }

                Position pos = getBoardPosition(x, y);
                if (pos == null) {
                    selected = null;
                    legalMoves = new boolean[8][8];
                    clearDrag();
                    return false;
                }

                Piece p = board.getPiece(pos);

                // Click-to-move destination: if piece already selected and clicking legal target
                if (selected != null && legalMoves[pos.row][pos.col]) {
                    handlePlayerMove(selected, pos, false);
                    selected = null;
                    legalMoves = new boolean[8][8];
                    clearDrag();
                    return true;
                }

                // Click on own piece: select and prime for potential drag
                if (p != null && p.getColor() == currentTurn) {
                    selected = pos;
                    computeLegalMoves(selected);
                    dragSource = pos;
                    draggedPiece = p;
                    dragStartX = x;
                    dragStartY = y;
                    dragCurrentX = x;
                    dragCurrentY = y;
                    isDragging = false;
                    return true;
                }

                selected = null;
                legalMoves = new boolean[8][8];
                clearDrag();
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (dragSource == null || draggedPiece == null) return false;
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;
                dragCurrentX = x;
                dragCurrentY = y;
                if (!isDragging) {
                    if (Math.hypot(x - dragStartX, y - dragStartY) > 6.0f) {
                        isDragging = true;
                    }
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (dragSource == null) return false;
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;

                if (isDragging) {
                    Position targetPos = getBoardPosition(x, y);
                    if (targetPos != null && !targetPos.equals(dragSource) && legalMoves[targetPos.row][targetPos.col]) {
                        handlePlayerMove(dragSource, targetPos, true);
                        selected = null;
                        legalMoves = new boolean[8][8];
                    }
                    clearDrag();
                    return true;
                } else {
                    clearDrag();
                    return false;
                }
            }
        });
    }

    private void onSquareClick(int row, int col) {
        Position clicked = new Position(row, col);

        if (selected == null) {
            Piece p = board.getPiece(clicked);
            if (p != null && p.getColor() == currentTurn) {
                selected = clicked;
                computeLegalMoves(selected);
            }
        } else {
            if (legalMoves[row][col]) {
                handlePlayerMove(selected, clicked, false);
                selected = null;
                legalMoves = new boolean[8][8];
            } else {
                Piece p = board.getPiece(clicked);
                if (p != null && p.getColor() == currentTurn) {
                    selected = clicked;
                    computeLegalMoves(selected);
                } else {
                    selected = null;
                    legalMoves = new boolean[8][8];
                }
            }
        }
    }

    private void handlePlayerMove(Position from, Position to) {
        handlePlayerMove(from, to, false);
    }

    private void handlePlayerMove(Position from, Position to, boolean fromDrag) {
        Piece moving = board.getPiece(from);
        Piece promoChoice = null;
        if (moving instanceof Pawn && (to.row == 0 || to.row == 7)) {
            promoChoice = new Queen(moving.getColor());
        }

        String moveUci = FENUtils.toUCIMove(from, to, promoChoice);
        List<String> expectedMoves = currentPuzzle.getSolutionMoves();

        if (solutionStepIndex < expectedMoves.size() && expectedMoves.get(solutionStepIndex).equalsIgnoreCase(moveUci)) {
            // Correct Move!
            Piece captured = board.getPiece(to);
            startGlide(moving, from, to, fromDrag);
            board.moveRecord(from, to, currentTurn, promoChoice);
            lastMoveFrom = from;
            lastMoveTo = to;
            lastMoveColor = moving.getColor();
            if (captured != null) {
                SoundManager.getInstance().playCaptureSound();
            } else {
                SoundManager.getInstance().playMoveSound();
            }

            solutionStepIndex++;

            if (solutionStepIndex >= expectedMoves.size()) {
                // Puzzle Fully Solved!
                isSolved = true;
                isFailed = false;
                statusMessage = "[สำเร็จ!] แก้ปริศนาสำเร็จ!";
                int stars = hintUsed ? 2 : 3;
                PuzzleManager.recordPuzzleSolved(currentPuzzle.getId(), stars);
                SoundManager.getInstance().playVictorySound();

                // Launch Victory Confetti
                particleSystem.emitVictoryConfetti(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            } else {
                // Opponent Response Step
                final String oppMoveUci = expectedMoves.get(solutionStepIndex);
                solutionStepIndex++;
                statusMessage = "ถูกต้อง! กำลังตอบโต้...";

                new Thread(() -> {
                    try {
                        Thread.sleep(450L);
                    } catch (InterruptedException ignored) {}

                    Gdx.app.postRunnable(() -> {
                        Position oppFrom = FENUtils.fromUCISquare(oppMoveUci.substring(0, 2));
                        Position oppTo = FENUtils.fromUCISquare(oppMoveUci.substring(2, 4));
                        if (oppFrom != null && oppTo != null) {
                            Piece oppPiece = board.getPiece(oppFrom);
                            Piece oppCap = board.getPiece(oppTo);
                            startGlide(oppPiece, oppFrom, oppTo, false);
                            Piece.Color oppColor = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                            board.moveRecord(oppFrom, oppTo, oppColor, null);
                            lastMoveFrom = oppFrom;
                            lastMoveTo = oppTo;
                            lastMoveColor = oppColor;
                            if (oppCap != null) {
                                SoundManager.getInstance().playCaptureSound();
                            } else {
                                SoundManager.getInstance().playMoveSound();
                            }
                            statusMessage = "ถึงตาคุณเดินต่อเพื่อปิดเกม!";
                        }
                    });
                }).start();
            }
        } else {
            // Incorrect Move!
            isFailed = true;
            statusMessage = "ยังไม่ใช่ตาที่ดีที่สุด! บอร์ดจะรีเซ็ตในไม่ช้า...";
            SoundManager.getInstance().playCheckSound();

            new Thread(() -> {
                try {
                    Thread.sleep(800L);
                } catch (InterruptedException ignored) {}
                Gdx.app.postRunnable(() -> {
                    loadPuzzle(currentPuzzleIndex);
                    statusMessage = "ลองใหม่อีกครั้ง!";
                });
            }).start();
        }
    }

    private void startGlide(Piece piece, Position from, Position to) {
        startGlide(piece, from, to, false);
    }

    private void startGlide(Piece piece, Position from, Position to, boolean fromDrag) {
        if (piece == null || from == null || to == null) return;
        float boardSize = getBoardSize();
        float offsetX = getOffsetX(boardSize);
        float offsetY = getOffsetY();
        float inset = boardInsetPercent * boardSize;
        float innerX = offsetX + inset;
        float innerY = offsetY + inset;
        float innerSquare = (boardSize - 2f * inset) / 8f;

        float fromX = fromDrag ? (dragCurrentX - innerSquare / 2f) : (innerX + from.col * innerSquare);
        float fromY = fromDrag ? (dragCurrentY - innerSquare / 2f) : (innerY + (7 - from.row) * innerSquare);
        float toX = innerX + to.col * innerSquare;
        float toY = innerY + (7 - to.row) * innerSquare;
        boolean isCapture = board.getPiece(to) != null;
        float duration = fromDrag ? 0.08f : 0.20f;

        activeGlideAnim = new PieceGlideAnimation(piece, from, to, fromX, fromY, toX, toY, isCapture, duration);
    }

    private void computeLegalMoves(Position pos) {
        legalMoves = new boolean[8][8];
        Piece p = board.getPiece(pos);
        if (p == null || p.getColor() != currentTurn) return;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position to = new Position(r, c);
                if (ChessUtils.isLegalMove(board, pos, to, currentTurn)) {
                    legalMoves[r][c] = true;
                }
            }
        }
    }

    private float getBoardSize() {
        int availW = Gdx.graphics.getWidth() - 360; // Leave space for side puzzle info card
        int availH = Gdx.graphics.getHeight() - HEADER_BAR_HEIGHT - BUTTON_BAR_HEIGHT - (VERTICAL_MARGIN * 2);
        return Math.max(8, Math.min(MAX_BOARD_SIZE, Math.min(availW, availH)));
    }

    private float getOffsetX(float boardSize) {
        return 20f;
    }

    private float getOffsetY() {
        return VERTICAL_MARGIN + BUTTON_BAR_HEIGHT;
    }

    private Position getBoardPosition(float worldX, float worldY) {
        float boardSize = getBoardSize();
        float offsetX = getOffsetX(boardSize);
        float offsetY = getOffsetY();
        float inset = boardInsetPercent * boardSize;
        float innerX = offsetX + inset;
        float innerY = offsetY + inset;
        float innerSquare = (boardSize - 2f * inset) / 8f;

        int col = (int) Math.floor((worldX - innerX) / innerSquare);
        int localRow = (int) Math.floor((worldY - innerY) / innerSquare);
        int row = 7 - localRow;

        if (col >= 0 && col < 8 && row >= 0 && row < 8) {
            return new Position(row, col);
        }
        return null;
    }

    private void clearDrag() {
        isDragging = false;
        dragSource = null;
        draggedPiece = null;
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

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (camera == null) camera = new OrthographicCamera();
        camera.setToOrtho(false, screenW, screenH);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // Background
        if (backgroundTex != null) {
            batch.begin();
            batch.setColor(0.35f, 0.35f, 0.40f, 1f);
            batch.draw(backgroundTex, 0f, 0f, screenW, screenH);
            batch.setColor(Color.WHITE);
            batch.end();
        }

        // --- Render Board & Highlights ---
        float boardSize = getBoardSize();
        float offsetX = getOffsetX(boardSize);
        float offsetY = getOffsetY();
        float inset = boardInsetPercent * boardSize;
        float innerX = offsetX + inset;
        float innerY = offsetY + inset;
        float innerSquare = (boardSize - 2f * inset) / 8f;

        // Board Shadow / Frame
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.05f, 0.05f, 0.08f, 0.8f);
        shapes.rect(offsetX - 4f, offsetY - 4f, boardSize + 8f, boardSize + 8f);
        shapes.end();

        // Draw Board Texture
        if (boardTex != null) {
            batch.begin();
            batch.draw(boardTex, offsetX, offsetY, boardSize, boardSize);
            batch.end();
        }

        // Draw Square Highlights
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Highlight Origin & Destination Squares of Last Move (1 Move Back)
        if (lastMoveFrom != null && lastMoveTo != null) {
            shapes.setColor(0.95f, 0.82f, 0.25f, 0.32f);
            float fx = innerX + lastMoveFrom.col * innerSquare;
            float fy = innerY + (7 - lastMoveFrom.row) * innerSquare;
            shapes.rect(fx, fy, innerSquare, innerSquare);

            float tx = innerX + lastMoveTo.col * innerSquare;
            float ty = innerY + (7 - lastMoveTo.row) * innerSquare;
            shapes.rect(tx, ty, innerSquare, innerSquare);
        }

        if (selected != null) {
            shapes.setColor(0.2f, 0.85f, 0.45f, 0.45f);
            shapes.rect(innerX + selected.col * innerSquare, innerY + (7 - selected.row) * innerSquare, innerSquare, innerSquare);
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (legalMoves[r][c]) {
                    float sqX = innerX + c * innerSquare;
                    float sqY = innerY + (7 - r) * innerSquare;
                    Piece target = board.getPiece(new Position(r, c));
                    if (target != null) {
                        shapes.setColor(0.95f, 0.25f, 0.25f, 0.55f);
                        shapes.rect(sqX, sqY, innerSquare, innerSquare);
                    } else {
                        shapes.setColor(0.15f, 0.85f, 0.4f, 0.65f);
                        shapes.circle(sqX + innerSquare / 2f, sqY + innerSquare / 2f, innerSquare * 0.16f);
                    }
                }
            }
        }

        // Origin square highlight while dragging
        if (isDragging && dragSource != null) {
            shapes.setColor(1.0f, 0.9f, 0.3f, 0.35f);
            shapes.rect(innerX + dragSource.col * innerSquare, innerY + (7 - dragSource.row) * innerSquare, innerSquare, innerSquare);
        }

        // Target hover highlight while dragging
        if (isDragging) {
            Position hoverPos = getBoardPosition(dragCurrentX, dragCurrentY);
            if (hoverPos != null && !hoverPos.equals(dragSource)) {
                if (legalMoves[hoverPos.row][hoverPos.col]) {
                    shapes.setColor(0.2f, 0.85f, 0.35f, 0.38f);
                } else {
                    shapes.setColor(0.85f, 0.25f, 0.25f, 0.22f);
                }
                shapes.rect(innerX + hoverPos.col * innerSquare, innerY + (7 - hoverPos.row) * innerSquare, innerSquare, innerSquare);
            }
        }
        shapes.end();

        // Draw Pieces
        batch.begin();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position p = new Position(r, c);
                Piece piece = board.getPiece(p);
                if (piece != null) {
                    if (activeGlideAnim != null && activeGlideAnim.getPiece() == piece) {
                        continue; // drawn via glide animation
                    }
                    if (isDragging && dragSource != null && r == dragSource.row && c == dragSource.col) {
                        continue; // drawn floating under cursor
                    }
                    String key = (piece.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(piece.getSymbol());
                    Texture tex = pieceTex.get(key);
                    if (tex != null) {
                        float sqX = innerX + c * innerSquare;
                        float sqY = innerY + (7 - r) * innerSquare;
                        float pieceBaseSize = innerSquare * 0.82f;
                        float textureRatio = (float) tex.getHeight() / tex.getWidth();
                        float drawWidth = pieceBaseSize;
                        float drawHeight = pieceBaseSize * textureRatio;
                        float drawX = sqX + (innerSquare - drawWidth) / 2f;
                        float drawY = sqY + (innerSquare - pieceBaseSize) / 2f;
                        batch.draw(tex, drawX, drawY, drawWidth, drawHeight);
                    }
                }
            }
        }
        batch.end();

        // Directional Arrow for Last Move (1 Move Back)
        if (lastMoveFrom != null && lastMoveTo != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);

            float pFromX = innerX + (lastMoveFrom.col + 0.5f) * innerSquare;
            float pFromY = innerY + (7 - lastMoveFrom.row + 0.5f) * innerSquare;
            float pToX = innerX + (lastMoveTo.col + 0.5f) * innerSquare;
            float pToY = innerY + (7 - lastMoveTo.row + 0.5f) * innerSquare;

            Color arrowColor = (lastMoveColor == Piece.Color.BLACK)
                    ? new Color(0.18f, 0.82f, 0.98f, 0.85f)  // Cyan
                    : new Color(1.0f, 0.78f, 0.18f, 0.85f);  // Amber Gold

            drawDirectionalArrow(pFromX, pFromY, pToX, pToY, arrowColor);

            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        batch.begin();

        // Draw Active Gliding Piece
        if (activeGlideAnim != null) {
            Piece gp = activeGlideAnim.getPiece();
            String key = (gp.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(gp.getSymbol());
            Texture tex = pieceTex.get(key);
            if (tex != null) {
                float pieceBaseSize = innerSquare * 0.82f;
                float textureRatio = (float) tex.getHeight() / tex.getWidth();
                float drawWidth = pieceBaseSize;
                float drawHeight = pieceBaseSize * textureRatio;
                batch.draw(tex, activeGlideAnim.getCurrentX(), activeGlideAnim.getCurrentY(), drawWidth, drawHeight);
            }
        }

        // Draw Dragged Piece floating under cursor
        if (isDragging && draggedPiece != null) {
            String key = (draggedPiece.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(draggedPiece.getSymbol());
            Texture tex = pieceTex.get(key);
            if (tex != null) {
                float pieceBaseSize = innerSquare * 0.95f;
                float textureRatio = (float) tex.getHeight() / tex.getWidth();
                float drawWidth = pieceBaseSize;
                float drawHeight = pieceBaseSize * textureRatio;
                float drawX = dragCurrentX - drawWidth / 2f;
                float drawY = dragCurrentY - drawHeight / 2f;
                batch.draw(tex, drawX, drawY, drawWidth, drawHeight);
            }
        }
        batch.end();

        // Particles (e.g. Confetti)
        particleSystem.render(shapes);

        // --- Side Info Card (Right Side) ---
        float sideCardX = offsetX + boardSize + 24f;
        float sideCardW = screenW - sideCardX - 24f;
        float sideCardY = offsetY;
        float sideCardH = boardSize;

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Card Main Background
        shapes.setColor(0.10f, 0.11f, 0.16f, 0.95f);
        shapes.rect(sideCardX, sideCardY, sideCardW, sideCardH);

        // Section 1: Category Header Banner
        float catH = 46f;
        float catY = sideCardY + sideCardH - catH;
        shapes.setColor(isSolved ? new Color(0.14f, 0.65f, 0.38f, 1f) : new Color(0.20f, 0.45f, 0.85f, 1f));
        shapes.rect(sideCardX, catY, sideCardW, catH);

        // Section 2: Title & Stars Card
        float titleCardY = catY - 74f;
        float titleCardH = 66f;
        shapes.setColor(0.14f, 0.16f, 0.23f, 0.95f);
        shapes.rect(sideCardX + 12f, titleCardY, sideCardW - 24f, titleCardH);
        shapes.setColor(0.95f, 0.75f, 0.25f, 0.85f);
        shapes.rect(sideCardX + 12f, titleCardY, 4f, titleCardH);

        // Section 3: Mission / Objective Card
        float missionCardY = titleCardY - 84f;
        float missionCardH = 76f;
        shapes.setColor(0.13f, 0.15f, 0.22f, 0.95f);
        shapes.rect(sideCardX + 12f, missionCardY, sideCardW - 24f, missionCardH);
        shapes.setColor(0.20f, 0.75f, 0.90f, 0.85f);
        shapes.rect(sideCardX + 12f, missionCardY, 4f, missionCardH);

        // Section 4: Live Turn & Status Banner
        float statusCardY = missionCardY - 68f;
        float statusCardH = 60f;
        if (isSolved) {
            shapes.setColor(0.12f, 0.50f, 0.28f, 0.95f); // Victory Green
        } else if (isFailed) {
            shapes.setColor(0.55f, 0.18f, 0.18f, 0.95f); // Failure Red
        } else {
            shapes.setColor(0.18f, 0.24f, 0.38f, 0.95f); // Turn Active Blue
        }
        shapes.rect(sideCardX + 12f, statusCardY, sideCardW - 24f, statusCardH);

        // Section 5: Hint Card (if showHint)
        float hintCardY = statusCardY - 84f;
        float hintCardH = 76f;
        if (showHint) {
            shapes.setColor(0.24f, 0.19f, 0.10f, 0.95f);
            shapes.rect(sideCardX + 12f, hintCardY, sideCardW - 24f, hintCardH);
            shapes.setColor(0.95f, 0.70f, 0.20f, 0.9f);
            shapes.rect(sideCardX + 12f, hintCardY, 4f, hintCardH);
        }

        // Section 6: Explanation Card (if isSolved)
        float explainCardY = (showHint ? hintCardY : statusCardY) - 84f;
        float explainCardH = 76f;
        if (isSolved) {
            shapes.setColor(0.10f, 0.26f, 0.18f, 0.95f);
            shapes.rect(sideCardX + 12f, explainCardY, sideCardW - 24f, explainCardH);
            shapes.setColor(0.30f, 0.85f, 0.45f, 0.9f);
            shapes.rect(sideCardX + 12f, explainCardY, 4f, explainCardH);
        }

        // --- Top Header Bar & Bottom Action Bar ---
        // Top Header
        shapes.setColor(0.10f, 0.11f, 0.16f, 0.95f);
        shapes.rect(0f, screenH - HEADER_BAR_HEIGHT, screenW, HEADER_BAR_HEIGHT);
        shapes.setColor(0.95f, 0.75f, 0.25f, 0.9f);
        shapes.rect(0f, screenH - HEADER_BAR_HEIGHT, screenW, 3f);

        // Back Button
        shapes.setColor(0.28f, 0.34f, 0.46f, 1f);
        shapes.rect(20f, screenH - 54f, 130f, 38f);

        // Bottom Action Bar
        shapes.setColor(0.10f, 0.11f, 0.16f, 0.95f);
        shapes.rect(0f, 0f, screenW, BUTTON_BAR_HEIGHT);

        float bY = 16f;
        float bH = 40f;

        // Button: Prev (w: 120)
        shapes.setColor(currentPuzzleIndex > 0 ? new Color(0.22f, 0.45f, 0.75f, 1f) : new Color(0.20f, 0.22f, 0.28f, 1f));
        shapes.rect(20f, bY, 120f, bH);

        // Button: Next (w: 120)
        shapes.setColor(currentPuzzleIndex < PuzzleManager.getAllPuzzles().size() - 1 ? new Color(0.22f, 0.45f, 0.75f, 1f) : new Color(0.20f, 0.22f, 0.28f, 1f));
        shapes.rect(150f, bY, 120f, bH);

        // Button: Reset (w: 130)
        shapes.setColor(0.85f, 0.48f, 0.18f, 1f);
        shapes.rect(280f, bY, 130f, bH);

        // Button: Hint (w: 140)
        shapes.setColor(showHint ? new Color(0.25f, 0.65f, 0.45f, 1f) : new Color(0.18f, 0.65f, 0.75f, 1f));
        shapes.rect(420f, bY, 140f, bH);

        shapes.end();

        // --- Render Text & Labels ---
        batch.begin();

        // 1. Category Tag Text (Centered in header banner)
        titleFont.setColor(Color.WHITE);
        String catText = "หมวด: " + currentPuzzle.getCategory();
        layout.setText(titleFont, catText);
        titleFont.draw(batch, catText, sideCardX + (sideCardW - layout.width) / 2f, catY + (catH + layout.height) / 2f);

        // 2. Puzzle Title & Rating Text
        titleFont.setColor(Color.GOLD);
        String titleText = "ข้อที่ " + currentPuzzle.getId() + ": " + currentPuzzle.getTitle();
        titleFont.draw(batch, titleText, sideCardX + 26f, titleCardY + titleCardH - 14f);

        int stars = PuzzleManager.getPuzzleStars(currentPuzzle.getId());
        smallFont.setColor(stars > 0 ? Color.GOLD : new Color(0.70f, 0.72f, 0.80f, 1f));
        String starLabel = (stars > 0) ? ("สถานะ: ผ่านแล้ว (" + stars + " / 3 ดาว ★)") : "สถานะ: ยังไม่ผ่าน";
        smallFont.draw(batch, starLabel, sideCardX + 26f, titleCardY + 22f);

        // 3. Mission / Objective Text
        font.setColor(new Color(0.35f, 0.85f, 1.0f, 1f));
        font.draw(batch, "🎯 ภารกิจ:", sideCardX + 26f, missionCardY + missionCardH - 14f);

        font.setColor(Color.WHITE);
        font.draw(batch, currentPuzzle.getDescription(), sideCardX + 26f, missionCardY + missionCardH - 42f, sideCardW - 52f, Align.left, true);

        // 4. Status Message Text (Turn & Result)
        font.setColor(Color.WHITE);
        layout.setText(font, statusMessage);
        font.draw(batch, statusMessage, sideCardX + (sideCardW - layout.width) / 2f, statusCardY + (statusCardH + layout.height) / 2f);

        // 5. Hint Text (if showHint)
        if (showHint) {
            smallFont.setColor(new Color(1f, 0.85f, 0.35f, 1f));
            smallFont.draw(batch, "💡 คำใบ้:", sideCardX + 26f, hintCardY + hintCardH - 12f);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, currentPuzzle.getHint(), sideCardX + 26f, hintCardY + hintCardH - 34f, sideCardW - 52f, Align.left, true);
        }

        // 6. Explanation Text (if isSolved)
        if (isSolved) {
            smallFont.setColor(new Color(0.4f, 0.95f, 0.6f, 1f));
            smallFont.draw(batch, "📖 เฉลยแท็กติก:", sideCardX + 26f, explainCardY + explainCardH - 12f);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, currentPuzzle.getExplanation(), sideCardX + 26f, explainCardY + explainCardH - 34f, sideCardW - 52f, Align.left, true);
        }

        // Header Bar Titles & Stats
        headerFont.setColor(Color.GOLD);
        headerFont.draw(batch, "โหมดแก้ปริศนาหมากรุก (Chess Puzzles)", 175f, screenH - 26f);

        int totalSolved = PuzzleManager.getTotalSolved();
        int totalStars = PuzzleManager.getTotalPuzzleStars();
        font.setColor(new Color(0.95f, 0.95f, 1f, 1f));
        String statsText = "แก้สำเร็จ: " + totalSolved + " / " + PuzzleManager.getAllPuzzles().size() + "   |   สะสม: " + totalStars + " ดาว";
        layout.setText(font, statsText);
        font.draw(batch, statsText, screenW - layout.width - 24f, screenH - 28f);

        // Back Button Text (Centered)
        font.setColor(Color.WHITE);
        String backBtnText = "◀ เมนูหลัก";
        layout.setText(font, backBtnText);
        font.draw(batch, backBtnText, 20f + (130f - layout.width) / 2f, screenH - 54f + (38f + layout.height) / 2f);

        // Bottom Action Buttons Text (All centered with GlyphLayout)
        String prevText = "◀ ก่อนหน้า";
        layout.setText(font, prevText);
        font.setColor(currentPuzzleIndex > 0 ? Color.WHITE : new Color(0.60f, 0.62f, 0.68f, 1f));
        font.draw(batch, prevText, 20f + (120f - layout.width) / 2f, bY + (bH + layout.height) / 2f);

        String nextText = "ถัดไป ▶";
        layout.setText(font, nextText);
        font.setColor(currentPuzzleIndex < PuzzleManager.getAllPuzzles().size() - 1 ? Color.WHITE : new Color(0.60f, 0.62f, 0.68f, 1f));
        font.draw(batch, nextText, 150f + (120f - layout.width) / 2f, bY + (bH + layout.height) / 2f);

        String resetText = "🔄 เริ่มใหม่";
        layout.setText(font, resetText);
        font.setColor(Color.WHITE);
        font.draw(batch, resetText, 280f + (130f - layout.width) / 2f, bY + (bH + layout.height) / 2f);

        String hintBtnText = showHint ? "💡 ซ่อนคำใบ้" : "💡 ขอคำใบ้";
        layout.setText(font, hintBtnText);
        font.setColor(Color.WHITE);
        font.draw(batch, hintBtnText, 420f + (140f - layout.width) / 2f, bY + (bH + layout.height) / 2f);

        batch.end();
    }

    private void drawDirectionalArrow(float fromX, float fromY, float toX, float toY, Color mainColor) {
        float dx = toX - fromX;
        float dy = toY - fromY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0.001f) return;

        float nx = dx / len;
        float ny = dy / len;

        float startOffset = Math.min(14f, len * 0.15f);
        float startX = fromX + nx * startOffset;
        float startY = fromY + ny * startOffset;

        float endOffset = Math.min(6f, len * 0.08f);
        float endX = toX - nx * endOffset;
        float endY = toY - ny * endOffset;

        float effectiveLen = len - startOffset - endOffset;
        if (effectiveLen <= 5f) return;

        float shaftWidth = 6.5f;
        float headLen = Math.min(22f, effectiveLen * 0.45f);
        float headWidth = 24f;

        Color outlineColor = new Color(0.04f, 0.05f, 0.07f, 0.50f);
        drawSingleArrow(startX - nx * 1.5f, startY - ny * 1.5f, endX + nx * 2.5f, endY + ny * 2.5f,
                nx, ny, effectiveLen + 4.0f, shaftWidth + 4.5f, headLen + 3.0f, headWidth + 6.0f, outlineColor);

        drawSingleArrow(startX, startY, endX, endY, nx, ny, effectiveLen, shaftWidth, headLen, headWidth, mainColor);
    }

    private void drawSingleArrow(float startX, float startY, float endX, float endY, float nx, float ny, float len,
                                 float shaftWidth, float headLength, float headWidth, Color color) {
        float shaftLen = len - headLength;
        float shaftEndX = startX + nx * shaftLen;
        float shaftEndY = startY + ny * shaftLen;

        float px = -ny;
        float py = nx;

        float h1X = shaftEndX + px * (headWidth / 2f);
        float h1Y = shaftEndY + py * (headWidth / 2f);
        float h2X = shaftEndX - px * (headWidth / 2f);
        float h2Y = shaftEndY - py * (headWidth / 2f);

        shapes.setColor(color);
        shapes.circle(startX, startY, shaftWidth / 2f);
        shapes.rectLine(startX, startY, shaftEndX, shaftEndY, shaftWidth);
        shapes.triangle(endX, endY, h1X, h1Y, h2X, h2Y);
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.setToOrtho(false, width, height);
            camera.update();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        if (headerFont != null) headerFont.dispose();
        font.dispose();
        titleFont.dispose();
        smallFont.dispose();
        coordFont.dispose();
        if (backgroundTex != null) backgroundTex.dispose();
        if (boardTex != null) boardTex.dispose();
        java.util.Set<Texture> disposed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Texture t : pieceTex.values()) {
            if (t != null && disposed.add(t)) t.dispose();
        }
    }
}
