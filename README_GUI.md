# 🎮 Chess Game - GUI Version (JavaFX)

## ✅ สิ่งที่ทำเสร็จแล้ว

### 1. ✨ GUI ที่ปรับปรุง (ChessApp.java)
- ✔️ บอร์ด 8x8 พร้อม Unicode symbols
- ✔️ Click-to-move interface
- ✔️ ไฮไลท์ชิ้นที่เลือก (border สีทอง)
- ✔️ อัปเดตสถานะผู้เล่นในเรียลไทม์
- ✔️ ข้อความแจ้งเตือน Alert
- ✔️ Checkmate/Stalemate detection
- ✔️ เกมรีเซ็ตอัตโนมัติหลังเกมจบ

### 2. 🛡️ Checkmate System (ChessUtils.java)
- ✔️ Check detection
- ✔️ Checkmate detection
- ✔️ Stalemate detection
- ✔️ Legal move validation (ไม่ให้เดินที่ทำให้ราชาถูกโจมตี)

### 3. 📚 Documentation
- ✔️ `QUICK_START.md` - วิธีเล่นด่วน
- ✔️ `GUI_INSTALLATION_GUIDE.md` - ติดตั้ง OpenJFX เต็มรูป
- ✔️ `CHECKMATE_SYSTEM.md` - เอกสารระบบ
- ✔️ `วิธีเล่น.md` - คู่มือผู้เล่น

---

## 🚀 วิธีเล่นทันที

### 1️⃣ ติดตั้ง OpenJFX (ครั้งแรก)
ดาวน์โหลดจาก https://gluonhq.com/products/javafx/ แล้วตั้ง JAVAFX_LIB environment variable

### 2️⃣ คอมไพล์และเล่น
```bash
cd C:\Users\agentJ\Desktop\chessegame
build-javafx.bat
```

---

## 🎨 ส่วนประกอบ GUI

### Header (สีเข้ม #333333)
```
♟ Chess Game ♟
White's turn
```

### Board (สีเขียวหญ้า)
- 8x8 grid พร้อม alternating colors
- ขนาดช่อง: 80x80 pixels
- Unicode symbols สำหรับทุกชิ้น

### Footer (สีอ่อน #f0f0f0)
```
Click a piece, then click where you want to move it
```

### Alerts
- Error: "Illegal move" (เดินผิด)
- Warning: "[Color] is in check!" (เรือก)
- Information: "[Color] wins! Checkmate!" (เรือกแม่ท)
- Information: "Stalemate! Draw" (เสมอ)

---

## 🎮 Game Flow

```
1. ผู้เล่นคลิกชิ้น (selected = true)
   ↓
2. ชิ้นมี border สีทอง
   ↓
3. ผู้เล่นคลิกตำแหน่ง
   ↓
4. ระบบตรวจสอบ:
   ├─ ชิ้นเดินได้ไหม?
   ├─ ไม่ทำให้ราชาถูกโจมตีไหม?
   ├─ Move ถูกต้องหรือไม่?
   └─ Check/Checkmate/Stalemate หรือไม่?
   ↓
5. หากทั้งหมด OK → เดิน → เปลี่ยนผู้เล่น
   หากผิด → Show Alert → ลองอีกครั้ง
   ↓
6. หากเกมจบ → Show Alert → Reset board
```

---

## 📝 ไฟล์ที่เกี่ยวข้อง

| ไฟล์ | ความเป็นมา | ความสำคัญ |
|-----|----------|---------|
| `src/com/chessegame/ui/ChessApp.java` | **อัพเดต** | Main GUI |
| `src/com/chessegame/ChessUtils.java` | **เพิ่มเติม** | Checkmate logic |
| `src/com/chessegame/Game.java` | อัพเดต | Console version |
| `build-javafx.bat` | ไม่เปลี่ยน | Compile for GUI |
| `build.bat` | ไม่เปลี่ยน | Compile for Console |

---

## 🔧 ฟีเจอร์

### ✅ ทำเสร็จแล้ว
- ✔️ Checkmate detection
- ✔️ Stalemate detection
- ✔️ Check detection
- ✔️ Legal move validation
- ✔️ GUI interface
- ✔️ Unicode chess symbols
- ✔️ Click-to-move
- ✔️ Game end detection

### 🔄 ยังไม่ทำ (Future)
- ⬜ Castling (โยกเรือ)
- ⬜ En-passant (เบี้ยพิเศษ)
- ⬜ Undo/Redo moves
- ⬜ Move history
- ⬜ AI opponent
- ⬜ PGN notation
- ⬜ Time controls
- ⬜ Custom piece images

---

## 🎯 การอ้างอิงการเล่น

### Opening ที่นิยม
```
Italian Game:
1. e2-e4 e7-e5
2. g1-f3 b8-c6
3. f1-c4 ...

Ruy Lopez:
1. e2-e4 e7-e5
2. g1-f3 b8-c6
3. f1-b5 ...
```

### Fool's Mate (พ่ายแพ้เร็ว!)
```
1. f2-f3 e7-e6
2. g2-g4 d8-h4
# Checkmate!
```

---

## 💬 Support

หากมีปัญหา:
1. ตรวจสอบ OpenJFX ติดตั้งถูกต้อง
2. ตรวจสอบ JAVAFX_LIB environment variable
3. Restart PowerShell/Command Prompt
4. ลองใช้ Console version (build.bat) เพื่อทดสอบ core game

---

## 📊 Statistics

- **Lines of Code:** ~600 (ChessApp + ChessUtils)
- **Board Size:** 8x8
- **Total Pieces:** 32
- **Move Validation:** Full (including check)
- **Game States:** 3+ (Normal, Check, Checkmate, Stalemate)
- **GUI Components:** GridPane, Buttons, Alerts, Labels

---

ขอให้สนุกกับการเล่น! 🎮♟️✨
