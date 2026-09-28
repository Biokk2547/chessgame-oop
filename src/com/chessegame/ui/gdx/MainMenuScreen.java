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
import com.chessegame.ai.Evaluation;
import com.chessegame.audio.MusicManager;
import com.chessegame.particle.ParticleSystem;

/**
 * MainMenuScreen: Premium main menu title screen with animated character avatars,
 * floating particle effects, match setup controls, and smooth screen navigation.
 */
public class MainMenuScreen extends ScreenAdapter {
    private final LibGdxChessApp app;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont titleFont;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private ParticleSystem particleSystem;
    private Texture backgroundTex;

    // Match Setup Settings
    private boolean isVsAi = true;
    private int aiDepth = 3;
    private Evaluation.AIStyle aiStyle = Evaluation.AIStyle.MASTER;
    private float stateTime = 0f;

    public MainMenuScreen(LibGdxChessApp app) {
        this.app = app;
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
        return null;
    }

    private boolean firstRenderLogged = false;

    @Override
    public void show() {
        System.out.println("[DEBUG] MainMenuScreen.show() STARTED");
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        particleSystem = new ParticleSystem();

        // Load FreeType Fonts via resolveAsset
        try {
            System.out.println("[DEBUG] Resolving fonts/thai.ttf");
            com.badlogic.gdx.files.FileHandle ttfFile = resolveAsset("fonts/thai.ttf");
            if (ttfFile != null && ttfFile.exists()) {
                System.out.println("[DEBUG] Generating FreeType fonts from " + ttfFile.path());
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttfFile);
                StringBuilder sb = new StringBuilder();
                for (char c = '\u0E01'; c <= '\u0E5B'; c++) {
                    sb.append(c);
                }

                // Subtitle / Button Font (21px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
                param.size = 21;
                param.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + "•+-*!?:.()[]'\"%/#1234567890<>";
                param.minFilter = Texture.TextureFilter.Linear;
                param.magFilter = Texture.TextureFilter.Linear;
                param.borderWidth = 1.2f;
                param.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                font = gen.generateFont(param);

                // Title Font (34px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter titleParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
                titleParam.size = 34;
                titleParam.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + "•+-*!?:.()[]'\"%/#1234567890<>";
                titleParam.minFilter = Texture.TextureFilter.Linear;
                titleParam.magFilter = Texture.TextureFilter.Linear;
                titleParam.borderWidth = 1.4f;
                titleParam.borderColor = new Color(0.02f, 0.02f, 0.05f, 0.95f);
                titleFont = gen.generateFont(titleParam);

                gen.dispose();
                System.out.println("[DEBUG] FreeType fonts generated successfully");
            } else {
                System.out.println("[DEBUG] thai.ttf not found, using BitmapFont fallback");
                font = new BitmapFont();
                titleFont = new BitmapFont();
            }
        } catch (Throwable e) {
            System.out.println("[DEBUG] Font loading error: " + e.getMessage());
            font = new BitmapFont();
            titleFont = new BitmapFont();
        }

        // Load Background Graphic
        try {
            com.badlogic.gdx.files.FileHandle bfh = resolveAsset("background.png");
            if (bfh == null || !bfh.exists()) {
                bfh = resolveAsset("charactor/assetbackgroud/background.png");
            }
            if (bfh != null && bfh.exists()) {
                backgroundTex = new Texture(bfh);
                System.out.println("[DEBUG] Loaded background: " + bfh.path());
            }
        } catch (Throwable ignored) {}

        System.out.println("[DEBUG] MainMenuScreen.show() COMPLETED successfully");

        // Ensure background music is running
        try {
            MusicManager.getInstance().playBGM();
        } catch (Throwable ignored) {}

        // Input Processor for Main Menu Buttons
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
                float y = Gdx.graphics.getHeight() - screenY;
                float x = screenX;
                float menuW = 340f;
                float menuX = (Gdx.graphics.getWidth() - menuW) / 2f;
                float btnH = 44f;

                // Top-Right Music Toggle Button
                float musicBtnW = 150f;
                float musicBtnH = 38f;
                float musicBtnX = Gdx.graphics.getWidth() - musicBtnW - 20f;
                float musicBtnY = Gdx.graphics.getHeight() - musicBtnH - 20f;
                if (x >= musicBtnX && x <= musicBtnX + musicBtnW && y >= musicBtnY && y <= musicBtnY + musicBtnH) {
                    MusicManager.getInstance().toggleMute();
                    return true;
                }

