<div align="center">

# Minecraft Beta 1.7.3 — Apple Silicon (ARM64)

Native Minecraft Beta 1.7.3 client for Apple Silicon (M1/M2/M3/M4/M5) macOS, built on OpenJDK 17 and LWJGL 3.

<br/>

[![Platform](https://img.shields.io/badge/Platform-macOS%20Apple%20Silicon-000000?style=for-the-badge&logo=apple&logoColor=white)](https://apple.com)
[![Architecture](https://img.shields.io/badge/Architecture-ARM64%20Native-0078D4?style=for-the-badge&logo=arm&logoColor=white)](https://arm.com)
[![Java](https://img.shields.io/badge/Java-17%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Graphics](https://img.shields.io/badge/Graphics-LWJGL%203%20%2F%20GLFW-5586A4?style=for-the-badge&logo=opengl&logoColor=white)](https://www.lwjgl.org)
[![Display](https://img.shields.io/badge/Display-Liquid%20Retina%20%2F%20120Hz-34C759?style=for-the-badge&logo=apple&logoColor=white)](https://apple.com)
[![Release](https://img.shields.io/badge/Release-v1.0.4-blue?style=for-the-badge)](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases)

<br/>

</div>

## Overview

Running Beta 1.7.3 on modern macOS typically runs into several issues:
- Rosetta 2 translation overhead and occasional stuttering.
- Blurry 1x window scaling on Retina displays.
- Mouse acceleration and trackpad panning issues from legacy AWT/Carbon.
- Movement jitter and 3-3-2 cadence beats caused by 20 TPS tick interpolation desync with 60Hz and 120Hz displays.

This port updates the client to run natively on modern macOS with raw mouse input, true Retina framebuffer support, and smooth frame pacing.

---

## Features & Changes

- **Native ARM64**: Runs directly on Java 17 LTS without Rosetta 2.
- **LWJGL 3 (GLFW)**: Replaced legacy AWT/Carbon windowing. Uses raw mouse input (`GLFW_RAW_MOUSE_MOTION`) and fixes macOS modifier key stuck states.
- **Retina HiDPI**: True 2x framebuffer scaling using `glfwGetFramebufferSize`.
- **Frame Pacing & Judder Fix**: Hybrid precision frame pacer (`parkNanos` + `Thread.onSpinWait`) to keep tick interpolation in phase with display refreshes (60Hz and 120Hz ProMotion). Camera and chunk offsets are unified to `prevPos + (pos - prevPos) * partialTicks`.
- **Chunk VBOs**: Replaced Display Lists with `GL_ARB_vertex_buffer_object` stored in Unified Memory.
- **Fullscreen**: Native fullscreen toggle via `Cmd + F` or `Fn + F11`.
- **Audio**: Bundled local OpenAL audio, no broken 2011 AWS resource requests.

---

## Installation

### DMG (Recommended)
1. Download **`Minecraft-Beta-1.7.3-macOS-arm64.dmg`** from [Releases](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases).
2. Drag `Minecraft Beta 1.7.3.app` to `/Applications`.
3. Clear the macOS quarantine attribute (since this is an unsigned community build):
   ```bash
   xattr -cr "/Applications/Minecraft Beta 1.7.3.app"
   ```

### Building from Source
Requires JDK 17 (`brew install openjdk@17`).

```bash
git clone https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon.git
cd minecraft-beta-1.7.3-apple-silicon

# Build and run the app
./gradlew bundleApp
open "build/apps/Minecraft Beta 1.7.3.app"

# Or build the DMG installer
./gradlew createDmg
```

---

## Data Location
Game data, saves, and options are stored in:  
`~/Library/Application Support/minecraft-b1.7.3/`

---

## Disclaimer
This project is not affiliated with Mojang Studios or Microsoft. Minecraft is a trademark of Mojang AB. Reverse-engineered code modifications are for compatibility and preservation purposes on modern hardware.
