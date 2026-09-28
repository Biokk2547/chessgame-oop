package com.chessegame.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.chessegame.model.Piece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PuzzleManager: Curated tactical chess puzzles and local persistence of puzzle progress.
 */
public class PuzzleManager {

    private static final String PREF_NAME = "chessgame_puzzle_progress";
    private static final String KEY_SOLVED_PREFIX = "puzzle_solved_";
    private static final String KEY_STARS_PREFIX = "puzzle_stars_";

    private static final List<Puzzle> puzzles = new ArrayList<>();

    static {
        initPuzzles();
    }

    private static void initPuzzles() {
        puzzles.clear();

        // Puzzle 1: Scholar's Mate in 1
        puzzles.add(new Puzzle(
                1,
                "รุกฆาตพระอาจารย์ (Scholar's Mate)",
                "รุกฆาตใน 1 ตา",
                "ขาวเดินและรุกฆาตดำใน 1 ตาเดิน!",
                "r1bqkb1r/pppp1ppp/2n5/4p3/2B1n3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 1",
                Piece.Color.WHITE,
                Collections.singletonList("f3f7"),
                "สังเกตจุดอ่อน f7 ข้างราชาดำที่ไม่มีตัวป้องกันนอกจากคิง!",
                "ควีนขาวกิน f7 รุกฆาต โดยมีบิชอปที่ c4 คอยคุ้มกัน"
        ));

        // Puzzle 2: Back-Rank Mate in 1
        puzzles.add(new Puzzle(
                2,
                "รุกฆาตแถวหลัง (Back-Rank Mate)",
                "รุกฆาตใน 1 ตา",
                "ราชาดำติดเบี้ยตัวเองในแถวหลัง จงหาตาเดินรุกฆาต!",
                "6k1/5ppp/8/8/8/8/5PPP/1R4K1 w - - 0 1",
                Piece.Color.WHITE,
                Collections.singletonList("b1b8"),
                "ราชาดำถูกเบี้ยของตัวเองปิดทางหนีไว้ในแถวหลัง ส่งเรือไปจู่โจม!",
                "เรือขาวพุ่งไป b8 รุกฆาตในแถวหลัง (Back Rank Mate)"
        ));

        // Puzzle 3: Arabian Mate in 1
        puzzles.add(new Puzzle(
                3,
                "มนตราม้าและเรือ (Arabian Mate)",
                "รุกฆาตใน 1 ตา",
                "ประสานงานระหว่างเรือและม้าเพื่อปิดประตูตาย!",
                "7k/5R2/5N2/8/8/8/8/4K3 w - - 0 1",
                Piece.Color.WHITE,
                Collections.singletonList("f7h7"),
                "เรือและม้าทำงานร่วมกันเพื่อปิดกั้นทางหนีทุกช่องของราชา!",
                "เรือเดินไป h7 รุกฆาต โดยม้าที่ f6 คอยคุ้มกันเรือและคุมช่อง g8"
        ));

        // Puzzle 4: Smothered Mate in 1
        puzzles.add(new Puzzle(
                4,
                "รุกฆาตหายใจไม่ออก (Smothered Mate)",
                "รุกฆาตใน 1 ตา",
                "ราชาดำถูกล้อมรอบด้วยหมากของตนเอง ใช้ม้าแทงเผด็จศึก!",
                "6rk/6pp/8/4N3/8/1Q6/8/4K3 w - - 0 1",
                Piece.Color.WHITE,
                Collections.singletonList("e5f7"),
                "ราชาดำถูกล้อมรอบจนขยับไม่ได้ ใช้ม้ากระโดดไปรุกฆาตโดยมีควีนคอยคุ้มกัน!",
                "ม้ากระโดดไป f7 รุกฆาต โดยควีน b3 คุ้มกัน ราชาดำถูกตัวของตนเองอุดตันทุกช่อง"
        ));

        // Puzzle 5: Corner Trap Mate in 1
        puzzles.add(new Puzzle(
                5,
                "ปิดมุมสังหาร (Corner Trap Mate)",
                "รุกฆาตใน 1 ตา",
                "ราชาดำจนมุมอยู่ที่มุมกระดาน ส่งเรือไปปิดฉาก!",
                "k7/7R/1K6/8/8/8/8/8 w - - 0 1",
                Piece.Color.WHITE,
                Collections.singletonList("h7h8"),
                "ราชาดำติดอยู่ที่มุมกระดาน ส่งเรือไปปิดฉากที่แถว 8!",
                "เรือขึ้นไป h8 รุกฆาต ราชาขาวที่ b6 คุมช่อง a7 และ b7 ไว้หมดแล้ว"
        ));

        // Puzzle 6: Knight Fork
        puzzles.add(new Puzzle(
                6,
                "ม้าจับสองทาง (Knight Fork Tactic)",
                "กลยุทธ์จับกินสองทาง",
                "กระโดดม้าไปรุกคิงและกินเรือฟรี!",
                "r3kb1r/pp3ppp/2n5/1N6/8/8/PPPP1PPP/R1BQK2R w KQkq - 0 1",
                Piece.Color.WHITE,
                Arrays.asList("b5c7", "e8d8", "c7a8"),
                "กระโดดม้าไปที่ช่อง c7 เพื่อรุกราชาพร้อมเล็งกินเรือที่มุมกระดาน!",
                "ม้าที่ c7 รุกคิงและเล็งเรือพร้อมกัน ทำให้ขาวได้เปรียบกินเรือ a8 ฟรี!"
        ));

        // Puzzle 7: Opera Queen Sacrifice Mate in 2
        puzzles.add(new Puzzle(
                7,
                "การเสียสละควีนแห่งโอเปร่า (Opera Mate)",
                "รุกฆาตใน 2 ตา",
                "สละควีนเพื่อเบี่ยงเบนตัวป้องกันแถวหลัง แล้วใช้เรือรุกฆาต!",
                "4kb1r/p2n1ppp/4q3/4p1B1/4P3/1Q6/PPP2PPP/2KR4 w k - 0 1",
                Piece.Color.WHITE,
                Arrays.asList("b3b8", "d7b8", "d1d8"),
                "เสียสละควีนที่ b8 เพื่อเบี่ยงเบนตัวป้องกันแถวหลัง แล้วใช้เรือรุกฆาต!",
                "ขาวสละควีนที่ b8 ดึงม้าดำออกจากการคุมช่อง d8 แล้วใช้เรือลงไปรุกฆาตที่ d8!"
        ));

        // Puzzle 8: Deflection Back-Rank Mate in 2
        puzzles.add(new Puzzle(
                8,
                "เบี่ยงเบนแถวหลัง (Back-Rank Deflection)",
                "รุกฆาตใน 2 ตา",
                "บุกทะลวงแนวรับแถวหลังด้วยการสละควีน!",
                "3r2k1/5ppp/8/8/8/8/5PPP/R3Q1K1 w - - 0 1",
                Piece.Color.WHITE,
                Arrays.asList("e1e8", "d8e8", "a1e8"),
                "สละควีนที่ e8 เพื่อบังคับให้เรือดำต้องกิน แล้วใช้เรือขาวซ้ำแถวหลัง!",
                "ควีนบุก e8 บังคับให้เรือดำกิน จากนั้นเรือขาวบุกเข้ายึดแถวหลัง รุกฆาตสมบูรณ์แบบ!"
        ));

        // Puzzle 9: Absolute Pin and Win
        puzzles.add(new Puzzle(
                9,
                "การตรึงหมากสมบูรณ์ (Absolute Pin)",
                "กลยุทธ์ตรึงหมาก",
                "ใช้เรือตรึงควีนดำเข้ากับราชาเพื่อจับกินฟรี!",
                "r1b1k2r/pp2qppp/2n5/8/8/8/PPPP1PPP/R1B2RK1 w kq - 0 1",
                Piece.Color.WHITE,
                Collections.singletonList("f1e1"),
                "เดินเรือมาที่แนว e เพื่อตรึงควีนคู่ต่อสู้ไว้กับราชา!",
                "เรือที่ e1 ตรึงควีนดำไม่ให้หนี เพราะราชาอยู่ข้างหลัง ขาวจึงกินควีนได้แน่นอน"
        ));

        // Puzzle 10: Bishop Skewer Tactic
        puzzles.add(new Puzzle(
                10,
                "การเสียบหมากพิฆาต (Bishop Skewer)",
                "กลยุทธ์แทงทะลุ",
                "บิชอปรุกคิงพร้อมแทงทะลุไปถึงควีนที่อยู่ด้านหลัง!",
                "1q6/8/3k4/8/8/8/8/2B1K3 w - - 0 1",
                Piece.Color.WHITE,
                Arrays.asList("c1f4", "d6e7", "f4b8"),
                "เดินบิชอปไปที่ f4 เพื่อรุกคิงพร้อมแทงทะลุไปถึงควีนที่อยู่ข้างหลัง!",
                "บิชอปที่ f4 รุกคิงดำ เมื่อคิงต้องเดินหนี บิชอปจะจับกินควีนดำที่อยู่ด้านหลังฟรี!"
        ));
    }

