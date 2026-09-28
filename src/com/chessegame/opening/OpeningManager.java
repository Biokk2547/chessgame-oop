package com.chessegame.opening;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.chessegame.model.Piece;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * OpeningManager: Curated chess opening repertoire database and local progress persistence.
 */
public class OpeningManager {

    private static final String PREF_NAME = "chessgame_opening_progress";
    private static final String KEY_COMPLETED_PREFIX = "opening_completed_";
    private static final String KEY_STARS_PREFIX = "opening_stars_";

    private static final List<Opening> openings = new ArrayList<>();

    static {
        initOpenings();
    }

    private static void initOpenings() {
        openings.clear();

        // 1. Italian Game (Giuoco Piano)
        openings.add(new Opening(
                1,
                "Italian Game",
                "เกมอิตาเลียน (Italian Game)",
                "Open Game",
                Piece.Color.WHITE,
                Arrays.asList("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"),
                Arrays.asList("1. e4", "1... e5", "2. Nf3", "2... Nc6", "3. Bc4"),
                Arrays.asList(
                        "1. e4: ยึดครองศูนย์กลางกระดานและเปิดทางให้ควีนกับบิชอป",
                        "1... e5: ดำตอบโต้เพื่อยึดพื้นที่กลางกระดานเช่นกัน",
                        "2. Nf3: พัฒนาม้าเข้าสู่กลางกระดานและโจมตีเบี้ย e5 ของดำ",
                        "2... Nc6: ดำพัฒนาม้าเพื่อคุ้มกันเบี้ย e5",
                        "3. Bc4: นำบิชอปออกสู่แนวทแยง เล็งโจมตีจุดอ่อนช่อง f7 ของราชาดำ!"
                ),
                "เปิดเกมคลาสสิก พัฒนาตัวหมากรวดเร็ว และเล็งจุดอ่อนช่อง f7 ของดำ",
                "ง่าย"
        ));

        // 2. Ruy Lopez (Spanish Opening)
        openings.add(new Opening(
                2,
                "Ruy Lopez",
                "รูย โลเปซ (Ruy Lopez)",
                "Open Game",
                Piece.Color.WHITE,
                Arrays.asList("e2e4", "e7e5", "g1f3", "b8c6", "f1b5"),
                Arrays.asList("1. e4", "1... e5", "2. Nf3", "2... Nc6", "3. Bb5"),
                Arrays.asList(
                        "1. e4: ยึดครองพื้นที่กลางกระดาน",
                        "1... e5: ดำส่งเบี้ยมาคุมกลางกระดานสมมาตร",
                        "2. Nf3: พัฒนาม้าและขู่กินเบี้ย e5 ของดำ",
                        "2... Nc6: ดำส่งม้ามาคุ้มกันเบี้ย",
                        "3. Bb5: ตรึง (Pin) และกดดันม้า c6 ซึ่งเป็นตัวคุ้มกันหลักของดำ!"
                ),
                "สร้างแรงกดดันต่อตัวคุ้มกันของดำ เพื่อช่วงชิงความได้เปรียบเชิงโครงสร้างระยะยาว",
                "ปานกลาง"
        ));

        // 3. King's Gambit
        openings.add(new Opening(
                3,
                "King's Gambit",
                "คิงส์แกมบิท (King's Gambit)",
                "Gambit",
                Piece.Color.WHITE,
                Arrays.asList("e2e4", "e7e5", "f2f4", "e5f4", "g1f3"),
                Arrays.asList("1. e4", "1... e5", "2. f4", "2... exf4", "3. Nf3"),
                Arrays.asList(
                        "1. e4: ส่งเบี้ยครองศูนย์กลางกระดาน",
                        "1... e5: ดำรับด้วยเบี้ย e5",
                        "2. f4!: สังเวยเบี้ย f4 ทันทีเพื่อดึงเบี้ยดำออกจากศูนย์กลางและเปิดเสา f ให้เรือ",
                        "2... exf4: ดำกินรับคำท้า (King's Gambit Accepted)",
                        "3. Nf3: พัฒนาม้าเพื่อป้องกันควีนดำรุกที่ h4 และเตรียมคุมกลางกระดาน"
                ),
                "สละเบี้ยปีกเพื่อครองกลางกระดานเบ็ดเสร็จ และเปิดเกมบุกใส่คิงดำอย่างดุดัน",
                "ท้าทาย"
        ));

        // 4. Queen's Gambit
        openings.add(new Opening(
                4,
                "Queen's Gambit",
                "ควีนส์แกมบิท (Queen's Gambit)",
                "Closed Game",
                Piece.Color.WHITE,
                Arrays.asList("d2d4", "d7d5", "c2c4", "e7e6", "b1c3"),
                Arrays.asList("1. d4", "1... d5", "2. c4", "2... e6", "3. Nc3"),
                Arrays.asList(
                        "1. d4: ควบคุมกลางกระดานช่อง d4 และ e5 อย่างมั่นคง",
                        "1... d5: ดำตอบโต้ยึดกลางกระดานเช่นกัน",
                        "2. c4!: ยื่นเบี้ย c4 ให้ดำกิน เพื่อดึงเบี้ยดำออกจากศูนย์กลางกระดาน",
                        "2... e6: ดำไม่กิน แต่สร้างแนวกำแพงคุ้มกัน (Queen's Gambit Declined)",
                        "3. Nc3: เพิ่มแรงกดดันต่อช่อง d5 และเตรียมดัน e4 เพื่อครองกลางกระดาน"
                ),
                "สละเบี้ยปีกชั่วคราวเพื่อยึดครองพื้นที่กลางกระดานทั้งหมดอย่างเหนียวแน่น",
                "ปานกลาง"
        ));

        // 5. Sicilian Defense
        openings.add(new Opening(
                5,
                "Sicilian Defense",
                "ซิซิเลียน ดีเฟนซ์ (Sicilian Defense)",
                "Semi-Open",
                Piece.Color.BLACK,
                Arrays.asList("e2e4", "c7c5", "g1f3", "d7d6", "d2d4", "c5d4"),
                Arrays.asList("1. e4", "1... c5", "2. Nf3", "2... d6", "3. d4", "3... cxd4"),
                Arrays.asList(
                        "1. e4: ขาวเปิดเกมด้วยเบี้ยหน้าคิง",
                        "1... c5!: ซิซิเลียน! ควบคุมช่อง d4 จากด้านข้างเพื่อสร้างเกมที่ไม่สมมาตร",
                        "2. Nf3: ขาวพัฒนาม้าเตรียมเปิดเสากลาง",
                        "2... d6: ดำคุมช่อง e5 และเปิดทางให้บิชอป c8",
                        "3. d4: ขาวดันเบี้ยเปิดเกมปะทะกลางกระดาน (Open Sicilian)",
                        "3... cxd4: ดำแลกเบี้ยริมกับเบี้ยกลางกระดานของขาวอย่างได้เปรียบ!"
                ),
                "อาวุธเคาน์เตอร์แอทแทคที่อันตรายที่สุดของดำ แลกเบี้ยปีกกับเบี้ยกลางกระดานของขาว",
                "ท้าทาย"
        ));

        // 6. French Defense
        openings.add(new Opening(
                6,
                "French Defense",
                "เฟรนช์ ดีเฟนซ์ (French Defense)",
                "Semi-Open",
                Piece.Color.BLACK,
                Arrays.asList("e2e4", "e7e6", "d2d4", "d7d5", "b1c3", "g8f6"),
                Arrays.asList("1. e4", "1... e6", "2. d4", "2... d5", "3. Nc3", "3... Nf6"),
                Arrays.asList(
                        "1. e4: ขาวเปิดเกมด้วยเบี้ยหน้าคิง",
                        "1... e6: เตรียมดันเบี้ย d5 เพื่อท้าทายกลางกระดานของขาว",
                        "2. d4: ขาวครอบครองพื้นที่กลางกระดานเต็มรูปแบบ",
                        "2... d5!: ดำโจมตีใจกลางกระดานของขาวทันที สร้างแนวรับแข็งแกร่ง",
                        "3. Nc3: ขาวพัฒนาม้ามาคุ้มกันเบี้ย e4",
                        "3... Nf6: ดำพัฒนาม้ากดดันเบี้ย e4 ของขาวอย่างต่อเนื่อง"
                ),
                "สร้างกำแพงเบี้ยทแยงที่แข็งแกร่ง ท้าทายและบดทำลายศูนย์กลางของขาว",
                "ง่าย"
        ));

        // 7. Caro-Kann Defense
        openings.add(new Opening(
                7,
                "Caro-Kann Defense",
                "คาโร-คานน์ (Caro-Kann Defense)",
                "Semi-Open",
                Piece.Color.BLACK,
                Arrays.asList("e2e4", "c7c6", "d2d4", "d7d5", "b1c3", "d5e4"),
                Arrays.asList("1. e4", "1... c6", "2. d4", "2... d5", "3. Nc3", "3... dxe4"),
                Arrays.asList(
                        "1. e4: ขาวเปิดเกมด้วยเบี้ยหน้าคิง",
                        "1... c6: เตรียมพร้อมดันเบี้ย d5 โดยไม่ปิดทางเดินบิชอปช่อง c8",
                        "2. d4: ขาวสร้างศูนย์กลางกระดาน d4-e4",
                        "2... d5!: ดำโจมตีกลางกระดานทันที โครงสร้างเบี้ยแข็งแกร่งมาก",
                        "3. Nc3: ขาวส่งม้ามาคุ้มกันเบี้ย e4",
                        "3... dxe4: ดำกินแลกเบี้ยกลางกระดาน ลดทอนกำลังของขาว"
                ),
                "สายตั้งรับที่ปลอดภัยที่สุด ไร้จุดอ่อน และบิชอปเดินได้อย่างอิสระ",
                "ง่าย"
        ));

        // 8. King's Indian Defense
        openings.add(new Opening(
                8,
                "King's Indian Defense",
                "คิงส์ อินเดียน (King's Indian)",
                "Closed Game",
                Piece.Color.BLACK,
                Arrays.asList("d2d4", "g8f6", "c2c4", "g7g6", "b1c3", "f8g7"),
                Arrays.asList("1. d4", "1... Nf6", "2. c4", "2... g6", "3. Nc3", "3... Bg7"),
                Arrays.asList(
                        "1. d4: ขาวเปิดเกมด้วยเบี้ยหน้าควีน",
                        "1... Nf6: ดำพัฒนาม้าคุมช่อง e4 ป้องกันขาวดันเบี้ยสองตัว",
                        "2. c4: ขาวเพิ่มการควบคุมกลางกระดาน",
                        "2... g6: ดำเตรียมทำ Fianchetto บิชอปไปที่ช่อง g7",
                        "3. Nc3: ขาวเตรียมดัน e4 เพื่อครองกลางกระดานเต็มที่",
                        "3... Bg7!: บิชอปเข้ามุมคุมแนวทแยงยาว เล็งถล่มคิงขาวในอนาคต!"
                ),
                "ยอมให้ขาวคุมกลางกระดานชั่วคราว แล้วซุ่มเตรียมบุกสายฟ้าแลบใส่คิงขาว",
                "ท้าทาย"
        ));

        // 9. London System
        openings.add(new Opening(
                9,
                "London System",
                "ลอนดอน ซิสเต็ม (London System)",
                "System",
                Piece.Color.WHITE,
                Arrays.asList("d2d4", "d7d5", "c1f4", "g8f6", "e2e3"),
                Arrays.asList("1. d4", "1... d5", "2. Bf4", "2... Nf6", "3. e3"),
                Arrays.asList(
                        "1. d4: ยึดครองกลางกระดานด้วยเบี้ยหน้าควีน",
                        "1... d5: ดำตอบโต้ด้วยเบี้ย d5",
                        "2. Bf4!: หัวใจของลอนดอน! นำบิชอปออกมานอกแนวกำแพงเบี้ยก่อนปิดประตู",
                        "2... Nf6: ดำพัฒนาม้าตามมาตรฐาน",
                        "3. e3: สร้างโครงสร้างพีระมิดเบี้ยที่มั่นคงและคุ้มกัน d4 อย่างสมบูรณ์แบบ"
                ),
                "ระบบสากลที่เล่นง่าย มั่นคง ไร้จุดอ่อน เดินแบบเดิมได้กับทุกกลยุทธ์ของคู่ต่อสู้",
                "ง่าย"
        ));

        // 10. English Opening
        openings.add(new Opening(
                10,
                "English Opening",
                "อิงลิช โอเพนนิ่ง (English Opening)",
                "Flank",
                Piece.Color.WHITE,
                Arrays.asList("c2c4", "e7e5", "b1c3", "g8f6", "g2g3"),
                Arrays.asList("1. c4", "1... e5", "2. Nc3", "2... Nf6", "3. g3"),
                Arrays.asList(
                        "1. c4!: ใช้เบี้ยริมเข้าคุมช่อง d5 แทนการเปิดด้วยเบี้ยกลางกระดาน",
                        "1... e5: ดำตอบโต้ด้วยการยึดกลางกระดาน",
                        "2. Nc3: ขาวพัฒนาม้าเสริมแรงกดดันช่อง d5",
                        "2... Nf6: ดำพัฒนาม้าสมดุล",
                        "3. g3: เตรียมนำบิชอปเข้ามุม g2 (Fianchetto) เพื่อคุมแนวทแยงยาว"
                ),
                "เปิดเกมทางปีกที่ยืดหยุ่นสูง สามารถปรับเปลี่ยนแผนตามการตอบโต้ของคู่ต่อสู้ได้ตลอดเวลา",
                "ปานกลาง"
        ));
    }

