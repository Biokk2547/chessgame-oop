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
import com.chessegame.audio.MusicManager;
import com.chessegame.audio.SoundManager;
import com.chessegame.model.Piece;
import com.chessegame.opening.Opening;
import com.chessegame.opening.OpeningManager;
import com.chessegame.particle.ParticleSystem;

import java.util.List;

/**
 * OpeningSelectScreen: Interactive opening repertoire selection screen.
 * Displays 10 famous chess opening cards with tactical descriptions, categories,
 * player roles (White/Black), moves preview, and completion status.
 */
public class OpeningSelectScreen extends ScreenAdapter {

    private final LibGdxChessApp app;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont headerFont;
    private BitmapFont titleFont;
    private BitmapFont font;
    private BitmapFont smallFont;
    private final GlyphLayout layout = new GlyphLayout();
    private ParticleSystem particleSystem;

    private final List<Opening> openings = OpeningManager.getAllOpenings();
    private Texture backgroundTex;

    public OpeningSelectScreen(LibGdxChessApp app) {
        this.app = app;
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
        return null;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        particleSystem = new ParticleSystem();

        // Load fonts
        try {
            com.badlogic.gdx.files.FileHandle ttfFile = resolveAsset("fonts/thai.ttf");
            if (ttfFile != null && ttfFile.exists()) {
                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(ttfFile);
                StringBuilder sb = new StringBuilder();
                for (char c = '\u0E01'; c <= '\u0E5B'; c++) sb.append(c);
                String extraChars = "•+-*!?:.()[]'\"%/#1234567890<>=~_@$&,;";

                // Header Font (26px)
                FreeTypeFontGenerator.FreeTypeFontParameter hp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                hp.size = 26;
                hp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                hp.minFilter = Texture.TextureFilter.Linear;
                hp.magFilter = Texture.TextureFilter.Linear;
                hp.borderWidth = 1.4f;
                hp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                headerFont = gen.generateFont(hp);

                // Title Font (18px)
                FreeTypeFontGenerator.FreeTypeFontParameter tp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                tp.size = 18;
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

                gen.dispose();
            } else {
                headerFont = new BitmapFont();
                titleFont = new BitmapFont();
                font = new BitmapFont();
                smallFont = new BitmapFont();
            }
        } catch (Throwable ignored) {
            headerFont = new BitmapFont();
            titleFont = new BitmapFont();
            font = new BitmapFont();
            smallFont = new BitmapFont();
        }

        // Load background
        try {
            com.badlogic.gdx.files.FileHandle bfh = resolveAsset("background.png");
            if (bfh != null && bfh.exists()) {
                backgroundTex = new Texture(bfh);
            }
        } catch (Throwable ignored) {}

        setupInput();
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.setToOrtho(false, width, height);
            camera.update();
        }
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
                    app.setScreen(new MainMenuScreen(app));
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;

                int screenW = Gdx.graphics.getWidth();
                int screenH = Gdx.graphics.getHeight();

                // Top-Right Music Toggle
                float musicBtnW = 140f;
                float musicBtnH = 36f;
                float musicBtnX = screenW - musicBtnW - 20f;
                float musicBtnY = screenH - musicBtnH - 18f;
                if (x >= musicBtnX && x <= musicBtnX + musicBtnW && y >= musicBtnY && y <= musicBtnY + musicBtnH) {
                    MusicManager.getInstance().toggleMute();
                    return true;
                }

                // Bottom Back Button
                float backBtnW = 200f;
                float backBtnH = 44f;
                float backBtnX = (screenW - backBtnW) / 2f;
                float backBtnY = 18f;
                if (x >= backBtnX && x <= backBtnX + backBtnW && y >= backBtnY && y <= backBtnY + backBtnH) {
                    SoundManager.getInstance().playClickSound();
                    app.setScreen(new MainMenuScreen(app));
                    return true;
                }

