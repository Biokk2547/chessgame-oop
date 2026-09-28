package com.chessegame.character;

import com.chessegame.model.Piece;
import java.util.Random;

/**
 * Manages in-game character dialogues in Thai, speech bubble display timers, and event reaction lines.
 * Dynamically adapts dialogues to match the active boss character (PirateCat, WitchKitty, Shadow Vampire, EvilBox).
 */
public class DialogManager {
    public enum CharacterType {
        VAMPIRE("Vampire (ผู้เล่นหมากขาว)"),
        EVILBOX("EvilBox (ผู้เล่นหมากดำ)"),
        OPPONENT("คู่ต่อสู้ (ผู้เล่นหมากดำ)");

        private final String displayName;

        CharacterType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum OpponentArchetype {
        PIRATE_CAT,
        WITCH_KITTY,
        SHADOW_VAMPIRE,
        EVILBOX
    }

    private CharacterType currentSpeaker = CharacterType.VAMPIRE;
    private String currentDialogue = "ยินดีต้อนรับสู่เกมหมากรุก! มาดูกันว่าใครจะชนะ";
    private float displayTimer = 5.0f; // seconds remaining to show dialogue
    private float idleTimer = 0.0f;
    private final Random random = new Random();

    // Active opponent context
    private String opponentName = "EvilBox";
    private String opponentAvatarPath = "";
    private OpponentArchetype archetype = OpponentArchetype.EVILBOX;

    // Dialogue Queueing Buffer to prevent instant AI move dialogue cut-offs
    private CharacterType pendingSpeaker = null;
    private String pendingDialogue = null;
    private float pendingDuration = 0f;

    public DialogManager() {
        triggerGameStart();
    }

    public void setOpponent(String name, String avatarPath) {
        this.opponentName = (name != null && !name.isEmpty()) ? name : "EvilBox";
        this.opponentAvatarPath = (avatarPath != null) ? avatarPath : "";
        if (opponentAvatarPath.contains("PirateCat") || opponentName.contains("Pirate") || opponentName.contains("โจรสลัด")) {
            this.archetype = OpponentArchetype.PIRATE_CAT;
        } else if (opponentAvatarPath.contains("witchitty") || opponentAvatarPath.contains("witchkitty") || opponentName.contains("Witch") || opponentName.contains("แม่มด")) {
            this.archetype = OpponentArchetype.WITCH_KITTY;
        } else if (opponentAvatarPath.contains("Vampire") || opponentName.contains("เงาแวมไพร์") || opponentName.contains("Shadow")) {
            this.archetype = OpponentArchetype.SHADOW_VAMPIRE;
        } else {
            this.archetype = OpponentArchetype.EVILBOX;
        }
    }

    public String getOpponentName() {
        return opponentName;
    }

    public OpponentArchetype getArchetype() {
        return archetype;
    }

    public void update(float delta) {
        if (displayTimer > 0) {
            displayTimer -= delta;
            if (displayTimer <= 0 && pendingDialogue != null) {
                // Smoothly dequeue pending dialogue
                this.currentSpeaker = pendingSpeaker;
                this.currentDialogue = pendingDialogue;
                this.displayTimer = pendingDuration;
                this.idleTimer = 0.0f;
                this.pendingSpeaker = null;
                this.pendingDialogue = null;
            }
        }
        idleTimer += delta;
        // Trigger idle dialogue every 14 seconds if no event dialogue is active
        if (idleTimer >= 14.0f) {
            idleTimer = 0.0f;
            triggerIdleDialogue();
        }
    }

    public void setDialogue(CharacterType speaker, String text, float durationSeconds) {
        // If an active dialogue has more than 1.5 seconds remaining, queue the incoming dialogue
        if (displayTimer > 1.5f) {
            this.pendingSpeaker = speaker;
            this.pendingDialogue = text;
            this.pendingDuration = durationSeconds;
            return;
        }

        this.currentSpeaker = speaker;
        this.currentDialogue = text;
        this.displayTimer = durationSeconds;
        this.idleTimer = 0.0f;
        this.pendingSpeaker = null;
        this.pendingDialogue = null;
    }

    public void triggerDialogue(CharacterType speaker, String text) {
        setDialogue(speaker, text, 4.0f);
    }

