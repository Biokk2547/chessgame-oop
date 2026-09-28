package com.chessegame.level;

import com.chessegame.ai.Evaluation;
import com.chessegame.model.Board;
import com.chessegame.model.Piece;
import com.chessegame.model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * BossLevel: Specification of a boss stage in Boss Rush Mode.
 * Defines AI depth, style, time controls, handicap rules, dialogues, and star criteria.
 */
public class BossLevel {

    public enum HandicapType {
        NONE("จอมโจรแล่นเรือ (Pirate Rush)", "บอสโจรสลัดชอบเดินเกมเร็วและจู่โจมฉับพลัน"),
        MAGIC_BARRIER("ค่ายกลมนตรา (Magic Barrier)", "บอสแม่มดเน้นการคุมแนวรับและเวทมนตร์โครงสร้างเบี้ย"),
        STONE_WALL("ค่ายกลศิลา (Stone Fortress)", "บอสเน้นการคุมแนวรับและโครงสร้างเบี้ย"),
        TIME_BLITZ("สายฟ้าแลบ (Blitz 90s)", "เวลาเดินกระชั้นชิดเพียง 90 วินาที"),
        TACTICAL_SHADOW("เงามายา (Shadow Tactics)", "บอสคำนวณแทคติกสูง Depth 5"),
        OVERLORD_STOCKFISH("จอมมารไร้พ่าย (Overlord Master)", "ขับเคลื่อนด้วย Master AI ระดับสูงสุด");

        private final String title;
        private final String description;

        HandicapType(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }

    private final int levelId; // 1 to 5
    private final String bossName;
    private final String bossTitle;
    private final String avatarPath;
    private final int aiDepth;
    private final Evaluation.AIStyle aiStyle;
    private final long timeLimitMs;
    private final long incrementMs;
    private final HandicapType handicap;
    private final String introDialogue;
    private final String winDialogue;
    private final String loseDialogue;
    private final int targetAccuracyFor3Stars;

    private final String abilityName;
    private final String abilityDescription;

    public BossLevel(int levelId, String bossName, String bossTitle, String avatarPath,
                     int aiDepth, Evaluation.AIStyle aiStyle, long timeLimitMs, long incrementMs,
                     HandicapType handicap, String introDialogue, String winDialogue,
                     String loseDialogue, int targetAccuracyFor3Stars) {
        this(levelId, bossName, bossTitle, avatarPath, aiDepth, aiStyle, timeLimitMs, incrementMs,
             handicap, introDialogue, winDialogue, loseDialogue, targetAccuracyFor3Stars,
             "สกิลบอส", "สกิลพิเศษเฉพาะตัวของบอส");
    }

    public BossLevel(int levelId, String bossName, String bossTitle, String avatarPath,
                     int aiDepth, Evaluation.AIStyle aiStyle, long timeLimitMs, long incrementMs,
                     HandicapType handicap, String introDialogue, String winDialogue,
                     String loseDialogue, int targetAccuracyFor3Stars,
                     String abilityName, String abilityDescription) {
        this.levelId = levelId;
        this.bossName = bossName;
        this.bossTitle = bossTitle;
        this.avatarPath = avatarPath;
        this.aiDepth = aiDepth;
        this.aiStyle = aiStyle;
        this.timeLimitMs = timeLimitMs;
        this.incrementMs = incrementMs;
        this.handicap = handicap;
        this.introDialogue = introDialogue;
        this.winDialogue = winDialogue;
        this.loseDialogue = loseDialogue;
        this.targetAccuracyFor3Stars = targetAccuracyFor3Stars;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
    }

    public int getLevelId() { return levelId; }
    public String getBossName() { return bossName; }
    public String getBossTitle() { return bossTitle; }
    public String getAvatarPath() { return avatarPath; }
    public int getAiDepth() { return aiDepth; }
    public Evaluation.AIStyle getAiStyle() { return aiStyle; }
    public long getTimeLimitMs() { return timeLimitMs; }
    public long getIncrementMs() { return incrementMs; }
    public HandicapType getHandicap() { return handicap; }
    public String getIntroDialogue() { return introDialogue; }
    public String getWinDialogue() { return winDialogue; }
    public String getLoseDialogue() { return loseDialogue; }
    public int getTargetAccuracyFor3Stars() { return targetAccuracyFor3Stars; }
    public String getAbilityName() { return abilityName; }
    public String getAbilityDescription() { return abilityDescription; }

    /**
     * Initializes any custom handicap board configuration if needed.
     */
    public Board createStartingBoard() {
        return new Board();
    }

