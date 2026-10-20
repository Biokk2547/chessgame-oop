package com.chessegame.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;
import com.chessegame.ai.Evaluation;
import com.chessegame.opening.OpeningManager;

/** Optional real OpenGL render checks; output images are saved under build/ui-smoke. */
public class UiSmokeTest extends LibGdxChessApp {
    private int phase;
    private int frames;
    private int sizeIndex;
    private final int[][] sizes = {{1280, 900}, {960, 720}, {640, 480}};
    private Throwable failure;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Chess UI verification");
        config.setWindowedMode(1280, 900);
        config.setInitialVisible(false);
        config.disableAudio(true);
        UiSmokeTest test = new UiSmokeTest();
        new Lwjgl3Application(test, config);
        if (test.failure != null) throw new AssertionError("UI smoke test failed", test.failure);
    }

    @Override public void create() {
        super.create();
    }

    private void showScreen(Screen next) {
        Screen old = getScreen();
        setScreen(next);
        if (old != null) old.dispose();
    }

    @Override public void render() {
        try {
            super.render();
            if (++frames < 4) return;
            frames = 0;
            String name = new String[] {"menu", "game", "bosses", "openings", "practice", "review"}[phase];
            Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0,
                    Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            try {
                PixmapIO.writePNG(Gdx.files.local("build/ui-smoke/" + sizes[sizeIndex][0] + "-" + name + ".png"), image, -1, true);
            } finally {
                image.dispose();
            }
            if (phase == 0) {
                // Use physical pointer coordinates to verify scaled menu hit testing.
                float buttonY = (UiLayout.height() - 392f) / 2f + 348f + 22f;
                int x = Gdx.graphics.getWidth() / 2;
                int y = Math.round((UiLayout.height() - buttonY) * Gdx.graphics.getHeight() / UiLayout.height());
                Screen old = getScreen();
                Gdx.input.getInputProcessor().touchDown(x, y, 0, 0);
                if (!(getScreen() instanceof GameScreen)) throw new AssertionError("Scaled Quick Play click missed");
                old.dispose();
            } else if (phase == 1) {
                showScreen(new BossLevelSelectScreen(this));
            } else if (phase == 2) {
                showScreen(new OpeningSelectScreen(this));
            } else if (phase == 3) {
                showScreen(new OpeningPracticeScreen(this, OpeningManager.getAllOpenings().get(0).getId()));
            } else if (phase == 4) {
                showScreen(new GameReviewScreen(this, java.util.Collections.emptyList(), false, 1, Evaluation.AIStyle.MASTER));
            } else {
                phase = -1;
                if (++sizeIndex == sizes.length) {
                    System.out.println("[PASS] Rendered 18 UI screens and verified scaled menu input at 3 sizes");
                    Gdx.app.exit();
                    return;
                }
                Gdx.graphics.setWindowedMode(sizes[sizeIndex][0], sizes[sizeIndex][1]);
                showScreen(new MainMenuScreen(this));
            }
            phase++;
        } catch (Throwable error) {
            failure = error;
            error.printStackTrace();
            Gdx.app.exit();
        }
    }
}
