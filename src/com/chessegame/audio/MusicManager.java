package com.chessegame.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

/**
 * MusicManager: Handles looping background music (BGM) playback across screens,
 * supporting LibGDX audio with graceful desktop/Java fallback, volume controls,
 * and seamless track transitions.
 */
public class MusicManager {
    private static final MusicManager instance = new MusicManager();

    private Music currentMusic = null;
    private Clip fallbackClip = null;
    private String currentTrack = null;

    private boolean isMuted = false;
    private float volume = 0.45f; // Pleasant, non-intrusive default volume

    public static final String DEFAULT_BGM = "music/bgm.wav";

    private MusicManager() {}

    public static MusicManager getInstance() {
        return instance;
    }

    /**
     * Resolves the music file path across possible asset directories.
     */
    private FileHandle resolveAsset(String name) {
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
                if (Gdx.files != null) {
                    FileHandle fh = c.startsWith(userDir) ? Gdx.files.absolute(c) : Gdx.files.internal(c);
                    if (fh != null && fh.exists()) {
                        return fh;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    /**
     * Resolves a standard Java File for fallback playback.
     */
    private File resolveFile(String name) {
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
            File f = new File(c);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Plays the default background music.
     */
    public synchronized void playBGM() {
        playBGM(DEFAULT_BGM);
    }

    /**
     * Plays the specified background music track in a seamless loop.
     * If the same track is already playing, it will keep playing uninterrupted.
     */
    public synchronized void playBGM(String trackPath) {
        if (trackPath == null) return;

        // If the same track is already playing, do not restart it
        if (trackPath.equals(currentTrack) && isPlaying()) {
            return;
        }

        stopBGM();
        currentTrack = trackPath;

        // Try LibGDX audio first
        boolean gdxLoaded = false;
        try {
            if (Gdx.audio != null) {
                FileHandle fh = resolveAsset(trackPath);
                if (fh != null && fh.exists()) {
                    currentMusic = Gdx.audio.newMusic(fh);
                    currentMusic.setLooping(true);
                    currentMusic.setVolume(isMuted ? 0f : volume);
                    currentMusic.play();
                    gdxLoaded = true;
                    System.out.println("[MusicManager] LibGDX BGM started: " + fh.path());
                } else {
                    System.out.println("[MusicManager] BGM asset not found via Gdx.files: " + trackPath);
                }
            }
        } catch (Throwable t) {
            System.err.println("[MusicManager] Failed to start LibGDX music: " + t.getMessage());
            currentMusic = null;
        }

        // Fallback to Java AudioSystem if LibGDX audio is not active
        if (!gdxLoaded) {
            try {
                File file = resolveFile(trackPath);
                if (file != null && file.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                    fallbackClip = AudioSystem.getClip();
                    fallbackClip.open(audioStream);
                    applyFallbackVolume();
                    fallbackClip.loop(Clip.LOOP_CONTINUOUSLY);
                    fallbackClip.start();
                    System.out.println("[MusicManager] Java Sound fallback BGM started: " + file.getAbsolutePath());
                } else {
                    System.out.println("[MusicManager] BGM audio file not found on disk: " + trackPath);
                }
            } catch (Throwable t) {
                System.err.println("[MusicManager] Java Sound fallback failed: " + t.getMessage());
                fallbackClip = null;
            }
        }
    }

    /**
     * Checks whether background music is currently active/playing.
     */
    public synchronized boolean isPlaying() {
        if (currentMusic != null) {
            try {
                return currentMusic.isPlaying();
            } catch (Throwable ignored) {}
        }
        if (fallbackClip != null) {
            try {
                return fallbackClip.isRunning();
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Pauses the currently playing background music.
     */
    public synchronized void pauseBGM() {
        if (currentMusic != null) {
            try {
                currentMusic.pause();
            } catch (Throwable ignored) {}
        }
        if (fallbackClip != null) {
            try {
                fallbackClip.stop();
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Resumes background music playback if paused.
     */
    public synchronized void resumeBGM() {
        if (isMuted) return;
        if (currentMusic != null) {
            try {
                currentMusic.play();
            } catch (Throwable ignored) {}
        } else if (fallbackClip != null) {
            try {
                fallbackClip.start();
            } catch (Throwable ignored) {}
        } else if (currentTrack != null) {
            playBGM(currentTrack);
        }
    }

    /**
     * Stops background music and cleans up the current clip.
     */
    public synchronized void stopBGM() {
        if (currentMusic != null) {
            try {
                currentMusic.stop();
                currentMusic.dispose();
            } catch (Throwable ignored) {}
            currentMusic = null;
        }
        if (fallbackClip != null) {
            try {
                fallbackClip.stop();
                fallbackClip.close();
            } catch (Throwable ignored) {}
            fallbackClip = null;
        }
        currentTrack = null;
    }

    /**
     * Sets BGM volume (0.0 to 1.0).
     */
    public synchronized void setVolume(float newVolume) {
        this.volume = Math.max(0f, Math.min(1f, newVolume));
        if (currentMusic != null) {
            try {
                currentMusic.setVolume(isMuted ? 0f : volume);
            } catch (Throwable ignored) {}
        }
        applyFallbackVolume();
    }

    public synchronized float getVolume() {
        return volume;
    }

    /**
     * Toggles mute state for background music.
     */
    public synchronized void toggleMute() {
        setMuted(!isMuted);
    }

    public synchronized void setMuted(boolean muted) {
        this.isMuted = muted;
        if (currentMusic != null) {
            try {
                currentMusic.setVolume(isMuted ? 0f : volume);
            } catch (Throwable ignored) {}
        }
        applyFallbackVolume();
    }

    public synchronized boolean isMuted() {
        return isMuted;
    }

    private void applyFallbackVolume() {
        if (fallbackClip != null) {
            try {
                if (fallbackClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) fallbackClip.getControl(FloatControl.Type.MASTER_GAIN);
                    float effectiveVol = isMuted ? 0.0001f : Math.max(0.0001f, volume);
                    float dB = (float) (Math.log10(effectiveVol) * 20.0);
                    gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
                }
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Releases all audio resources upon application shutdown.
     */
    public synchronized void dispose() {
        stopBGM();
    }
}