    public void triggerGameStart() {
        if (random.nextBoolean()) {
            setDialogue(CharacterType.VAMPIRE, "Vampire: ยินดีต้อนรับสู่สนามรบ! มาเริ่มเกมกันเลย " + opponentName + "!", 4.5f);
        } else {
            String line;
            switch (archetype) {
                case PIRATE_CAT:
                    line = opponentName + ": เหมียวย่าฮ่า! ข้าคือกัปตันแมวโจรสลัด เตรียมตัวสละเรือได้เลย!";
                    break;
                case WITCH_KITTY:
                    line = opponentName + ": เมี้ยว~ คาถามนต์ดำของข้าจะสาปหมากของเจ้าให้หยุดนิ่ง!";
                    break;
                case SHADOW_VAMPIRE:
                    line = opponentName + ": ข้าคือเงาสะท้อนในกระจกโลหิตของเจ้า... จงหาทางเอาชนะตัวเองดูสิ!";
                    break;
                default:
                    line = opponentName + ": กร๊าซซซ! ข้าจะเขมือบหมากของเจ้าลงกล่องให้หมด!";
                    break;
            }
            setDialogue(CharacterType.EVILBOX, line, 4.5f);
        }
    }

    public void triggerMove(Piece.Color turn, boolean captured) {
        if (captured) {
            if (turn == Piece.Color.WHITE) {
                String[] lines;
                switch (archetype) {
                    case PIRATE_CAT:
                        lines = new String[]{
                            "Vampire: หมากของเจ้าสูญสลายไปอีกตัวแล้ว " + opponentName + "!",
                            "Vampire: กรงเล็บโจรสลัดเริ่มทื่อแล้วนะ!",
                            "Vampire: พื้นที่บนเรือของเจ้ากำลังแคบลงเรื่อยๆ!"
                        };
                        break;
                    case WITCH_KITTY:
                        lines = new String[]{
                            "Vampire: เกราะเวทมนตร์ของเจ้าแตกสลายไปอีกชิ้นแล้ว " + opponentName + "!",
                            "Vampire: การโจมตีอันงดงาม! ล้างมนตร์สะกดสำเร็จ!",
                            "Vampire: พลังมนตราของเจ้ากำลังถดถอย!"
                        };
                        break;
                    case SHADOW_VAMPIRE:
                        lines = new String[]{
                            "Vampire: ร่างเงาของเจ้าถูกฉีกกระชากไปอีกส่วน!",
                            "Vampire: ข้าจะไม่แพ้ให้กับภาพลวงตา!",
                            "Vampire: ตัวจริงย่อมแข็งแกร่งกว่าเงาเสมอ!"
                        };
                        break;
                    default:
                        lines = new String[]{
                            "Vampire: หมากของเจ้าสูญสลายไปอีกตัวแล้ว!",
                            "Vampire: การโจมตีอันงดงาม! จับหมากสำเร็จ!",
                            "Vampire: พื้นที่ของเจ้ากำลังแคบลงเรื่อยๆ แล้วนะ!"
                        };
                        break;
                }
                setDialogue(CharacterType.VAMPIRE, lines[random.nextInt(lines.length)], 3.5f);
            } else {
                String[] lines;
                switch (archetype) {
                    case PIRATE_CAT:
                        lines = new String[]{
                            opponentName + ": ง่ำ! หมากตัวนั้นเสร็จข้าแล้ว! เหมียวฮ่าๆ!",
                            opponentName + ": จับหมากได้เหมือนจับหนูในครัวเรือ!",
                            opponentName + ": สมบัติชิ้นนี้ข้าขอยึดไปล่ะนะ!"
                        };
                        break;
                    case WITCH_KITTY:
                        lines = new String[]{
                            opponentName + ": เมี้ยว! หมากของเจ้าโดนมนตร์สะกดจนสลายไปแล้ว!",
                            opponentName + ": คิกๆๆ หมากหายไปในหม้อปรุงยาของข้าแล้ว!",
                            opponentName + ": พลังเวทมนตร์กลืนกินหมากของเจ้าแล้วนะ!"
                        };
                        break;
                    case SHADOW_VAMPIRE:
                        lines = new String[]{
                            opponentName + ": หมากของเจ้าสูญสลายในเงามืด!",
                            opponentName + ": ทุกสิ่งที่เจ้าสร้าง ข้าทำลายได้เร็วกว่า!",
                            opponentName + ": เงามืดกลืนกินแสงสว่างจนหมดสิ้น!"
                        };
                        break;
                    default:
                        lines = new String[]{
                            opponentName + ": ง่ำๆๆ! " + opponentName + " กลืนกินหมากของเจ้าแล้ว!",
                            opponentName + ": กรวบ! หมากของเจ้าถูกทำลายเรียบร้อย!",
                            opponentName + ": เจ้าไม่มีวันหนีพ้นความหิวของข้า!"
                        };
                        break;
                }
                setDialogue(CharacterType.EVILBOX, lines[random.nextInt(lines.length)], 3.5f);
            }
        } else {
            if (turn == Piece.Color.WHITE) {
                String[] lines = {
                    "Vampire: ขยับอย่างมีชั้นเชิง... ชัยชนะอยู่อีกไม่ไกล",
                    "Vampire: ท่าเดินอันสมบูรณ์แบบ... ตาเจ้าแล้ว " + opponentName + "!",
                    "Vampire: ก้าวต่อไปอย่างเยือกเย็น..."
                };
                setDialogue(CharacterType.VAMPIRE, lines[random.nextInt(lines.length)], 3.0f);
            } else {
                String[] lines;
                switch (archetype) {
                    case PIRATE_CAT:
                        lines = new String[]{
                            opponentName + ": ก้าวไปข้างหน้าอย่างว่องไว! เหมียว!",
                            opponentName + ": เล็งตาเดินต่อไปไว้แล้ว เจ้ามนุษย์!",
                            opponentName + ": ปืนใหญ่พร้อมยิง! ระวังตัวให้ดี!"
                        };
                        break;
                    case WITCH_KITTY:
                        lines = new String[]{
                            opponentName + ": เวทมนตร์ลอยละล่อง... เมี้ยว!",
                            opponentName + ": ลูกแก้วพยากรณ์บอกว่าตาเดินนี้ดีเลิศ!",
                            opponentName + ": ก้าวต่อไปในหมอกมนตรา..."
                        };
                        break;
                    case SHADOW_VAMPIRE:
                        lines = new String[]{
                            opponentName + ": ก้าวตามรอยเงาของเจ้า...",
                            opponentName + ": ข้ารู้จักทุกตาเดินของเจ้าดีกว่าตัวเจ้าเอง!",
                            opponentName + ": เงามืดยังคงคืบคลาน..."
                        };
                        break;
                    default:
                        lines = new String[]{
                            opponentName + ": คืบคลาน... คืบคลาน... ข้ากำลังจ้องเจ้าอยู่!",
                            opponentName + ": ขยับเข้าใกล้คิงของเจ้าเข้าไปอีกก้าว!",
                            opponentName + ": ลองรับมือตาเดินนี้ดูสิ!"
                        };
                        break;
                }
                setDialogue(CharacterType.EVILBOX, lines[random.nextInt(lines.length)], 3.0f);
            }
        }
    }