    public static List<Opening> getAllOpenings() {
        return Collections.unmodifiableList(openings);
    }

    public static Opening getOpeningById(int id) {
        for (Opening o : openings) {
            if (o.getId() == id) return o;
        }
        return openings.get(0);
    }

    // --- Persistence ---
    public static boolean isOpeningCompleted(int openingId) {
        try {
            Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
            return prefs.getBoolean(KEY_COMPLETED_PREFIX + openingId, false);
        } catch (Throwable t) {
            return false;
        }
    }

    public static int getOpeningStars(int openingId) {
        try {
            Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
            return prefs.getInteger(KEY_STARS_PREFIX + openingId, 0);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static void recordOpeningCompleted(int openingId, int stars) {
        try {
            Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
            prefs.putBoolean(KEY_COMPLETED_PREFIX + openingId, true);
            int currentStars = prefs.getInteger(KEY_STARS_PREFIX + openingId, 0);
            if (stars > currentStars) {
                prefs.putInteger(KEY_STARS_PREFIX + openingId, stars);
            }
            prefs.flush();
        } catch (Throwable ignored) {}
    }

    public static int getTotalCompletedCount() {
        int count = 0;
        for (Opening o : openings) {
            if (isOpeningCompleted(o.getId())) count++;
        }
        return count;
    }
}
