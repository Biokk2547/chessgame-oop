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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessegame.ai.ChessAI;
import com.chessegame.ai.Evaluation;
import com.chessegame.ai.GameReviewReport;
import com.chessegame.ai.GameReviewer;
import com.chessegame.character.CharacterAnimator;
import com.chessegame.level.BossLevel;
import com.chessegame.model.Board;
import com.chessegame.model.MoveRecord;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;
import com.chessegame.particle.ParticleSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameReviewScreen: Comprehensive Grandmaster Post-Match Analysis Screen.
 * Features:
 * 1. Side Evaluation Bar (Chess.com/Lichess Style) with centipawn & mate calculations
 * 2. Vector Neon Best Move Arrows & Move Quality Indicators
 * 3. Dual-Tab Right Panel: [ Move Analysis ] & [ Move Classification Breakdown Table ]
 * 4. Interactive Clickable Move Ribbon/List with quality badges
 * 5. Keyboard Navigation (Arrows, Space, Home, End, Tab) & Speed Stepper (1x, 1.5x, 2x)
 * 6. Dynamic Boss Profile Integration (Avatar, Name, Title, and Dialogues)
 */
public class GameReviewScreen extends ScreenAdapter {

    public enum ReviewTab {
        ANALYSIS,
        STATISTICS
    }

    private final LibGdxChessApp app;
    private final List<MoveRecord> originalMoves;
    private final boolean isVsAi;
    private final int aiDepth;
    private final Evaluation.AIStyle aiStyle;
    private final BossLevel bossLevel;

    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;

    private Texture boardTex;
    private final Map<String, Texture> pieceTex = new HashMap<>();
    private final Texture[] vampireTextures = new Texture[4];
    private final Texture[] evilBoxTextures = new Texture[12];
    private Texture bossAvatarTex;
    private CharacterAnimator vampireAnimator;
    private CharacterAnimator evilBoxAnimator;
    private ParticleSystem particleSystem;

    // Review state
    private boolean isAnalyzing = true;
    private int analyzeProgress = 0;
    private int analyzeTotal = 0;
    private GameReviewReport report;
    private int currentMoveIdx = -1; // -1 = initial start position before any move
    private Board displayBoard = new Board();

    // Tab state
    private ReviewTab currentTab = ReviewTab.ANALYSIS;

    // Auto-play state & Speed Stepper
    private boolean isAutoPlaying = false;
    private float autoPlayTimer = 0f;
    private int speedIndex = 0;
    private final float[] speeds = {1.5f, 1.0f, 0.5f};
    private final String[] speedLabels = {"1.0x", "1.5x", "2.0x"};

    // Move Ribbon Paging
    private int moveListPage = 0;
    private static final int MOVES_PER_PAGE = 8;

    private float stateTime = 0f;
    private static final float MAX_BOARD_SIZE = 740f;
    private final float boardInsetPercent = 6.0f / 142.0f;

    public GameReviewScreen(LibGdxChessApp app, List<MoveRecord> moves, boolean isVsAi, int aiDepth, Evaluation.AIStyle aiStyle) {
        this(app, moves, isVsAi, aiDepth, aiStyle, null);
    }

    public GameReviewScreen(LibGdxChessApp app, List<MoveRecord> moves, boolean isVsAi, int aiDepth, Evaluation.AIStyle aiStyle, BossLevel bossLevel) {
        this.app = app;
        this.originalMoves = (moves != null) ? new ArrayList<>(moves) : new ArrayList<>();
        this.isVsAi = isVsAi;
        this.aiDepth = aiDepth;
        this.aiStyle = aiStyle;
        this.bossLevel = bossLevel;
        this.analyzeTotal = this.originalMoves.size();
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
        if (name.contains("characters/")) {
            return resolveAsset(name.replace("characters/", "charactor/"));
        } else if (name.contains("charactor/")) {
            return resolveAsset(name.replace("charactor/", "characters/"));
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

        // Load FreeType Fonts
        try {
            com.badlogic.gdx.files.FileHandle ttfFile = resolveAsset("fonts/thai.ttf");
            if (ttfFile != null && ttfFile.exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttfFile);
                StringBuilder sb = new StringBuilder();
                for (char c = '\u0E01'; c <= '\u0E5B'; c++) sb.append(c);
                String extraChars = "•+-*!?:.()[]'\"%/#1234567890<>";

                FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
                p.size = 20;
                p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                p.minFilter = Texture.TextureFilter.Linear;
                p.magFilter = Texture.TextureFilter.Linear;
                font = gen.generateFont(p);

                p.size = 24;
                p.borderWidth = 1.0f;
                p.borderColor = new Color(0.05f, 0.05f, 0.08f, 0.95f);
                titleFont = gen.generateFont(p);

                p.size = 14;
                p.borderWidth = 0.5f;
                smallFont = gen.generateFont(p);

                gen.dispose();
            }
        } catch (Throwable ignored) {}

        if (font == null) font = new BitmapFont();
        if (titleFont == null) titleFont = new BitmapFont();
        if (smallFont == null) smallFont = new BitmapFont();

        // Load Board texture
        try {
            com.badlogic.gdx.files.FileHandle bfh = resolveAsset("board.png");
            if (bfh != null && bfh.exists()) boardTex = new Texture(bfh);
        } catch (Throwable ignored) {}

        // Load Piece textures
        String[][] pieceDefs = {
            {"K", "King"},
            {"Q", "Queen"},
            {"R", "Rook"},
            {"B", "Bishop"},
            {"N", "Knight"},
            {"P", "Pawn"}
        };
        for (String[] def : pieceDefs) {
            String t = def[0];
            String name = def[1];
            try {
                com.badlogic.gdx.files.FileHandle pfh = resolveAsset("W_" + name + ".png");
                if (pfh == null || !pfh.exists()) pfh = resolveAsset("w_" + t + ".png");
                if (pfh != null && pfh.exists()) {
                    Texture tex = new Texture(pfh);
                    pieceTex.put("w_" + t, tex);
                    pieceTex.put("w_" + t.toLowerCase(), tex);
                }
            } catch (Throwable ignored) {}
            try {
                com.badlogic.gdx.files.FileHandle pfh = resolveAsset("B_" + name + ".png");
                if (pfh == null || !pfh.exists()) pfh = resolveAsset("b_" + t + ".png");
                if (pfh != null && pfh.exists()) {
                    Texture tex = new Texture(pfh);
                    pieceTex.put("b_" + t, tex);
                    pieceTex.put("b_" + t.toLowerCase(), tex);
                }
            } catch (Throwable ignored) {}
        }

        // Load character avatars
        for (int i = 1; i <= 4; i++) {
            String path = "charactor/Vampire/VampireAngryframe" + i + ".png";
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset(path);
                if (fh == null || !fh.exists()) {
                    fh = resolveAsset("charactor/Vampire/vampire_f" + i + ".png");
                }
                if (fh != null && fh.exists()) vampireTextures[i - 1] = new Texture(fh);
            } catch (Throwable ignored) {}
        }
        vampireAnimator = new CharacterAnimator(4, 0.18f);

