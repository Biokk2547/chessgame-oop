package com.chessegame.particle;

import com.badlogic.gdx.graphics.Color;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;

/**
 * Manages smooth interpolation (glide animation) of a moving chess piece.
 */
public class PieceGlideAnimation {
    private final Piece piece;
    private final Position fromPos;
    private final Position toPos;
    private final float startX, startY;
    private final float targetX, targetY;
    private final float duration; // duration in seconds
    private float elapsedTime;
    private final boolean isCapture;
    private boolean finished;

    public PieceGlideAnimation(Piece piece, Position fromPos, Position toPos,
                               float startX, float startY, float targetX, float targetY,
                               boolean isCapture, float duration) {
        this.piece = piece;
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.isCapture = isCapture;
        this.duration = Math.max(0.05f, duration);
        this.elapsedTime = 0f;
        this.finished = false;
    }

    public void update(float delta, ParticleSystem particleSystem) {
        if (finished) return;
        elapsedTime += delta;
        float progress = Math.min(1.0f, elapsedTime / duration);

        // Smooth ease-out curve
        float smoothProgress = 1.0f - (1.0f - progress) * (1.0f - progress);

        float currentX = startX + (targetX - startX) * smoothProgress;
        float currentY = startY + (targetY - startY) * smoothProgress;

        // Emit move trail particle
        if (particleSystem != null) {
            Color trailColor = (piece != null && piece.getColor() == Piece.Color.WHITE) ?
                    new Color(1.0f, 0.85f, 0.4f, 0.8f) : new Color(0.2f, 0.85f, 1.0f, 0.8f);
            particleSystem.emitMoveTrail(currentX + 24f, currentY + 24f, trailColor);
        }

        if (progress >= 1.0f) {
            finished = true;
            if (isCapture && particleSystem != null) {
                Color burstColor = (piece != null && piece.getColor() == Piece.Color.WHITE) ?
                        new Color(1.0f, 0.4f, 0.2f, 1.0f) : new Color(0.1f, 0.9f, 1.0f, 1.0f);
                particleSystem.emitCaptureBurst(targetX + 24f, targetY + 24f, burstColor);
            }
        }
    }

    public float getCurrentX() {
        float progress = Math.min(1.0f, elapsedTime / duration);
        float smoothProgress = 1.0f - (1.0f - progress) * (1.0f - progress);
        return startX + (targetX - startX) * smoothProgress;
    }

    public float getCurrentY() {
        float progress = Math.min(1.0f, elapsedTime / duration);
        float smoothProgress = 1.0f - (1.0f - progress) * (1.0f - progress);
        return startY + (targetY - startY) * smoothProgress;
    }

    public Piece getPiece() {
        return piece;
    }

    public Position getToPos() {
        return toPos;
    }

    public boolean isFinished() {
        return finished;
    }
}
