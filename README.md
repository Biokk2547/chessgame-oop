โครงโปรเจกต์เกมด้วย Pure Java

สิ่งที่สร้าง:
- src/com/chessegame/Main.java : จุดเริ่มต้นโปรแกรม
- src/com/chessegame/Game.java : โครงคลาสเกม (โฟลว์เริ่มต้น)
- src/com/chessegame/Player.java : คลาสผู้เล่นตัวอย่าง
- build.bat : คำสั่งคอมไพล์และรันบน Windows (คอนโซล)
- build-javafx.bat : คำสั่งคอมไพล์/รัน JavaFX UI (ต้องติดตั้ง OpenJFX SDK)
- .gitignore : ไฟล์ที่ไม่ต้องการเก็บใน VCS

คำสั่งใช้งาน (Windows):
1) คอมไพล์ (คอนโซล): build.bat
2) คอมไพล์/รัน JavaFX UI (ต้องติดตั้ง OpenJFX SDK):
   - ดาวน์โหลด OpenJFX SDK จาก https://openjfx.io/ แล้วตั้งตัวแปรสภาพแวดล้อม JAVAFX_LIB เป็นโฟลเดอร์ lib ของ SDK
   - รัน: build-javafx.bat

ตัวอย่างคอมไพล์ด้วยมือ (JavaFX):
   set JAVAFX_LIB=C:\path\to\javafx-sdk-20\lib
   javac --module-path %JAVAFX_LIB% --add-modules javafx.controls -d out src\com\chessegame\*.java src\com\chessegame\ui\*.java
   java --module-path %JAVAFX_LIB% --add-modules javafx.controls -cp out com.chessegame.ui.ChessApp

สิ่งที่ทำแล้ว:
- เพิ่มบอร์ด, ชิ้นหมาก และกฎการเดินพื้นฐาน
- เพิ่ม JavaFX UI ตัวอย่าง (click-to-move, Unicode chess symbols)

ข้อจำกัดปัจจุบัน:
- ยังไม่มีตรวจ check/checkmate, castling, en-passant, หรือการเลือก promotion (โปรโมตเป็น Queen อัตโนมัติ)
- UI ใช้สัญลักษณ์ Unicode ไม่ได้ใช้ภาพ

ถัดไปที่แนะนำ:
- เพิ่มการตรวจ check/checkmate
- รองรับ castling, en-passant, และเลือกการโปรโมต
- ใช้ภาพชิ้นหมากแทน Unicode หากต้องการกราฟิกที่สวยขึ้น
# chessgame-oop
# chessgame-oop
