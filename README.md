# Minecraft Beta 1.7.3 (Apple Silicon)

A native macOS ARM64 port of Minecraft Beta 1.7.3 running on OpenJDK 17 and LWJGL 3.

## Overview

Running legacy Beta 1.7.3 on modern Apple Silicon Macs usually relies on Rosetta 2 with legacy LWJGL 2, which comes with several issues:
- Input lag and microstutters from Rosetta translation and legacy AWT/Carbon
- Blurry window scaling on Retina displays
- Mouse sensitivity issues and broken window resizing
- Tick desync on high refresh rate displays

This port updates the game to run natively on Apple Silicon with LWJGL 3 (GLFW), supporting native Retina resolutions, raw mouse input, and VSync.

## Features

- **Native ARM64**: Runs directly on Apple Silicon without Rosetta 2 using Java 17.
- **LWJGL 3 & GLFW**: Replaces obsolete AWT windowing with GLFW, fixing macOS keyboard and mouse handling.
- **Retina Display**: Full resolution framebuffer scaling for sharp rendering.
- **Smooth Frame Pacing**: Proper VSync and frame synchronization for 60Hz and 120Hz displays.
- **VBO Rendering**: Uses OpenGL vertex buffer objects for chunk rendering.
- **Standalone App**: Self-contained `.app` bundle, no extra launcher or configuration needed.
- **Offline Audio**: Packaged with sound files, no connection to legacy Mojang servers required.

## Installation

### Pre-built DMG
1. Download `Minecraft-Beta-1.7.3-macOS-arm64.dmg` from [Releases](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases).
2. Open the disk image and drag `Minecraft Beta 1.7.3.app` into `/Applications`.
3. If macOS blocks opening the app (unsigned developer):
   ```bash
   xattr -cr "/Applications/Minecraft Beta 1.7.3.app"
   ```

### Building from Source
Requires JDK 17 (e.g. `brew install openjdk@17`).

```bash
git clone https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon.git
cd minecraft-beta-1.7.3-apple-silicon

# Build and run
./gradlew bundleApp
open "build/apps/Minecraft Beta 1.7.3.app"

# Or build the DMG installer
./gradlew createDmg
```

## Game Data
Game files, saves, and settings are saved to:  
`~/Library/Application Support/minecraft-b1.7.3`

## Controls
- `Cmd + F` or `Fn + F11`: Toggle fullscreen
- Standard Beta 1.7.3 keybindings apply.

## Donations
If you'd like to support the project:

## Disclaimer
Minecraft is a trademark of Mojang AB. This project is not affiliated with Mojang Studios or Microsoft. Reverse-engineered code modifications are for compatibility and preservation purposes on modern hardware.
