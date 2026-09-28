ต้องรู้ (Must know) — พัฒนาเกมหมากรุกด้วย Pure Java + JavaFX

ภาพรวมสั้นๆ
- ไฟล์นี้รวบรวมความรู้และขั้นตอนที่ควรรู้เพื่อพัฒนาต่อจากโครงโปรเจกต์ปัจจุบัน

พื้นฐานภาษาและเครื่องมือ
- Java 11+ (แนะนำ Java 17/21): syntax, OOP, exceptions, generics
- Build tools (javac, หรือ Maven/Gradle ถ้าต้องการจัดการ dependency)
- Git: ควบคุมเวอร์ชัน
- OpenJFX (JavaFX) สำหรับ UI เดสก์ท็อป

สถาปัตยกรรมโปรเจกต์ที่ควรเข้าใจ
- แยก Model / View / Controller (M-V-C):
  - Model: Board, Piece, GameState, Move
  - View: JavaFX UI (ChessApp)
  - Controller: เชื่อม UI กับ Model (รับ input, เรียก validate, update view)
- Immutable vs mutable state: เก็บ history เพื่อ undo/redo

คำจำเป็นด้านหมากรุก (game rules)
- พื้นฐาน: การเดินของแต่ละชิ้น (pawn, knight, bishop, rook, queen, king)
- ข้อพิเศษ: castling, en passant, promotion
- สถานะเกม: check, checkmate, stalemate, draw by repetition/50-move
- วิธีตรวจ check/checkmate: หาตำแหน่งคิง แล้วดูว่ามีการตอบโต้/บล็อก/จับได้หรือไม่

ตัวแทนข้อมูล (board representation)
- 8x8 array (Piece[][]) หรือ bitboards (ถ้าต้องการประสิทธิภาพสูง)
- ตำแหน่งใช้ algebraic (e2) แปลงเป็น (row,col)
- เก็บ move history (list of Move objects) สำหรับ undo และ ตรวจซ้ำ

การตรวจสอบการเดิน (move validation)
- แยก rule-level validation (isValidMove ของแต่ละ Piece)
- ตรวจ game-level validation: ห้ามเดินแล้วคิงอยู่ในเช็ค
- ตรวจเส้นทาง (path clear) สำหรับ rook/bishop/queen
- ทำ unit test ครอบคลุมกรณีพิเศษ

UI (JavaFX) ที่ควรรู้
- Scene, Stage, Nodes, GridPane, Button, ImageView
- Event handling: setOnAction, mouse events, drag-and-drop
- CSS styling และ font/Unicode (หรือใช้ภาพ PNG/SVG ของชิ้นหมาก)
- JavaFX Application Thread: อัปเดต UI ต้องอยู่บน thread นี้; ใช้ Platform.runLater สำหรับ background tasks
- Packaging: รันด้วย --module-path และ --add-modules ถ้าใช้ OpenJFX

คำสั่งรัน/คอมไพล์ (Windows)
- คอนโซล (ปัจจุบัน): build.bat
- JavaFX UI:
  1) ดาวน์โหลด OpenJFX SDK -> ตั้ง JAVAFX_LIB เป็น C:\path\to\javafx-sdk\lib
  2) รัน: build-javafx.bat (สคริปต์ใช้ --module-path %JAVAFX_LIB% --add-modules javafx.controls)
- ถา้ใช้ Maven/Gradle ให้เพิ่ม plugin/dep ของ OpenJFX (แนะนำเมื่อต้อง deploy/packaging)

ทดสอบและคุณภาพโค้ด
- ใช้ JUnit สำหรับ unit tests ของ move validation, check detection, promotion, castling
- เขียน integration test สำหรับ sequences ของเกม

ฟีเจอร์เพิ่มเติมที่มักต้องการ (ลำดับแนะนำ)
1) Check / Checkmate detection และห้ามเดินที่เปิดคิงเช็ค
2) Castling, En-passant, Promotion choice (UI dialog)
3) Undo/Redo และ move history (PGN export/import)
4) ใช้ภาพชิ้นหมาก + CSS ปรับสไตล์
5) AI (Minimax + alpha-beta) หรือเชื่อมกับ Stockfish
6) Multiplayer (socket หรือ WebSocket + server)
7) Packaging: native bundle (jpackage) หรือ jar ที่มาพร้อม OpenJFX

ประเด็นประสิทธิภาพ
- เริ่มต้นกับ array 8x8 พอเพียง; ถ้าต้องการ engine ที่เร็วให้พิจารณา bitboard และ optimized move generator

