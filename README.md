# ♟️ ศึกหมากรุกมหาประลัย (Chess Battle of Legends)

เกมหมากรุกสากล (Chess) ฉบับสมบูรณ์ที่พัฒนาด้วยภาษา **Pure Java** และขับเคลื่อนกราฟิกประสิทธิภาพสูงด้วย **LibGDX** พร้อมระบบการวิเคราะห์เกมระดับ Grandmaster, โหมดฝึกซ้อมเปิดเกม, โหมดแก้ปริศนา, และโหมดประลองบอสพร้อมสกิลพิเศษ Dowload = https://drive.google.com/drive/folders/1TJ8-dJgd3Td0usJgOssOtohcTjfqUICc?usp=drive_link

---

## 🌟 จุดเด่นและฟีเจอร์หลัก (Key Features)

### 1. โหมดการเล่นหลากหลาย (Game Modes)
* ⚔️ **เล่นเกมอิสระ (Quick Play)**:
  * เล่นกับ **Chess AI Engine** หรือเล่นแข่งกันเอง 2 คนบนเครื่องเดียวกัน (2 Players Local)
  * ปรับระดับความยากของ AI ได้ตั้งแต่ **Depth 1 - 4**
  * เลือกลักษณะนิสัยของ AI ได้ 3 สไตล์:
    * **เซียน (Master)**: คำนวณสมดุล รอบคอบ และหาตาเดินที่ดีที่สุด
    * **สายบุก (Aggressive)**: เน้นเปิดเกมรุก กินหมาก และแลกตัวเพื่อกดดันกษัตริย์
    * **สายรับ (Defensive)**: เน้นโครงสร้างเบี้ยที่เหนียวแน่น คุ้มกันทุกตัวหมาก
* 👑 **โหมดประลองบอส (Boss Rush)**:
  * เผชิญหน้ากับบอสตัวละครที่มีภาพแอนิเมชันและบทสนทนาเฉพาะตัว:
    * **Pirate Cat**: บอสแมวโจรสลัดจอมซน
    * **Vampire Lord**: บอสแวมไพร์สายบุก พร้อมเอฟเฟกต์กรงเล็บ
    * **Evil Box**: บอสกล่องปีศาจลึกลับ
    * **Witch Kitty**: บอสแม่มดแมวน้อยผู้ร่ายเวทมนตร์คุ้มกัน
