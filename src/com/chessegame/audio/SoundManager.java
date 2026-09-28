package com.chessegame.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    private static final SoundManager instance = new SoundManager();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ChessAudioThread");
        t.setDaemon(true);
        return t;
    });

    private boolean soundEnabled = true;

    private SoundManager() {}

    public static SoundManager getInstance() {
        return instance;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void playBGM() {
        MusicManager.getInstance().playBGM();
    }

    public void stopBGM() {
        MusicManager.getInstance().stopBGM();
    }

    public void toggleMusic() {
        MusicManager.getInstance().toggleMute();
    }

    public boolean isMusicMuted() {
        return MusicManager.getInstance().isMuted();
    }

    public void playClickSound() {
        if (!soundEnabled) return;
        executor.submit(() -> playTone(600, 25, 0.35f, true));
    }

    public void playMoveSound() {
        if (!soundEnabled) return;
        executor.submit(() -> playTone(320, 45, 0.4f, true));
    }

    public void playCaptureSound() {
        if (!soundEnabled) return;
        executor.submit(() -> {
            playTone(480, 40, 0.6f, false);
            playTone(220, 60, 0.7f, true);
        });
    }

    public void playCheckSound() {
        if (!soundEnabled) return;
        executor.submit(() -> {
            playTone(880, 70, 0.5f, false);
            playTone(1100, 100, 0.6f, false);
        });
    }

    public void playVictorySound() {
        if (!soundEnabled) return;
        executor.submit(() -> {
            playTone(523, 90, 0.5f, false);
            playTone(659, 90, 0.5f, false);
            playTone(783, 140, 0.7f, false);
            playTone(1046, 220, 0.8f, true);
        });
    }

    private void playTone(int freq, int durationMs, float volume, boolean decay) {
        try {
            float sampleRate = 22050f;
            int numSamples = (int) (sampleRate * (durationMs / 1000f));
            byte[] buf = new byte[numSamples];
            for (int i = 0; i < numSamples; i++) {
                double t = i / (double) sampleRate;
                double angle = 2.0 * Math.PI * freq * t;
                double sample = Math.sin(angle);
                if (decay) {
                    double factor = 1.0 - ((double) i / numSamples);
                    sample *= factor;
                }
                buf[i] = (byte) (sample * 127.0 * volume);
            }
            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format, buf.length);
            line.start();
            line.write(buf, 0, buf.length);
            line.drain();
            line.close();
        } catch (Throwable ignored) {}
    }
}
