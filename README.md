# Minecraft Beta 1.7.3 — Native Apple Silicon Port (ARM64)

[![Platform](https://img.shields.io/badge/platform-macOS%20Apple%20Silicon%20(M1--M5)-black.svg?style=flat&logo=apple)](https://apple.com)
[![Architecture](https://img.shields.io/badge/arch-arm64%20(native)-blue.svg?style=flat)](https://github.com)
[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg?style=flat&logo=openjdk)](https://openjdk.org)
[![LWJGL](https://img.shields.io/badge/backend-LWJGL%203%20(GLFW)-red.svg?style=flat)](https://www.lwjgl.org)
[![Display](https://img.shields.io/badge/Retina-Liquid%20Retina%20%2F%20120Hz%20ProMotion-green.svg?style=flat)](https://apple.com)

A native, zero-compromise port of the legendary **Minecraft Beta 1.7.3** running natively on **Apple Silicon (M1, M2, M3, M4, M5)** Macs via **Java 17 LTS (ARM64)** and **LWJGL 3 (GLFW / Modern OpenGL / OpenAL)**.

No Rosetta 2 translation. No mouse latency or acceleration issues. True Retina HiDPI resolution. Rock-solid 60 FPS / 120 FPS ProMotion frame pacing with **zero 20 Hz movement judder**.

---

## 📸 Preview

```
┌────────────────────────────────────────────────────────────────────────┐
│  Minecraft Beta 1.7.3 (b1.7.3-arm64)                                   │
│  60 / 120 fps, 0 chunk updates                                         │
│  C: 84/1024. F: 42, O: 898, E: 0                                       │
│  E: 14/120. B: 0, I: 106                                               │
│                                                                        │
│  Display: 2560x1664 (Liquid Retina HiDPI)                              │
│  GL_RENDERER: Apple M-Series GPU (AppleMetalOpenGLRenderer)            │
│  GL_VERSION: 2.1 Metal                                                 │
└────────────────────────────────────────────────────────────────────────┘
```

---

## ✨ Features & Apple Silicon Optimizations

### 1. 100% Native ARM64 (Zero Rosetta 2)
- Built directly on 64-bit ARM64 OpenJDK 17 with Apple Silicon optimizations.
- Near-zero power draw: runs cool and whisper-quiet on MacBook Air and MacBook Pro with hours of battery life.

### 2. Modern Windowing & Raw Mouse Input (LWJGL 3 / GLFW)
- Completely replaces deprecated Carbon/AWT and LWJGL 2 with modern GLFW 3.3.
- Native `GLFW_CURSOR_DISABLED` and `GLFW_RAW_MOUSE_MOTION` for authentic, buttery-smooth mouse look without system acceleration or discrete trackpad jitter.
- Fixed classic macOS bugs: no BGR blue-tint color inversion, no Command/Option key stuck states.

### 3. True Retina Display Support (HiDPI)
- Decouples logical window coordinates from physical framebuffer size (`glfwGetFramebufferSize`).
- Native 2x pixel density on Liquid Retina XDR displays and Apple Studio Displays — crystal-clear voxel textures with zero blur or bilinear upscaling.

### 4. Precision Frame Pacing & Zero 20 Hz Judder
- **The Problem in Vanilla:** Minecraft logic ticks at 20 TPS (50 ms). On 60 Hz or 120 Hz displays, timer jitter previously caused uneven 3-3-2 cadence beats and phase drift, creating subtle camera stuttering during movement even at high FPS.
- **The Solution:** A hybrid two-stage frame pacer in `Sync.java` combines kernel-level `LockSupport.parkNanos()` sleep with sub-millisecond ARM64 `Thread.onSpinWait()` alignment.
- Ticks run on a rock-solid **3–3–3–3 cadence** at 60 Hz (6 frames per tick at 120 Hz ProMotion). Every camera coordinate, chunk VBO, particle, and cloud is mathematically locked to `prevPos + (pos - prevPos) * partialTicks`.

### 5. TBDR & Unified Memory GPU Optimizations
- **Chunk VBOs (`GL_ARB_vertex_buffer_object`):** Replaces legacy Display Lists with VBOs stored directly in Apple Silicon Unified Memory, eliminating per-frame triple vertex copying.
- **Hardware Hidden Surface Removal (HSR):** Separates opaque solid blocks (Pass 0) from alpha cutout blocks (Pass 1), allowing Apple's Tile-Based Deferred Renderer (TBDR) to discard occluded fragments in hardware before executing fragment shaders.
- **Fast Chunk Culling:** Subchunks containing only air skip rendering passes; chunk rebuilds utilize a flat 3D index cache with zero lock contention.

### 6. Fullscreen & Keyboard Shortcuts
- Native macOS fullscreen toggle via **`Cmd + F`** or **`F11`** (or `Fn + F11`).
- Preserves mouse grab and hardware swap interval across macOS Mission Control and Spaces transitions.

---

## 📥 Download & Installation

### Option 1: Drag-and-Drop Disk Image (.dmg) — Recommended
1. Download the latest **`Minecraft-Beta-1.7.3-macOS-arm64.dmg`** from [Releases](https://github.com).
2. Open the `.dmg` and drag **`Minecraft Beta 1.7.3`** into your **`Applications`** folder.
3. Open Terminal and run the following command once to clear the macOS Gatekeeper quarantine flag for unnotarized builds:
   ```bash
   xattr -cr "/Applications/Minecraft Beta 1.7.3.app"
   ```
4. Double-click **Minecraft Beta 1.7.3** from Applications or Launchpad and enjoy!

### Option 2: Build from Source (Terminal)
Requires **JDK 17+** (Homebrew OpenJDK: `brew install openjdk@17`).

```bash
# 1. Clone the repository
git clone https://github.com/<your-username>/minecraft-beta-1.7.3-apple-silicon.git
cd minecraft-beta-1.7.3-apple-silicon

# 2. Build and launch standalone .app bundle
./gradlew bundleApp
open "build/apps/Minecraft Beta 1.7.3.app"

# 3. (Optional) Generate the styled .dmg installer
./gradlew createDmg
open "build/distributions/Minecraft-Beta-1.7.3-macOS-arm64.dmg"
```

---

## 📂 Project Architecture

```
minecraft-beta-1.7.3-apple-silicon/
├── src/main/java/
│   ├── net/minecraft/         # Decompiled & optimized Beta 1.7.3 codebase
│   │   ├── client/Minecraft.java  # Main loop, GLFW integration, Retina resize
│   │   └── src/
│   │       ├── Timer.java         # 20 TPS phase-locked tick accumulator
│   │       ├── EntityRenderer.java# Camera interpolation & TBDR pipeline
│   │       ├── RenderGlobal.java  # VBO chunk manager & occlusion culling
│   │       └── Tessellator.java   # Modernized direct vertex buffer pipeline
│   └── org/lwjgl/             # LWJGL 2 compatibility layer built on LWJGL 3
│       ├── opengl/
│       │   ├── Display.java       # GLFW native window, Retina FB, VSync
│       │   └── Sync.java          # Precision hybrid frame pacer (parkNanos + onSpinWait)
│       └── input/
│           ├── Mouse.java         # Raw mouse delta accumulator
│           └── Keyboard.java      # GLFW key event queue & macOS shortcuts
├── src/main/resources/        # Textures, models, icons, and local sound effects
├── tools/
│   └── generate_dmg_background.py # High-res Retina DMG background generator
└── build.gradle               # Gradle build script with bundleApp & createDmg tasks
```

---

## ⚖️ Legal & Disclaimer

* **Not an official Minecraft product.** Not approved by or associated with Mojang Studios or Microsoft.
* Minecraft is a registered trademark of Mojang AB / Microsoft Corporation.
* This repository contains reverse-engineered source code modifications for preservation, compatibility, and educational purposes on modern Apple Silicon hardware.
* All original game assets, brands, and registered trademarks belong to their respective owners.

---

## 🤝 Community & Credits

- Original game by **Markus "Notch" Persson** and Mojang AB (2011).
- Retro-computing community at **r/GoldenAgeMinecraft** and **Modification Station**.
- Ported and optimized for Apple Silicon ARM64 by **Marko** and the community.
