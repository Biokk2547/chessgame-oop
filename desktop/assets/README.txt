libGDX assets for Chess UI

Place piece images here with the following names (PNG recommended):
- W_King.png   (white king)
- W_Queen.png
- W_Rook.png
- W_Bishop.png
- W_Knight.png
- W_Pawn.png
- B_King.png   (black king)
- B_Queen.png
- B_Rook.png
- B_Bishop.png
- B_Knight.png
- B_Pawn.png

Icons should be square (e.g., 256x256). The GameScreen will scale them to the square size. If images are missing, the UI will fall back to Unicode glyphs.

To run: create a libGDX Gradle project or add libGDX jars to the classpath and run com.chessegame.libgdx.DesktopLauncher

Notes:
- This change does not add a Gradle build file. You must add libGDX dependencies (gdx, gdx-backend-lwjgl3, gdx-platform native jars) to build and run.
- Game logic (Board, ChessUtils, Clock) is reused from existing code.
