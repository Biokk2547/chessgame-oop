# 🎮 CHESS GUI - เริ่มเล่นด่วน

## ⚡ วิธีเล่นเร็วที่สุด (3 ขั้นตอน)

### 1️⃣ ติดตั้ง OpenJFX (ครั้งแรกเท่านั้น)

```bash
# ดาวน์โหลด OpenJFX 21
# ที่ https://gluonhq.com/products/javafx/

# หรือใช้ PowerShell:
$url = "https://gluonhq.com/download/javafx-21.0.0-sdk-windows.zip"
$output = "javafx-21.zip"
Invoke-WebRequest -Uri $url -OutFile $output
Expand-Archive -Path $output -DestinationPath C:\

# ตั้ง Environment Variable:
[Environment]::SetEnvironmentVariable("JAVAFX_LIB", "C:\javafx-sdk-21.0.0\lib", "User")
```

⚠️ **หลังจากตั้ง Environment Variable ต้อง RESTART PowerShell/Command Prompt!**

---

### 2️⃣ คอมไพล์ (ครั้งแรกหรือหลังแก้โค้ด)

```bash
cd C:\Users\agentJ\Desktop\chessegame
build-javafx.bat
```

---

### 3️⃣ เล่น!

GUI Chess Window จะเปิดขึ้นอัตโนมัติ ✨

---

## 🕹️ วิธีเล่นใน GUI

| การกระทำ | วิธี |
|---------|-----|
| **เลือกชิ้นหมาก** | คลิกบนชิ้นที่ต้องการเดิน |
| **ย้ายไปยังตำแหน่ง** | คลิกบนช่องที่ต้องการ (จะมี border สีทอง) |
| **ยกเลิกเลือก** | คลิกบนชิ้นอื่น |
| **เล่นใหม่** | เมื่อจบเกม ให้คลิก "Restart" ในข้อความแจ้ง |

---

## 🎨 สิ่งที่เห็นบนจอ

```
┌─────────────────────────────────┐
│  ♟ Chess Game ♟                 │  ← หัวข้อ
│  White's turn                   │  ← แสดงผู้เล่นปัจจุบัน
├─────────────────────────────────┤
│                                 │
│   ♚ ♞ ♝ ♛ ♔ ♝ ♞ ♚   ← ชิ้นดำ  │
│   ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟   ← เบี้ยดำ  │
│   ░ ▒ ░ ▒ ░ ▒ ░ ▒                │  ← บอร์ด
│   ▒ ░ ▒ ░ ▒ ░ ▒ ░                │     (สีเขียวหญ้า)
│   ░ ▒ ░ ▒ ░ ▒ ░ ▒                │
│   ▒ ░ ▒ ░ ▒ ░ ▒ ░                │
│   ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙   ← เบี้ยขาว  │
│   ♖ ♗ ♘ ♕ ♔ ♘ ♗ ♖   ← ชิ้นขาว  │
│                                 │
├─────────────────────────────────┤
│ Click a piece, then click where │  ← คำแนะนำ
│ you want to move it             │
└─────────────────────────────────┘
```

---

## ⚠️ ข้อความแจ้งต่าง ๆ

| ข้อความ | ความหมาย | ทำไง |
|--------|---------|------|
| **"White's turn"** | ถึงเวลา White เดิน | เลือกชิ้น White แล้วเดิน |
| **"Black is in check!"** | ราชาดำถูกโจมตี | ต้องหลีกเลี่ยงในเทิร์นถัดไป |
| **"Illegal move"** | เดินนี้ทำให้ราชาของคุณถูกโจมตี | เลือกเดินอื่น |
| **"White wins! Checkmate!"** | White ชนะ | เกมจบ → จะรีเซ็ต |
| **"Stalemate! Draw"** | ไม่มีเดินที่ถูกต้อง | เสมอ → จะรีเซ็ต |

---

## 📋 สัญลักษณ์ชิ้น

### ขาว (White)
| ♔ | ♕ | ♖ | ♗ | ♘ | ♙ |
|---|---|---|---|---|---|
| King | Queen | Rook | Bishop | Knight | Pawn |
| ราชา | ราชินี | เรือ | บิชอป | ม้า | เบี้ย |

### ดำ (Black)
| ♚ | ♛ | ♜ | ♝ | ♞ | ♟ |
|---|---|---|---|---|---|
| King | Queen | Rook | Bishop | Knight | Pawn |

---

## 💡 เคล็ดลับการเล่น

### Opening ที่ดี
- **e2-e4** (White) → **e7-e5** (Black) - King's Pawn Opening
- **d2-d4** (White) → **d7-d5** (Black) - Queen's Pawn Opening

### รูปแบบการเดิน
- **King (K/k)** = เดิน 1 ช่องทุกทิศ
- **Queen (Q/q)** = เดินหลายช่องทุกทิศ (อันตรายที่สุด!)
- **Rook (R/r)** = เดินตรง (แนวนอน/ตั้ง)
- **Bishop (B/b)** = เดินทแยง
- **Knight (N/n)** = เดินรูป L (ข้าม 2 ช่อง + ข้าง 1 ช่อง)
- **Pawn (P/p)** = เดินไป 1 ช่อง (ครั้งแรก 2 ช่อง) / กิน ทแยง

### ป้องกัน Check
1. **เดินหนีราชา** ไปตำแหน่งที่ปลอดภัย
2. **กีดขวาง** ด้วยชิ้นอื่น
3. **จับชิ้นที่โจมตี**

---

## 🐛 แก้ไขปัญหา

### ❌ "JAVAFX_LIB ไม่พบ"
```bash
# ตรวจสอบ
echo %JAVAFX_LIB%

# หากว่างเปล่า ให้ตั้งใหม่:
setx JAVAFX_LIB "C:\javafx-sdk-21.0.0\lib"

# แล้ว RESTART PowerShell!
```

### ❌ "javac: command not found"
- ตรวจสอบ Java JDK ติดตั้งหรือไม่
- ```bash
  javac -version
  ```

### ❌ GUI ไม่เปิด
- ลองใช้ PowerShell แทน Command Prompt
- ตรวจสอบ OpenJFX ติดตั้งถูกต้องไหม

---

## 📁 โครงสร้างไฟล์

```
chessegame/
├── src/
│   └── com/chessegame/
│       ├── Game.java (Console version)
│       ├── ChessUtils.java (Checkmate detection)
│       ├── Board.java
│       ├── Piece.java
│       ├── King.java, Queen.java, ...
│       └── ui/
│           └── ChessApp.java (GUI version) ← เราใช้อันนี้
├── build.bat (Console)
├── build-javafx.bat (GUI) ← เราใช้อันนี้
└── out/ (Compiled files)
```

---

## 🎯 เริ่มต้น

```bash
# ขั้นตอน 1: ไปโฟลเดอร์เกม
cd C:\Users\agentJ\Desktop\chessegame

# ขั้นตอน 2: คอมไพล์
build-javafx.bat

# ขั้นตอน 3: เล่น!
# (GUI จะเปิดอัตโนมัติ)
```

---

ขอให้สนุก! 🎮♟️✨
