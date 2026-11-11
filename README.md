# Minecraft Beta 1.7.3 (Apple Silicon)

[![Platform](https://img.shields.io/badge/Platform-macOS%20ARM64-black?logo=apple)](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon)
[![Release](https://img.shields.io/badge/Release-v1.2.0-brightgreen)](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases)

A native macOS ARM64 port of Minecraft Beta 1.7.3 running directly on Apple Silicon Macs (M1/M2/M3/M4/M5) with OpenJDK 17 and LWJGL 3 (GLFW).

---

## Overview

Running Minecraft Beta 1.7.3 on modern Apple Silicon Macs through legacy launchers usually requires Rosetta 2 translation together with legacy LWJGL 2 / Carbon / AWT libraries. This setup brings several known problems:
- Input latency and microstutters from AWT mouse grabbing and Rosetta 2 overhead.
- Blurry non-integer scaling on Retina displays.
- Lost mouse grab and focus issues when resizing windows or switching Spaces.
- Missing sounds due to dead 2011 Mojang resource servers.
- No native controller support.

This project updates the runtime environment to modern 64-bit ARM64 and LWJGL 3 while preserving original Beta 1.7.3 mechanics, terrain generation, and visuals.

---

## Features

### Native Controller Support
- Native integration with Apple's `GameController.framework` over Bluetooth or USB-C (Xbox Series X/S, Xbox One, PS5 DualSense, and standard MFi gamepads).
- In-game controller settings menu (accessible from Options):
  - Analog stick sensitivity and deadzone adjustments.
  - Invert look (Y-axis) option.
  - Swap triggers (RT / LT).
  - Connected controller status display.
- Virtual cursor navigation for all inventories, crafting tables, chests, furnaces, and menus using analog sticks or D-Pad.
  - Button A / RT: select and drag items.
  - Button X / LT: split stacks and place single items.
  - Button B: back or close menu.

### Performance and Display
- Native ARM64 binary: runs directly without Rosetta 2.
- Native Retina resolution rendering.
- VSync toggle and configurable Max Framerate slider (30, 60, 120, 144, 240 FPS or Unlimited).
- Monotonic frame pacing in `Sync.java` to avoid frame skipping on high refresh rate displays (120Hz ProMotion and 180Hz external panels).
- Compiled display list caching for fancy clouds (`glCloudList`), reducing tessellation overhead from ~1.5ms to <0.02ms.
- Allocation-free chunk meshing: reuses internal `ChunkCache` and `RenderBlocks` buffers to reduce GC churn.
- Single-chunk MRU voxel cache in `World.java` to speed up frequent block and light lookups.
- Fixed the pause menu level saving flicker bug from vanilla Beta 1.7.3.
- Resolved animal spawner biome cache hash collisions and moved `level.dat` saving to a background thread to prevent tick stutters.

### Dynamic Lighting and Quality of Life
- Dynamic handheld lighting (`Dynamic Lights: ON / OFF` in Video Settings) for torches, glowstone, Jack-o'-Lanterns, lava buckets, and redstone torches in either hand.
- Off-hand slot (`F` key or controller):
  - Dedicated off-hand slot in inventory and HUD indicator.
  - Press `F` (or D-Pad Left) to swap held items; press `F` while hovering over an item in any container to move it to the off-hand.
  - Left-hand first-person and third-person rendering, with secondary right-click block placement and food consumption.
- KeepInventory toggle (`/keepinventory true/false` or `/gamerule keepInventory true/false`), saved per-world in `level.dat`.
- Open to LAN: host a local server from the pause menu on port 25565 (`online-mode=false`). Supports connections from vanilla Beta 1.7.3 clients on Windows, Linux, and macOS (Prism Launcher, Betacraft, MultiMC).
- Multilingual chat support for Ukrainian, Russian, and English input and display (`Cmd+V` clipboard pasting, Cyrillic 8x8 font rendering).

### Packaging and Audio
- Packaged as a signed macOS `.app` bundle and styled `.dmg` installer.
- Bundled C418 soundtrack and sound effects included directly in the application bundle.

---

## Installation

### Download DMG
1. Download the latest `Minecraft-Beta-1.7.3-macOS-arm64.dmg` from [Releases](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases).
2. Open the disk image and drag **Minecraft Beta 1.7.3** into `/Applications`.
3. If macOS Gatekeeper prevents opening on first launch, clear quarantine attributes in Terminal:
   ```bash
   xattr -cr "/Applications/Minecraft Beta 1.7.3.app"
   ```

---

## Building from Source

### Prerequisites
- macOS 11.0 or newer on Apple Silicon.
- JDK 17 (e.g. `brew install openjdk@17`).
- Xcode Command Line Tools (`xcode-select --install`).

### Build
```bash
git clone https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon.git
cd minecraft-beta-1.7.3-apple-silicon

# Build and launch app bundle
./gradlew bundleApp
open "build/apps/Minecraft Beta 1.7.3.app"

# Or generate DMG installer
./gradlew createDmg
```
The resulting `.dmg` will be located in `build/distributions/`.

---

## Controls

### Controller Layout (Xbox / PlayStation 5)

| Xbox | PS5 | In-Game Action | Menu / Inventory Action |
|:---|:---|:---|:---|
| **RT** | **R2** | Attack / Mine | Left Click (Select / Drag) |
| **LT** | **L2** | Use / Place Block | Right Click (Split Stack) |
| **RB** | **R1** | Next Hotbar Slot | - |
| **LB** | **L1** | Previous Hotbar Slot | - |
| **Left Stick** | **Left Stick** | Movement | Virtual Cursor |
| **Right Stick** | **Right Stick** | Look Around | Virtual Cursor |
| **LS Click** | **L3** | Sneak | - |
| **RS Click** | **R3** | Toggle Perspective (F5) | - |
| **A** | **Cross** | Jump | Left Click (Select / Drag) |
| **B** | **Circle** | Sneak | Back / Close Menu |
| **X** | **Square** | Open Inventory | Right Click (Place 1) |
| **Y** | **Triangle** | Drop Item | Swap Slot with Off-Hand |
| **D-Pad Left** | **D-Pad Left** | Swap Hands (`F`) | Nudge Cursor |
| **D-Pad Right** | **D-Pad Right** | Cycle Hotbar | Nudge Cursor |
| **D-Pad Up** | **D-Pad Up** | Toggle Perspective (F5) | Nudge Cursor |
| **D-Pad Down** | **D-Pad Down** | Toggle Render Distance | Nudge Cursor |
| **Menu / Start** | **Options** | Pause Menu | Resume Game |
| **View / Back** | **Share** | Toggle F3 Debug Screen | - |

### Keyboard and Mouse

| Key | Action |
|:---|:---|
| `W`, `A`, `S`, `D` | Movement |
| `Space` | Jump |
| `Left Shift` | Sneak |
| `Left Click` | Attack / Mine |
| `Right Click` | Use / Place Block |
| `E` | Open Inventory |
| `F` | Swap Main Hand and Off-Hand |
| `T` or `/` | Open Chat / Commands |
| `Q` | Drop Item |
| `1` - `9` / Scroll | Select Hotbar Slot |
| `Cmd + F` / `F11` | Toggle Fullscreen |
| `F3` | Debug Screen |
| `F5` | Toggle Third-Person View |
| `F8` | Cinematic Camera Smoothing |
| `Esc` | Pause Menu |

---

## File Locations

Game data and settings are stored in standard macOS application paths:
- `~/Library/Application Support/minecraft-b1.7.3/`
  - `saves/`: World saves.
  - `texturepacks/`: Custom texture packs.
  - `screenshots/`: In-game screenshots (`F2`).
  - `options.txt`: Settings and controller configuration.

---

## System Requirements

- **OS**: macOS 11.0 (Big Sur) or newer.
- **Hardware**: Apple Silicon Mac (M1, M2, M3, M4, M5).
- **Memory**: 2 GB RAM minimum.

---

## Donations & Support

If you would like to support maintenance of this port:

---

## Disclaimer

Minecraft is a trademark of Mojang AB. This project is an independent community preservation project and is not affiliated with or endorsed by Mojang Studios or Microsoft.
