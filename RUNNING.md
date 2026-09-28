Run libGDX (Desktop LWJGL3) build instructions

Prerequisites:
- Java 11+ installed
- Gradle installed OR use the Gradle wrapper (not included)

1) Add piece images into ./assets (see assets/README.txt). Filenames: w_K.png, w_Q.png, ..., b_P.png

2) Build and run (with Gradle installed):
   gradle run

   If using Gradle wrapper, after generating wrapper or obtaining one:
   ./gradlew run  (on Unix/mac)
   gradlew.bat run (on Windows)

Notes:
- The run task launches com.chessegame.libgdx.DesktopLauncher and sets working dir to project root so assets/ is found.
- If you prefer to embed assets into classpath, move assets into src/main/resources and adjust GameScreen asset paths.
- This build file configures sourceSets to use the repository's src/ layout so existing classes (Board, ChessUtils, Clock, Game, UI) are reused.
- For packaging a distribution, run: gradle installDist (produces a runnable distribution under build/install)

If you'd like, generate a Gradle wrapper in this repo or I can create a full multi-module libGDX Gradle project scaffold.
