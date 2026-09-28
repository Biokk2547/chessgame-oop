# วิธีติดตั้ง และเล่นเกม Chess GUI (JavaFX)

## 📥 ขั้นตอนที่ 1: ติดตั้ง OpenJFX SDK

### วิธีที่ 1: ดาวน์โหลดจากเว็บ (แนะนำ)

1. ไปที่ https://gluonhq.com/products/javafx/
2. คลิก **Download** และเลือก **Windows**
3. ดาวน์โหลด **OpenJFX SDK** (ไม่ใช่ JMods)
4. แตกไฟล์ไปที่โฟลเดอร์เช่น: `C:\javafx-sdk-21`

### วิธีที่ 2: ใช้ Command Line

ถ้าคุณมี `wget` หรือ `curl`:

```bash
# ดาวน์โหลด OpenJFX 21 (เปลี่ยนเวอร์ชันตามต้องการ)
curl -o javafx-sdk-21.0.0-windows.zip https://gluonhq.com/download/javafx-21.0.0-sdk-windows.zip

# แตกไฟล์
# (ใช้ File Explorer หรือ PowerShell)
Expand-Archive -Path javafx-sdk-21.0.0-windows.zip -DestinationPath C:\
```

---

## 🔧 ขั้นตอนที่ 2: ตั้งค่า Environment Variable

### บน Windows:

1. **คลิกปุ่ม Windows** → ค้นหา **"Environment Variables"** → **Edit the system environment variables**

2. คลิก **Environment Variables** button

3. ภายใต้ **User variables** หรือ **System variables** → คลิก **New**

4. ตั้งค่า:
   - **Variable name:** `JAVAFX_LIB`
   - **Variable value:** `C:\javafx-sdk-21\lib` (เปลี่ยน path ให้ตรงกับที่คุณแตกไฟล์)

5. คลิก **OK** และ **Apply**

6. **Restart Command Prompt หรือ PowerShell** เพื่อให้ใช้ค่าใหม่

### ตรวจสอบว่าติดตั้งสำเร็จ:

```bash
echo %JAVAFX_LIB%
# ผลลัพธ์ควรแสดง path ของ OpenJFX lib
```

---

## 🎮 ขั้นตอนที่ 3: รัน Chess GUI

```bash
cd C:\Users\agentJ\Desktop\chessegame
build-javafx.bat
```

---

## 🕹️ วิธีเล่น GUI Version

### 1. เลือกชิ้นหมาก
- **คลิก** บนชิ้นที่คุณต้องการเดิน

### 2. เลือกตำแหน่ง
- **คลิก** บนช่องที่ต้องการให้ชิ้นไปถึง
- ชิ้นที่เลือกจะมี **border สีเหลือง** เพื่อให้เห็นชัดเจน

### 3. สัญลักษณ์ชิ้นหมาก

| ชิ้น | สีขาว | สีดำ |
|------|-------|------|
| King (ราชา) | ♔ | ♚ |
| Queen (ราชินี) | ♕ | ♛ |
| Rook (เรือ) | ♖ | ♜ |
| Bishop (บิชอป) | ♗ | ♝ |
| Knight (ม้า) | ♘ | ♞ |
| Pawn (เบี้ย) | ♙ | ♟ |

---

## ⚠️ สถานะเกม

### ✅ การเดินปกติ
- ชิ้นเลื่อนไปยังตำแหน่งใหม่

### 🟨 ด้านจะเลือกชิ้น
- border สีเหลืองจะแสดงรอบชิ้นที่เลือก

### 🚫 เดินไม่ได้
- **ข้อความแจ้ง:** "Illegal move: this move leaves your king in check!"
- ต้องเลือกเดินอื่น

### ⚠️ Check (เรือก)
- **ข้อความแจ้ง:** "[White/Black] is in check!"
- ราชาของคุณถูกโจมตี - ต้องหลีกเลี่ยง!

### 🏁 Checkmate (เรือกแม่ท)
- **ข้อความแจ้ง:** "[White/Black] wins! Checkmate!"
- **เกมจบ** → บอร์ดรีเซ็ตสำหรับเกมใหม่

### 🤝 Stalemate (เสมอ)
- **ข้อความแจ้ง:** "Stalemate! Game is a draw."
- **เกมจบ** → บอร์ดรีเซ็ตสำหรับเกมใหม่

---

## 🎨 ธีมของบอร์ด

- **ช่องสีอ่อน:** #EEEED2 (สีครีม)
- **ช่องสีเข้ม:** #769656 (สีเขียวหญ้า)
- **ขนาดปุ่ม:** 80x80 พิกเซล

---

## 🐛 แก้ไขปัญหา

### ปัญหา: "Please set the JAVAFX_LIB environment variable..."

**วิธีแก้:**
1. ตรวจสอบว่าคุณตั้ง environment variable `JAVAFX_LIB` ถูกต้อง
2. **Restart Command Prompt/PowerShell** หลังจากตั้ง variable
3. ยืนยันว่าไฟล์ `lib` มีอยู่:
   ```bash
   dir %JAVAFX_LIB%
   ```

### ปัญหา: "javac: command not found"

**วิธีแก้:**
- ตรวจสอบว่าติดตั้ง Java JDK แล้ว
- ตั้ง `JAVA_HOME` environment variable
- ```bash
  java -version
  javac -version
  ```

### ปัญหา: GUI ไม่เปิด

**วิธีแก้:**
- ลองรันจาก PowerShell แทน Command Prompt
- ลองเพิ่มเติม arguments:
  ```bash
  java --module-path %JAVAFX_LIB% --add-modules javafx.controls -cp out com.chessegame.ui.ChessApp
  ```

---

## 💡 เคล็ดลับ

✅ **ใช้ Mouse** เพื่อเลือก → เลือกสถานที่  
✅ **ดูข้อความแจ้ง** เพื่อทราบสถานะเกม  
✅ **Border เหลือง** บ่งบอกชิ้นที่เลือก  
✅ **เล่นใหม่หลังจากจบ** ด้วยการเล่นใหม่ (เกมจะรีเซ็ตโดยอัตโนมัติ)

---

## 📝 ตัวอย่างการเล่น

1. **White's turn:**
   - คลิกบนเบี้ยที่ e2
   - คลิกบน e4 (border เหลือง)
   - เบี้ยเลื่อนไป e4 ✓

2. **Black's turn:**
   - คลิกบนเบี้ยที่ e7
   - คลิกบน e5
   - เบี้ยเลื่อนไป e5 ✓

3. **ต่อไป...**
   - ทำจนกว่าเกมจบด้วย Checkmate หรือ Stalemate

---

ขอให้สนุกกับการเล่น! 🎮♟️
