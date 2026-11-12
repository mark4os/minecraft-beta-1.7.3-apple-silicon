# Minecraft Beta 1.7.3 for macOS (Apple Silicon)

Standalone port of Minecraft Beta 1.7.3 running natively on Apple Silicon (ARM64) with LWJGL 3 and OpenJDK 17.

No Rosetta, no mouse lag, sharp Retina rendering, and native controller support (Xbox & PS5 DualSense).

## Quick start
Grab the latest `.dmg` from [Releases](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases), drag the app to Applications, and run it.

If macOS blocks the first launch with an unsigned developer prompt:
```bash
xattr -cr "/Applications/Minecraft Beta 1.7.3.app"
```

Worlds and configs live in `~/Library/Application Support/minecraft-b1.7.3/saves/`. You can drop your existing b1.7.3 worlds right in.

## Features
- Native Apple Silicon ARM64 build with Retina HiDPI
- Smooth raw mouse input via GLFW
- Native Xbox and PS5 controller support with virtual inventory cursor
- Handheld dynamic lights and off-hand slot (F to swap)
- Open to LAN hosting (port 25565)
- Cyrillic chat support
- Bundled sound files included

## Controls
- Standard b1.7.3 controls (WASD, Space, Shift, E)
- `F`: Swap hands (rebindable in Controls)
- `R`: Toggle fog
- `Cmd + F` / `F11`: Fullscreen
- `F3`: Debug screen

Controller layout: Left stick to move, right stick to look, RT to mine, LT to place/use, A to jump, B to sneak, X for inventory.

## Building
Requires JDK 17:
```bash
git clone https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon.git
cd minecraft-beta-1.7.3-apple-silicon
./gradlew bundleApp
open "build/apps/Minecraft Beta 1.7.3.app"
```
