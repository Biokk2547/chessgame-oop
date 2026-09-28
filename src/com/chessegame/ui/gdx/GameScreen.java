package com.chessegame.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.chessegame.audio.MusicManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.chessegame.character.CharacterAnimator;
import com.chessegame.character.DialogManager;
import com.chessegame.model.Board;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;
import com.chessegame.model.MoveRecord;
import com.chessegame.model.Queen;
import com.chessegame.model.Rook;
import com.chessegame.model.Bishop;
import com.chessegame.model.Knight;
import com.chessegame.model.Pawn;
import com.chessegame.logic.ChessUtils;
import com.chessegame.logic.Clock;
import com.chessegame.logic.GameHistoryManager;
import com.chessegame.audio.SoundManager;
import com.chessegame.ai.ChessAI;
import com.chessegame.level.BossLevel;
import com.chessegame.level.LevelProgressManager;
import com.chessegame.particle.ParticleSystem;
import com.chessegame.particle.PieceGlideAnimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameScreen: renders board, pieces, smooth piece glide animations,
 * particle effects (move trails, capture explosions, victory confetti),
 * animated character avatars, real-time Thai dialogue bubbles, clocks, and overlays.
 */
public class GameScreen extends ScreenAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private BitmapFont coordFont;
    private Board board;
    private Clock clock;
    private Piece.Color currentTurn = Piece.Color.WHITE;

    private static final int MAX_BOARD_SIZE = 820;
    private static final int HORIZONTAL_MARGIN = 24;
    private static final int VERTICAL_MARGIN = 24;
    private static final int CLOCK_AREA_HEIGHT = 48;
    private final float boardInsetPercent = 6.0f / 142.0f; // Exact border inset matching board.png (6px out of 142px)

    private Texture dotTex;
    private Texture boardTex;
    private Map<String, Texture> pieceTex = new HashMap<>();
    private boolean[][] legalMoves = new boolean[8][8];
    private Position selected = null;
    private boolean overlayVisible = false;
    private String overlayTitle = "";
    private String overlayMessage = "";
    private boolean gameOver = false;
    private GameHistoryManager historyManager = new GameHistoryManager();
    private boolean promotionModalVisible = false;
    private Position pendingPromoFrom = null;
    private Position pendingPromoTo = null;

    // Drag & Drop State
    private boolean isDragging = false;
    private Position dragSource = null;
    private Piece draggedPiece = null;
    private float dragStartX = 0f;
    private float dragStartY = 0f;
    private float dragCurrentX = 0f;
    private float dragCurrentY = 0f;

    // AI Bot & Interactive Features State
    private LibGdxChessApp app = null;
    private boolean isVsAi = true;
    private int aiDepth = 3;
    private boolean aiThinking = false;
    private boolean showEvalBar = true;
    private com.chessegame.ai.Evaluation.AIStyle aiStyle = com.chessegame.ai.Evaluation.AIStyle.MASTER;
    private ChessAI.AIMove activeHintMove = null;
    private boolean hintThinking = false;
    private BossLevel bossLevel = null;
    private int starsEarned = 0;
    private static final int BUTTON_BAR_HEIGHT = 80;
    private static final int HEADER_BAR_HEIGHT = 76;
    private float stateTime = 0f;

    // Boss Ability Notifications
    private String abilityNotification = "";
    private Color abilityNotificationColor = Color.GOLD;
    private float abilityNotificationTimer = 0f;
    private boolean witchWardTriggered = false;
    private boolean overlordShieldTriggered = false;

    public void setAbilityNotification(String msg, Color color) {
        this.abilityNotification = msg;
        this.abilityNotificationColor = color;
        this.abilityNotificationTimer = 3.5f;
    }

    public GameScreen() {
        this(null, true, 3, com.chessegame.ai.Evaluation.AIStyle.MASTER);
    }

    public GameScreen(LibGdxChessApp app, boolean isVsAi, int aiDepth, com.chessegame.ai.Evaluation.AIStyle aiStyle) {
        this.app = app;
        this.isVsAi = isVsAi;
        this.aiDepth = aiDepth;
        this.aiStyle = aiStyle;
        this.bossLevel = null;
        this.aiColor = Piece.Color.BLACK;
        initScreen();
    }

    public GameScreen(LibGdxChessApp app, BossLevel bossLevel) {
        this.app = app;
        this.bossLevel = bossLevel;
        this.isVsAi = true;
        this.aiDepth = (bossLevel != null) ? bossLevel.getAiDepth() : 3;
        this.aiStyle = (bossLevel != null) ? bossLevel.getAiStyle() : com.chessegame.ai.Evaluation.AIStyle.MASTER;
        this.aiColor = Piece.Color.BLACK;
        initScreen();
    }

    public GameScreen(LibGdxChessApp app, Board startingBoard, Piece.Color startingTurn, Piece.Color playerColor, int aiDepth, com.chessegame.ai.Evaluation.AIStyle aiStyle) {
        this.app = app;
        this.bossLevel = null;
        this.isVsAi = true;
        this.aiDepth = aiDepth;
        this.aiStyle = aiStyle;
        this.customStartingBoard = (startingBoard != null) ? startingBoard.copy() : null;
        this.customStartingTurn = startingTurn;
        this.aiColor = (playerColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        initScreen();
    }

    private Piece.Color aiColor = Piece.Color.BLACK;
    private Board customStartingBoard = null;
    private Piece.Color customStartingTurn = null;

    // Animated Characters & Dialog System
    private Texture[] vampireTextures = new Texture[4];
    private Texture[] evilBoxTextures = new Texture[12];
    private CharacterAnimator vampireAnimator;
    private CharacterAnimator evilBoxAnimator;
    private DialogManager dialogManager;

    // Particle Effects & Piece Glide Animations
    private ParticleSystem particleSystem;
    private PieceGlideAnimation activeGlideAnim = null;

    private float getBoardSize() {
        int availableWidth = Gdx.graphics.getWidth() - (HORIZONTAL_MARGIN * 2);
        int availableHeight = Gdx.graphics.getHeight() - HEADER_BAR_HEIGHT - BUTTON_BAR_HEIGHT - (VERTICAL_MARGIN * 2);
        return Math.max(8, Math.min(MAX_BOARD_SIZE, Math.min(availableWidth, availableHeight)));
    }

    private float getOffsetX(float boardSize) {
        return (Gdx.graphics.getWidth() - boardSize) / 2f;
    }

    private float getOffsetY() {
        return VERTICAL_MARGIN + BUTTON_BAR_HEIGHT;
    }

    private Position getBoardPosition(float worldX, float worldY) {
        float destBoardSize = getBoardSize();
        float offsetX = getOffsetX(destBoardSize);
        float offsetY = getOffsetY();
        float destInset = boardInsetPercent * destBoardSize;
        float innerX = offsetX + destInset;
        float innerY = offsetY + destInset;
        float innerSquare = (destBoardSize - 2 * destInset) / 8f;

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

    private com.badlogic.gdx.files.FileHandle resolveAsset(String name) {
        String fileSep = System.getProperty("file.separator");
        String userDir = System.getProperty("user.dir");
        String[] candidates = new String[] {
            name,
            "assets/" + name,
            "desktop/assets/" + name,
            userDir + fileSep + "assets" + fileSep + name,
            userDir + fileSep + "desktop" + fileSep + "assets" + fileSep + name
        };
        for (String c : candidates) {
            try {
                com.badlogic.gdx.files.FileHandle fh = c.startsWith(userDir) ? com.badlogic.gdx.Gdx.files.absolute(c) : com.badlogic.gdx.Gdx.files.internal(c);
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
            String[] aliasCandidates = new String[] {
                alias,
                "assets/" + alias,
                "desktop/assets/" + alias,
                userDir + fileSep + "assets" + fileSep + alias,
                userDir + fileSep + "desktop" + fileSep + "assets" + fileSep + alias
            };
            for (String c : aliasCandidates) {
                try {
                    com.badlogic.gdx.files.FileHandle fh = c.startsWith(userDir) ? com.badlogic.gdx.Gdx.files.absolute(c) : com.badlogic.gdx.Gdx.files.internal(c);
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

    private void initScreen() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        // Ensure background music is playing seamlessly
        try {
            MusicManager.getInstance().playBGM();
        } catch (Throwable ignored) {}
        try {
            com.badlogic.gdx.files.FileHandle ttfFile = resolveAsset("fonts/thai.ttf");
            if (ttfFile != null && ttfFile.exists()) {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(ttfFile);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = 22;
                parameter.minFilter = Texture.TextureFilter.Linear;
                parameter.magFilter = Texture.TextureFilter.Linear;
                parameter.borderWidth = 0.8f;
                parameter.borderColor = new Color(0.05f, 0.05f, 0.08f, 0.95f);
                StringBuilder sb = new StringBuilder();
                for (char c = '\u0E01'; c <= '\u0E5B'; c++) {
                    sb.append(c);
                }
                parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + "•abcdefgh12345678+-*!?:.()[]'\"%/#";
                font = generator.generateFont(parameter);

                FreeTypeFontGenerator.FreeTypeFontParameter coordParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
                coordParam.size = 15;
                coordParam.borderWidth = 0.5f;
                coordParam.borderColor = new Color(0.05f, 0.05f, 0.08f, 0.9f);
                coordParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "abcdefgh12345678";
                coordFont = generator.generateFont(coordParam);

                generator.dispose();
            } else {
                font = new BitmapFont();
                coordFont = new BitmapFont();
            }
        } catch (Exception e) {
            font = new BitmapFont();
            coordFont = new BitmapFont();
        }
        if (customStartingBoard != null) {
            board = customStartingBoard.copy();
            if (customStartingTurn != null) {
                currentTurn = customStartingTurn;
            }
        } else {
            board = (bossLevel != null) ? bossLevel.createStartingBoard() : new Board();
        }
        historyManager.setInitialPositionKey(com.chessegame.ai.FENUtils.toPositionKey(board, currentTurn));
        if (bossLevel != null) {
            clock = new Clock(bossLevel.getTimeLimitMs(), bossLevel.getIncrementMs());
        } else {
            clock = new Clock(5 * 60 * 1000L, 2000L);
        }
        clock.startTurn(currentTurn == Piece.Color.WHITE ? Clock.Side.WHITE : Clock.Side.BLACK);

        dotTex = null;

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
        } catch (Exception ignored) {}

        // Load Vampire 4-frame animation assets
        List<String> vampPaths = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String path = "charactor/Vampire/VampireAngryframe" + i + ".png";
            vampPaths.add(path);
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset(path);
                if (fh == null || !fh.exists()) {
                    fh = resolveAsset("charactor/Vampire/vampire_f" + i + ".png");
                }
                if (fh != null && fh.exists()) vampireTextures[i - 1] = new Texture(fh);
            } catch (Exception ignored) {}
        }
        vampireAnimator = new CharacterAnimator("Vampire", vampPaths, 0.18f);

        // Load Opponent / Boss animation assets dynamically based on selected boss
        String avatarPath = (bossLevel != null) ? bossLevel.getAvatarPath() : "charactor/evilbox/EvilBox1_f1.png";
        List<String> oppPaths = new ArrayList<>();
        float duration = 0.12f;
        String oppName = (bossLevel != null) ? bossLevel.getBossName() : "EvilBox";

        if (avatarPath.contains("PirateCat")) {
            for (int i = 1; i <= 3; i++) {
                oppPaths.add("charactor/PirateCat/PirateCat_f" + i + ".png");
            }
            duration = 0.20f;
        } else if (avatarPath.contains("witchitty") || avatarPath.contains("witchkitty")) {
            for (int i = 1; i <= 6; i++) {
                oppPaths.add("charactor/witchitty/witchKitty_curiousIdleBreaker_f" + i + ".png");
            }
            duration = 0.16f;
        } else if (avatarPath.contains("Vampire")) {
            for (int i = 1; i <= 4; i++) {
                oppPaths.add("charactor/Vampire/VampireAngryframe" + i + ".png");
            }
            duration = 0.18f;
        } else {
            // Default EvilBox 12-frames
            for (int i = 1; i <= 12; i++) {
                oppPaths.add("charactor/evilbox/EvilBox1_f" + i + ".png");
            }
            duration = 0.12f;
        }

        evilBoxTextures = new Texture[oppPaths.size()];
        for (int i = 0; i < oppPaths.size(); i++) {
            String path = oppPaths.get(i);
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset(path);
                if (fh != null && fh.exists()) evilBoxTextures[i] = new Texture(fh);
            } catch (Exception ignored) {}
        }
        evilBoxAnimator = new CharacterAnimator(oppName, oppPaths, duration);

        dialogManager = new DialogManager();
        if (bossLevel != null) {
            dialogManager.setOpponent(bossLevel.getBossName(), bossLevel.getAvatarPath());
            dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, bossLevel.getIntroDialogue());
        } else {
            dialogManager.setOpponent(isVsAi ? "EvilBox" : "ผู้เล่นหมากดำ", "charactor/evilbox/EvilBox1_f1.png");
        }
        particleSystem = new ParticleSystem();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.M) {
                    MusicManager.getInstance().toggleMute();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int y = Gdx.graphics.getHeight() - screenY;
                int x = screenX;

                // Header Music Toggle Button
                float headerY = Gdx.graphics.getHeight() - HEADER_BAR_HEIGHT;
                float musicBtnX = 14f + 66f;
                float musicBtnY = headerY + 6f;
                float musicBtnW = 115f;
                float musicBtnH = 20f;
                if (x >= musicBtnX && x <= musicBtnX + musicBtnW && y >= musicBtnY && y <= musicBtnY + musicBtnH) {
                    MusicManager.getInstance().toggleMute();
                    return true;
                }

                // Header Draw Offer & Resign Buttons
                float eX = Gdx.graphics.getWidth() - 70f;
                float drawBtnW = 86f;
                float resignBtnW = 86f;
                float drawBtnX = eX - 180f;
                float resignBtnX = drawBtnX + drawBtnW + 8f;
                float actionBtnH = 20f;
                float actionBtnY = headerY + 6f;

                if (!overlayVisible && !gameOver) {
                    if (x >= drawBtnX && x <= drawBtnX + drawBtnW && y >= actionBtnY && y <= actionBtnY + actionBtnH) {
                        handleDrawOffer();
                        return true;
                    }
                    if (x >= resignBtnX && x <= resignBtnX + resignBtnW && y >= actionBtnY && y <= actionBtnY + actionBtnH) {
                        handleResign();
                        return true;
                    }
                }

                if (overlayVisible) {
                    float dialogW = Math.max(480f, Gdx.graphics.getWidth() * 0.58f);
                    float dialogH = 210f;
                    float dialogX = (Gdx.graphics.getWidth() - dialogW) / 2f;
                    float dialogY = (Gdx.graphics.getHeight() - dialogH) / 2f;

                    float gapD = 10f;
                    float btnDW = (dialogW - 40f) / 3f;
                    float btnDH = 40f;
                    float btnDY = dialogY + 20f;

                    // Button 1: Game Review (Left)
                    float btn1DX = dialogX + 10f;
                    if (x >= btn1DX && x <= btn1DX + btnDW && y >= btnDY && y <= btnDY + btnDH) {
                        overlayVisible = false;
                        if (app != null) {
                            app.setScreen(new GameReviewScreen(app, historyManager.getFullTrajectory(), isVsAi, aiDepth, aiStyle, bossLevel));
                        }
                        return true;
                    }

                    // Button 2: Play Again (Center)
                    float btn2DX = dialogX + 10f + (btnDW + gapD);
                    if (x >= btn2DX && x <= btn2DX + btnDW && y >= btnDY && y <= btnDY + btnDH) {
                        dismissDialog();
                        return true;
                    }

                    // Button 3: Main Menu / Level Select (Right)
                    float btn3DX = dialogX + 10f + 2 * (btnDW + gapD);
                    if (x >= btn3DX && x <= btn3DX + btnDW && y >= btnDY && y <= btnDY + btnDH) {
                        overlayVisible = false;
                        if (app != null) {
                            if (bossLevel != null) {
                                app.setScreen(new BossLevelSelectScreen(app));
                            } else {
                                app.setScreen(new MainMenuScreen(app));
                            }
                        }
                        return true;
                    }

                    return true;
                }

                if (promotionModalVisible) {
                    float modalW = 380f;
                    float modalH = 140f;
                    float modalX = (Gdx.graphics.getWidth() - modalW) / 2f;
                    float modalY = (Gdx.graphics.getHeight() - modalH) / 2f;
                    float cardW = 80f;
                    float cardH = 75f;
                    float startCardX = modalX + 15f;
                    float cardY = modalY + 15f;

                    for (int i = 0; i < 4; i++) {
                        float cx = startCardX + i * 90f;
                        if (x >= cx && x <= cx + cardW && y >= cardY && y <= cardY + cardH) {
                            Piece choice = null;
                            if (i == 0) choice = new Queen(currentTurn);
                            else if (i == 1) choice = new Rook(currentTurn);
                            else if (i == 2) choice = new Bishop(currentTurn);
                            else if (i == 3) choice = new Knight(currentTurn);

                            promotionModalVisible = false;
                            completeMove(pendingPromoFrom, pendingPromoTo, choice);
                            pendingPromoFrom = null;
                            pendingPromoTo = null;
                            return true;
                        }
                    }
                    return true;
                }

                float destBoardSize = getBoardSize();
                float offsetX = getOffsetX(destBoardSize);
                float gap = 10f;
                float btnW = (destBoardSize - 3f * gap) / 4f;
                float rowH = 32f;
                float row2Y = 44f;
                float row1Y = 8f;

                // --- Row 2 (Top Row: Game Action Buttons) ---
                if (y >= row2Y && y <= row2Y + rowH) {
                    if (x >= offsetX && x <= offsetX + btnW) {
                        resetGame();
                        return true;
                    }
                    if (x >= offsetX + (btnW + gap) && x <= offsetX + (btnW + gap) + btnW) {
                        handleUndo();
                        activeHintMove = null;
                        return true;
                    }
                    if (x >= offsetX + 2 * (btnW + gap) && x <= offsetX + 2 * (btnW + gap) + btnW) {
                        handleRedo();
                        activeHintMove = null;
                        return true;
                    }
                    if (x >= offsetX + 3 * (btnW + gap) && x <= offsetX + 3 * (btnW + gap) + btnW) {
                        requestAiHint();
                        return true;
                    }
                }

                // --- Row 1 (Bottom Row: AI & Setting Buttons) ---
                if (y >= row1Y && y <= row1Y + rowH) {
                    if (x >= offsetX && x <= offsetX + btnW) {
                        isVsAi = !isVsAi;
                        return true;
                    }
                    if (x >= offsetX + (btnW + gap) && x <= offsetX + (btnW + gap) + btnW) {
                        aiDepth++;
                        if (aiDepth > 6) aiDepth = 1;
                        return true;
                    }
                    if (x >= offsetX + 2 * (btnW + gap) && x <= offsetX + 2 * (btnW + gap) + btnW) {
                        String oppName = (bossLevel != null) ? bossLevel.getBossName() : (isVsAi ? "EvilBox" : "ผู้เล่นหมากดำ");
                        if (aiStyle == com.chessegame.ai.Evaluation.AIStyle.MASTER) {
                            aiStyle = com.chessegame.ai.Evaluation.AIStyle.AGGRESSIVE;
                            dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, oppName + " สายบุก: ข้าจะบดขยี้หมากของเจ้า!");
                        } else if (aiStyle == com.chessegame.ai.Evaluation.AIStyle.AGGRESSIVE) {
                            aiStyle = com.chessegame.ai.Evaluation.AIStyle.DEFENSIVE;
                            dialogManager.triggerDialogue(DialogManager.CharacterType.VAMPIRE, "Vampire สายรับ: ค่ายกลรัดกุม... ลองบุกเข้ามาดู!");
                        } else {
                            aiStyle = com.chessegame.ai.Evaluation.AIStyle.MASTER;
                            dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, oppName + " สไตล์เซียน: การเดินหมากอย่างมีสติคือชัยชนะ!");
                        }
                        return true;
                    }
                    if (x >= offsetX + 3 * (btnW + gap) && x <= offsetX + 3 * (btnW + gap) + btnW) {
                        showEvalBar = !showEvalBar;
                        return true;
                    }
                }

                if (button == Input.Buttons.RIGHT) {
                    selected = null;
                    clearLegalMoves();
                    clearDrag();
                    return true;
                }

                if (y < BUTTON_BAR_HEIGHT + 4f) {
                    return false;
                }

                if (gameOver || aiThinking || (isVsAi && currentTurn == aiColor)) {
                    return false;
                }

                Position pos = getBoardPosition(x, y);
                if (pos == null) {
                    selected = null;
                    clearLegalMoves();
                    clearDrag();
                    return false;
                }

                Piece p = board.getPiece(pos);

                // Click-to-move destination: if a piece was already selected and clicked a legal target
                if (selected != null && legalMoves[pos.row][pos.col]) {
                    onClick(pos.row, pos.col);
                    clearDrag();
                    return true;
                }

                // Click on player's own piece: select and prepare for dragging
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

                // Click on invalid or empty square: deselect
                selected = null;
                clearLegalMoves();
                clearDrag();
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (dragSource == null || draggedPiece == null) {
                    return false;
                }
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
                if (dragSource == null) {
                    return false;
                }
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;

                if (isDragging) {
                    Position targetPos = getBoardPosition(x, y);
                    if (targetPos != null && !targetPos.equals(dragSource) && legalMoves[targetPos.row][targetPos.col]) {
                        Piece movedPiece = board.getPiece(dragSource);
                        if (movedPiece instanceof Pawn && (targetPos.row == 0 || targetPos.row == 7)) {
                            pendingPromoFrom = dragSource;
                            pendingPromoTo = targetPos;
                            promotionModalVisible = true;
                            selected = null;
                            clearLegalMoves();
                        } else {
                            completeMove(dragSource, targetPos, null, true);
                            selected = null;
                            clearLegalMoves();
                        }
                    }
                    clearDrag();
                    return true;
                } else {
                    // Click without drag: piece remains selected from touchDown
                    clearDrag();
                    return false;
                }
            }
        });
    }

    private void showDialog(String title, String message) {
        overlayVisible = true;
        overlayTitle = title;
        overlayMessage = message;
        gameOver = true;
    }

    private void handleResign() {
        if (gameOver || overlayVisible) return;
        String oppName = (bossLevel != null) ? bossLevel.getBossName() : (isVsAi ? "EvilBox" : "หมากดำ");
        if (isVsAi) {
            SoundManager.getInstance().playMoveSound();
            dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, oppName + ": 'ฮ่าๆๆ! เจ้าขอยอมแพ้แล้ว ชัยชนะเป็นของข้า!'");
            showDialog("ขอยอมแพ้ (Resigned)", "คุณได้กดยอมแพ้การแข่งขัน\n" + oppName + " เป็นฝ่ายชนะ!");
        } else {
            Piece.Color resigned = currentTurn;
            Piece.Color winner = (resigned == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
            String resignedName = (resigned == Piece.Color.WHITE) ? "ผู้เล่นหมากขาว (Vampire)" : "ผู้เล่นหมากดำ (EvilBox)";
            String winnerName = (winner == Piece.Color.WHITE) ? "หมากขาว (Vampire)" : "หมากดำ (EvilBox)";
            dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, resignedName + " กดยอมแพ้!");
            showDialog("ขอยอมแพ้ (Resigned)", resignedName + " กดยอมแพ้การแข่งขัน\n" + winnerName + " เป็นฝ่ายชนะ!");
        }
        selected = null;
        clearLegalMoves();
        clearDrag();
    }

    private void handleDrawOffer() {
        if (gameOver || overlayVisible) return;
        String oppName = (bossLevel != null) ? bossLevel.getBossName() : (isVsAi ? "EvilBox" : "หมากดำ");
        if (isVsAi) {
            // AI evaluates the board position
            int whiteAdvantage = com.chessegame.ai.Evaluation.evaluateStyled(board, aiStyle);
            // From AI's perspective (Black): positive means AI is winning, negative means AI is losing
            int aiAdvantage = -whiteAdvantage;

            // If AI is not clearly winning (aiAdvantage < 150 centipawns), it accepts the draw
            if (aiAdvantage < 150) {
                dialogManager.triggerStalemate();
                dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, oppName + ": 'ตกลง! สถานการณ์สูสี ข้าขอยอมรับผลเสมอ!'");
                showDialog("เสมอกัน (Draw by Agreement)", oppName + " ยอมรับคำขอเสมอ\nการแข่งขันจบลงด้วยผลเสมอกัน!");
            } else {
                dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, oppName + ": 'ไม่มีทาง! ข้ากำลังได้เปรียบอยู่ ไม่รับผลเสมอเด็ดขาด!'");
                setAbilityNotification("บอทปฏิเสธคำขอเสมอ! (บอทกำลังได้เปรียบ)", Color.SALMON);
            }
        } else {
            // 2-Player Local match: mutual agreement draw
            dialogManager.triggerStalemate();
            showDialog("เสมอกัน (Draw by Agreement)", "ผู้เล่นทั้งสองฝ่ายตกลงยินยอมเสมอกัน (Draw by Agreement)");
        }
        selected = null;
        clearLegalMoves();
        clearDrag();
    }

    private void dismissDialog() {
        overlayVisible = false;
        overlayTitle = "";
        overlayMessage = "";
        resetGame();
    }

    private void resetGame() {
        board = (bossLevel != null) ? bossLevel.createStartingBoard() : new Board();
        historyManager.clear();
        historyManager.setInitialPositionKey(com.chessegame.ai.FENUtils.toPositionKey(board, Piece.Color.WHITE));
        currentTurn = Piece.Color.WHITE;
        selected = null;
        activeGlideAnim = null;
        promotionModalVisible = false;
        pendingPromoFrom = null;
        pendingPromoTo = null;
        clearLegalMoves();
        clearDrag();
        gameOver = false;
        starsEarned = 0;
        witchWardTriggered = false;
        overlordShieldTriggered = false;
        abilityNotification = "";
        abilityNotificationTimer = 0f;
        if (bossLevel != null) {
            clock = new Clock(bossLevel.getTimeLimitMs(), bossLevel.getIncrementMs());
            if (dialogManager != null) {
                dialogManager.setOpponent(bossLevel.getBossName(), bossLevel.getAvatarPath());
                dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, bossLevel.getIntroDialogue());
            }
        } else {
            clock = new Clock(5 * 60 * 1000L, 2000L);
            if (dialogManager != null) {
                dialogManager.setOpponent(isVsAi ? "EvilBox" : "ผู้เล่นหมากดำ", "charactor/evilbox/EvilBox1_f1.png");
                dialogManager.triggerGameStart();
            }
        }
        clock.startTurn(Clock.Side.WHITE);
    }

    private int calculateBossStars() {
        if (bossLevel == null) return 0;
        int stars = 1;
        if (clock != null) {
            long remaining = clock.getRemaining(Clock.Side.WHITE);
            if (remaining >= bossLevel.getTimeLimitMs() / 3) stars++;
        }
        if (!ChessUtils.isInCheck(board, Piece.Color.WHITE)) stars++;
        return Math.min(3, Math.max(1, stars));
    }

    private void onClick(int row, int col) {
        if (overlayVisible || promotionModalVisible) {
            if (overlayVisible) dismissDialog();
            return;
        }

        Position pos = new Position(row, col);
        Piece p = board.getPiece(pos);
        if (selected == null) {
            if (p != null && p.getColor() == currentTurn) {
                selected = pos;
                computeLegalMoves(selected);
            }
        } else {
            if (p != null && p.getColor() == currentTurn) {
                selected = pos;
                computeLegalMoves(selected);
                return;
            }
            if (!legalMoves[row][col]) {
                selected = null; clearLegalMoves();
                return;
            }

            Piece movedPiece = board.getPiece(selected);

            // Pawn promotion check
            if (movedPiece instanceof Pawn && (pos.row == 0 || pos.row == 7)) {
                pendingPromoFrom = selected;
                pendingPromoTo = pos;
                promotionModalVisible = true;
                selected = null;
                clearLegalMoves();
                return;
            }

            completeMove(selected, pos, null);
        }
    }

    private void completeMove(Position from, Position to, Piece promoChoice) {
        completeMove(from, to, promoChoice, false);
    }

    private void completeMove(Position from, Position to, Piece promoChoice, boolean fromDrag) {
        Piece movedPiece = board.getPiece(from);
        boolean isCapture = (board.getPiece(to) != null);

        if (movedPiece instanceof Pawn && from.col != to.col && board.getPiece(to) == null) {
            isCapture = true;
        }

        float destBoardSize = getBoardSize();
        float offsetX = getOffsetX(destBoardSize);
        float offsetY = getOffsetY();
        float destInset = boardInsetPercent * destBoardSize;
        float innerX = offsetX + destInset;
        float innerY = offsetY + destInset;
        float innerSquare = (destBoardSize - 2 * destInset) / 8f;
        float pieceBaseSize = innerSquare * 0.82f;

        float targetX = innerX + to.col * innerSquare + (innerSquare - pieceBaseSize) / 2f;
        float targetY = innerY + (7 - to.row) * innerSquare + (innerSquare - pieceBaseSize) / 2f;

        float startX;
        float startY;
        float duration;
        if (fromDrag) {
            startX = dragCurrentX - pieceBaseSize / 2f;
            startY = dragCurrentY - pieceBaseSize / 2f;
            duration = 0.08f;
        } else {
            startX = innerX + from.col * innerSquare + (innerSquare - pieceBaseSize) / 2f;
            startY = innerY + (7 - from.row) * innerSquare + (innerSquare - pieceBaseSize) / 2f;
            duration = 0.22f;
        }

        activeGlideAnim = new PieceGlideAnimation(movedPiece, from, to, startX, startY, targetX, targetY, isCapture, duration);

        MoveRecord record = board.moveRecord(from, to, currentTurn, promoChoice);

        Piece.Color moved = currentTurn;
        Piece.Color opponent = (currentTurn == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        currentTurn = opponent;

        String posKey = com.chessegame.ai.FENUtils.toPositionKey(board, opponent);
        historyManager.recordMove(record, posKey);

        if (clock != null) clock.moveCompleted();

        if (!board.hasKing(opponent)) {
            SoundManager.getInstance().playVictorySound();
            dialogManager.triggerCheckmate(moved);
            particleSystem.emitVictoryConfetti(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            if (bossLevel != null && moved == Piece.Color.WHITE) {
                starsEarned = calculateBossStars();
                LevelProgressManager.recordLevelCompletion(bossLevel.getLevelId(), starsEarned);
                showDialog("พิชิตบอสสำเร็จ! (" + starsEarned + " ดาว)", bossLevel.getBossName() + " พ่ายแพ้!\n" + bossLevel.getWinDialogue());
            } else if (bossLevel != null && moved == Piece.Color.BLACK) {
                showDialog("พ่ายแพ้บอส", bossLevel.getLoseDialogue());
            } else {
                showDialog("จบเกม", (moved == Piece.Color.WHITE ? "Vampire (ขาว)" : "EvilBox (ดำ)") + " ชนะ! จับคิงสำเร็จ");
            }
            selected = null; clearLegalMoves();
            return;
        }

        if (ChessUtils.isCheckmate(board, opponent)) {
            SoundManager.getInstance().playVictorySound();
            dialogManager.triggerCheckmate(moved);
            particleSystem.emitVictoryConfetti(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            if (bossLevel != null && moved == Piece.Color.WHITE) {
                starsEarned = calculateBossStars();
                LevelProgressManager.recordLevelCompletion(bossLevel.getLevelId(), starsEarned);
                showDialog("พิชิตบอสสำเร็จ! (" + starsEarned + " ดาว)", bossLevel.getBossName() + " พ่ายแพ้!\n" + bossLevel.getWinDialogue());
            } else if (bossLevel != null && moved == Piece.Color.BLACK) {
                showDialog("พ่ายแพ้บอส", bossLevel.getLoseDialogue());
            } else {
                showDialog("รุกฆาต", (moved == Piece.Color.WHITE ? "Vampire (ขาว)" : "EvilBox (ดำ)") + " ชนะด้วยการรุกฆาต!");
            }
            selected = null; clearLegalMoves();
            return;
        }

        if (ChessUtils.isStalemate(board, opponent)) {
            dialogManager.triggerStalemate();
            showDialog("เสมอกัน (Stalemate)", "เกมเสมอกันเนื่องจากไม่มีตาเดินที่ถูกต้อง (Stalemate)");
            selected = null; clearLegalMoves();
            return;
        }

        if (ChessUtils.isInsufficientMaterial(board)) {
            dialogManager.triggerStalemate();
            showDialog("เสมอกัน (Insufficient Material)", "หมากทั้งสองฝ่ายไม่เพียงพอสำหรับการรุกฆาต (Insufficient Material)");
            selected = null; clearLegalMoves();
            return;
        }

        if (historyManager.isThreefoldRepetition(posKey)) {
            dialogManager.triggerStalemate();
            showDialog("เสมอกัน (Threefold Repetition)", "เกิดรูปแบบตำแหน่งเดิมซ้ำครบ 3 ครั้ง (Threefold Repetition)");
            selected = null; clearLegalMoves();
            return;
        }

        if (ChessUtils.isFiftyMoveRule(board)) {
            dialogManager.triggerStalemate();
            showDialog("เสมอกัน (Fifty-Move Rule)", "ไม่มีการเดินเบี้ยหรือกินหมากต่อเนื่องครบ 50 ตาเดิน (Fifty-Move Rule)");
            selected = null; clearLegalMoves();
            return;
        }

        if (ChessUtils.isInCheck(board, opponent)) {
            SoundManager.getInstance().playCheckSound();
            dialogManager.triggerCheck(opponent);

            // Boss Check Reactions / Ward Abilities
            if (bossLevel != null && opponent == Piece.Color.BLACK) {
                if (bossLevel.getLevelId() == 2 && !witchWardTriggered) {
                    witchWardTriggered = true;
                    setAbilityNotification("[มนตราเกราะ 9 ชีวิต] มนต์คุ้มภัยสำแดงเดช!", new Color(0.75f, 0.35f, 0.85f, 1f));
                    dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, "WitchKitty: 'เมี้ยว! มนต์เกราะ 9 ชีวิตของข้าทำงาน!'");
                } else if (bossLevel.getLevelId() == 5 && !overlordShieldTriggered) {
                    overlordShieldTriggered = true;
                    setAbilityNotification("[บาเรียจักรกล] Overlord Core ทำงาน!", new Color(0.95f, 0.75f, 0.2f, 1f));
                    dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, "Overlord EvilBox: 'การคำนวณยังไม่จบสิ้น... เข้ามา!'");
                }
            }
        } else if (isCapture) {
            SoundManager.getInstance().playCaptureSound();
            dialogManager.triggerMove(moved, true);

            // Boss Capture Abilities
            if (bossLevel != null) {
                if (moved == Piece.Color.WHITE && bossLevel.getLevelId() == 1) {
                    // Player captured Pirate piece -> Bonus Time! (+20s)
                    if (clock != null) clock.addTime(Clock.Side.WHITE, 20000L);
                    setAbilityNotification("[ขุมทรัพย์โจรสลัด] ได้รับเวลาโบนัส +20 วิ!", new Color(0.2f, 0.85f, 0.45f, 1f));
                    dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, "PirateCat: 'แง้ววว! สมบัติของข้าถูกขโมยไป!'");
                } else if (moved == Piece.Color.BLACK && bossLevel.getLevelId() == 4) {
                    // Vampire captured player piece -> Life Leech! (Softened to -2s)
                    if (clock != null) clock.addTime(Clock.Side.WHITE, -2000L);
                    setAbilityNotification("[ดูดกลืนเวลา] แวมไพร์ขโมยเวลาผู้เล่น -2 วิ!", new Color(0.95f, 0.25f, 0.25f, 1f));
                    dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, "Shadow Vampire: 'เวลาและโลหิตของเจ้า... เป็นของข้า!'");
                } else if (moved == Piece.Color.BLACK && bossLevel.getLevelId() == 3) {
                    // Berserker rage
                    setAbilityNotification("[จิตวิญญาณแห่งความบ้าคลั่ง] กล่องคำรามด้วยไฟสงคราม!", new Color(0.95f, 0.55f, 0.2f, 1f));
                    dialogManager.triggerDialogue(DialogManager.CharacterType.OPPONENT, "Berserker Box: 'บดขยี้! บุกทะลวงเข้าไป!'");
                }
            }
        } else {
            SoundManager.getInstance().playMoveSound();
            dialogManager.triggerMove(moved, false);
        }

        selected = null; clearLegalMoves(); activeHintMove = null;
    }

    private void handleUndo() {
        if (!historyManager.canUndo()) return;
        MoveRecord move = historyManager.undo(board);
        if (move != null) {
            currentTurn = move.getMovedPiece().getColor();
            if (clock != null) clock.startTurn(currentTurn == Piece.Color.WHITE ? Clock.Side.WHITE : Clock.Side.BLACK);
            SoundManager.getInstance().playMoveSound();
            selected = null;
            clearLegalMoves();
            clearDrag();
            activeGlideAnim = null;
        }
    }

    private void handleRedo() {
        if (!historyManager.canRedo()) return;
        MoveRecord move = historyManager.redo(board);
        if (move != null) {
            currentTurn = (move.getMovedPiece().getColor() == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
            if (clock != null) clock.startTurn(currentTurn == Piece.Color.WHITE ? Clock.Side.WHITE : Clock.Side.BLACK);
            SoundManager.getInstance().playMoveSound();
            selected = null;
            clearLegalMoves();
            clearDrag();
            activeGlideAnim = null;
            activeHintMove = null;
        }
    }

    private void requestAiHint() {
        if (gameOver || hintThinking) return;
        hintThinking = true;
        final Board searchBoard = board.copy();
        new Thread(() -> {
            final ChessAI.AIMove hint = ChessAI.findBestMove(searchBoard, currentTurn, Math.max(aiDepth, 3), aiStyle);
            Gdx.app.postRunnable(() -> {
                hintThinking = false;
                if (hint != null && !gameOver) {
                    activeHintMove = hint;
                    char colChar = (char) ('a' + hint.to.col);
                    int rowNum = 8 - hint.to.row;
                    dialogManager.triggerDialogue(
                        currentTurn == Piece.Color.WHITE ? DialogManager.CharacterType.VAMPIRE : DialogManager.CharacterType.EVILBOX,
                        "[คำแนะนำ]: เดินไปยังช่อง " + colChar + rowNum + " เพื่อกุมความได้เปรียบ!"
                    );
                }
            });
        }).start();
    }

    private void computeLegalMoves(Position from) {
        clearLegalMoves();
        if (from == null) return;
        Piece p = board.getPiece(from);
        if (p == null || p.getColor() != currentTurn) return;
        for (int r = 0; r < 8; r++) for (int c = 0; c < 8; c++) {
            Position to = new Position(r, c);
            if (ChessUtils.isLegalMove(board, from, to, currentTurn)) legalMoves[r][c] = true;
        }
    }

    private void clearLegalMoves() { for (int r=0;r<8;r++) for (int c=0;c<8;c++) legalMoves[r][c] = false; }

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

        // Update character frame animators, particle system, and dialog timers
        vampireAnimator.update(delta);
        evilBoxAnimator.update(delta);
        dialogManager.update(delta);
        particleSystem.update(delta);

        if (abilityNotificationTimer > 0f) {
            abilityNotificationTimer -= delta;
            if (abilityNotificationTimer <= 0f) {
                abilityNotification = "";
            }
        }

        if (activeGlideAnim != null) {
            activeGlideAnim.update(delta, particleSystem);
        }

        Gdx.gl.glClearColor(0.12f, 0.12f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        if (camera == null) camera = new OrthographicCamera();
        camera.setToOrtho(false, screenW, screenH);
        camera.update();

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        float destBoardSize = getBoardSize();
        float offsetX = getOffsetX(destBoardSize);
        float offsetY = getOffsetY();
        float destInset = boardInsetPercent * destBoardSize;
        float innerX = offsetX + destInset;
        float innerY = offsetY + destInset;
        float innerSquare = (destBoardSize - 2 * destInset) / 8f;

        // Board Texture Background
        if (boardTex != null) {
            batch.begin();
            batch.draw(boardTex, offsetX, offsetY, destBoardSize, destBoardSize);
            batch.end();
        } else {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    boolean light = ((r + c) % 2) == 0;
                    if (light) shapes.setColor(0.93f, 0.93f, 0.82f, 1f);
                    else shapes.setColor(0.46f, 0.58f, 0.34f, 1f);
                    float x = innerX + c * innerSquare;
                    float y = innerY + (7 - r) * innerSquare;
                    shapes.rect(x, y, innerSquare, innerSquare);
                }
            }
            shapes.end();
        }

        // Highlight Origin & Destination Squares of Last Move (1 Move Back)
        MoveRecord lastMove = historyManager.getLastMove();
        if (lastMove != null && lastMove.getFrom() != null && lastMove.getTo() != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.95f, 0.82f, 0.25f, 0.32f); // Warm soft golden amber tint under pieces

            Position lFrom = lastMove.getFrom();
            Position lTo = lastMove.getTo();

            float fx = innerX + lFrom.col * innerSquare;
            float fy = innerY + (7 - lFrom.row) * innerSquare;
            shapes.rect(fx, fy, innerSquare, innerSquare);

            float tx = innerX + lTo.col * innerSquare;
            float ty = innerY + (7 - lTo.row) * innerSquare;
            shapes.rect(tx, ty, innerSquare, innerSquare);

            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // Legal Moves Indicator Circles
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (legalMoves[r][c]) {
                    float x = innerX + c * innerSquare + innerSquare / 2f;
                    float y = innerY + (7 - r) * innerSquare + innerSquare / 2f;
                    shapes.setColor(0.15f, 0.15f, 0.15f, 0.75f);
                    shapes.circle(x, y, innerSquare * 0.07f);
                }
            }
        }
        shapes.end();

        // Pulsing Selection Ring around selected square
        if (selected != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(3.5f);
            float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 7.0f);
            shapes.setColor(1.0f, 0.84f, 0.0f, 0.6f + pulse * 0.4f);
            float sx = innerX + selected.col * innerSquare;
            float sy = innerY + (7 - selected.row) * innerSquare;
            shapes.rect(sx + 2f, sy + 2f, innerSquare - 4f, innerSquare - 4f);
            shapes.end();
            Gdx.gl.glLineWidth(1.0f);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // Origin Square Highlight while Dragging
        if (isDragging && dragSource != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1.0f, 0.9f, 0.3f, 0.35f);
            float ox = innerX + dragSource.col * innerSquare;
            float oy = innerY + (7 - dragSource.row) * innerSquare;
            shapes.rect(ox, oy, innerSquare, innerSquare);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // Hover Target Square Highlight while Dragging
        if (isDragging) {
            Position hoverPos = getBoardPosition(dragCurrentX, dragCurrentY);
            if (hoverPos != null && !hoverPos.equals(dragSource)) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                if (legalMoves[hoverPos.row][hoverPos.col]) {
                    shapes.setColor(0.2f, 0.85f, 0.35f, 0.38f);
                } else {
                    shapes.setColor(0.85f, 0.25f, 0.25f, 0.22f);
                }
                float hx = innerX + hoverPos.col * innerSquare;
                float hy = innerY + (7 - hoverPos.row) * innerSquare;
                shapes.rect(hx, hy, innerSquare, innerSquare);
                shapes.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }

        // Render Pieces on the Board
        batch.begin();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                // If this square is currently receiving a gliding piece, hide static render until glide completes
                if (activeGlideAnim != null && !activeGlideAnim.isFinished() &&
                    r == activeGlideAnim.getToPos().row && c == activeGlideAnim.getToPos().col) {
                    continue;
                }

                // If this piece is currently being dragged, don't draw it static on the board
                if (isDragging && dragSource != null && r == dragSource.row && c == dragSource.col) {
                    continue;
                }

                Position ppos = new Position(r, c);
                Piece p = board.getPiece(ppos);
                float x = innerX + c * innerSquare;
                float y = innerY + (7 - r) * innerSquare;
                if (p != null) {
                    String key = (p.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(p.getSymbol());
                    Texture t = pieceTex.get(key);
                    if (t != null) {
                        float pieceBaseSize = innerSquare * 0.82f;
                        float textureRatio = (float) t.getHeight() / t.getWidth();
                        float drawWidth = pieceBaseSize;
                        float drawHeight = pieceBaseSize * textureRatio;
                        float drawX = x + (innerSquare - drawWidth) / 2f;
                        float drawY = y + (innerSquare - pieceBaseSize) / 2f;
                        batch.draw(t, drawX, drawY, drawWidth, drawHeight);
                    } else {
                        font.setColor(p.getColor() == Piece.Color.WHITE ? Color.WHITE : Color.BLACK);
                        font.getData().setScale(innerSquare / 32f);
                        font.draw(batch, getUnicodeFor(p), x + innerSquare * 0.28f, y + innerSquare * 0.7f);
                    }
                }
            }
        }
        batch.end();

        // Directional Arrow for Last Move (1 Move Back)
        if (lastMove != null && lastMove.getFrom() != null && lastMove.getTo() != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);

            Position lFrom = lastMove.getFrom();
            Position lTo = lastMove.getTo();
            float pFromX = innerX + (lFrom.col + 0.5f) * innerSquare;
            float pFromY = innerY + (7 - lFrom.row + 0.5f) * innerSquare;
            float pToX = innerX + (lTo.col + 0.5f) * innerSquare;
            float pToY = innerY + (7 - lTo.row + 0.5f) * innerSquare;

            Color arrowColor;
            if (lastMove.getMovedPiece() != null && lastMove.getMovedPiece().getColor() == Piece.Color.BLACK) {
                arrowColor = new Color(0.18f, 0.82f, 0.98f, 0.85f); // Vibrant Cyan for EvilBox / Black
            } else {
                arrowColor = new Color(1.0f, 0.78f, 0.18f, 0.85f); // Radiant Amber Gold for White
            }

            drawDirectionalArrow(pFromX, pFromY, pToX, pToY, arrowColor);

            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        batch.begin();

        // Draw Gliding Moving Piece Texture
        if (activeGlideAnim != null && !activeGlideAnim.isFinished()) {
            Piece p = activeGlideAnim.getPiece();
            if (p != null) {
                String key = (p.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(p.getSymbol());
                Texture t = pieceTex.get(key);
                if (t != null) {
                    float pieceBaseSize = innerSquare * 0.82f;
                    float textureRatio = (float) t.getHeight() / t.getWidth();
                    float drawWidth = pieceBaseSize;
                    float drawHeight = pieceBaseSize * textureRatio;
                    batch.draw(t, activeGlideAnim.getCurrentX(), activeGlideAnim.getCurrentY(), drawWidth, drawHeight);
                }
            }
        }

        // Draw Dragged Piece floating under cursor (elevated on top of board)
        if (isDragging && draggedPiece != null) {
            String key = (draggedPiece.getColor() == Piece.Color.WHITE ? "w_" : "b_") + Character.toUpperCase(draggedPiece.getSymbol());
            Texture t = pieceTex.get(key);
            float pieceBaseSize = innerSquare * 0.95f;
            if (t != null) {
                float textureRatio = (float) t.getHeight() / t.getWidth();
                float drawWidth = pieceBaseSize;
                float drawHeight = pieceBaseSize * textureRatio;
                float drawX = dragCurrentX - drawWidth / 2f;
                float drawY = dragCurrentY - drawHeight / 2f;
                batch.draw(t, drawX, drawY, drawWidth, drawHeight);
            } else {
                font.setColor(draggedPiece.getColor() == Piece.Color.WHITE ? Color.WHITE : Color.BLACK);
                font.getData().setScale(innerSquare / 28f);
                font.draw(batch, getUnicodeFor(draggedPiece), dragCurrentX - innerSquare * 0.2f, dragCurrentY + innerSquare * 0.3f);
            }
        }

        // Draw Coordinates (a-h and 1-8) in Chess.com / Lichess style inside board corners
        if (coordFont != null) {
            // File letters (a-h) in bottom row
            for (int c = 0; c < 8; c++) {
                char colChar = (char) ('a' + c);
                boolean isLight = ((7 + c) % 2) == 0;
                coordFont.setColor(isLight ? new Color(0.35f, 0.45f, 0.30f, 0.95f) : new Color(0.92f, 0.92f, 0.82f, 0.95f));
                float lx = innerX + c * innerSquare + innerSquare - 14f;
                float ly = innerY + 16f;
                coordFont.draw(batch, String.valueOf(colChar), lx, ly);
            }
            // Rank numbers (1-8) in left column
            for (int r = 0; r < 8; r++) {
                int rowNum = 8 - r;
                boolean isLight = ((r + 0) % 2) == 0;
                coordFont.setColor(isLight ? new Color(0.35f, 0.45f, 0.30f, 0.95f) : new Color(0.92f, 0.92f, 0.82f, 0.95f));
                float lx = innerX + 4f;
                float ly = innerY + (7 - r) * innerSquare + innerSquare - 4f;
                coordFont.draw(batch, String.valueOf(rowNum), lx, ly);
            }

            // Selected square indicator badge (e.g. "e4")
            if (selected != null) {
                char colChar = (char) ('a' + selected.col);
                int rowNum = 8 - selected.row;
                String squareName = "" + colChar + rowNum;
                font.setColor(Color.GOLD);
                font.getData().setScale(0.85f);
                float sx = innerX + selected.col * innerSquare + 6f;
                float sy = innerY + (7 - selected.row) * innerSquare + 22f;
                font.draw(batch, squareName, sx, sy);
            }
        }

        batch.end();

        // --- Render Evaluation Bar (Vertical Advantage Bar beside board) ---
        if (showEvalBar) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            float evalX = offsetX - 20f;
            float evalY = offsetY;
            float evalW = 12f;
            float evalH = destBoardSize;

            shapes.setColor(0.12f, 0.12f, 0.15f, 1f);
            shapes.rect(evalX - 2f, evalY - 2f, evalW + 4f, evalH + 4f);

            int rawScore = com.chessegame.ai.Evaluation.evaluateStyled(board, aiStyle);
            float ratio = 0.5f + (rawScore / 3000.0f);
            ratio = Math.max(0.05f, Math.min(0.95f, ratio));

            float whiteH = evalH * ratio;
            float blackH = evalH - whiteH;

            // Black Ratio (EvilBox cyan) top
            shapes.setColor(0.1f, 0.75f, 0.95f, 1f);
            shapes.rect(evalX, evalY + whiteH, evalW, blackH);

            // White Ratio (Vampire coral) bottom
            shapes.setColor(0.95f, 0.35f, 0.25f, 1f);
            shapes.rect(evalX, evalY, evalW, whiteH);
            shapes.end();
        }

        // --- Render AI Hint Visual Highlights (Pulsing Emerald Green Aura) ---
        if (activeHintMove != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);

            float pulse = 0.65f + 0.35f * (float) Math.sin(stateTime * 6.0f);
            shapes.setColor(0.1f, 0.95f, 0.5f, 0.45f * pulse);

            float fX = innerX + activeHintMove.from.col * innerSquare;
            float fY = innerY + (7 - activeHintMove.from.row) * innerSquare;
            shapes.rect(fX, fY, innerSquare, innerSquare);

            float tX = innerX + activeHintMove.to.col * innerSquare;
            float tY = innerY + (7 - activeHintMove.to.row) * innerSquare;
            shapes.rect(tX, tY, innerSquare, innerSquare);

            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(3.0f);
            shapes.setColor(0.1f, 0.95f, 0.5f, 0.9f * pulse);
            shapes.rect(fX + 1f, fY + 1f, innerSquare - 2f, innerSquare - 2f);
            shapes.rect(tX + 1f, tY + 1f, innerSquare - 2f, innerSquare - 2f);
            shapes.end();
            Gdx.gl.glLineWidth(1.0f);
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // Render Particle System Effects (Move trails, Capture bursts, Victory confetti)
        particleSystem.render(shapes);

        float headerY = Gdx.graphics.getHeight() - HEADER_BAR_HEIGHT;
        float vX = 14f;
        float vY = Gdx.graphics.getHeight() - 66f;
        float eX = Gdx.graphics.getWidth() - 70f;
        float eY = Gdx.graphics.getHeight() - 66f;

        // --- 1. Top Header Bar & Background Cards (Filled Shapes) ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Header Bar Background
        shapes.setColor(0.13f, 0.13f, 0.16f, 1.0f);
        shapes.rect(0f, headerY, Gdx.graphics.getWidth(), HEADER_BAR_HEIGHT);

        // Avatar Background Cards
        shapes.setColor(0.18f, 0.18f, 0.23f, 1f);
        shapes.rect(vX - 4f, vY - 4f, 60f, 60f);
        shapes.rect(eX - 4f, eY - 4f, 60f, 60f);

        // Header Music Toggle Pill
        float musicBtnX = vX + 66f;
        float musicBtnY = headerY + 6f;
        float musicBtnW = 115f;
        float musicBtnH = 20f;
        boolean musicMuted = MusicManager.getInstance().isMuted();
        shapes.setColor(musicMuted ? new Color(0.45f, 0.18f, 0.18f, 0.95f) : new Color(0.18f, 0.55f, 0.35f, 0.95f));
        shapes.rect(musicBtnX, musicBtnY, musicBtnW, musicBtnH);

        // Header Draw Offer & Resign Buttons
        float drawBtnW = 86f;
        float resignBtnW = 86f;
        float drawBtnX = eX - 180f;
        float resignBtnX = drawBtnX + drawBtnW + 8f;
        float actionBtnH = 20f;
        float actionBtnY = headerY + 6f;

        // Draw Offer Button Pill (Teal / Slate)
        shapes.setColor(new Color(0.20f, 0.48f, 0.55f, 0.95f));
        shapes.rect(drawBtnX, actionBtnY, drawBtnW, actionBtnH);

        // Resign Button Pill (Crimson Red)
        shapes.setColor(new Color(0.70f, 0.22f, 0.22f, 0.95f));
        shapes.rect(resignBtnX, actionBtnY, resignBtnW, actionBtnH);

        // Trigger AI Bot Turn (Async Background Search)
        if (isVsAi && currentTurn == aiColor && !gameOver && !overlayVisible && !promotionModalVisible && !aiThinking) {
            aiThinking = true;
            final Board searchBoard = board.copy();
            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                final ChessAI.AIMove aiMove = ChessAI.findBestMove(searchBoard, aiColor, aiDepth, aiStyle);

                long computeTime = System.currentTimeMillis() - startTime;
                long adaptiveDelay = Math.max(100L, 350L - computeTime);
                try {
                    Thread.sleep(adaptiveDelay);
                } catch (InterruptedException ignored) {}
                Gdx.app.postRunnable(() -> {
                    aiThinking = false;
                    if (aiMove != null && currentTurn == aiColor && !gameOver) {
                        completeMove(aiMove.from, aiMove.to, aiMove.promoChoice);
                    }
                });
            }).start();
        }

        // Bottom Control Bar Background
        shapes.setColor(0.14f, 0.14f, 0.17f, 1f);
        shapes.rect(0f, 0f, Gdx.graphics.getWidth(), BUTTON_BAR_HEIGHT + 4f);

        float gap = 10f;
        float btnW = (destBoardSize - 3f * gap) / 4f;
        float rowH = 32f;
        float row2Y = 44f;
        float row1Y = 8f;

        // --- Row 2 (Top Row - Game Action Buttons) ---
        // Button 1: New Game (Gold)
        shapes.setColor(0.95f, 0.78f, 0.32f, 1f);
        shapes.rect(offsetX + 0 * (btnW + gap), row2Y, btnW, rowH);

        // Button 2: Undo (Slate Blue)
        shapes.setColor(historyManager.canUndo() ? new Color(0.3f, 0.5f, 0.8f, 1f) : new Color(0.2f, 0.25f, 0.3f, 1f));
        shapes.rect(offsetX + 1 * (btnW + gap), row2Y, btnW, rowH);

        // Button 3: Redo (Slate Green)
        shapes.setColor(historyManager.canRedo() ? new Color(0.3f, 0.7f, 0.5f, 1f) : new Color(0.2f, 0.25f, 0.3f, 1f));
        shapes.rect(offsetX + 2 * (btnW + gap), row2Y, btnW, rowH);

        // Button 4: AI Hint Request (Emerald Green)
        shapes.setColor(hintThinking ? new Color(0.2f, 0.4f, 0.3f, 1f) : new Color(0.15f, 0.75f, 0.45f, 1f));
        shapes.rect(offsetX + 3 * (btnW + gap), row2Y, btnW, rowH);

        // --- Row 1 (Bottom Row - AI & Settings Buttons) ---
        // Button 5: Game Mode (Purple)
        shapes.setColor(isVsAi ? new Color(0.55f, 0.35f, 0.85f, 1f) : new Color(0.35f, 0.4f, 0.5f, 1f));
        shapes.rect(offsetX + 0 * (btnW + gap), row1Y, btnW, rowH);

        // Button 6: AI Depth (Dark Blue)
        shapes.setColor(isVsAi ? new Color(0.2f, 0.45f, 0.75f, 1f) : new Color(0.25f, 0.25f, 0.3f, 1f));
        shapes.rect(offsetX + 1 * (btnW + gap), row1Y, btnW, rowH);

        // Button 7: AI Boss Style (Crimson Red)
        shapes.setColor(isVsAi ? new Color(0.85f, 0.35f, 0.25f, 1f) : new Color(0.25f, 0.25f, 0.3f, 1f));
        shapes.rect(offsetX + 2 * (btnW + gap), row1Y, btnW, rowH);

        // Button 8: Eval Bar Toggle (Teal)
        shapes.setColor(showEvalBar ? new Color(0.15f, 0.65f, 0.7f, 1f) : new Color(0.25f, 0.25f, 0.3f, 1f));
        shapes.rect(offsetX + 3 * (btnW + gap), row1Y, btnW, rowH);

        // Real-Time Dialogue Banner Card (Center Header)
        if (dialogManager.isDialogueActive()) {
            shapes.setColor(0.09f, 0.09f, 0.12f, 0.95f);
            float dlgX = 225f;
            float dlgW = Gdx.graphics.getWidth() - 450f;
            if (dlgW < 200f) {
                dlgX = 170f;
                dlgW = Gdx.graphics.getWidth() - 340f;
            }
            float dlgH = 48f;
            float dlgY = Gdx.graphics.getHeight() - 62f;
            shapes.rect(dlgX, dlgY, dlgW, dlgH);

            // Active Speaker Color Bar
            shapes.setColor(dialogManager.getCurrentSpeaker() == DialogManager.CharacterType.VAMPIRE ?
                    new Color(0.95f, 0.35f, 0.25f, 1f) : new Color(0.1f, 0.85f, 1.0f, 1f));
            shapes.rect(dlgX, dlgY, 4f, dlgH);
        }
        shapes.end();

        // --- 2. Active Player Glow Border around Avatar Card ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3.0f);
        if (currentTurn == Piece.Color.WHITE) {
            shapes.setColor(0.95f, 0.35f, 0.25f, 0.9f);
            shapes.rect(vX - 4f, vY - 4f, 60f, 60f);
        } else {
            shapes.setColor(0.2f, 0.85f, 1.0f, 0.9f);
            shapes.rect(eX - 4f, eY - 4f, 60f, 60f);
        }
        shapes.end();
        Gdx.gl.glLineWidth(1.0f);
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- 3. Draw Character Textures (SpriteBatch) ---
        batch.begin();
        int vIdx = vampireAnimator.getCurrentFrameIndex();
        if (vampireTextures[vIdx] != null) {
            batch.draw(vampireTextures[vIdx], vX, vY, 52f, 52f);
        }
        int eIdx = evilBoxAnimator.getCurrentFrameIndex();
        if (eIdx >= 0 && eIdx < evilBoxTextures.length && evilBoxTextures[eIdx] != null) {
            batch.draw(evilBoxTextures[eIdx], eX, eY, 52f, 52f);
        }

        // --- 4. Render Clocks, Player Titles, and Dialogue Text ---
        font.setColor(Color.WHITE);
        font.getData().setScale(0.95f);
        String whiteClock = "Vampire: " + Clock.formatMs(clock.getRemaining(Clock.Side.WHITE));
        String opponentName = (bossLevel != null) ? bossLevel.getBossName() : (isVsAi ? "Bot EvilBox" : "EvilBox");
        String blackClock = opponentName + ": " + Clock.formatMs(clock.getRemaining(Clock.Side.BLACK));

        // White Player Info (Left side, right of Vampire avatar)
        font.draw(batch, whiteClock, vX + 66f, Gdx.graphics.getHeight() - 16f);
        font.setColor(new Color(0.9f, 0.9f, 0.95f, 1f));
        font.getData().setScale(0.82f);
        font.draw(batch, "ผู้เล่นหมากขาว", vX + 66f, Gdx.graphics.getHeight() - 42f);

        // Music Toggle Label
        font.setColor(Color.WHITE);
        font.getData().setScale(0.72f);
        font.draw(batch, musicMuted ? "✕ เพลง: ปิด [M]" : "♫ เพลง: เปิด [M]", musicBtnX + 8f, musicBtnY + 15f);

        // Draw Offer & Resign Button Labels (Under Black Info)
        font.setColor(Color.WHITE);
        font.getData().setScale(0.70f);
        font.draw(batch, "ขอเสมอ", drawBtnX + 18f, actionBtnY + 14f);
        font.draw(batch, "ยอมแพ้", resignBtnX + 18f, actionBtnY + 14f);

        // Black Player Info (Right side, left of EvilBox avatar)
        font.setColor(Color.WHITE);
        font.getData().setScale(0.95f);
        font.draw(batch, blackClock, eX - 180f, Gdx.graphics.getHeight() - 16f);
        font.setColor(new Color(0.9f, 0.9f, 0.95f, 1f));
        font.getData().setScale(0.82f);
        String subLabel = (bossLevel != null) ? bossLevel.getHandicap().getTitle() : (isVsAi ? "AI (หมากดำ)" : "ผู้เล่นหมากดำ");
        font.draw(batch, subLabel, eX - 180f, Gdx.graphics.getHeight() - 42f);

        // Dialogue Text in Center Banner Card
        if (dialogManager.isDialogueActive()) {
            font.getData().setScale(0.90f);
            font.setColor(dialogManager.getCurrentSpeaker() == DialogManager.CharacterType.VAMPIRE ?
                    Color.CORAL : Color.CYAN);
            float dlgX = 225f;
            float dlgW = Gdx.graphics.getWidth() - 450f;
            if (dlgW < 200f) {
                dlgX = 170f;
            }
            font.draw(batch, dialogManager.getCurrentDialogue(), dlgX + 14f, Gdx.graphics.getHeight() - 30f);
        }

        // Ability Notification Text Banner
        if (!abilityNotification.isEmpty()) {
            font.getData().setScale(0.85f);
            font.setColor(abilityNotificationColor);
            float notifX = 225f;
            float notifW = Gdx.graphics.getWidth() - 450f;
            if (notifW < 200f) notifX = 170f;
            font.draw(batch, abilityNotification, notifX + 14f, Gdx.graphics.getHeight() - 50f);
        }

        // --- Action Button Text Labels (2-Row Clean Layout) ---
        font.getData().setScale(0.82f);

        // Row 2 Text Labels (Top Row: Game Actions)
        font.setColor(Color.BLACK);
        font.draw(batch, "เกมใหม่", offsetX + 0 * (btnW + gap) + (btnW - 52f) / 2f, row2Y + 22f);

        font.setColor(historyManager.canUndo() ? Color.WHITE : Color.GRAY);
        font.draw(batch, "ย้อนหมาก", offsetX + 1 * (btnW + gap) + (btnW - 62f) / 2f, row2Y + 22f);

        font.setColor(historyManager.canRedo() ? Color.WHITE : Color.GRAY);
        font.draw(batch, "ทำซ้ำ", offsetX + 2 * (btnW + gap) + (btnW - 42f) / 2f, row2Y + 22f);

        font.setColor(Color.WHITE);
        font.draw(batch, "คำแนะนำ", offsetX + 3 * (btnW + gap) + (btnW - 60f) / 2f, row2Y + 22f);

        // Row 1 Text Labels (Bottom Row: AI Settings)
        font.draw(batch, isVsAi ? "โหมด: vs AI" : "โหมด: 2 คน", offsetX + 0 * (btnW + gap) + (btnW - 82f) / 2f, row1Y + 22f);

        font.draw(batch, "Depth: " + aiDepth, offsetX + 1 * (btnW + gap) + (btnW - 58f) / 2f, row1Y + 22f);

        String styleLabel = "สไตล์: เซียน";
        if (aiStyle == com.chessegame.ai.Evaluation.AIStyle.AGGRESSIVE) styleLabel = "สไตล์: สายบุก";
        else if (aiStyle == com.chessegame.ai.Evaluation.AIStyle.DEFENSIVE) styleLabel = "สไตล์: สายรับ";
        font.draw(batch, styleLabel, offsetX + 2 * (btnW + gap) + (btnW - 82f) / 2f, row1Y + 22f);

        font.draw(batch, showEvalBar ? "Eval: เปิด" : "Eval: ปิด", offsetX + 3 * (btnW + gap) + (btnW - 64f) / 2f, row1Y + 22f);

        // End the main text batch
        if (batch.isDrawing()) {
            batch.end();
        }

        // --- 5. Pawn Promotion Choice Modal Overlay ---
        if (promotionModalVisible) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.75f);
            shapes.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            float modalW = 380f;
            float modalH = 140f;
            float modalX = (Gdx.graphics.getWidth() - modalW) / 2f;
            float modalY = (Gdx.graphics.getHeight() - modalH) / 2f;

            shapes.setColor(0.15f, 0.15f, 0.20f, 0.98f);
            shapes.rect(modalX, modalY, modalW, modalH);

            float cardW = 80f;
            float cardH = 75f;
            float startCardX = modalX + 15f;
            float cardY = modalY + 15f;
            for (int i = 0; i < 4; i++) {
                shapes.setColor(0.26f, 0.28f, 0.36f, 1f);
                shapes.rect(startCardX + i * 90f, cardY, cardW, cardH);
            }
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);

            batch.begin();
            font.setColor(Color.GOLD);
            font.getData().setScale(1.05f);
            font.draw(batch, "👑 เลือกหมากโปรโมตเบี้ย", modalX + 80f, modalY + 125f);

            String[] promoTitles = {"ควีน (Q)", "เรือ (R)", "บิชอป (B)", "อัศวิน (N)"};
            font.setColor(Color.WHITE);
            font.getData().setScale(0.80f);
            for (int i = 0; i < 4; i++) {
                font.draw(batch, promoTitles[i], startCardX + i * 90f + 6f, cardY + 45f);
            }
            batch.end();
        }

        // --- 6. Game Over / Victory Dialogue Modal Overlay ---
        if (overlayVisible) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.75f);
            shapes.rect(0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

            float dialogW = Math.max(480f, Gdx.graphics.getWidth() * 0.58f);
            float dialogH = 210f;
            float dialogX = (Gdx.graphics.getWidth() - dialogW) / 2f;
            float dialogY = (Gdx.graphics.getHeight() - dialogH) / 2f;

            shapes.setColor(0.14f, 0.14f, 0.18f, 0.98f);
            shapes.rect(dialogX, dialogY, dialogW, dialogH);

            float gapD = 10f;
            float btnDW = (dialogW - 40f) / 3f;
            float btnDH = 40f;
            float btnDY = dialogY + 20f;

            // Button 1: Game Review (Teal/Cyan)
            shapes.setColor(0.15f, 0.75f, 0.85f, 1f);
            shapes.rect(dialogX + 10f, btnDY, btnDW, btnDH);

            // Button 2: Play Again (Gold)
            shapes.setColor(0.95f, 0.75f, 0.25f, 1f);
            shapes.rect(dialogX + 10f + (btnDW + gapD), btnDY, btnDW, btnDH);

            // Button 3: Main Menu (Slate Blue)
            shapes.setColor(0.35f, 0.45f, 0.65f, 1f);
            shapes.rect(dialogX + 10f + 2 * (btnDW + gapD), btnDY, btnDW, btnDH);

            // Gold border
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(3.0f);
            shapes.setColor(0.95f, 0.78f, 0.32f, 0.9f);
            shapes.rect(dialogX, dialogY, dialogW, dialogH);
            shapes.end();
            Gdx.gl.glLineWidth(1.0f);
            Gdx.gl.glDisable(GL20.GL_BLEND);

            batch.begin();
            font.setColor(Color.GOLD);
            font.getData().setScale(1.25f);
            String titleText = (overlayTitle != null && !overlayTitle.isEmpty()) ? overlayTitle : "จบการแข่งขัน!";
            font.draw(batch, titleText, dialogX + (dialogW - titleText.length() * 14f) / 2f, dialogY + dialogH - 26f);

            font.setColor(Color.WHITE);
            font.getData().setScale(1.0f);
            String msgText = (overlayMessage != null && !overlayMessage.isEmpty()) ? overlayMessage : "ผู้เล่นชนะการแข่งขัน!";
            font.draw(batch, msgText, dialogX + (dialogW - msgText.length() * 9f) / 2f, dialogY + dialogH - 72f);

            // Button text labels
            font.getData().setScale(0.85f);
            font.setColor(Color.BLACK);
            font.draw(batch, "🔍 วิเคราะห์เกม", dialogX + 10f + (btnDW - 110f) / 2f, btnDY + 26f);
            font.draw(batch, "🔄 เล่นใหม่", dialogX + 10f + (btnDW + gapD) + (btnDW - 82f) / 2f, btnDY + 26f);
            font.setColor(Color.WHITE);
            font.draw(batch, "🏠 เมนูหลัก", dialogX + 10f + 2 * (btnDW + gapD) + (btnDW - 82f) / 2f, btnDY + 26f);
            batch.end();
        }
    }

    private String getUnicodeFor(Piece p){
        if (p == null) return "";
        char s = p.getSymbol();
        switch (Character.toUpperCase(s)){
            case 'K': return p.getColor() == Piece.Color.WHITE ? "\u2654" : "\u265A";
            case 'Q': return p.getColor() == Piece.Color.WHITE ? "\u2655" : "\u265B";
            case 'R': return p.getColor() == Piece.Color.WHITE ? "\u2656" : "\u265C";
            case 'B': return p.getColor() == Piece.Color.WHITE ? "\u2657" : "\u265D";
            case 'N': return p.getColor() == Piece.Color.WHITE ? "\u2658" : "\u265E";
            case 'P': return p.getColor() == Piece.Color.WHITE ? "\u2659" : "\u265F";
            default: return String.valueOf(s);
        }
    }

    private void drawDirectionalArrow(float fromX, float fromY, float toX, float toY, Color mainColor) {
        float dx = toX - fromX;
        float dy = toY - fromY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0.001f) return;

        float nx = dx / len;
        float ny = dy / len;

        // Offset start slightly from center of source square for a clean aesthetic
        float startOffset = Math.min(14f, len * 0.15f);
        float startX = fromX + nx * startOffset;
        float startY = fromY + ny * startOffset;

        // Stop slightly before center of target square so arrowhead tip is crisp
        float endOffset = Math.min(6f, len * 0.08f);
        float endX = toX - nx * endOffset;
        float endY = toY - ny * endOffset;

        float effectiveLen = len - startOffset - endOffset;
        if (effectiveLen <= 5f) return;

        float shaftWidth = 6.5f;
        float headLen = Math.min(22f, effectiveLen * 0.45f);
        float headWidth = 24f;

        // 1. Dark high-contrast outline / shadow for crisp visibility on all board squares
        Color outlineColor = new Color(0.04f, 0.05f, 0.07f, 0.50f);
        drawSingleArrow(startX - nx * 1.5f, startY - ny * 1.5f, endX + nx * 2.5f, endY + ny * 2.5f,
                nx, ny, effectiveLen + 4.0f, shaftWidth + 4.5f, headLen + 3.0f, headWidth + 6.0f, outlineColor);

        // 2. Main vibrant arrow body
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
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        java.util.Set<Texture> disposed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Texture t : pieceTex.values()) if (t != null && disposed.add(t)) t.dispose();
        for (Texture t : vampireTextures) if (t != null && disposed.add(t)) t.dispose();
        for (Texture t : evilBoxTextures) if (t != null && disposed.add(t)) t.dispose();
        if (boardTex != null) boardTex.dispose();
    }
}