                // Check clicks on Opening Cards (2 rows of 5 columns)
                int cols = 5;
                float marginX = 24f;
                float availableW = screenW - (marginX * 2f);
                float gapX = 14f;
                float cardW = Math.min(186f, (availableW - (gapX * (cols - 1))) / (float) cols);
                float cardH = 290f;
                float totalGridW = cols * cardW + (cols - 1) * gapX;
                float startX = (screenW - totalGridW) / 2f;

                float gapY = 16f;
                float topGridY = screenH - 100f - cardH;
                float bottomGridY = topGridY - cardH - gapY;

                for (int i = 0; i < openings.size(); i++) {
                    int row = i / cols;
                    int col = i % cols;

                    float cx = startX + col * (cardW + gapX);
                    float cy = (row == 0) ? topGridY : bottomGridY;

                    if (x >= cx && x <= cx + cardW && y >= cy && y <= cy + cardH) {
                        Opening op = openings.get(i);
                        SoundManager.getInstance().playClickSound();
                        app.setScreen(new OpeningPracticeScreen(app, op.getId()));
                        return true;
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        particleSystem.update(delta);

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

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.11f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Draw Background Image
        if (backgroundTex != null) {
            batch.begin();
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(backgroundTex, 0f, 0f, (float) screenW, (float) screenH);
            batch.end();
        }

        // 2. Dark Vignette Overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.07f, 0.12f, backgroundTex != null ? 0.75f : 1.0f);
        shapes.rect(0f, 0f, (float) screenW, (float) screenH);
        shapes.end();

        // 3. Floating particle ambience
        particleSystem.render(shapes);

        // Layout Dimensions for Cards Grid
        int cols = 5;
        float marginX = 24f;
        float availableW = screenW - (marginX * 2f);
        float gapX = 14f;
        float cardW = Math.min(186f, (availableW - (gapX * (cols - 1))) / (float) cols);
        float cardH = 290f;
        float totalGridW = cols * cardW + (cols - 1) * gapX;
        float startX = (screenW - totalGridW) / 2f;

        float gapY = 16f;
        float topGridY = screenH - 100f - cardH;
        float bottomGridY = topGridY - cardH - gapY;

        // --- 4. Render Card Backgrounds with Shapes ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Header Background Bar
        shapes.setColor(0.10f, 0.12f, 0.18f, 0.94f);
        shapes.rect(0f, screenH - 80f, screenW, 80f);
        shapes.setColor(0.95f, 0.75f, 0.25f, 0.90f);
        shapes.rect(0f, screenH - 80f, screenW, 3f);

        // Music Toggle Card
        float musicBtnW = 140f;
        float musicBtnH = 36f;
        float musicBtnX = screenW - musicBtnW - 20f;
        float musicBtnY = screenH - musicBtnH - 18f;
        boolean musicMuted = MusicManager.getInstance().isMuted();
        boolean musicHover = (mx >= musicBtnX && mx <= musicBtnX + musicBtnW && my >= musicBtnY && my <= musicBtnY + musicBtnH);
        if (musicMuted) {
            shapes.setColor(musicHover ? new Color(0.55f, 0.22f, 0.22f, 1f) : new Color(0.42f, 0.18f, 0.18f, 0.95f));
        } else {
            shapes.setColor(musicHover ? new Color(0.22f, 0.65f, 0.40f, 1f) : new Color(0.18f, 0.55f, 0.32f, 0.95f));
        }
        shapes.rect(musicBtnX, musicBtnY, musicBtnW, musicBtnH);

        // Bottom Back Button
        float backBtnW = 200f;
        float backBtnH = 44f;
        float backBtnX = (screenW - backBtnW) / 2f;
        float backBtnY = 18f;
        boolean backHover = (mx >= backBtnX && mx <= backBtnX + backBtnW && my >= backBtnY && my <= backBtnY + backBtnH);
        shapes.setColor(backHover ? new Color(0.35f, 0.42f, 0.58f, 1f) : new Color(0.24f, 0.28f, 0.38f, 0.95f));
        shapes.rect(backBtnX, backBtnY, backBtnW, backBtnH);

        // Draw 10 Opening Cards
        for (int i = 0; i < openings.size(); i++) {
            Opening op = openings.get(i);
            int row = i / cols;
            int col = i % cols;
            float cx = startX + col * (cardW + gapX);
            float cy = (row == 0) ? topGridY : bottomGridY;

            boolean completed = OpeningManager.isOpeningCompleted(op.getId());
            boolean cardHover = (mx >= cx && mx <= cx + cardW && my >= cy && my <= cy + cardH);

            // Card Base Fill
            if (completed) {
                shapes.setColor(cardHover ? new Color(0.16f, 0.26f, 0.22f, 0.98f) : new Color(0.12f, 0.20f, 0.18f, 0.96f));
            } else {
                shapes.setColor(cardHover ? new Color(0.15f, 0.18f, 0.28f, 0.98f) : new Color(0.11f, 0.13f, 0.20f, 0.96f));
            }
            shapes.rect(cx, cy, cardW, cardH);

            // Card Category Header Strip
            Color catColor = getCategoryColor(op.getCategory());
            shapes.setColor(catColor);
            shapes.rect(cx, cy + cardH - 24f, cardW, 24f);

            // Play As Indicator Strip (Bottom Accent)
            boolean isWhite = (op.getPlayerColor() == Piece.Color.WHITE);
            shapes.setColor(isWhite ? new Color(0.95f, 0.95f, 0.85f, 0.9f) : new Color(0.35f, 0.35f, 0.45f, 0.9f));
            shapes.rect(cx, cy, cardW, 4f);

            // Draw player color role circle
            float circleX = cx + 20f;
            float circleY = cy + cardH - 105f;
            shapes.setColor(isWhite ? Color.WHITE : new Color(0.18f, 0.18f, 0.22f, 1f));
            shapes.circle(circleX, circleY, 5f);
        }

        shapes.end();

        // Card Border Outlines
        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < openings.size(); i++) {
            Opening op = openings.get(i);
            int row = i / cols;
            int col = i % cols;
            float cx = startX + col * (cardW + gapX);
            float cy = (row == 0) ? topGridY : bottomGridY;
            boolean completed = OpeningManager.isOpeningCompleted(op.getId());
            boolean cardHover = (mx >= cx && mx <= cx + cardW && my >= cy && my <= cy + cardH);

            if (cardHover) {
                shapes.setColor(Color.GOLD);
            } else if (completed) {
                shapes.setColor(0.20f, 0.75f, 0.45f, 0.90f);
            } else {
                shapes.setColor(0.28f, 0.34f, 0.46f, 0.75f);
            }
            shapes.rect(cx, cy, cardW, cardH);
        }
        shapes.end();

        // --- 5. Render Texts & Labels ---
        batch.begin();

        // Header Title & Subtitle
        headerFont.setColor(Color.GOLD);
        layout.setText(headerFont, "โหมดฝึกซ้อมเปิดเกม (Opening Practice Repertoire)");
        headerFont.draw(batch, "โหมดฝึกซ้อมเปิดเกม (Opening Practice Repertoire)", (screenW - layout.width) / 2f, screenH - 24f);

        int totalDone = OpeningManager.getTotalCompletedCount();
        font.setColor(new Color(0.85f, 0.90f, 1.0f, 1f));
        String subText = "เชี่ยวชาญแล้ว: " + totalDone + " / 10 สาย  •  เลือกสายเพื่อเริ่มฝึกซ้อมทีละก้าว";
        layout.setText(font, subText);
        font.draw(batch, subText, (screenW - layout.width) / 2f, screenH - 54f);

        // Music Toggle Text
        font.setColor(Color.WHITE);
        String musicLabel = musicMuted ? "เพลง: ปิด [M]" : "เพลง: เปิด [M]";
        layout.setText(font, musicLabel);
        font.draw(batch, musicLabel, musicBtnX + (musicBtnW - layout.width) / 2f, musicBtnY + (musicBtnH + layout.height) / 2f);

        // Back Button Text
        font.setColor(Color.WHITE);
        layout.setText(font, "<- กลับสู่เมนูหลัก");
        font.draw(batch, "<- กลับสู่เมนูหลัก", backBtnX + (backBtnW - layout.width) / 2f, backBtnY + (backBtnH + layout.height) / 2f);

        // Render Card Texts
        for (int i = 0; i < openings.size(); i++) {
            Opening op = openings.get(i);
            int row = i / cols;
            int col = i % cols;
            float cx = startX + col * (cardW + gapX);
            float cy = (row == 0) ? topGridY : bottomGridY;

            boolean completed = OpeningManager.isOpeningCompleted(op.getId());

            // 1. Category Tag (on top strip)
            smallFont.setColor(Color.WHITE);
            String catText = op.getCategory().toUpperCase();
            layout.setText(smallFont, catText);
            smallFont.draw(batch, catText, cx + (cardW - layout.width) / 2f, cy + cardH - 7f);

            // 2. Opening English Name
            titleFont.setColor(Color.GOLD);
            layout.setText(titleFont, op.getNameEn());
            float titleY = cy + cardH - 36f;
            titleFont.draw(batch, op.getNameEn(), cx + (cardW - layout.width) / 2f, titleY);

            // 3. Opening Thai Name
            font.setColor(Color.WHITE);
            String thShort = op.getNameTh().split("\\(")[0].trim();
            layout.setText(font, thShort);
            font.draw(batch, thShort, cx + (cardW - layout.width) / 2f, titleY - 24f);

            // 4. Role Badge
            boolean isWhite = (op.getPlayerColor() == Piece.Color.WHITE);
            String roleText = isWhite ? "ผู้เล่น: หมากขาว (White)" : "ผู้เล่น: หมากดำ (Black)";
            smallFont.setColor(isWhite ? new Color(0.95f, 0.90f, 0.60f, 1f) : new Color(0.70f, 0.85f, 1.0f, 1f));
            layout.setText(smallFont, roleText);
            smallFont.draw(batch, roleText, cx + 30f, cy + cardH - 100f);

            // 5. Moves Preview (SAN)
            smallFont.setColor(new Color(0.75f, 0.82f, 0.95f, 1f));
            String movesSummary = getMovesSummary(op);
            layout.setText(smallFont, movesSummary, new Color(0.75f, 0.82f, 0.95f, 1f), cardW - 16f, Align.center, true);
            smallFont.draw(batch, layout, cx + 8f, titleY - 84f);

            // 6. Tactical Goal / Description
            smallFont.setColor(new Color(0.85f, 0.85f, 0.85f, 0.85f));
            layout.setText(smallFont, op.getTacticalGoal(), new Color(0.85f, 0.85f, 0.85f, 0.85f), cardW - 16f, Align.center, true);
            smallFont.draw(batch, layout, cx + 8f, cy + 90f);

            // 7. Status & Completion
            if (completed) {
                font.setColor(new Color(0.30f, 0.95f, 0.45f, 1f));
                String doneLabel = "เชี่ยวชาญแล้ว (3/3)";
                layout.setText(font, doneLabel);
                font.draw(batch, doneLabel, cx + (cardW - layout.width) / 2f, cy + 28f);
            } else {
                font.setColor(new Color(0.40f, 0.75f, 1.0f, 1f));
                String playLabel = "คลิกเพื่อเริ่มฝึกซ้อม";
                layout.setText(font, playLabel);
                font.draw(batch, playLabel, cx + (cardW - layout.width) / 2f, cy + 28f);
            }
        }

        batch.end();
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

    private String getMovesSummary(Opening op) {
        StringBuilder sb = new StringBuilder();
        List<String> nots = op.getMoveNotations();
        for (int i = 0; i < Math.min(nots.size(), 4); i++) {
            sb.append(nots.get(i)).append(" ");
        }
        if (nots.size() > 4) sb.append("...");
        return sb.toString().trim();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (headerFont != null) headerFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (font != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        if (backgroundTex != null) {
            backgroundTex.dispose();
            backgroundTex = null;
        }
    }
}
