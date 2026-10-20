package com.chessegame.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Shared logical canvas keeps controls and their hit areas aligned on small windows. */
public final class UiLayout {
    private UiLayout() {}

    private static float scale() {
        return Math.min(1f, Math.min(Math.max(1, Gdx.graphics.getWidth()) / 1280f,
                Math.max(1, Gdx.graphics.getHeight()) / 900f));
    }

    public static int width() { return Math.round(Math.max(1, Gdx.graphics.getWidth()) / scale()); }
    public static int height() { return Math.round(Math.max(1, Gdx.graphics.getHeight()) / scale()); }
    public static float x(float screenX) { return screenX * width() / Math.max(1, Gdx.graphics.getWidth()); }
    public static float y(float screenY) { return screenY * height() / Math.max(1, Gdx.graphics.getHeight()); }

    /** Fit a label or paragraph inside its own area and restore the caller's font scale. */
    public static void text(BitmapFont font, SpriteBatch batch, String text, float x, float top,
                            float width, float height, int align, boolean wrap) {
        float sx = font.getData().scaleX;
        float sy = font.getData().scaleY;
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text, font.getColor(), width, align, wrap);
        float fit = Math.min(1f, Math.min(width / Math.max(1f, layout.width),
                height / Math.max(1f, layout.height)));
        if (fit < 1f) {
            font.getData().setScale(sx * fit, sy * fit);
            layout.setText(font, text, font.getColor(), width, align, wrap);
        }
        font.draw(batch, layout, x, top);
        font.getData().setScale(sx, sy);
    }
}