ทรัพยากรอ้างอิง
- OpenJFX: https://openjfx.io/
- Chess programming: https://www.chessprogramming.org/
- Stockfish (engine): https://stockfishchess.org/
- Algebraic notation, PGN: https://en.wikipedia.org/wiki/Portable_Game_Notation

แนะนำขั้นตอนถัดไป (ปฏิบัติ)
1) เขียน unit tests สำหรับ isValidMove แต่ละชิ้น และสำหรับ pawn special cases
2) เพิ่ม game-level validation: ห้ามเดินแล้วคิงถูกเช็ค
3) ใน UI: เพิ่ม dialog เมื่อโปรโมต และเปลี่ยนจาก Unicode เป็นภาพ (ImageView)
4) เพิ่มเมนู New Game / Undo / Save Game (export PGN)

ถ้าต้องการ ให้เริ่มจากข้อ 1 (unit tests + move validation) หรือข้อ 2 (check/checkmate) — ระบุที่ต้องการให้ทำต่อได้เลย.

แนวทางการทำ Algorithm ตรวจสอบ Check / Checkmate

1) คำจำกัดความสั้นๆ
- "Check": คิงของฝ่ายกำลังถูกคุกคามโดยชิ้นหมากฝ่ายตรงข้าม
- "Checkmate": ฝ่ายที่ถูกคุกคามไม่มีการเดินใด ๆ ที่ทำให้คิงรอดพ้นจากการคุกคาม

2) แนวทางพื้นฐาน (ง่ายและถูกต้อง)
- หา King position ของฝ่ายที่กำลังถูกตรวจ
- สร้างรายการทุก "pseudo-legal" moves ของฝ่ายที่กำลังเดิน (คือ moves ที่ถูกกฎการเดินของชิ้นแต่ยังไม่ตรวจว่าทำให้คิงอยู่ในเช็คหรือไม่)
- สำหรับแต่ละ move ในรายการ: ทำการจำลอง (make move) ลงบน board (หรือใช้ undo stack)
  - หลังจำลอง ให้ตรวจว่า King ของฝ่ายนั้นยังอยู่ภายใต้การโจมตีหรือไม่ (ตรวจหา attacker จากฝ่ายตรงข้าม)
  - หากมี move ใดที่ทำให้คิงไม่ถูกคุกคาม ให้ยกเลิกการจำลองและสรุปว่าไม่ใช่ checkmate (เป็นเพียง check)
- ถ้าไม่มี move ใดที่แก้สถานะได้ => checkmate

3) วิธีตรวจ "คิงถูกคุกคาม"
- จากตำแหน่งคิง ตรวจหา attacker โดยสแกนทิศทางที่เกี่ยวข้อง:
  - pawn captures (ตำแหน่งที่ pawn สามารถจับได้)
  - knights (ท่า L)
  - sliding pieces (rook/ bishop/ queen): ตรวจเส้นทางและหา piece ตรงทิศ
  - king รอบๆ (ควรตรวจป้องกันการชนกันของสองคิง)
- ใช้ฟังก์ชันตรวจว่า square ถูกโจมตี (isSquareAttacked(Position, attackerColor))

4) ประเด็นการจำลองและประสิทธิภาพ
- ใช้ makeMove()/unmakeMove() หรือ apply/rollback ด้วย undo stack เพื่อลดการคัดลอกบอร์ด
- เริ่มด้วย pseudo-legal generation แล้วกรองด้วยการตรวจ check (ถูกต้องและง่ายทำ)
- หากต้องการความเร็วสูง: พิจารณา bitboards และ incremental attack tables

5) ข้อพิเศษที่ต้องคิดร่วม
- castling: ตรวจเงื่อนไขพิเศษ (king ไม่เคยเดิน, rook ไม่เคยเดิน, squares ระหว่างไม่ถูกครอบครองและไม่ถูกโจมตี)
- en-passant: การจำลองอาจต้องระวังเพราะจับ en-passant อาจเปิดเส้นโจมตีไปยังคิง
- promotion: รวม move promotion variants ใน generation

6) การทดสอบ
- เขียนชุดทดสอบ (unit test/perft) สำหรับตำแหน่ง known checkmate เช่น Fool's mate, Scholar's mate, basic castling cases
- ใช้ perft tests เพื่อยืนยันความถูกต้องของ move generator

สรุป: เริ่มจาก implement isSquareAttacked() และ pseudo-legal move generation แล้วใช้ make/unmake move เพื่อตรวจว่าแต่ละ move แก้ check ได้หรือไม่ — วิธีนี้แม่นยำและเรียบง่าย เมื่อต้องการความเร็วให้ขยับไปที่ bitboards และ incremental attack caching.