    /**
     * Returns the list of all 5 predefined Boss Levels (Balanced for fun gameplay).
     */
    public static List<BossLevel> getAllLevels() {
        List<BossLevel> levels = new ArrayList<>();

        // Level 1: PirateCat (Easy / Beginner Friendly)
        levels.add(new BossLevel(
                1,
                "กัปตันแมวโจรสลัด",
                "PirateCat (Rookie)",
                "charactor/PirateCat/PirateCat_f1.png",
                1,
                Evaluation.AIStyle.AGGRESSIVE,
                5 * 60 * 1000L, // 5 mins
                3000L,
                HandicapType.NONE,
                "PirateCat: 'เหมียวย่าฮ่าฮ่า! ส่งสมบัติบนกระดานหมากรุกมาให้ข้าซะดีๆ!'",
                "PirateCat: 'แง้ววว! เรือโจรสลัดของข้าอัปปางซะแล้ว! ฝากไว้ก่อนเถอะ!'",
                "PirateCat: 'ฮ่าๆๆ! สมบัติและกระดานนี้ตกเป็นของกัปตันแมวแล้ว!'",
                40,
                "ขุมทรัพย์โจรสลัด (Pirate's Plunder)",
                "เมื่อผู้เล่นกินหมากของโจรสลัดได้ จะได้รับเวลาโบนัส +20 วินาที!"
        ));

        // Level 2: WitchKitty (Casual / Balanced)
        levels.add(new BossLevel(
                2,
                "แม่มดเหมียวมนตรา",
                "WitchKitty (Mystic)",
                "charactor/witchitty/witchKitty_curiousIdleBreaker_f1.png",
                1, // Adjusted from 2 to 1 for casual fun
                Evaluation.AIStyle.DEFENSIVE,
                5 * 60 * 1000L, // 5 mins (was 4 mins)
                3000L,
                HandicapType.MAGIC_BARRIER,
                "WitchKitty: 'เมี้ยว~ คาถามนต์ดำของข้าจะสาปหมากของเจ้าให้หยุดนิ่ง!'",
                "WitchKitty: 'มนตร์สะท้อนกลับ... เมี้ยว! เจ้าแก้กลมนตราของข้าได้ยังไงกัน?!'",
                "WitchKitty: 'คิกๆๆ เวทมนตร์ของข้าสมบูรณ์แบบ เจ้าพ่ายแพ้แล้ว!'",
                45,
                "มนตราเกราะ 9 ชีวิต (Nine Lives Ward)",
                "เมื่อคิงแม่มดถูกรุกครั้งแรก มนต์คุ้มภัยจะสร้างคลื่นเวทสีม่วงปกป้อง"
        ));

        // Level 3: Berserker Box (Rapid 180s / Accessible Blitz)
        levels.add(new BossLevel(
                3,
                "กล่องคลั่งดาบโลหิต",
                "Berserker Box (Rapid 180s)",
                "charactor/evilbox/EvilBox1_f6.png",
                2,
                Evaluation.AIStyle.AGGRESSIVE,
                180 * 1000L, // 180 seconds Rapid (extended from 120s)
                2000L, // +2s increment
                HandicapType.TIME_BLITZ,
                "Berserker Box: 'บุกแหลก! ข้าจะฉีกกระดานนี้เป็นชิ้นๆ ใน 180 วินาที!'",
                "Berserker Box: 'เร็วเกินไป... ข้าเดินเร็วเกินไปจนพลาดท่า!'",
                "Berserker Box: 'เวลาหมดแล้ว! สายฟ้าฟาดของข้าไร้ผู้ต้าน!'",
                50,
                "จิตวิญญาณแห่งความบ้าคลั่ง (Berserk Rage)",
                "บอสเดินเกมเร็วและจู่โจมด้วยไฟสะท้าน แต่หากผู้เล่นตั้งรับแน่นหนา บอสจะพลาดง่ายขึ้น"
        ));

        // Level 4: Shadow Vampire (Tactics / Accessible Depth 2)
        levels.add(new BossLevel(
                4,
                "เงาแวมไพร์มายา",
                "Shadow Vampire (Tactics)",
                "charactor/Vampire/VampireAngryframe1.png",
                2, // Adjusted from 3 to 2 for accessible tactics
                Evaluation.AIStyle.MASTER,
                5 * 60 * 1000L, // 5 mins (was 4 mins)
                3000L,
                HandicapType.TACTICAL_SHADOW,
                "Shadow Vampire: 'ข้าคือเงาสะท้อนในกระจกโลหิตของเจ้า... จงหาทางเอาชนะตัวเองดูสิ!'",
                "Shadow Vampire: 'เงามืดสลายตัว... เจ้าก้าวข้ามขีดจำกัดของตัวเองแล้ว!'",
                "Shadow Vampire: 'เจ้าแพ้ให้กับเงามืดในจิตใจของตนเอง!'",
                55,
                "ดูดกลืนเวลา (Time Leech)",
                "เมื่อแวมไพร์กินหมากผู้เล่นได้ จะขโมยเวลาเพียง 2 วินาที!"
        ));

        // Level 5: Overlord EvilBox (Final Boss / Fun & Beatable)
        levels.add(new BossLevel(
                5,
                "จอมมารกล่องทมิฬ",
                "Overlord EvilBox (Final Boss)",
                "charactor/evilbox/EvilBox1_f12.png",
                2, // Adjusted from 3 to 2 so human players can beat the boss
                Evaluation.AIStyle.MASTER,
                6 * 60 * 1000L, // 6 mins (was 5 mins)
                3000L,
                HandicapType.OVERLORD_STOCKFISH,
                "Overlord EvilBox: 'ข้าคือจุดสูงสุดแห่งปัญญาประดิษฐ์... เข้ามาประลองกันเลย!'",
                "Overlord EvilBox: 'เป็นไปไม่ได้... มนุษย์เอาชนะพลังสมบูรณ์แบบของข้าได้หรือนี่?!'",
                "Overlord EvilBox: 'หมากรุกคือตรรกะอันสมบูรณ์แบบ... และเจ้าไม่มีวันชนะข้า!'",
                60,
                "บาเรียกล่องทมิฬ (Overlord Core)",
                "บอสมีเกราะออร่าพลังงานจักรกล แต่ถูกปรับสมดุลให้ผู้เล่นสามารถโค่นล้มได้จริง!"
        ));

        return levels;
    }

    public static BossLevel getLevelById(int id) {
        for (BossLevel l : getAllLevels()) {
            if (l.getLevelId() == id) return l;
        }
        return getAllLevels().get(0);
    }
}
