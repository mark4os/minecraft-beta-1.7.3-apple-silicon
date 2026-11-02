# Minecraft Beta 1.7.3 (Apple Silicon)

[![Platform](https://img.shields.io/badge/Platform-macOS%20ARM64%20(Apple%20Silicon)-black?logo=apple)](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon)
[![Java](https://img.shields.io/badge/Java-17%20(Temurin%20%2F%20OpenJDK)-orange?logo=openjdk)](https://adoptium.net)
[![LWJGL](https://img.shields.io/badge/LWJGL-3.3.3%20(GLFW)-blue)](https://www.lwjgl.org)
[![Version](https://img.shields.io/badge/Release-v1.1.1-brightgreen)](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases)
[![Roadmap](https://img.shields.io/badge/Roadmap-View%20Development%20Plan-blue)](ROADMAP.md)
[![License](https://img.shields.io/badge/Status-Community%20Preservation-purple)](#disclaimer)

A fully native, standalone macOS ARM64 port of **Minecraft Beta 1.7.3** running directly on modern Apple Silicon Macs (M1/M2/M3/M4/M5) with OpenJDK 17 and LWJGL 3 (GLFW).

---

## Overview

Playing vintage Minecraft Beta 1.7.3 on modern Apple Silicon hardware normally requires Rosetta 2 emulation running legacy LWJGL 2 / Carbon / AWT libraries. This introduces significant issues:
- **Input Lag & Stuttering**: Rosetta 2 translation overhead and obsolete AWT cursor grabbing causes frame pacing inconsistencies and microstutters.
- **Retina Display Blur**: Legacy windowing renders at low resolution and stretches non-integer pixels across Retina screens.
- **Broken Window Resizing & Mouse Drift**: Dragging window edges or switching spaces breaks input focus and mouse centering.
- **Audio Server Disconnects**: Old sound resources fail to download because original 2011 Mojang resource servers are defunct.
- **Zero Controller Support**: Classic Beta 1.7.3 only supported keyboard and mouse.

This port completely modernizes the underlying runtime while strictly preserving the **100% authentic Minecraft Beta 1.7.3 look, feel, and gameplay mechanics**.

---

## Key Features & Improvements

### 🎮 Native Controller Support (Xbox & PlayStation 5)
- **Zero Configuration / Plug-and-Play**: Built directly on Apple's native `GameController.framework`. Connect your controller via Bluetooth or USB-C and it works immediately.
- **Supported Controllers**:
  - **Xbox Series X/S**, **Xbox One**, and **Xbox Elite** controllers.
  - **PlayStation 5 DualSense** and **DualSense Edge** controllers.
  - Any MFi / standard GameController-compatible gamepads.
- **In-Game Controller Settings Menu**: Accessible directly from the main Options screen:
  - **Stick Sensitivity Slider**: 20% to 200% with smooth analog response curve.
  - **Stick Deadzone Slider**: 5% to 35% customizable deadzone to prevent drift.
  - **Invert Look (Y) Toggle**: Canonical camera direction by default (push UP looks UP), with flight-sim invert option.
  - **Swap Triggers Toggle**: Swap RT (Attack) and LT (Place) if desired.
  - **Live Connection Status**: Automatically displays connected controller name in green (`Connected: Xbox Controller` / `Connected: PlayStation 5 Controller`).
  - **Visual Layout Reference**: In-menu button cheat sheet for quick reference.
- **Full Virtual Cursor Menu Navigation**:
  - Navigate all game menus, pause screens, Inventory, Chests, Furnaces, and Crafting Tables using either analog stick or the D-Pad.
  - **Button A** / **RT**: Left Click (select items, click buttons, drag and drop).
  - **Button X** / **LT**: Right Click (split stacks, place single items).
  - **Button B**: Back / Cancel (returns from submenus or resumes game).
  - Seamless dual-input: freely switch between controller and trackpad/mouse at any moment without cursor jumps.

### ⚡ Performance & Display
- **100% Native ARM64 Code**: Compiled directly for Apple Silicon; no Rosetta 2 translation layer.
- **Retina Display Rendering**: Pixel-perfect native framebuffer rendering for crisp, sharp block textures and UI.
- **Modern Video Settings**:
  - **VSync Toggle**: Independent VSync control (ON / OFF).
  - **Max Framerate Slider**: Adjust limit from 30, 60, 120, 144, 240 FPS up to **Unlimited**.
  - Smooth frame pacing on 60Hz and 120Hz ProMotion displays.
- **VBO Acceleration**: Uses OpenGL Vertex Buffer Objects for chunk rendering to reduce CPU-GPU draw overhead.
- **Garbage Collector Tuning**: Pre-configured with Low-Latency G1GC parameters (`-XX:+UseG1GC -XX:MaxGCPauseMillis=5 -XX:+DisableExplicitGC`) eliminating memory-allocation spikes.
- **Flicker-Free Pause Menu**: Fixed the classic Beta 1.7.3 bug where chunk saving was executed inside the OpenGL rendering loop, causing "Saving level.." to flash and flicker when opening or returning from settings.
- **Zero Standing-Still Tick Spikes**: Fixed the vanilla `WorldChunkManager` biome cache hash collision bug (where 98.6% of `SpawnerAnimals` biome lookups missed cache and ran 4,335 Simplex noise evaluations per second) and made `level.dat` disk writes asynchronous, completely eliminating periodic tick spikes.

### 🔦 Dynamic Lighting, Off-Hand & Gameplay QoL (`v1.1.0`)
- **Smooth Dynamic Lighting (`Dynamic Lights: ON / OFF`)**: Holding a Torch, Glowstone, Jack-o'-Lantern, Lava Bucket, or Redstone Torch in either your main hand or off-hand smoothly illuminates surrounding blocks, caves, and entities in real time without melting ice or snow.
- **Second Hand / Off-Hand (`F` Key & Controller Support)**:
  - Dedicated off-hand slot in the player inventory (`GuiInventory`) and left-of-hotbar HUD indicator.
  - Press `F` in-game (or `D-Pad Left` on controller) to swap items between main hand and off-hand; press `F` (or `Y` / `Triangle` on controller) while hovering over any slot in an inventory/chest to move it directly to the off-hand.
  - Full first-person and third-person left-hand item rendering and automatic right-click usage/placement (e.g., mine with a pickaxe in your main hand while placing torches or eating food from your off-hand).
- **Per-World Keep Inventory (`/keepinventory true/false`)**:
  - Press `T` or `/` in Singleplayer or LAN Multiplayer and run `/keepinventory true` (or `/gamerule keepInventory true`) to preserve all inventory, armor, and off-hand items upon death and respawn. Saved persistently per world in `level.dat`.
- **Cross-Platform Open to LAN (`Open to LAN` in Pause Menu)**:
  - Host your Singleplayer world on your local network (`port 25565`, `online-mode=false`) with one click.
  - Automatically copies your `<LAN_IP>:25565` address to the macOS clipboard and prints it in chat.
  - **100% Cross-Platform Compatible**: Any official/vanilla Minecraft Beta 1.7.3 client on Windows, Linux, or macOS (Betacraft, Prism Launcher, MultiMC, official launcher) can join directly via IP (Wi-Fi LAN or VPN/Tunnel IP)!
- **Multilingual In-Game Chat (Ukrainian, Russian & English)**:
  - Full keyboard input, `Cmd+V` clipboard pasting, and crisp 8x8 pixel-art font rendering for Ukrainian (`Іі, Її, Єє, Ґґ, А-Я, а-я`), Russian (`Ёё, Ъъ, Ыы, Ээ, А-Я, а-я`), and English in chat, signs, and text fields.

### 📦 Self-Contained App & Offline Audio
- **Drag-and-Drop Installation**: Distributed as a signed standalone macOS `.app` bundle inside a styled `.dmg` installer.
- **Bundled Sound & Music**: All authentic 2011 C418 music tracks and sound effects are embedded locally—no internet connection or broken server downloads needed.

---

## Installation

### 1. Download Pre-built DMG (Recommended)
1. Head to the **[Releases](https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon/releases)** page.
2. Download the latest `Minecraft-Beta-1.7.3-macOS-arm64.dmg`.
3. Open the disk image and drag **Minecraft Beta 1.7.3** into `/Applications`.
4. If macOS Gatekeeper shows an unsigned developer notification on first launch, run this command in Terminal:
   ```bash
   xattr -cr "/Applications/Minecraft Beta 1.7.3.app"
   ```

---

## Building from Source

### Prerequisites
- macOS 11.0+ on Apple Silicon (M1 or newer).
- JDK 17 (e.g., via Homebrew: `brew install openjdk@17`).
- Command Line Tools for native GameController bridge compilation (`xcode-select --install`).

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/mark4os/minecraft-beta-1.7.3-apple-silicon.git
cd minecraft-beta-1.7.3-apple-silicon

# Build and run directly
./gradlew bundleApp
open "build/apps/Minecraft Beta 1.7.3.app"

# Or generate the styled drag-and-drop DMG installer
./gradlew createDmg
```
The output disk image will be placed in `build/distributions/Minecraft-Beta-1.7.3-macOS-arm64.dmg`.

---

## Controls Reference

### Controller Layout (Xbox / PlayStation 5)

| Xbox Button | PS5 Button | Gameplay Action | Menu / Inventory Action |
|:---|:---|:---|:---|
| **Right Trigger (RT)** | **R2** | Attack / Mine Blocks (continuous) | Left Click (Select / Drag) |
| **Left Trigger (LT)** | **L2** | Use / Place Block | Right Click (Split Stack) |
| **Right Bumper (RB)** | **R1** | Next Hotbar Slot | — |
| **Left Bumper (LB)** | **L1** | Previous Hotbar Slot | — |
| **Left Stick** | **Left Stick** | Analog Movement | Move Virtual Cursor |
| **Right Stick** | **Right Stick** | Camera Look (Look Around) | Move Virtual Cursor |
| **Stick Click (LS)** | **L3** | Sneak / Crouch | — |
| **Stick Click (RS)** | **R3** | Toggle Perspective (F5) | — |
| **Button A** | **Cross (✕)** | Jump | Left Click (Select / Drag) |
| **Button B** | **Circle (◯)** | Sneak / Crouch | Back / Close Menu / Resume |
| **Button X** | **Square (□)** | Open Inventory | Right Click (Place 1 item) |
| **Button Y** | **Triangle (△)** | Drop Current Item | Swap Hovered Slot with Off-Hand |
| **D-Pad Left** | **D-Pad Left** | Swap Main Hand / Off-Hand (`F`) | Nudge Virtual Cursor |
| **D-Pad Right** | **D-Pad Right** | Cycle Hotbar | Nudge Virtual Cursor |
| **D-Pad Up** | **D-Pad Up** | Toggle Perspective (F5) | Nudge Virtual Cursor |
| **D-Pad Down** | **D-Pad Down** | Toggle Render Distance (Fog) | Nudge Virtual Cursor |
| **Start / Menu** | **Options** | Pause Menu | Resume Game |
| **Back / View** | **Share / Create** | Toggle F3 Debug Screen | — |

### Keyboard & Mouse

| Key | Action |
|:---|:---|
| `W`, `A`, `S`, `D` | Move |
| `Space` | Jump |
| `Left Shift` | Sneak / Crouch |
| `Left Click` | Attack / Mine |
| `Right Click` | Use / Place Block |
| `E` | Open Inventory |
| `F` | Swap Main Hand & Off-Hand (in-game or hovered slot in inventory) |
| `T` or `/` | Open Chat / Command Line (`/keepinventory true/false`, `/seed`, `/time set`) |
| `Q` | Drop Item |
| `1` – `9` / `Scroll Wheel` | Select Hotbar Slot |
| `Cmd + F` or `Fn + F11` | Toggle Fullscreen |
| `Fn + F3` / `F3` | Debug Info & Coordinates |
| `Fn + F5` / `F5` | Toggle Third-Person View |
| `Fn + F8` / `F8` | Cinematic Camera Smoothing |
| `Esc` | Pause Menu |

---

## File Locations & Game Data

All player data, worlds, screenshots, and settings are saved in standard macOS application support paths:
- **Game Directory**: `~/Library/Application Support/minecraft-b1.7.3/`
  - `saves/`: World saves (compatible with any standard Beta 1.7.3 world format).
  - `texturepacks/`: Custom texture packs.
  - `screenshots/`: In-game screenshots (`F2`).
  - `options.txt`: Controls, video settings, and controller configuration.

---

## System Requirements

- **Operating System**: macOS 11.0 (Big Sur) or newer (macOS 12 Monterey, 13 Ventura, 14 Sonoma, 15 Sequoia fully supported).
- **Architecture**: Apple Silicon ARM64 (M1, M2, M3, M4, M5, Pro, Max, Ultra).
- **Memory**: Minimum 2 GB RAM (allocated 1 GB heap by default).

---

## Donations & Support

If you enjoy playing vintage Beta 1.7.3 natively on your Mac and would like to support maintenance and future improvements:


---

## Disclaimer

Minecraft is a trademark of Mojang AB. This project is an independent community preservation effort for modern hardware compatibility and is not endorsed by or affiliated with Mojang Studios or Microsoft.