    public void triggerCheck(Piece.Color targetColor) {
        if (targetColor == Piece.Color.BLACK) {
            String line;
            switch (archetype) {
                case PIRATE_CAT:
                    line = "Vampire: รุก! ปกป้องคิงของเจ้าให้ดี " + opponentName + "!";
                    break;
                case WITCH_KITTY:
                    line = "Vampire: รุก! คาถาของเจ้าไม่อาจปกป้องคิงได้ " + opponentName + "!";
                    break;
                case SHADOW_VAMPIRE:
                    line = "Vampire: รุก! เงามายาไม่อาจหลบหนีความจริงพ้น!";
                    break;
                default:
                    line = "Vampire: รุก! ปกป้องคิงของเจ้าให้ดี " + opponentName + "!";
                    break;
            }
            setDialogue(CharacterType.VAMPIRE, line, 4.0f);
        } else {
            String line;
            switch (archetype) {
                case PIRATE_CAT:
                    line = opponentName + ": รุก! คิงของเจ้าโดนกรงเล็บโจรสลัดล้อมไว้แล้ว! เหมียว!";
                    break;
                case WITCH_KITTY:
                    line = opponentName + ": รุก! คิงของเจ้าโดนเวทมนตร์สะกดจนมุมแล้ว เมี้ยว!";
                    break;
                case SHADOW_VAMPIRE:
                    line = opponentName + ": รุก! เงาโลหิตกำลังบีบรัดคิงของเจ้า!";
                    break;
                default:
                    line = opponentName + ": รุก! คิงของเจ้าจนมุมแล้ว Vampire!";
                    break;
            }
            setDialogue(CharacterType.EVILBOX, line, 4.0f);
        }
    }

