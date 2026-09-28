package com.chessegame.particle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Lightweight LibGDX Particle System for move trails, capture explosions, and victory sparkles.
 */
public class ParticleSystem {
    public static class Particle {
        public float x, y;
        public float vx, vy;
        public float size;
        public float alpha;
        public float maxLife;
        public float currentLife;
        public Color color;

        public Particle(float x, float y, float vx, float vy, float size, float maxLife, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.alpha = 1.0f;
            this.maxLife = maxLife;
            this.currentLife = 0f;
            this.color = new Color(color);
        }

        public boolean update(float delta) {
            currentLife += delta;
            if (currentLife >= maxLife) return false;
            x += vx * delta;
            y += vy * delta;
            alpha = 1.0f - (currentLife / maxLife);
            return true;
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public void emitMoveTrail(float x, float y, Color color) {
        for (int i = 0; i < 3; i++) {
            float vx = (random.nextFloat() - 0.5f) * 40f;
            float vy = (random.nextFloat() - 0.5f) * 40f;
            float size = 3f + random.nextFloat() * 4f;
            float life = 0.25f + random.nextFloat() * 0.2f;
            particles.add(new Particle(x, y, vx, vy, size, life, color));
        }
    }

    public void emitCaptureBurst(float x, float y, Color color) {
        for (int i = 0; i < 28; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            float speed = 60f + random.nextFloat() * 180f;
            float vx = (float) (Math.cos(angle) * speed);
            float vy = (float) (Math.sin(angle) * speed);
            float size = 4f + random.nextFloat() * 6f;
            float life = 0.4f + random.nextFloat() * 0.35f;
            particles.add(new Particle(x, y, vx, vy, size, life, color));
        }
    }

    public void emitVictoryConfetti(float screenWidth, float screenHeight) {
        for (int i = 0; i < 60; i++) {
            float x = random.nextFloat() * screenWidth;
            float y = screenHeight + random.nextFloat() * 40f;
            float vx = (random.nextFloat() - 0.5f) * 80f;
            float vy = -80f - random.nextFloat() * 120f;
            float size = 5f + random.nextFloat() * 7f;
            float life = 1.5f + random.nextFloat() * 1.5f;
            Color c = (i % 2 == 0) ? new Color(1f, 0.84f, 0f, 1f) : new Color(0.2f, 0.8f, 1f, 1f);
            particles.add(new Particle(x, y, vx, vy, size, life, c));
        }
    }

    public void update(float delta) {
        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            if (!p.update(delta)) {
                iter.remove();
            }
        }
    }

    public void render(ShapeRenderer shapes) {
        if (particles.isEmpty()) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Particle p : particles) {
            shapes.setColor(p.color.r, p.color.g, p.color.b, p.alpha * 0.85f);
            shapes.circle(p.x, p.y, p.size);
        }
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
