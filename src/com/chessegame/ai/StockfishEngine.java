package com.chessegame.ai;

import com.chessegame.model.Piece;
import com.chessegame.model.Position;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StockfishEngine: UCI (Universal Chess Interface) Protocol integration.
 * Communicates with the Stockfish chess engine binary for deep grandmaster analysis.
 */
public class StockfishEngine {

    public static class EvaluationResult {
        public final int scoreCp; // Centipawns from White perspective
        public final boolean isMate;
        public final int mateIn;
        public final Position bestFrom;
        public final Position bestTo;
        public final String bestMoveUCI;

        public EvaluationResult(int scoreCp, boolean isMate, int mateIn, Position bestFrom, Position bestTo, String bestMoveUCI) {
            this.scoreCp = scoreCp;
            this.isMate = isMate;
            this.mateIn = mateIn;
            this.bestFrom = bestFrom;
            this.bestTo = bestTo;
            this.bestMoveUCI = bestMoveUCI;
        }
    }

    private static String cachedBinaryPath = null;
    private static String cachedEngineName = "Stockfish Engine (UCI)";
    private static Boolean cachedAvailability = null;

    /**
     * Checks if a Stockfish binary is installed and executable.
     */
    public static boolean isAvailable() {
        if (cachedAvailability != null) {
            return cachedAvailability;
        }
        String path = findStockfishPath();
        if (path != null) {
            cachedBinaryPath = path;
            cachedAvailability = true;
            return true;
        }
        cachedAvailability = false;
        return false;
    }

    public static String getEnginePath() {
        if (isAvailable()) {
            return cachedBinaryPath;
        }
        return null;
    }

    public static String getEngineName() {
        if (isAvailable()) {
            return cachedEngineName;
        }
        return "Master AI Engine (Built-in)";
    }

    private static String findStockfishPath() {
        String userDir = System.getProperty("user.dir");
        String sep = File.separator;

        // Candidate search directories
        String[] searchDirs = new String[]{
                "assets" + sep + "stockfish",
                "assets",
                "tools" + sep + "stockfish",
                "tools",
                "desktop" + sep + "assets" + sep + "stockfish",
                "desktop" + sep + "assets",
                userDir + sep + "assets" + sep + "stockfish",
                userDir + sep + "assets",
                userDir + sep + "tools" + sep + "stockfish",
                userDir + sep + "tools",
                userDir
        };

        for (String dirPath : searchDirs) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName().toLowerCase();
                        if (name.startsWith("stockfish") && (name.endsWith(".exe") || !name.contains("."))) {
                            if (f.isFile() && f.canExecute()) {
                                System.out.println("✅ Found Stockfish Engine at: " + f.getAbsolutePath());
                                if (name.contains("avx2")) {
                                    cachedEngineName = "Stockfish 18 (AVX2)";
                                } else if (name.contains("bmi2")) {
                                    cachedEngineName = "Stockfish 18 (BMI2)";
                                } else {
                                    cachedEngineName = "Stockfish 18 (UCI)";
                                }
                                return f.getAbsolutePath();
                            }
                        }
                    }
                }
            }
        }

        // Test running stockfish directly from system PATH
        try {
            Process p = new ProcessBuilder("stockfish").start();
            p.destroy();
            cachedEngineName = "Stockfish (System PATH)";
            return "stockfish";
        } catch (Exception ignored) {}

        try {
            Process p = new ProcessBuilder("stockfish.exe").start();
            p.destroy();
            cachedEngineName = "Stockfish (System PATH)";
            return "stockfish.exe";
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Evaluates a FEN position using Stockfish at the specified depth.
     */
    public static EvaluationResult evaluatePosition(String fen, Piece.Color activeTurn, int depth) {
        if (!isAvailable()) return null;

        Process process = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            ProcessBuilder pb = new ProcessBuilder(cachedBinaryPath);
            pb.redirectErrorStream(true);
            process = pb.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            // Initialize UCI
            writer.write("uci\n");
            writer.write("isready\n");
            writer.write("position fen " + fen + "\n");
            writer.write("go depth " + Math.max(8, depth) + "\n");
            writer.flush();

            String line;
            int lastScoreCp = 0;
            boolean isMate = false;
            int mateIn = 0;
            String bestMoveStr = null;

            long deadline = System.currentTimeMillis() + 800L;

            // Watchdog to guarantee no thread will ever hang on readLine()
            final Process pFinal = process;
            Thread watchdog = new Thread(() -> {
                try {
                    Thread.sleep(1000L);
                    if (pFinal != null && pFinal.isAlive()) {
                        pFinal.destroyForcibly();
                    }
                } catch (InterruptedException ignored) {}
            });
            watchdog.setDaemon(true);
            watchdog.start();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("info ") && line.contains("score ")) {
                    if (line.contains("score cp ")) {
                        String[] parts = line.split("score cp ");
                        if (parts.length > 1) {
                            String numStr = parts[1].split(" ")[0];
                            try {
                                int cp = Integer.parseInt(numStr);
                                // Stockfish outputs score from active side perspective
                                lastScoreCp = (activeTurn == Piece.Color.WHITE) ? cp : -cp;
                                isMate = false;
                            } catch (Exception ignored) {}
                        }
                    } else if (line.contains("score mate ")) {
                        String[] parts = line.split("score mate ");
                        if (parts.length > 1) {
                            String numStr = parts[1].split(" ")[0];
                            try {
                                mateIn = Integer.parseInt(numStr);
                                isMate = true;
                                lastScoreCp = (mateIn > 0) ? 90000 : -90000;
                            } catch (Exception ignored) {}
                        }
                    }
                }

                if (line.startsWith("bestmove ")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1) {
                        bestMoveStr = parts[1];
                    }
                    break;
                }

                if (System.currentTimeMillis() > deadline) {
                    try {
                        writer.write("stop\n");
                        writer.flush();
                    } catch (Exception ignored) {}
                }
            }

            watchdog.interrupt();

            try {
                writer.write("quit\n");
                writer.flush();
            } catch (Exception ignored) {}

            Position bestFrom = null;
            Position bestTo = null;
            if (bestMoveStr != null && bestMoveStr.length() >= 4 && !bestMoveStr.contains("none")) {
                bestFrom = FENUtils.fromUCISquare(bestMoveStr.substring(0, 2));
                bestTo = FENUtils.fromUCISquare(bestMoveStr.substring(2, 4));
            }

            return new EvaluationResult(lastScoreCp, isMate, mateIn, bestFrom, bestTo, bestMoveStr);

        } catch (Exception e) {
            return null;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }
}