    private static Preferences getPrefs() {
        try {
            return Gdx.app.getPreferences(PREF_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Puzzle> getAllPuzzles() {
        return Collections.unmodifiableList(puzzles);
    }

    public static Puzzle getPuzzleById(int id) {
        for (Puzzle p : puzzles) {
            if (p.getId() == id) return p;
        }
        return puzzles.get(0);
    }

    public static boolean isPuzzleSolved(int puzzleId) {
        Preferences p = getPrefs();
        if (p == null) return false;
        return p.getBoolean(KEY_SOLVED_PREFIX + puzzleId, false);
    }

    public static int getPuzzleStars(int puzzleId) {
        Preferences p = getPrefs();
        if (p == null) return 0;
        return p.getInteger(KEY_STARS_PREFIX + puzzleId, 0);
    }

    public static void recordPuzzleSolved(int puzzleId, int stars) {
        Preferences p = getPrefs();
        if (p == null) return;
        p.putBoolean(KEY_SOLVED_PREFIX + puzzleId, true);
        int currentStars = p.getInteger(KEY_STARS_PREFIX + puzzleId, 0);
        if (stars > currentStars) {
            p.putInteger(KEY_STARS_PREFIX + puzzleId, Math.min(3, stars));
        }
        p.flush();
    }

    public static int getTotalSolved() {
        int count = 0;
        for (Puzzle p : puzzles) {
            if (isPuzzleSolved(p.getId())) count++;
        }
        return count;
    }

    public static int getTotalPuzzleStars() {
        int total = 0;
        for (Puzzle p : puzzles) {
            total += getPuzzleStars(p.getId());
        }
        return total;
    }
}
