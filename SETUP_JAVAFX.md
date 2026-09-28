# 🎮 CHESS GUI - Fast Setup (ตั้งค่าอย่างรวดเร็ว)

## ⚡ 3 ขั้นตอนง่ายๆ

### ✅ ขั้นตอนที่ 1: ดาวน์โหลด OpenJFX

**วิธี A: อัตโนมัติ (แนะนำ)**
```bash
cd C:\Users\agentJ\Desktop\chessegame
.\install-javafx.bat
```
(script จะดาวน์โหลดและตั้งค่าให้อัตโนมัติ)

**วิธี B: ดาวน์โหลดด้วยมือ**

1. ไปที่: https://gluonhq.com/products/javafx/
2. คลิก **Download**
3. เลือก **Windows**
4. ดาวน์โหลด **SDK** (ไม่ใช่ JMods!)
5. แตกไฟล์ไปที่: `C:\javafx-sdk-21.0.0`

---

### ✅ ขั้นตอนที่ 2: ตั้ง Environment Variable

หลังจากแตกไฟล์แล้ว ให้รันคำสั่งนี้:

```bash
setx JAVAFX_LIB "C:\javafx-sdk-21.0.0\lib"
```

**⚠️ สำคัญมาก:** ปิด PowerShell/Command Prompt ทั้งหมด แล้วเปิดใหม่!

---

### ✅ ขั้นตอนที่ 3: เล่นเกม!

```bash
cd C:\Users\agentJ\Desktop\chessegame
.\build-javafx.bat
```

✨ GUI จะเปิดขึ้นโดยอัตโนมัติ!

---

## 🐛 หากยังไม่ทำงาน

### ❌ ข้อความ: "JAVAFX_LIB environment variable not set"

**วิธีแก้:**
1. ตรวจสอบว่าแตกไฟล์ OpenJFX แล้ว:
   ```bash
   dir C:\javafx-sdk-21.0.0\lib
   ```
   ผลลัพธ์ควรแสดง: `javafx-base.jar`, `javafx-controls.jar` เป็นต้น

2. ตั้ง environment variable ใหม่:
   ```bash
   setx JAVAFX_LIB "C:\javafx-sdk-21.0.0\lib"
   ```

3. **ปิดทั้งหมด** แล้วเปิด PowerShell ใหม่

4. ตรวจสอบ:
   ```bash
   echo %JAVAFX_LIB%
   ```
   ควรแสดง: `C:\javafx-sdk-21.0.0\lib`

### ❌ ข้อความ: "javac: command not found"

**วิธีแก้:**
- ติดตั้ง Java JDK: https://www.oracle.com/java/technologies/javase-jdk21-downloads.html
- ตั้ง `JAVA_HOME` environment variable

### ❌ GUI ไม่เปิด

**วิธีแก้:**
- ลองใช้ PowerShell แทน Command Prompt
- ลองรันคำสั่งด้วยมือ:
  ```bash
  set JAVAFX_LIB=C:\javafx-sdk-21.0.0\lib
  javac --module-path %JAVAFX_LIB% --add-modules javafx.controls -d out src\com\chessegame\*.java src\com\chessegame\model\*.java src\com\chessegame\logic\*.java src\com\chessegame\window\*.java src\com\chessegame\window\fx\*.java src\com\chessegame\ui\fx\*.java
  java --module-path %JAVAFX_LIB% --add-modules javafx.controls -cp out com.chessegame.window.fx.JavaFXWindow
  ```

---

## 📋 Checklist

- [ ] ดาวน์โหลด OpenJFX SDK
- [ ] แตกไฟล์ไปที่ `C:\javafx-sdk-21.0.0`
- [ ] ตั้ง `JAVAFX_LIB` environment variable
- [ ] **ปิดและเปิด PowerShell ใหม่**
- [ ] รัน `.\build-javafx.bat`
- [ ] GUI Chess เปิดขึ้น! 🎮

---

## 🎮 เมื่อเปิด GUI แล้ว

```
Click piece → Click destination
```

- ✅ Border สีทอง = ชิ้นที่เลือก
- ⚠️ Alert = ข้อความสำคัญ
- 🏁 Checkmate = เกมจบ

---

## 💡 ทำไมต้อง OpenJFX?

JavaFX เป็น library สำหรับสร้าง GUI ใน Java
- ทำให้แสดงบอร์ด ชิ้นหมาก และปุ่มได้สวยงาม
- ใช้ Unicode symbols สำหรับชิ้นหมาก
- ทำให้ interact ได้ด้วยเมาส์

---

## 🚀 Quick Links

- OpenJFX Download: https://gluonhq.com/products/javafx/
- Java JDK: https://www.oracle.com/java/technologies/javase-jdk21-downloads.html
- Chess Rules: https://en.wikipedia.org/wiki/Rules_of_chess

---

ขอให้สำเร็จ! 🎮♟️✨