                // Button 1: Quick Play (y: 480f - 524f)
                if (x >= menuX && x <= menuX + menuW && y >= 480f && y <= 480f + btnH) {
                    app.setScreen(new GameScreen(app, isVsAi, aiDepth, aiStyle));
                    return true;
                }
                // Button 2: Boss Rush Mode (y: 422f - 466f)
                if (x >= menuX && x <= menuX + menuW && y >= 422f && y <= 422f + btnH) {
                    app.setScreen(new BossLevelSelectScreen(app));
                    return true;
                }
                // Button 3: Opening Practice Mode (y: 364f - 408f)
                if (x >= menuX && x <= menuX + menuW && y >= 364f && y <= 364f + btnH) {
                    app.setScreen(new OpeningSelectScreen(app));
                    return true;
                }
                // Button 4: Puzzle Mode (y: 306f - 350f)
                if (x >= menuX && x <= menuX + menuW && y >= 306f && y <= 306f + btnH) {
                    app.setScreen(new PuzzleScreen(app));
                    return true;
                }
                // Button 5: Toggle Game Mode (y: 248f - 292f)
                if (x >= menuX && x <= menuX + menuW && y >= 248f && y <= 248f + btnH) {
                    isVsAi = !isVsAi;
                    return true;
                }
                // Button 6: AI Depth Stepper (y: 190f - 234f)
                if (x >= menuX && x <= menuX + menuW && y >= 190f && y <= 190f + btnH) {
                    aiDepth++;
                    if (aiDepth > 6) aiDepth = 1;
                    return true;
                }
                // Button 7: AI Boss Style (y: 132f - 176f)
                if (x >= menuX && x <= menuX + menuW && y >= 132f && y <= 132f + btnH) {
                    if (aiStyle == Evaluation.AIStyle.MASTER) aiStyle = Evaluation.AIStyle.AGGRESSIVE;
                    else if (aiStyle == Evaluation.AIStyle.AGGRESSIVE) aiStyle = Evaluation.AIStyle.DEFENSIVE;
                    else aiStyle = Evaluation.AIStyle.MASTER;
                    return true;
                }
                // Button 8: Exit Game (y: 54f - 98f)
                if (x >= menuX && x <= menuX + menuW && y >= 54f && y <= 54f + btnH) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });
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
        if (!firstRenderLogged) {
            firstRenderLogged = true;
            System.out.println("[DEBUG] MainMenuScreen.render() FIRST FRAME! screenW=" + Gdx.graphics.getWidth() + ", screenH=" + Gdx.graphics.getHeight());
        }
        stateTime += delta;
        particleSystem.update(delta);

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        if (camera == null) camera = new OrthographicCamera();
        camera.setToOrtho(false, screenW, screenH);
        camera.update();

        Gdx.gl.glViewport(0, 0, screenW, screenH);
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        // 1. Draw Gothic Cathedral Background Image
        if (backgroundTex != null) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(backgroundTex, 0f, 0f, (float) screenW, (float) screenH);
            batch.end();
        }

        // 2. Translucent Vignette / Shadow Overlay to keep UI elements, text, and avatars ultra clear
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.06f, 0.10f, backgroundTex != null ? 0.62f : 1.0f);
        shapes.rect(0f, 0f, (float) screenW, (float) screenH);
        shapes.end();

        // 3. Render Background Particle Effects
        particleSystem.render(shapes);

        float centerX = Gdx.graphics.getWidth() / 2f;
        float menuW = 340f;
        float menuX = (Gdx.graphics.getWidth() - menuW) / 2f;
        float btnH = 44f;

        // --- 4. Filled Shapes (Backdrop & Menu Action Cards) ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Header Title Card Backdrop
        shapes.setColor(0.10f, 0.11f, 0.16f, 0.95f);
        shapes.rect(centerX - 240f, Gdx.graphics.getHeight() - 110f, 480f, 86f);
        // Header gold underline accent
        shapes.setColor(0.95f, 0.75f, 0.25f, 0.90f);
        shapes.rect(centerX - 240f, Gdx.graphics.getHeight() - 110f, 480f, 3f);

        // Button 1: Quick Play (Vibrant Emerald Green)
        shapes.setColor(0.12f, 0.68f, 0.40f, 1f);
        shapes.rect(menuX, 480f, menuW, btnH);

        // Button 2: Boss Rush Mode (Warm Amber / Gold with pulse)
        float pulse = 0.92f + 0.08f * (float) Math.sin(stateTime * 4.0f);
        shapes.setColor(0.92f * pulse, 0.58f * pulse, 0.12f, 1f);
        shapes.rect(menuX, 422f, menuW, btnH);

        // Button 3: Opening Practice (Electric Cyan / Teal)
        shapes.setColor(0.15f, 0.65f, 0.85f, 1f);
        shapes.rect(menuX, 364f, menuW, btnH);

        // Button 4: Puzzle Mode (Sapphire / Indigo)
        shapes.setColor(0.30f, 0.45f, 0.85f, 1f);
        shapes.rect(menuX, 306f, menuW, btnH);

        // Button 5: Game Mode (Royal Purple)
        shapes.setColor(isVsAi ? new Color(0.52f, 0.32f, 0.78f, 1f) : new Color(0.35f, 0.40f, 0.50f, 1f));
        shapes.rect(menuX, 248f, menuW, btnH);

        // Button 6: AI Depth (Cobalt Blue)
        shapes.setColor(isVsAi ? new Color(0.22f, 0.45f, 0.75f, 1f) : new Color(0.25f, 0.25f, 0.30f, 1f));
        shapes.rect(menuX, 190f, menuW, btnH);

        // Button 7: AI Boss Style (Coral Red)
        shapes.setColor(isVsAi ? new Color(0.85f, 0.35f, 0.25f, 1f) : new Color(0.25f, 0.25f, 0.30f, 1f));
        shapes.rect(menuX, 132f, menuW, btnH);

        // Button 8: Exit Game (Muted Dark Slate Red)
        shapes.setColor(0.38f, 0.18f, 0.18f, 1f);
        shapes.rect(menuX, 54f, menuW, btnH);

        // Music Toggle Card (Top-Right)
        float musicBtnW = 150f;
        float musicBtnH = 38f;
        float musicBtnX = screenW - musicBtnW - 20f;
        float musicBtnY = screenH - musicBtnH - 20f;
        boolean musicMuted = MusicManager.getInstance().isMuted();
        shapes.setColor(musicMuted ? new Color(0.42f, 0.18f, 0.18f, 0.95f) : new Color(0.18f, 0.58f, 0.35f, 0.95f));
        shapes.rect(musicBtnX, musicBtnY, musicBtnW, musicBtnH);

        shapes.end();

        // --- 2. SpriteBatch Rendering (Title & Text Labels) ---
        batch.begin();

        // Header Title Labels (Centered mathematically with GlyphLayout)
        titleFont.setColor(Color.GOLD);
        layout.setText(titleFont, "ศึกหมากรุกมหาประลัย");
        titleFont.draw(batch, "ศึกหมากรุกมหาประลัย", centerX - layout.width / 2f, Gdx.graphics.getHeight() - 42f);

        font.setColor(new Color(0.85f, 0.90f, 1.0f, 1f));
        layout.setText(font, "CHESS BATTLE OF LEGENDS");
        font.draw(batch, "CHESS BATTLE OF LEGENDS", centerX - layout.width / 2f, Gdx.graphics.getHeight() - 78f);

        // Draw Menu Action Button Text Labels (All crisp WHITE, perfectly centered)
        drawButtonText(batch, "เล่นเกมอิสระ (Quick Play)", menuX, 480f, menuW, btnH);
        drawButtonText(batch, "โหมดประลองบอส (Boss Rush)", menuX, 422f, menuW, btnH);
        drawButtonText(batch, "โหมดฝึกซ้อมเปิดเกม (Opening)", menuX, 364f, menuW, btnH);
        drawButtonText(batch, "โหมดฝึกแก้ปริศนา (Puzzle Mode)", menuX, 306f, menuW, btnH);
        drawButtonText(batch, isVsAi ? "โหมดการเล่น: vs AI" : "โหมดการเล่น: 2 คน", menuX, 248f, menuW, btnH);
        drawButtonText(batch, "ระดับ AI: Depth " + aiDepth, menuX, 190f, menuW, btnH);

        String styleLabel = "สไตล์ AI: เซียน (Master)";
        if (aiStyle == Evaluation.AIStyle.AGGRESSIVE) styleLabel = "สไตล์ AI: สายบุก (Aggressive)";
        else if (aiStyle == Evaluation.AIStyle.DEFENSIVE) styleLabel = "สไตล์ AI: สายรับ (Defensive)";
        drawButtonText(batch, styleLabel, menuX, 132f, menuW, btnH);

        drawButtonText(batch, "ออกจากเกม", menuX, 54f, menuW, btnH);

        // Music Toggle Label (Top-Right, perfectly centered)
        font.setColor(Color.WHITE);
        String musicLabel = musicMuted ? "เพลง: ปิด [M]" : "เพลง: เปิด ♫ [M]";
        layout.setText(font, musicLabel);
        font.draw(batch, musicLabel, musicBtnX + (musicBtnW - layout.width) / 2f, musicBtnY + (musicBtnH + layout.height) / 2f);

        batch.end();
    }

    private void drawButtonText(SpriteBatch batch, String text, float x, float y, float w, float h) {
        layout.setText(font, text);
        float tx = x + (w - layout.width) / 2f;
        float ty = y + (h + layout.height) / 2f;
        font.setColor(Color.WHITE);
        font.draw(batch, text, tx, ty);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (backgroundTex != null) {
            backgroundTex.dispose();
            backgroundTex = null;
        }
    }
}