* 📖 **โหมดฝึกซ้อมเปิดเกม (Opening Practice Repertoire)**:
  * ฝึกซ้อมสายเปิดเกมระดับมาสเตอร์ 10 สาย (Ruy Lopez, Italian Game, King's Gambit, Queen's Gambit, Sicilian Defense, French Defense, Caro-Kann, London System, English Opening, King's Indian)
  * หน้าจอ **2-Column Dashboard** แสดงเป้าหมายยุทธวิธี, แถบความคืบหน้า (Progress Bar) และคำอธิบายตาเดินแบบเรียลไทม์
  * **ระบบลูกศรคำใบ้นีออน (Neon Hint Arrow)**: แสดงทิศทางการเดินที่ถูกต้องบนกระดานทันที
  * สามารถสลับไปเล่นต่อกับ AI จากตำแหน่งกระดานของสายเปิดเกมได้ทันที
* 🧩 **โหมดแก้ปริศนา (Puzzle Mode)**:
  * รวม 10 ปริศนาหมากรุกคลาสสิก (Mate in 1, Mate in 2, Fork, Pin, Skewer)
  * มีระบบตรวจสอบตาเดินอัตโนมัติ, ระบบคำใบ้, และเฉลยกลยุทธ์
* 📊 **ระบบวิเคราะห์เกมหลังจบแมตช์ (Game Review & Analysis)**:
  * ระบบรีวิวเกมสไตล์ Chess.com / Lichess คำนวณค่า **Accuracy %** ทั้งฝ่ายขาวและดำ
  * จำแนกคุณภาพตาเดิน 9 ระดับ: **Brilliant, Great, Best, Excellent, Good, Inaccuracy, Mistake, Miss, Blunder**
  * กราฟและหลอดประเมินแต้มได้เปรียบ (Evaluation Bar) แบบเรียลไทม์
  * แนะนำตาเดินที่ดีที่สุดด้วยลูกศรนีออนเรืองแสงสีเขียวบนกระดาน

---

### 2. กฎกติกาหมากรุกสากลครบถ้วน (Full FIDE Rules)
* ✅ การเดินและการกินของตัวหมากทุกตัว (King, Queen, Rook, Bishop, Knight, Pawn)
* ✅ การเข้าป้อม (Castling ทั้งฝั่ง Kingside และ Queenside)
* ✅ การกินผ่าน (En Passant)
* ✅ การเลื่อนขั้นเบี้ย (Pawn Promotion)
* ✅ การตรวจจับสถานะ **Check (รุก)**, **Checkmate (รุกฆาต)** และ **Stalemate (อับ/เสมอ)**
* ✅ ระบบ Undo / Redo พร้อมประวัติการเดินแบบบันทึก FEN Notation

---

### 3. ระบบกราฟิกและการควบคุม (Graphics & Controls)
* 🎨 **การควบคุม 2 รูปแบบ**:
  * **Click-to-Move**: คลิกเลือกตัวหมากเพื่อดูตำแหน่งที่เดินได้ แล้วคลิกช่องเป้าหมาย
  * **Drag & Drop**: ลากตัวหมากไปวางยังช่องเป้าหมายอย่างเป็นธรรมชาติ
* 🏹 **Last Move Arrow**: แสดงลูกศรทิศทางและไฮไลท์สีทองบอกตาเดินล่าสุด
* 💥 **Particle Effects**: ระบบละอองเกสรบรรยากาศ และพลุกระดาษเฉลิมฉลอง (Confetti) เมื่อชนะเกม
* 🎵 **ระบบเสียง**: เสียงเดินหมาก, เสียงกินหมาก, เสียงรุก, เสียงชัยชนะ และเพลงประกอบพื้นหลัง (BGM) พร้อมปุ่มเปิด/ปิดเสียงดนตรี `[M]`

---

### 4. สถาปัตยกรรม Chess AI Engine
* **Opening Book (0ms)**: ตอบสนองสายเปิดเกม 5 ตาแรกทันทีโดยไม่ต้องคำนวณ
* **Minimax with Alpha-Beta Pruning**: ตัดกิ่งไม้ค้นหาที่ไม่จำเป็น
* **MVV-LVA (Most Valuable Victim - Least Valuable Attacker)**: จัดลำดับการกินหมากที่มีมูลค่าสูงก่อน ช่วยเพิ่มความเร็วการตัดกิ่งถึง 2 เท่า
* **Quiescence Search**: ค้นหาการกินหมากต่อเนื่องในชั้นลึกสุด ป้องกันปัญหาจุดบอดสายตา (Horizon Effect)
* **Iterative Deepening Search (IDS)**: ควบคุมเวลาค้นหาไม่ให้กระทบต่อเฟรมเรต UI (ตอบสนองไวภายใน 200ms - 350ms)
* **Stockfish UCI Engine Integration**: รองรับการเชื่อมต่อกับ Stockfish Engine ภายนอกเพื่อการวิเคราะห์ระดับ Super Grandmaster

---

## 🚀 วิธีการติดตั้งและการรันเกม (Getting Started)

### ความต้องการของระบบ (Requirements)
* **Java Development Kit (JDK)**: เวอร์ชัน 17 ขึ้นไป (แนะนำ JDK 21)
* **ระบบปฏิบัติการ**: Windows, macOS หรือ Linux

---

### 1. วิธีรันเกมอย่างง่ายบน Windows (แนะนำ)
ดับเบิลคลิกไฟล์ใดไฟล์หนึ่งต่อไปนี้ในโฟลเดอร์โปรเจกต์:
* **`Play.bat`**: คอมไพล์และเปิดเกมโหมดกราฟิก LibGDX ทันที
* **`Play_Silent.vbs`**: เปิดเกมโดยไม่แสดงหน้าต่างคอนโซลสีดำ
* **`Play_Console_Mode.bat`**: เปิดเกมในโหมด Text / Terminal Console (เล่นผ่าน Command Line)

---

### 2. วิธีรันด้วย Gradle Wrapper
เปิด Terminal หรือ PowerShell ที่โฟลเดอร์โปรเจกต์ แล้วใช้คำสั่ง:

```bash
# บน Windows
.\gradlew.bat run

# บน macOS / Linux
./gradlew run
```

---

### 3. วิธีการคอมไพล์และทดสอบ (Build & Test)
```bash
# ตรวจสอบการคอมไพล์
.\gradlew.bat compileJava

# รันชุดทดสอบความถูกต้องของตรรกะเกม (Logic Tests)
.\gradlew.bat test
```

---

## 📂 โครงสร้างโปรเจกต์ (Project Architecture)

```
chessegame/
│
├── assets/                     # ไฟล์ Asset ภาพหมากรุก, กระดาน, ฟอนต์, เสียง และเอฟเฟกต์
│   ├── characters/             # สไปรต์แอนิเมชันของบอส
│   ├── fonts/thai.ttf          # ฟอนต์รองรับภาษาไทย
│   ├── music/                  # ไฟล์เพลงประกอบ BGM
│   └── board.png               # พื้นผิวกระดานหมากรุก
│
├── src/com/chessegame/
│   ├── Main.java               # จุดเริ่มต้นโปรแกรม (Launcher & Window Selector)
│   │
│   ├── model/                  # โครงสร้างตัวหมากและกระดาน (Data Models)
│   │   ├── Board.java          # กระดาน 8x8 และตรรกะวางหมาก
│   │   ├── Piece.java          # Abstract Class ตัวหมากพื้นฐาน
│   │   ├── King.java, Queen.java, Rook.java, Bishop.java, Knight.java, Pawn.java
│   │   ├── Position.java       # พิกัดแถวและคอลัมน์ (Row/Col)
│   │   └── MoveRecord.java     # บันทึกประวัติการเดินและค่า FEN
│   │
│   ├── logic/                  # ตรรกะการตรวจสอบกฎกติกา (Business Logic)
│   │   ├── ChessUtils.java     # กฎการเดิน, ตรวจจับ Check, Checkmate, Stalemate
│   │   ├── GameHistoryManager.java # ระบบ Undo / Redo
│   │   └── Clock.java          # นาฬิกาจับเวลาหมากรุก
│   │
│   ├── ai/                     # ระบบสมองกล AI & การวิเคราะห์เกม
│   │   ├── ChessAI.java        # อัลกอริทึม Minimax + Alpha-Beta + IDS + Quiescence Search
│   │   ├── Evaluation.java     # ฟังก์ชันประเมินคะแนนกระดานและ Piece-Square Tables (PST)
│   │   ├── OpeningBook.java    # คลังเปิดเกมสากล (0ms response)
│   │   ├── GameReviewer.java   # ระบบจำแนกความแม่นยำและคุณภาพตาเดิน
│   │   └── StockfishEngine.java# ตัวเชื่อมต่อ UCI Engine
│   │
│   ├── opening/                # ระบบฝึกซ้อมเปิดเกม (Repertoire)
│   │   ├── Opening.java        # Data Model สายเปิดเกม
│   │   └── OpeningManager.java # ข้อมูลตำรา 10 สาย และระบบเซฟความคืบหน้า
│   │
│   ├── level/                  # โหมดแก้ปริศนาและโหมดบอส
│   │   ├── BossLevel.java      # ข้อมูลและสกิลของบอสแต่ละด่าน
│   │   └── PuzzleManager.java  # คลังโจทย์ปริศนาหมากรุก
│   │
│   ├── audio/                  # ระบบจัดการเสียง
│   │   ├── SoundManager.java   # Sound Effects (Move, Capture, Check, Victory)
│   │   └── MusicManager.java   # Background Music (BGM)
│   │
│   └── ui/gdx/                 # หน้าจอแสดงผลด้วย LibGDX
│       ├── LibGdxChessApp.java # Game Controller หลักของ LibGDX
│       ├── MainMenuScreen.java # หน้าเมนูหลัก
│       ├── GameScreen.java     # หน้าจอกระดานแข่งขันจริง
│       ├── GameReviewScreen.java# หน้าจอรีวิวเกมและบทวิเคราะห์
│       ├── OpeningSelectScreen.java # หน้าจอเลือกสายเปิดเกม
│       ├── OpeningPracticeScreen.java # หน้าจอฝึกซ้อมเปิดเกม (2-Column Dashboard)
│       ├── BossLevelSelectScreen.java # หน้าจอเลือกบอส
│       └── PuzzleScreen.java   # หน้าจอฝึกแก้ปริศนา
│
├── build.gradle                # สคริปต์กำหนดการพึ่งพาและการสร้างโปรเจกต์ (Gradle)
└── README.md                   # เอกสารประกอบโปรเจกต์
```

---

## ⌨️ ปุ่มลัดบนคีย์บอร์ด (Keyboard Shortcuts)
* **`M`**: เปิด / ปิด เสียงเพลงประกอบ (Mute/Unmute BGM)
* **`ESC`**: ย้อนกลับสู่หน้าก่อนหน้า หรือกลับสู่หน้าเมนูหลัก
* **`คลิกขวา (Right Click)`**: ยกเลิกการเลือกหมาก หรือยกเลิกการลากหมาก
* **`Tab`** *(ในหน้า Game Review)*: สลับมุมมองระหว่างบทวิเคราะห์รายตาเดิน กับหน้าสรุปสถิติภาพรวม

---

## 👥 ผู้พัฒนาและลิขสิทธิ์ (Author & License)
* **ผู้พัฒนา**: Biokk2547
* **Repository**: [https://github.com/Biokk2547/chessgame-oop](https://github.com/Biokk2547/chessgame-oop)
* **License**: MIT License