    public void triggerCheckmate(Piece.Color winnerColor) {
        if (winnerColor == Piece.Color.WHITE) {
            String line;
            switch (archetype) {
                case WITCH_KITTY:
                    line = "Vampire: รุกฆาต! มนตร์ดำถูกทำลาย! ชัยชนะเป็นของข้า!";
                    break;
                case PIRATE_CAT:
                    line = "Vampire: รุกฆาต! กัปตันแมวจนมุมแล้ว! ชัยชนะเป็นของข้า!";
                    break;
                case SHADOW_VAMPIRE:
                    line = "Vampire: รุกฆาต! เงามืดสลายตัว! ข้าก้าวข้ามขีดจำกัดแล้ว!";
                    break;
                default:
                    line = "Vampire: รุกฆาต! ความมืดมิดครอบครองกระดาน! ชัยชนะเป็นของข้า!";
                    break;
            }
            setDialogue(CharacterType.VAMPIRE, line, 8.0f);
        } else {
            String line;
            switch (archetype) {
                case PIRATE_CAT:
                    line = opponentName + ": รุกฆาต! ชัยชนะเป็นของกองทัพโจรสลัดแมวเหมียว! ฮ่าๆๆ!";
                    break;
                case WITCH_KITTY:
                    line = opponentName + ": รุกฆาต! คาถาแห่งความมืดมิดสมบูรณ์แบบ เจ้าพ่ายแพ้แล้ว!";
                    break;
                case SHADOW_VAMPIRE:
                    line = opponentName + ": รุกฆาต! เจ้าพ่ายแพ้ให้กับเงามืดในจิตใจของตนเอง!";
                    break;
                default:
                    line = opponentName + ": รุกฆาต! เจ้าถูกขังอยู่ในกล่องตลอดกาล! ฮ่าๆๆ!";
                    break;
            }
            setDialogue(CharacterType.EVILBOX, line, 8.0f);
        }
    }

    public void triggerStalemate() {
        setDialogue(CharacterType.VAMPIRE, "Vampire: เสมอกันรึ? ช่างน่าเสียดายยิ่งนัก...", 5.0f);
    }

    public void triggerIdleDialogue() {
        if (displayTimer > 0) return; // don't override active event dialogue
        if (random.nextBoolean()) {
            String[] vampireIdle = {
                "Vampire: กำลังคิดตาเดินพลาดครั้งต่อไปอยู่รึ?",
                "Vampire: เงามืดเริ่มยาวนานขึ้น... รีบเดินเสียสิ!",
                "Vampire: ความอดทนคือวิถีของแวมไพร์"
            };
            setDialogue(CharacterType.VAMPIRE, vampireIdle[random.nextInt(vampireIdle.length)], 3.5f);
        } else {
            String[] oppIdle;
            switch (archetype) {
                case PIRATE_CAT:
                    oppIdle = new String[]{
                        opponentName + ": เมี้ยว... หาว~ เดินช้าจัง ปลาทูจะเย็นหมดแล้วนะ!",
                        opponentName + ": *เลียอุ้งเท้า* กำลังคิดกลยุทธ์หนีข้าอยู่รึ?",
                        opponentName + ": ตาเจ้าเดินแล้วนะ อย่าให้กัปตันต้องรอนาน!"
                    };
                    break;
                case WITCH_KITTY:
                    oppIdle = new String[]{
                        opponentName + ": เมี้ยว~ *กวนหม้อปรุงยา* กำลังคิดกลยุทธ์อะไรอยู่นะ?",
                        opponentName + ": ไม้กายสิทธิ์ของข้ากำลังสั่น... รีบเดินสิ!",
                        opponentName + ": ลางร้ายกำลังคืบคลานเข้าหาคิงของเจ้านะ เมี้ยว!"
                    };
                    break;
                case SHADOW_VAMPIRE:
                    oppIdle = new String[]{
                        opponentName + ": แสงสว่างเริ่มริบหรี่... เจ้าจะต้านทานได้อีกนานแค่ไหน?",
                        opponentName + ": ร่างเงาของข้าไม่มีวันเหน็ดเหนื่อย...",
                        opponentName + ": ยิ่งเจ้าลังเล เงามืดยิ่งแข็งแกร่งขึ้น"
                    };
                    break;
                default:
                    oppIdle = new String[]{
                        opponentName + ": ติ๊กต่อก... เวลาเดินไปเรื่อยๆ นะ!",
                        opponentName + ": *เสียงกล่องสั่น* กล่องใบนี้รอเขมือบหมากอยู่นะ!",
                        opponentName + ": กลัวสิ่งที่อยู่ในตัวข้าแล้วรึยัง?"
                    };
                    break;
            }
            setDialogue(CharacterType.EVILBOX, oppIdle[random.nextInt(oppIdle.length)], 3.5f);
        }
    }

    public CharacterType getCurrentSpeaker() {
        return currentSpeaker;
    }

    public String getCurrentDialogue() {
        return currentDialogue;
    }

    public boolean isDialogueActive() {
        return displayTimer > 0;
    }
}

