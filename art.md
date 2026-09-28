# ใช้งานภาพงานศิลป์ (Art) แทนสัญลักษณ์ชิ้นหมาก

เอกสารนี้อธิบายวิธีนำภาพ (PNG/PNG-โปร่งใส) มาแทน Unicode chess symbols ทั้งสำหรับ Swing และ JavaFX

1) โครงสร้างไฟล์ที่แนะนำ
- วางไฟล์ภาพในโฟลเดอร์ทรัพยากรของโปรเจกต์ (classpath):
  - src\resources\images\w_K.png
  - src\resources\images\w_Q.png
  - ...
  - src\resources\images\b_P.png

ชื่อไฟล์โดย convention:
- prefix: `w_` = white, `b_` = black
- piece codes: K Q R B N P
- ex: w_K.png, b_Q.png, w_P.png

2) ข้อแนะนำภาพ
- ฟอร์แมต: PNG (รองรับ transparency)
- ขนาดที่เหมาะสม: 64×64 หรือ 80×80 px (ปรับตามขนาดปุ่ม)
- ใช้ background โปร่งใส เพื่อให้ board color โชว์
- ให้มี margin รอบภาพเล็กน้อย (padding)

3) วิธีโหลดและใช้งาน (Swing)
- โหลดเป็น ImageIcon และเก็บใน Map ที่ cache ไว้

ตัวอย่าง (Swing):

// ในคลาส ChessAppSwing
private Map<String, ImageIcon> icons = new HashMap<>();

private ImageIcon loadIcon(String name){
    java.net.URL url = getClass().getResource("/images/" + name);
    if (url == null) return null; // fallback
    ImageIcon icon = new ImageIcon(url);
    Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
    return new ImageIcon(img);
}

private void loadAllIcons(){
    String[] pieces = {"K","Q","R","B","N","P"};
    for (String p : pieces){
        icons.put("w"+p, loadIcon("w_"+p+".png"));
        icons.put("b"+p, loadIcon("b_"+p+".png"));
    }
}

// ใน updateUI() แทนการ setText:
ImageIcon ic = icons.get((p.getColor()==Piece.Color.WHITE?"w":"b") + Character.toUpperCase(p.getSymbol()));
if (ic != null){
    btn.setText("");
    btn.setIcon(ic);
} else {
    btn.setIcon(null);
    btn.setText(getUnicodeFor(p)); // fallback
}

4) วิธีโหลดและใช้งาน (JavaFX)
- โหลดเป็น Image แล้วใช้ ImageView เป็น graphic ของ Button

ตัวอย่าง (JavaFX):

private Map<String, Image> icons = new HashMap<>();

private Image loadFx(String name){
    try (InputStream is = getClass().getResourceAsStream("/images/"+name)){
        if (is==null) return null;
        Image img = new Image(is, 64, 64, true, true);
        return img;
    } catch (IOException e){ return null; }
}

// loadAllIcons คล้าย Swing

// ใน updateUI():
Image img = icons.get(key);
if (img != null){
    button.setText("");
    button.setGraphic(new ImageView(img));
} else {
    button.setGraphic(null);
    button.setText(getUnicodeFor(p));
}

5) การจัดเก็บใน JAR / การ deploy
- วางภาพใน src/resources/images หรือใน classpath ที่จะรวมใน jar
- ใช้ getResource/getResourceAsStream เพื่อให้ทำงานทั้งใน IDE และ jar

6) ประสิทธิภาพ
- โหลดภาพครั้งเดียว (cache) ไม่โหลดซ้ำในทุกเฟรม
- ปรับขนาด( scale ) เมื่อตอนโหลด แทนการปรับขนาดที่ runtime บ่อย ๆ

7) เคล็ดลับเพิ่มเติม
- เตรียมภาพสำรอง (fallback) เป็น Unicode ถ้าภาพหาย
- สำหรับ HiDPI ให้เตรียม 2 ขนาด (e.g., 64px และ 128px) และเลือกตาม DPI
- หากต้องการ animation ให้ใช้ GIF หรือ animate ภาพด้วย code

8) สรุปการเปลี่ยนในโค้ด
- โหลด icons ใน constructor หรือ init method
- ใน updateUI() แทนการ setText ด้วย setIcon / setGraphic
- อย่าลืมล้าง icon เมื่อช่องว่าง (setIcon(null) / setGraphic(null))

ถ้าต้องการ ฉันช่วยสร้างตัวอย่างโค้ดในโปรเจกต์ (แก้ ChessAppSwing.updateUI และเพิ่ม loadAllIcons) และย้ายไฟล์ตัวอย่างเข้าโฟลเดอร์ resources ให้เลย — ต้องการให้ทำให้หรือไม่? 