        for (int i = 1; i <= 12; i++) {
            String path = "charactor/evilbox/EvilBox1_f" + i + ".png";
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset(path);
                if (fh != null) evilBoxTextures[i - 1] = new Texture(fh);
            } catch (Throwable ignored) {}
        }
        evilBoxAnimator = new CharacterAnimator(12, 0.12f);

        // Load Boss Avatar if in Boss Rush mode
        if (bossLevel != null && bossLevel.getAvatarPath() != null) {
            try {
                com.badlogic.gdx.files.FileHandle bfh = resolveAsset(bossLevel.getAvatarPath());
                if (bfh != null && bfh.exists()) {
                    bossAvatarTex = new Texture(bfh);
                }
            } catch (Throwable ignored) {}
        }

        setupInput();
        startAsyncAnalysis();
    }

    private void startAsyncAnalysis() {
        new Thread(() -> {
            report = GameReviewer.analyzeGame(originalMoves, (current, total) -> {
                analyzeProgress = current;
                analyzeTotal = total;
            });
            Gdx.app.postRunnable(() -> {
                isAnalyzing = false;
                if (!originalMoves.isEmpty()) {
                    currentMoveIdx = 0; // Jump to move 1
                    updateDisplayBoard();
                }
            });
        }).start();
    }

    private void updateDisplayBoard() {
        displayBoard = new Board();
        for (int i = 0; i <= currentMoveIdx && i < originalMoves.size(); i++) {
            MoveRecord m = originalMoves.get(i);
            displayBoard.moveRecord(m.getFrom(), m.getTo(), m.getMovedPiece().getColor(), m.getPromotedPiece());
        }
        if (currentMoveIdx >= 0) {
            moveListPage = currentMoveIdx / MOVES_PER_PAGE;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.setToOrtho(false, width, height);
            camera.update();
        }
    }

    private float getBoardSize() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float availableH = screenH - 95f - 65f;
        float availableW = (screenW - 400f) * 0.95f;
        float s = Math.min(availableW, availableH);
        return Math.max(260f, Math.min(s, MAX_BOARD_SIZE));
    }

    private float getBoardX() {
        return 56f; // Leaves room on the left (x: 18f - 42f) for Evaluation Bar
    }

    private float getBoardY() {
        return 58f;
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.M) {
                    com.chessegame.audio.MusicManager.getInstance().toggleMute();
                    return true;
                }

                if (isAnalyzing) return false;

                // Left Arrow / A: Previous Move
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
                    if (currentMoveIdx > -1) {
                        currentMoveIdx--;
                        isAutoPlaying = false;
                        updateDisplayBoard();
                    }
                    return true;
                }
                // Right Arrow / D: Next Move
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
                    if (currentMoveIdx < originalMoves.size() - 1) {
                        currentMoveIdx++;
                        isAutoPlaying = false;
                        updateDisplayBoard();
                    }
                    return true;
                }
                // Spacebar: Play / Pause
                if (keycode == Input.Keys.SPACE) {
                    isAutoPlaying = !isAutoPlaying;
                    autoPlayTimer = 0f;
                    return true;
                }
                // Home: Start Position
                if (keycode == Input.Keys.HOME) {
                    currentMoveIdx = -1;
                    isAutoPlaying = false;
                    updateDisplayBoard();
                    return true;
                }
                // End: Final Move
                if (keycode == Input.Keys.END) {
                    currentMoveIdx = originalMoves.size() - 1;
                    isAutoPlaying = false;
                    updateDisplayBoard();
                    return true;
                }
                // Tab: Toggle Tabs
                if (keycode == Input.Keys.TAB) {
                    currentTab = (currentTab == ReviewTab.ANALYSIS) ? ReviewTab.STATISTICS : ReviewTab.ANALYSIS;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isAnalyzing) return true;

                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;

                float boardSize = getBoardSize();
                float boardX = getBoardX();
                float boardY = getBoardY();

                // 1. Navigation Bar Buttons (directly under board)
                float navY = boardY - 38f;
                float navBtnW = 46f;
                float navBtnH = 30f;
                float navStartX = boardX;

                // |<< (Start)
                if (x >= navStartX && x <= navStartX + navBtnW && y >= navY && y <= navY + navBtnH) {
                    currentMoveIdx = -1;
                    isAutoPlaying = false;
                    updateDisplayBoard();
                    return true;
                }
                // < (Prev)
                if (x >= navStartX + (navBtnW + 5f) && x <= navStartX + (navBtnW + 5f) + navBtnW && y >= navY && y <= navY + navBtnH) {
                    if (currentMoveIdx > -1) {
                        currentMoveIdx--;
                        isAutoPlaying = false;
                        updateDisplayBoard();
                    }
                    return true;
                }
                // ▶ / ⏸ (Auto-play Toggle)
                if (x >= navStartX + 2 * (navBtnW + 5f) && x <= navStartX + 2 * (navBtnW + 5f) + navBtnW && y >= navY && y <= navY + navBtnH) {
                    isAutoPlaying = !isAutoPlaying;
                    autoPlayTimer = 0f;
                    return true;
                }
                // > (Next)
                if (x >= navStartX + 3 * (navBtnW + 5f) && x <= navStartX + 3 * (navBtnW + 5f) + navBtnW && y >= navY && y <= navY + navBtnH) {
                    if (currentMoveIdx < originalMoves.size() - 1) {
                        currentMoveIdx++;
                        isAutoPlaying = false;
                        updateDisplayBoard();
                    }
                    return true;
                }
                // >>| (End)
                if (x >= navStartX + 4 * (navBtnW + 5f) && x <= navStartX + 4 * (navBtnW + 5f) + navBtnW && y >= navY && y <= navY + navBtnH) {
                    currentMoveIdx = originalMoves.size() - 1;
                    isAutoPlaying = false;
                    updateDisplayBoard();
                    return true;
                }
                // Speed Stepper Button (1.0x / 1.5x / 2.0x)
                float speedBtnW = 54f;
                float speedBtnX = navStartX + 5 * (navBtnW + 5f);
                if (x >= speedBtnX && x <= speedBtnX + speedBtnW && y >= navY && y <= navY + navBtnH) {
                    speedIndex = (speedIndex + 1) % speeds.length;
                    return true;
                }

                // 2. Right Panel Layout Coordinates
                float panelX = boardX + boardSize + 18f;
                float panelW = Gdx.graphics.getWidth() - panelX - 20f;
                float panelH = boardSize + 38f;
                float panelY = boardY - 38f;

                // Tab Switch Buttons at Top of Right Panel
                float tabH = 34f;
                float tabY = panelY + panelH - tabH - 6f;
                float tabW = (panelW - 20f) / 2f;
                float tab1X = panelX + 8f;
                float tab2X = tab1X + tabW + 4f;

                if (y >= tabY && y <= tabY + tabH) {
                    if (x >= tab1X && x <= tab1X + tabW) {
                        currentTab = ReviewTab.ANALYSIS;
                        return true;
                    }
                    if (x >= tab2X && x <= tab2X + tabW) {
                        currentTab = ReviewTab.STATISTICS;
                        return true;
                    }
                }

                // 3. Interactive Move Ribbon (In Analysis Tab)
                if (currentTab == ReviewTab.ANALYSIS && !originalMoves.isEmpty()) {
                    float ribbonY = panelY + 54f;
                    float ribbonH = 68f;

                    // Check Move Page Prev (<)
                    if (x >= panelX + 8f && x <= panelX + 32f && y >= ribbonY && y <= ribbonY + ribbonH) {
                        if (moveListPage > 0) moveListPage--;
                        return true;
                    }
                    // Check Move Page Next (>)
                    if (x >= panelX + panelW - 32f && x <= panelX + panelW - 8f && y >= ribbonY && y <= ribbonY + ribbonH) {
                        int maxPages = (originalMoves.size() + MOVES_PER_PAGE - 1) / MOVES_PER_PAGE;
                        if (moveListPage < maxPages - 1) moveListPage++;
                        return true;
                    }

                    // Check Clicks on Move Pills in current page
                    float pillAreaX = panelX + 36f;
                    float pillAreaW = panelW - 72f;
                    float pillW = (pillAreaW - 12f) / 4f;
                    float pillH = 28f;

                    int startIdx = moveListPage * MOVES_PER_PAGE;
                    int endIdx = Math.min(originalMoves.size(), startIdx + MOVES_PER_PAGE);

                    for (int i = startIdx; i < endIdx; i++) {
                        int rel = i - startIdx;
                        int row = rel / 4; // 0 = top, 1 = bottom
                        int col = rel % 4;

                        float px = pillAreaX + col * (pillW + 4f);
                        float py = ribbonY + (row == 0 ? 36f : 4f);

                        if (x >= px && x <= px + pillW && y >= py && y <= py + pillH) {
                            currentMoveIdx = i;
                            isAutoPlaying = false;
                            updateDisplayBoard();
                            return true;
                        }
                    }
                }

                // 4. Action Buttons inside right panel bottom (Play Again / Main Menu)
                float btnW = Math.min(160f, (panelW - 24f) / 2f);
                float btnH = 38f;
                float btnY = navY;
                float btn1X = panelX + 10f;
                float btn2X = panelX + 10f + btnW + 10f;

                // Play Again (Rematch)
                if (x >= btn1X && x <= btn1X + btnW && y >= btnY && y <= btnY + btnH) {
                    if (bossLevel != null) {
                        app.setScreen(new GameScreen(app, bossLevel));
                    } else {
                        app.setScreen(new GameScreen(app, isVsAi, aiDepth, aiStyle));
                    }
                    return true;
                }

                // Main Menu / Boss Select
                if (x >= btn2X && x <= btn2X + btnW && y >= btnY && y <= btnY + btnH) {
                    if (bossLevel != null) {
                        app.setScreen(new BossLevelSelectScreen(app));
                    } else {
                        app.setScreen(new MainMenuScreen(app));
                    }
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        vampireAnimator.update(delta);
        evilBoxAnimator.update(delta);
        particleSystem.update(delta);

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        if (camera == null) camera = new OrthographicCamera();
        camera.setToOrtho(false, screenW, screenH);
        camera.update();

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // Auto-play timer logic
        if (isAutoPlaying && !isAnalyzing && !originalMoves.isEmpty()) {
            autoPlayTimer += delta;
            float targetInterval = speeds[speedIndex];
            if (autoPlayTimer >= targetInterval) {
                autoPlayTimer = 0f;
                if (currentMoveIdx < originalMoves.size() - 1) {
                    currentMoveIdx++;
                    updateDisplayBoard();
                } else {
                    isAutoPlaying = false;
                }
            }
        }

        Gdx.gl.glClearColor(0.09f, 0.09f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        particleSystem.render(shapes);

        if (isAnalyzing) {
            renderLoadingOverlay(screenW, screenH);
            return;
        }

        renderReviewScreen(screenW, screenH);
    }

    private void renderLoadingOverlay(int screenW, int screenH) {
        float cardW = 380f;
        float cardH = 140f;
        float cardX = (screenW - cardW) / 2f;
        float cardY = (screenH - cardH) / 2f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.12f, 0.12f, 0.16f, 0.95f);
        shapes.rect(cardX, cardY, cardW, cardH);

        // Progress Bar
        shapes.setColor(0.2f, 0.2f, 0.28f, 1f);
        shapes.rect(cardX + 30f, cardY + 30f, cardW - 60f, 16f);

        float progressRatio = (analyzeTotal > 0) ? (float) analyzeProgress / analyzeTotal : 0.5f;
        shapes.setColor(0.2f, 0.8f, 0.45f, 1f);
        shapes.rect(cardX + 30f, cardY + 30f, (cardW - 60f) * progressRatio, 16f);
        shapes.end();

        batch.begin();
        titleFont.setColor(Color.GOLD);
        titleFont.draw(batch, "กำลังวิเคราะห์เกม...", cardX + 85f, cardY + 110f);

        font.setColor(Color.WHITE);
        font.draw(batch, "ประเมินตาเดินที่: " + analyzeProgress + " / " + analyzeTotal, cardX + 105f, cardY + 75f);
        batch.end();
    }

    private void renderReviewScreen(int screenW, int screenH) {
        float boardSize = getBoardSize();
        float boardX = getBoardX();
        float boardY = getBoardY();
        float destInset = boardInsetPercent * boardSize;
        float innerX = boardX + destInset;
        float innerY = boardY + destInset;
        float innerSquare = (boardSize - 2 * destInset) / 8f;

        float headerH = 88f;
        float headerY = screenH - headerH;

        float panelX = boardX + boardSize + 18f;
        float panelW = screenW - panelX - 20f;
        float panelH = boardSize + 38f;
        float panelY = boardY - 38f;

        GameReviewReport.ReviewedMove currentRev = (currentMoveIdx >= 0 && currentMoveIdx < report.getReviewedMoves().size())
                ? report.getReviewedMoves().get(currentMoveIdx) : null;

        // --- 1. Top Header Dashboard Cards ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.12f, 0.12f, 0.16f, 1f);
        shapes.rect(0f, headerY, screenW, headerH);

        float cardW = Math.min(260f, (screenW - 50f) / 3f);

        // White Accuracy Card (Left)
        shapes.setColor(0.16f, 0.16f, 0.22f, 1f);
        shapes.rect(14f, headerY + 8f, cardW, 72f);
        shapes.setColor(0.95f, 0.35f, 0.25f, 1f);
        shapes.rect(14f, headerY + 8f, 4f, 72f);

        // Black / Boss Accuracy Card (Center/Left)
        float blackCardX = 14f + cardW + 10f;
        shapes.setColor(0.16f, 0.16f, 0.22f, 1f);
        shapes.rect(blackCardX, headerY + 8f, cardW, 72f);
        shapes.setColor(0.15f, 0.8f, 0.95f, 1f);
        shapes.rect(blackCardX, headerY + 8f, 4f, 72f);

        // Summary Match Card (Right)
        float sumCardX = blackCardX + cardW + 10f;
        float sumCardW = screenW - sumCardX - 14f;
        if (sumCardW > 100f) {
            shapes.setColor(0.15f, 0.15f, 0.20f, 1f);
            shapes.rect(sumCardX, headerY + 8f, sumCardW, 72f);
            shapes.setColor(0.95f, 0.75f, 0.25f, 1f);
            shapes.rect(sumCardX, headerY + 8f, 4f, 72f);
        }

        // --- 2. Evaluation Bar (Feature 1: Chess.com / Lichess Style) ---
        float evalBarW = 20f;
        float evalBarX = boardX - evalBarW - 12f;
        float evalBarY = boardY;
        float evalBarH = boardSize;

        // Outer border
        shapes.setColor(0.08f, 0.08f, 0.12f, 1f);
        shapes.rect(evalBarX - 2f, evalBarY - 2f, evalBarW + 4f, evalBarH + 4f);

        int evalCp = (currentRev != null) ? currentRev.getEvalAfter() : 0;
        // Sigmoid winning chance
        float whiteRatio = 1.0f / (1.0f + (float) Math.exp(-0.0035f * evalCp));
        whiteRatio = Math.max(0.05f, Math.min(0.95f, whiteRatio));
        float whiteH = evalBarH * whiteRatio;
        float blackH = evalBarH - whiteH;

        // Black section (Top)
        shapes.setColor(0.18f, 0.18f, 0.22f, 1f);
        shapes.rect(evalBarX, evalBarY + whiteH, evalBarW, blackH);

        // White section (Bottom)
        shapes.setColor(0.92f, 0.92f, 0.96f, 1f);
        shapes.rect(evalBarX, evalBarY, evalBarW, whiteH);

        // --- 3. Chess Board Background (Texture or Fallback Tiles) ---
        if (boardTex != null) {
            shapes.end();
            batch.begin();
            batch.draw(boardTex, boardX, boardY, boardSize, boardSize);
            batch.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
        } else {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    boolean light = ((r + c) % 2) == 0;
                    shapes.setColor(light ? new Color(0.93f, 0.93f, 0.82f, 1f) : new Color(0.46f, 0.58f, 0.34f, 1f));
                    shapes.rect(innerX + c * innerSquare, innerY + (7 - r) * innerSquare, innerSquare, innerSquare);
                }
            }
        }

        // Move Quality Highlights on Board
        if (currentRev != null) {
            MoveRecord m = currentRev.getMoveRecord();
            Position from = m.getFrom();
            Position to = m.getTo();

            Color qualColor = getQualityColor(currentRev.getQuality());
            shapes.setColor(qualColor.r, qualColor.g, qualColor.b, 0.40f);
            shapes.rect(innerX + from.col * innerSquare, innerY + (7 - from.row) * innerSquare, innerSquare, innerSquare);
            shapes.rect(innerX + to.col * innerSquare, innerY + (7 - to.row) * innerSquare, innerSquare, innerSquare);
        }

        // --- 4. Right Side Panel Card & Tab Buttons ---
        shapes.setColor(0.13f, 0.13f, 0.18f, 0.98f);
        shapes.rect(panelX, panelY, panelW, panelH);

        // Quality border line on top of panel
        if (currentRev != null) {
            shapes.setColor(getQualityColor(currentRev.getQuality()));
            shapes.rect(panelX, panelY + panelH - 4f, panelW, 4f);
        }

        // Dual Tabs (Feature 3: Analysis vs Statistics)
        float tabH = 34f;
        float tabY = panelY + panelH - tabH - 6f;
        float tabW = (panelW - 20f) / 2f;
        float tab1X = panelX + 8f;
        float tab2X = tab1X + tabW + 4f;

        // Tab 1: Analysis
        shapes.setColor(currentTab == ReviewTab.ANALYSIS ? new Color(0.24f, 0.36f, 0.55f, 1f) : new Color(0.16f, 0.18f, 0.24f, 1f));
        shapes.rect(tab1X, tabY, tabW, tabH);

        // Tab 2: Statistics
        shapes.setColor(currentTab == ReviewTab.STATISTICS ? new Color(0.24f, 0.36f, 0.55f, 1f) : new Color(0.16f, 0.18f, 0.24f, 1f));
        shapes.rect(tab2X, tabY, tabW, tabH);

        // --- 5. Navigation Bar Under Board ---
        float navY = boardY - 38f;
        float navBtnW = 46f;
        float navBtnH = 30f;
        float navStartX = boardX;

        // Button 1: Start |<<
        shapes.setColor(0.24f, 0.26f, 0.35f, 1f);
        shapes.rect(navStartX, navY, navBtnW, navBtnH);

        // Button 2: Prev <
        shapes.setColor(currentMoveIdx > -1 ? new Color(0.28f, 0.38f, 0.55f, 1f) : new Color(0.18f, 0.20f, 0.26f, 1f));
        shapes.rect(navStartX + (navBtnW + 5f), navY, navBtnW, navBtnH);

        // Button 3: Auto-Play ▶ / ⏸
        shapes.setColor(isAutoPlaying ? new Color(0.85f, 0.35f, 0.25f, 1f) : new Color(0.2f, 0.75f, 0.45f, 1f));
        shapes.rect(navStartX + 2 * (navBtnW + 5f), navY, navBtnW, navBtnH);

        // Button 4: Next >
        shapes.setColor(currentMoveIdx < originalMoves.size() - 1 ? new Color(0.28f, 0.38f, 0.55f, 1f) : new Color(0.18f, 0.20f, 0.26f, 1f));
        shapes.rect(navStartX + 3 * (navBtnW + 5f), navY, navBtnW, navBtnH);

        // Button 5: End >>|
        shapes.setColor(0.24f, 0.26f, 0.35f, 1f);
        shapes.rect(navStartX + 4 * (navBtnW + 5f), navY, navBtnW, navBtnH);

        // Speed Stepper Button (Feature 5: 1.0x / 1.5x / 2.0x)
        float speedBtnW = 54f;
        float speedBtnX = navStartX + 5 * (navBtnW + 5f);
        shapes.setColor(0.32f, 0.25f, 0.45f, 1f);
        shapes.rect(speedBtnX, navY, speedBtnW, navBtnH);

        // Action Buttons inside right panel bottom (Play Again & Menu)
        float btnW = Math.min(160f, (panelW - 24f) / 2f);
        float btnH = 38f;
        float btnY = navY;
        float btn1X = panelX + 10f;
        float btn2X = panelX + 10f + btnW + 10f;

        // Play Again (Gold)
        shapes.setColor(0.95f, 0.75f, 0.25f, 1f);
        shapes.rect(btn1X, btnY, btnW, btnH);

        // Main Menu (Blue)
        shapes.setColor(0.35f, 0.45f, 0.65f, 1f);
        shapes.rect(btn2X, btnY, btnW, btnH);

        // Ribbon Background (If in Analysis Tab)
        if (currentTab == ReviewTab.ANALYSIS && !originalMoves.isEmpty()) {
            float ribbonY = panelY + 54f;
            float ribbonH = 68f;
            shapes.setColor(0.10f, 0.10f, 0.14f, 0.95f);
            shapes.rect(panelX + 8f, ribbonY, panelW - 16f, ribbonH);

            // Move Paging buttons (< and >)
            shapes.setColor(0.20f, 0.22f, 0.30f, 1f);
            shapes.rect(panelX + 8f, ribbonY, 24f, ribbonH);
            shapes.rect(panelX + panelW - 32f, ribbonY, 24f, ribbonH);

            // Move Pills
            float pillAreaX = panelX + 36f;
            float pillAreaW = panelW - 72f;
            float pillW = (pillAreaW - 12f) / 4f;
            float pillH = 28f;

            int startIdx = moveListPage * MOVES_PER_PAGE;
            int endIdx = Math.min(originalMoves.size(), startIdx + MOVES_PER_PAGE);

            for (int i = startIdx; i < endIdx; i++) {
                int rel = i - startIdx;
                int row = rel / 4;
                int col = rel % 4;

                float px = pillAreaX + col * (pillW + 4f);
                float py = ribbonY + (row == 0 ? 36f : 4f);

                GameReviewReport.ReviewedMove rm = (i < report.getReviewedMoves().size()) ? report.getReviewedMoves().get(i) : null;
                boolean isCurrent = (i == currentMoveIdx);

                if (isCurrent) {
                    shapes.setColor(0.35f, 0.55f, 0.85f, 1f);
                } else {
                    shapes.setColor(0.16f, 0.17f, 0.24f, 1f);
                }
                shapes.rect(px, py, pillW, pillH);

                if (rm != null) {
                    shapes.setColor(getQualityColor(rm.getQuality()));
                    shapes.rect(px, py, 3f, pillH); // Quality indicator strip
                }
            }
        }

        // --- Feature 2: Best Move Neon Arrow & Played Move Arrow ---
        if (currentRev != null) {
            float pulse = 0.70f + 0.30f * (float) Math.sin(stateTime * 6.0f);

            // 1. Played Move Arrow (Quality color)
            Position from = currentRev.getMoveRecord().getFrom();
            Position to = currentRev.getMoveRecord().getTo();
            float pFromX = innerX + (from.col + 0.5f) * innerSquare;
            float pFromY = innerY + (7 - from.row + 0.5f) * innerSquare;
            float pToX = innerX + (to.col + 0.5f) * innerSquare;
            float pToY = innerY + (7 - to.row + 0.5f) * innerSquare;

            Color qColor = getQualityColor(currentRev.getQuality());
            drawArrow(pFromX, pFromY, pToX, pToY, 5f, 14f, 16f, new Color(qColor.r, qColor.g, qColor.b, 0.65f));

            // 2. Best Alternative Recommendation Arrow (Glowing Neon Green)
            if (currentRev.getQuality() == GameReviewReport.MoveQuality.BLUNDER ||
                currentRev.getQuality() == GameReviewReport.MoveQuality.MISTAKE ||
                currentRev.getQuality() == GameReviewReport.MoveQuality.INACCURACY) {
                ChessAI.AIMove best = currentRev.getBestAlternative();
                if (best != null) {
                    float bFromX = innerX + (best.from.col + 0.5f) * innerSquare;
                    float bFromY = innerY + (7 - best.from.row + 0.5f) * innerSquare;
                    float bToX = innerX + (best.to.col + 0.5f) * innerSquare;
                    float bToY = innerY + (7 - best.to.row + 0.5f) * innerSquare;

                    Color arrowColor = new Color(0.12f, 0.95f, 0.45f, 0.88f * pulse);
                    drawArrow(bFromX, bFromY, bToX, bToY, 8f, 20f, 22f, arrowColor);
                }
            }
        }

        shapes.end();

        // --- 6. Render Pieces on Board ---
        batch.begin();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = displayBoard.getPiece(new Position(r, c));
                if (p != null) {
                    String key = (p.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(p.getSymbol());
                    Texture t = pieceTex.get(key);
                    float x = innerX + c * innerSquare;
                    float y = innerY + (7 - r) * innerSquare;
                    if (t != null) {
                        float pieceBaseSize = innerSquare * 0.82f;
                        float textureRatio = (float) t.getHeight() / t.getWidth();
                        float drawWidth = pieceBaseSize;
                        float drawHeight = pieceBaseSize * textureRatio;
                        float drawX = x + (innerSquare - drawWidth) / 2f;
                        float drawY = y + (innerSquare - pieceBaseSize) / 2f;
                        batch.draw(t, drawX, drawY, drawWidth, drawHeight);
                    }
                }
            }
        }

        // Draw Coordinates (a-h and 1-8)
        if (smallFont != null) {
            for (int c = 0; c < 8; c++) {
                char colChar = (char) ('a' + c);
                boolean isLight = ((7 + c) % 2) == 0;
                smallFont.setColor(isLight ? new Color(0.35f, 0.45f, 0.30f, 0.95f) : new Color(0.92f, 0.92f, 0.82f, 0.95f));
                smallFont.draw(batch, String.valueOf(colChar), innerX + c * innerSquare + innerSquare - 14f, innerY + 16f);
            }
            for (int r = 0; r < 8; r++) {
                int rowNum = 8 - r;
                boolean isLight = ((r + 0) % 2) == 0;
                smallFont.setColor(isLight ? new Color(0.35f, 0.45f, 0.30f, 0.95f) : new Color(0.92f, 0.92f, 0.82f, 0.95f));
                smallFont.draw(batch, String.valueOf(rowNum), innerX + 4f, innerY + (7 - r) * innerSquare + innerSquare - 4f);
            }
        }

        // --- 7. Evaluation Bar Text Label ---
        if (smallFont != null) {
            String evalStr = (evalCp >= 0) ? String.format("+%.1f", evalCp / 100.0f) : String.format("%.1f", evalCp / 100.0f);
            if (evalCp >= 0) {
                smallFont.setColor(Color.BLACK);
                smallFont.draw(batch, evalStr, evalBarX + 1f, evalBarY + 20f);
            } else {
                smallFont.setColor(Color.WHITE);
                smallFont.draw(batch, evalStr, evalBarX + 1f, evalBarY + evalBarH - 8f);
            }
        }

        // --- 8. Top Header Labels (Feature 6: Dynamic Boss Integration) ---
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(0.85f);
        titleFont.draw(batch, "Vampire (หมากขาว)", 24f, headerY + 68f);
        titleFont.setColor(new Color(0.95f, 0.35f, 0.25f, 1f));
        titleFont.draw(batch, String.format("%.1f%%", report.getWhiteAccuracy()), 24f, headerY + 44f);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, report.getWhitePerformanceTitle(), 24f, headerY + 22f);

        // Black Profile: Boss or AI
        String blackName = (bossLevel != null) ? bossLevel.getBossName() : (isVsAi ? "EvilBox (AI)" : "ผู้เล่นหมากดำ");
        String blackTitle = (bossLevel != null) ? bossLevel.getBossTitle() : report.getBlackPerformanceTitle();

        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, blackName, blackCardX + 12f, headerY + 68f);
        titleFont.setColor(new Color(0.15f, 0.8f, 0.95f, 1f));
        titleFont.draw(batch, String.format("%.1f%%", report.getBlackAccuracy()), blackCardX + 12f, headerY + 44f);
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, blackTitle, blackCardX + 12f, headerY + 22f);

        // Match Summary
        if (sumCardW > 100f) {
            titleFont.setColor(Color.GOLD);
            titleFont.draw(batch, "บทสรุปภาพรวม", sumCardX + 14f, headerY + 68f);
            font.setColor(Color.WHITE);
            font.getData().setScale(0.85f);
            font.draw(batch, report.getMatchSummaryThai(), sumCardX + 14f, headerY + 38f);
        }

        // --- 9. Navigation Bar Labels ---
        font.setColor(Color.WHITE);
        font.getData().setScale(0.82f);
        font.draw(batch, "|<<", navStartX + 12f, navY + 22f);
        font.draw(batch, "<", navStartX + (navBtnW + 5f) + 16f, navY + 22f);
        font.draw(batch, isAutoPlaying ? "⏸" : "▶", navStartX + 2 * (navBtnW + 5f) + 16f, navY + 22f);
        font.draw(batch, ">", navStartX + 3 * (navBtnW + 5f) + 16f, navY + 22f);
        font.draw(batch, ">>|", navStartX + 4 * (navBtnW + 5f) + 12f, navY + 22f);

        // Speed Label
        font.setColor(Color.YELLOW);
        font.draw(batch, speedLabels[speedIndex], speedBtnX + 10f, navY + 22f);

        // Move counter info
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "ตา: " + (currentMoveIdx + 1) + " / " + originalMoves.size(), speedBtnX + speedBtnW + 8f, navY + 20f);

        // Right Panel Bottom Action Button Labels
        font.setColor(Color.BLACK);
        font.getData().setScale(0.88f);
        font.draw(batch, "เล่นใหม่อีกครั้ง", btn1X + (btnW - 95f) / 2f, btnY + 25f);
        font.setColor(Color.WHITE);
        font.draw(batch, (bossLevel != null ? "เลือกบอส" : "เมนูหลัก"), btn2X + (btnW - 65f) / 2f, btnY + 25f);

        // Tab Button Titles
        font.setColor(currentTab == ReviewTab.ANALYSIS ? Color.GOLD : Color.LIGHT_GRAY);
        font.draw(batch, "💡 บทวิเคราะห์ตาเดิน", tab1X + (tabW - 130f) / 2f, tabY + 23f);

        font.setColor(currentTab == ReviewTab.STATISTICS ? Color.GOLD : Color.LIGHT_GRAY);
        font.draw(batch, "📊 สถิติภาพรวม", tab2X + (tabW - 95f) / 2f, tabY + 23f);

        // --- 10. TAB CONTENTS ---
        float px = panelX + 16f;
        float py = tabY - 18f;

        if (currentTab == ReviewTab.ANALYSIS) {
            // --- TAB 1: MOVE ANALYSIS VIEW ---
            if (currentRev != null) {
                GameReviewReport.MoveQuality q = currentRev.getQuality();
                Color qc = getQualityColor(q);

                titleFont.setColor(qc);
                titleFont.getData().setScale(0.95f);
                titleFont.draw(batch, "ตาเดินที่ " + currentRev.getMoveNumber() + " : " + currentRev.getNotation(), px, py);

                smallFont.setColor(new Color(0.7f, 0.75f, 0.85f, 1f));
                smallFont.draw(batch, "Engine: " + report.getEngineName(), panelX + panelW - 230f, py);
                py -= 26f;

                titleFont.draw(batch, "[" + q.getLabelThai() + " " + q.getSymbol() + "]", px, py);
                py -= 28f;

                // Evaluation and Centipawn Loss
                font.setColor(Color.WHITE);
                font.getData().setScale(0.85f);
                String evalSide = (currentRev.getEvalAfter() >= 0) ? "ขาวได้เปรียบ" : "ดำได้เปรียบ";
                font.draw(batch, String.format("คะแนนประเมิน: %+.2f (%s)", (currentRev.getEvalAfter() / 100.0f), evalSide), px, py);
                py -= 22f;

                if (currentRev.getCentipawnLoss() > 20) {
                    font.setColor(new Color(0.95f, 0.4f, 0.4f, 1f));
                    font.draw(batch, "การเสียเปรียบ: -" + currentRev.getCentipawnLoss() + " แต้ม", px, py);
                    py -= 22f;
                }

                // Thai Commentary Explanation
                font.setColor(new Color(0.9f, 0.95f, 1.0f, 1f));
                font.draw(batch, "บทวิเคราะห์:", px, py);
                py -= 20f;
                font.draw(batch, currentRev.getExplanationThai(), px, py);
                py -= 42f;

                // Avatar and Reaction Dialogue (Feature 6)
                boolean isWhite = (currentRev.getPlayerColor() == Piece.Color.WHITE);
                Texture avatar = null;
                if (isWhite) {
                    avatar = vampireTextures[vampireAnimator.getCurrentFrameIndex()];
                } else {
                    if (bossAvatarTex != null) {
                        avatar = bossAvatarTex;
                    } else {
                        avatar = evilBoxTextures[evilBoxAnimator.getCurrentFrameIndex()];
                    }
                }

                if (avatar != null) {
                    batch.draw(avatar, px, py - 36f, 48f, 48f);
                }

                font.setColor(isWhite ? Color.CORAL : Color.CYAN);
                String reactionText = currentRev.getCharacterReaction();
                if (!isWhite && bossLevel != null) {
                    reactionText = "[" + bossLevel.getBossName() + "]: " + reactionText;
                }
                font.draw(batch, reactionText, px + 56f, py - 4f);

            } else {
                titleFont.setColor(Color.GOLD);
                titleFont.getData().setScale(0.95f);
                titleFont.draw(batch, "ตำแหน่งเริ่มต้นกระดาน", px, py);
                py -= 32f;
                font.setColor(Color.WHITE);
                font.getData().setScale(0.85f);
                font.draw(batch, "กดปุ่ม [ > ] หรือกดปุ่มลูกศร [ → ] เพื่อเริ่มทบทวนเกม", px, py);
            }

            // Feature 4: Interactive Move Ribbon Text
            if (!originalMoves.isEmpty()) {
                float ribbonY = panelY + 54f;
                font.setColor(Color.LIGHT_GRAY);
                font.draw(batch, "<", panelX + 16f, ribbonY + 40f);
                font.draw(batch, ">", panelX + panelW - 24f, ribbonY + 40f);

                float pillAreaX = panelX + 36f;
                float pillAreaW = panelW - 72f;
                float pillW = (pillAreaW - 12f) / 4f;

                int startIdx = moveListPage * MOVES_PER_PAGE;
                int endIdx = Math.min(originalMoves.size(), startIdx + MOVES_PER_PAGE);

                for (int i = startIdx; i < endIdx; i++) {
                    int rel = i - startIdx;
                    int row = rel / 4;
                    int col = rel % 4;

                    float ppx = pillAreaX + col * (pillW + 4f);
                    float ppy = ribbonY + (row == 0 ? 36f : 4f);

                    GameReviewReport.ReviewedMove rm = (i < report.getReviewedMoves().size()) ? report.getReviewedMoves().get(i) : null;
                    if (rm != null) {
                        smallFont.setColor(i == currentMoveIdx ? Color.WHITE : Color.LIGHT_GRAY);
                        String label = (rm.getPlayerColor() == Piece.Color.WHITE ? rm.getMoveNumber() + "." : rm.getMoveNumber() + "...") + " " + rm.getNotation() + " " + rm.getQuality().getSymbol();
                        smallFont.draw(batch, label, ppx + 6f, ppy + 18f);
                    }
                }
            }

        } else {
            // --- TAB 2: MOVE CLASSIFICATION BREAKDOWN TABLE (Feature 3) ---
            titleFont.setColor(Color.GOLD);
            titleFont.getData().setScale(0.90f);
            titleFont.draw(batch, "ตารางเปรียบเทียบสถิติคุณภาพตาเดิน", px, py);
            py -= 32f;

            // Table Header
            smallFont.setColor(Color.GRAY);
            smallFont.draw(batch, "ระดับคุณภาพตาเดิน", px, py);
            smallFont.draw(batch, "ขาว (Vampire)", px + panelW * 0.45f, py);
            smallFont.draw(batch, "ดำ (" + (bossLevel != null ? bossLevel.getBossName() : "บอส/AI") + ")", px + panelW * 0.72f, py);
            py -= 22f;

            // Table Rows
            GameReviewReport.MoveQuality[] qualities = {
                    GameReviewReport.MoveQuality.BRILLIANT,
                    GameReviewReport.MoveQuality.BEST,
                    GameReviewReport.MoveQuality.EXCELLENT,
                    GameReviewReport.MoveQuality.GOOD,
                    GameReviewReport.MoveQuality.BOOK,
                    GameReviewReport.MoveQuality.INACCURACY,
                    GameReviewReport.MoveQuality.MISTAKE,
                    GameReviewReport.MoveQuality.BLUNDER
            };

            for (GameReviewReport.MoveQuality q : qualities) {
                int wCount = report.getWhiteQualityCount(q);
                int bCount = report.getBlackQualityCount(q);

                Color qc = getQualityColor(q);
                font.setColor(qc);
                font.getData().setScale(0.82f);
                font.draw(batch, q.getSymbol() + "  " + q.getLabelThai(), px, py);

                font.setColor(Color.WHITE);
                font.draw(batch, String.valueOf(wCount), px + panelW * 0.50f, py);
                font.draw(batch, String.valueOf(bCount), px + panelW * 0.78f, py);
                py -= 26f;
            }

            py -= 10f;
            smallFont.setColor(Color.LIGHT_GRAY);
            smallFont.draw(batch, "💡 คำแนะนำ: กดปุ่ม [ Tab ] เพื่อสลับกลับไปดูบทวิเคราะห์ทีละตา", px, py);
        }

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

    private Color getQualityColor(GameReviewReport.MoveQuality quality) {
        switch (quality) {
            case BRILLIANT:
                return new Color(0.1f, 0.85f, 1.0f, 1f); // Cyan
            case BEST:
                return new Color(0.15f, 0.85f, 0.45f, 1f); // Emerald Green
            case EXCELLENT:
            case GOOD:
                return new Color(0.45f, 0.85f, 0.35f, 1f); // Light Green
            case BOOK:
                return new Color(0.85f, 0.65f, 0.25f, 1f); // Gold
            case INACCURACY:
                return new Color(0.95f, 0.85f, 0.2f, 1f); // Yellow
            case MISTAKE:
                return new Color(0.95f, 0.55f, 0.2f, 1f); // Orange
            case BLUNDER:
            default:
                return new Color(0.95f, 0.25f, 0.25f, 1f); // Crimson Red
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (boardTex != null) boardTex.dispose();
        java.util.Set<Texture> disposed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Texture t : pieceTex.values()) if (t != null && disposed.add(t)) t.dispose();
        for (Texture t : vampireTextures) if (t != null && disposed.add(t)) t.dispose();
        for (Texture t : evilBoxTextures) if (t != null && disposed.add(t)) t.dispose();
        if (bossAvatarTex != null) bossAvatarTex.dispose();
    }
}
