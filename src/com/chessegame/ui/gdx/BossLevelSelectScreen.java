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
import com.chessegame.level.BossLevel;
import com.chessegame.level.LevelProgressManager;
import com.chessegame.particle.ParticleSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BossLevelSelectScreen: Interactive boss selection map.
 * Displays 5 boss cards with portraits, handicap tags, time controls, star ratings, and unlock states.
 */
public class BossLevelSelectScreen extends ScreenAdapter {

    private final LibGdxChessApp app;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont headerFont;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
    private final GlyphLayout layout = new GlyphLayout();
    private ParticleSystem particleSystem;

    private final List<BossLevel> levels = BossLevel.getAllLevels();
    private final Map<String, Texture> avatarTextures = new HashMap<>();
    private Texture backgroundTex;
    private float stateTime = 0f;

    public BossLevelSelectScreen(LibGdxChessApp app) {
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
            } catch (Throwable ignored) {
            }
        }
        if (name.contains("characters/")) {
            return resolveAsset(name.replace("characters/", "charactor/"));
        } else if (name.contains("charactor/")) {
            return resolveAsset(name.replace("charactor/", "characters/"));
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
                String extraChars = "•+-*!?:.()[]'\"%/#1234567890<>";

                // Header Font (28px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter hp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                hp.size = 28;
                hp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                hp.minFilter = Texture.TextureFilter.Linear;
                hp.magFilter = Texture.TextureFilter.Linear;
                hp.borderWidth = 1.4f;
                hp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                headerFont = gen.generateFont(hp);

                // Title Font for Boss Name (20px crisp)
                FreeTypeFontGenerator.FreeTypeFontParameter tp = new FreeTypeFontGenerator.FreeTypeFontParameter();
                tp.size = 20;
                tp.characters = FreeTypeFontGenerator.DEFAULT_CHARS + sb.toString() + extraChars;
                tp.minFilter = Texture.TextureFilter.Linear;
                tp.magFilter = Texture.TextureFilter.Linear;
                tp.borderWidth = 1.2f;
                tp.borderColor = new Color(0.04f, 0.04f, 0.06f, 0.95f);
                titleFont = gen.generateFont(tp);

                // Button / Standard Font (17px crisp)
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

                gen.dispose();
            }
        } catch (Throwable ignored) {
        }

        if (headerFont == null) headerFont = new BitmapFont();
        if (titleFont == null) titleFont = new BitmapFont();
        if (font == null) font = new BitmapFont();
        if (smallFont == null) smallFont = new BitmapFont();

        // Load avatar textures for bosses
        for (BossLevel l : levels) {
            try {
                com.badlogic.gdx.files.FileHandle fh = resolveAsset(l.getAvatarPath());
                if (fh != null && fh.exists()) {
                    avatarTextures.put(l.getAvatarPath(), new Texture(fh));
                }
            } catch (Throwable ignored) {}
        }

        // Load background graphic
        try {
            com.badlogic.gdx.files.FileHandle bfh = resolveAsset("background.png");
            if (bfh == null || !bfh.exists()) {
                bfh = resolveAsset("charactor/assetbackgroud/background.png");
            }
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
                if (keycode == com.badlogic.gdx.Input.Keys.M) {
                    com.chessegame.audio.MusicManager.getInstance().toggleMute();
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

                // Bottom Back Button
                float backBtnW = 200f;
                float backBtnH = 42f;
                float backBtnX = (screenW - backBtnW) / 2f;
                float backBtnY = 24f;

                if (x >= backBtnX && x <= backBtnX + backBtnW && y >= backBtnY && y <= backBtnY + backBtnH) {
                    app.setScreen(new MainMenuScreen(app));
                    return true;
                }

                // Check clicks on Boss Level Cards
                float marginX = 24f;
                float availableW = screenW - (marginX * 2f);
                float gap = 14f;
                float cardW = Math.min(180f, (availableW - (gap * 4f)) / 5f);
                float cardH = 340f;
                float startX = (screenW - (5 * cardW + 4 * gap)) / 2f;
                float cardY = (screenH - cardH) / 2f - 10f;

                for (int i = 0; i < levels.size(); i++) {
                    BossLevel level = levels.get(i);
                    float cx = startX + i * (cardW + gap);

                    if (x >= cx && x <= cx + cardW && y >= cardY && y <= cardY + cardH) {
                        if (LevelProgressManager.isLevelUnlocked(level.getLevelId())) {
                            // Start Boss Match in GameScreen
                            app.setScreen(new GameScreen(app, level));
                            return true;
                        }
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        particleSystem.update(delta);

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

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

        // 2. Translucent Vignette Overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.06f, 0.10f, backgroundTex != null ? 0.65f : 1.0f);
        shapes.rect(0f, 0f, (float) screenW, (float) screenH);
        shapes.end();

        particleSystem.render(shapes);

        // --- 1. Top Header Card & Star Counter ---
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float headerH = 90f;
        float headerY = screenH - headerH;
        shapes.setColor(0.12f, 0.12f, 0.16f, 0.95f);
        shapes.rect(0f, headerY, screenW, headerH);

        shapes.setColor(0.95f, 0.75f, 0.25f, 1f);
        shapes.rect(0f, headerY, screenW, 3f);

        // --- 2. Render 5 Boss Cards ---
        float marginX = 24f;
        float availableW = screenW - (marginX * 2f);
        float gap = 14f;
        float cardW = Math.min(185f, (availableW - (gap * 4f)) / 5f);
        float cardH = 340f;
        float startX = (screenW - (5 * cardW + 4 * gap)) / 2f;
        float cardY = (screenH - cardH) / 2f - 10f;

        for (int i = 0; i < levels.size(); i++) {
            BossLevel level = levels.get(i);
            boolean isUnlocked = LevelProgressManager.isLevelUnlocked(level.getLevelId());
            float cx = startX + i * (cardW + gap);

            // Card background
            if (isUnlocked) {
                if (level.getLevelId() == 5) {
                    // Final Boss pulsating red/gold
                    float pulse = 0.8f + 0.2f * (float) Math.sin(stateTime * 4.0f);
                    shapes.setColor(0.24f * pulse + 0.08f, 0.12f, 0.16f, 0.98f);
                } else {
                    shapes.setColor(0.13f, 0.16f, 0.23f, 0.98f);
                }
            } else {
                // Sleek slate dark background for locked cards
                shapes.setColor(0.12f, 0.13f, 0.18f, 0.95f);
            }
            shapes.rect(cx, cardY, cardW, cardH);

            // Top level stripe
            Color stripeColor = getLevelColor(level.getLevelId(), isUnlocked);
            shapes.setColor(stripeColor);
            shapes.rect(cx, cardY + cardH - 5f, cardW, 5f);

            // Avatar frame backdrop
            shapes.setColor(0.08f, 0.09f, 0.14f, 0.90f);
            shapes.rect(cx + (cardW - 68f) / 2f, cardY + cardH - 120f, 68f, 68f);

            // Action Button inside card bottom
            float btnH = 36f;
            float btnY = cardY + 12f;
            float btnW = cardW - 16f;
            float btnX = cx + 8f;

            if (isUnlocked) {
                if (level.getLevelId() == 5) {
                    shapes.setColor(0.85f, 0.30f, 0.25f, 1f); // Crimson for Final Boss
                } else {
                    shapes.setColor(0.14f, 0.68f, 0.40f, 1f); // Vibrant Emerald Green
                }
            } else {
                shapes.setColor(0.20f, 0.22f, 0.28f, 1f); // Sleek Slate Gray
            }
            shapes.rect(btnX, btnY, btnW, btnH);
        }

        // --- 3. Bottom Back to Menu Button ---
        float backBtnW = 200f;
        float backBtnH = 42f;
        float backBtnX = (screenW - backBtnW) / 2f;
        float backBtnY = 24f;

        shapes.setColor(0.28f, 0.36f, 0.50f, 1f);
        shapes.rect(backBtnX, backBtnY, backBtnW, backBtnH);

        shapes.end();

        // --- 4. Render Avatars & Text Labels ---
        batch.begin();

        // Title Header
        headerFont.setColor(Color.GOLD);
        headerFont.draw(batch, "โหมดประลองบอส (Boss Rush)", 24f, headerY + 58f);

        int totalStars = LevelProgressManager.getTotalStars();
        font.setColor(new Color(0.95f, 0.95f, 1f, 1f));
        String starsText = "ดาวสะสมทั้งหมด: " + totalStars + " / 15 ดาว";
        layout.setText(font, starsText);
        font.draw(batch, starsText, screenW - layout.width - 24f, headerY + 54f);

        // Boss Cards Details
        for (int i = 0; i < levels.size(); i++) {
            BossLevel level = levels.get(i);
            boolean isUnlocked = LevelProgressManager.isLevelUnlocked(level.getLevelId());
            int stars = LevelProgressManager.getStarsForLevel(level.getLevelId());
            float cx = startX + i * (cardW + gap);

            // Level ID & Name
            font.setColor(isUnlocked ? Color.GOLD : new Color(0.68f, 0.72f, 0.82f, 1f));
            String levelTag = "ด่าน " + level.getLevelId();
            layout.setText(font, levelTag);
            font.draw(batch, levelTag, cx + (cardW - layout.width) / 2f, cardY + cardH - 16f);

            // Boss Name (Crisp & Centered)
            titleFont.setColor(isUnlocked ? Color.WHITE : new Color(0.78f, 0.82f, 0.90f, 1f));
            layout.setText(titleFont, level.getBossName());
            titleFont.draw(batch, level.getBossName(), cx + (cardW - layout.width) / 2f, cardY + cardH - 38f);

            // Boss Avatar Image
            Texture av = avatarTextures.get(level.getAvatarPath());
            if (av != null) {
                if (!isUnlocked) {
                    batch.setColor(0.5f, 0.5f, 0.6f, 0.65f);
                }
                batch.draw(av, cx + (cardW - 64f) / 2f, cardY + cardH - 118f, 64f, 64f);
                batch.setColor(Color.WHITE);
            }

            // Clean Ability Name (Thai only to prevent overlapping neighboring cards)
            String rawAbility = level.getAbilityName();
            String cleanAbility = rawAbility.contains(" (") ? rawAbility.substring(0, rawAbility.indexOf(" (")) : rawAbility;
            String abilityText = "สกิล: " + cleanAbility;

            smallFont.setColor(isUnlocked ? new Color(1.0f, 0.88f, 0.45f, 1f) : new Color(0.70f, 0.75f, 0.84f, 1f));
            layout.setText(smallFont, abilityText);
            smallFont.draw(batch, abilityText, cx + (cardW - layout.width) / 2f, cardY + cardH - 134f);

            // Time Limit
            smallFont.setColor(isUnlocked ? new Color(0.85f, 0.90f, 1.0f, 1f) : new Color(0.65f, 0.70f, 0.78f, 1f));
            long sec = level.getTimeLimitMs() / 1000L;
            String timeText = (sec < 120) ? ("เวลา: " + sec + " วิ") : ("เวลา: " + (sec / 60) + " นาที");
            layout.setText(smallFont, timeText);
            smallFont.draw(batch, timeText, cx + (cardW - layout.width) / 2f, cardY + cardH - 156f);

            // Difficulty Stars
            smallFont.setColor(isUnlocked ? Color.CYAN : new Color(0.60f, 0.75f, 0.85f, 1f));
            String diffText = "ความยาก: " + level.getLevelId() + " / 5";
            layout.setText(smallFont, diffText);
            smallFont.draw(batch, diffText, cx + (cardW - layout.width) / 2f, cardY + cardH - 178f);

            // Earned Stars Display
            font.setColor(stars > 0 ? Color.GOLD : (isUnlocked ? new Color(0.75f, 0.78f, 0.85f, 1f) : new Color(0.62f, 0.66f, 0.74f, 1f)));
            String starLabel = (stars > 0) ? ("คะแนน: " + stars + " / 3 ดาว") : (isUnlocked ? "ยังไม่ผ่าน" : "ยังไม่ปลดล็อก");
            layout.setText(font, starLabel);
            font.draw(batch, starLabel, cx + (cardW - layout.width) / 2f, cardY + cardH - 208f);

            // Action Button Text (Crisp WHITE with dark outline, perfectly centered)
            float btnW = cardW - 16f;
            float btnX = cx + 8f;
            float btnY = cardY + 12f;
            float btnH = 36f;

            String btnText = isUnlocked ? (level.getLevelId() == 5 ? "ท้าดวลบอส!" : "เริ่มประลอง") : "ล็อกอยู่";
            font.setColor(Color.WHITE);
            layout.setText(font, btnText);
            font.draw(batch, btnText, btnX + (btnW - layout.width) / 2f, btnY + (btnH + layout.height) / 2f);
        }

        // Back button label
        font.setColor(Color.WHITE);
        String backLabel = "◀ กลับสู่เมนูหลัก";
        layout.setText(font, backLabel);
        font.draw(batch, backLabel, backBtnX + (backBtnW - layout.width) / 2f, backBtnY + (backBtnH + layout.height) / 2f);

        batch.end();
    }

    private Color getLevelColor(int levelId, boolean isUnlocked) {
        if (!isUnlocked) return new Color(0.28f, 0.30f, 0.38f, 1f);
        switch (levelId) {
            case 1: return new Color(0.2f, 0.75f, 0.45f, 1f); // Green
            case 2: return new Color(0.35f, 0.65f, 0.85f, 1f); // Blue
            case 3: return new Color(0.95f, 0.65f, 0.2f, 1f); // Orange
            case 4: return new Color(0.75f, 0.35f, 0.85f, 1f); // Purple
            case 5: default: return new Color(0.95f, 0.25f, 0.25f, 1f); // Crimson Red
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
        if (backgroundTex != null) {
            backgroundTex.dispose();
            backgroundTex = null;
        }
        for (Texture t : avatarTextures.values()) {
            if (t != null) t.dispose();
        }
    }
}
