package net.minecraft.src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

public class ControllerManager {

    private static boolean nativeLoaded = false;
    private static ByteBuffer stateBuffer;

    // Controller Types
    public static final int TYPE_NONE = 0;
    public static final int TYPE_XBOX = 1;
    public static final int TYPE_PLAYSTATION = 2;
    public static final int TYPE_GENERIC = 3;

    // Button Indices in internal state
    public static final int BTN_A = 0;
    public static final int BTN_B = 1;
    public static final int BTN_X = 2;
    public static final int BTN_Y = 3;
    public static final int BTN_LB = 4;
    public static final int BTN_RB = 5;
    public static final int BTN_LS = 6;
    public static final int BTN_RS = 7;
    public static final int BTN_DPAD_UP = 8;
    public static final int BTN_DPAD_DOWN = 9;
    public static final int BTN_DPAD_LEFT = 10;
    public static final int BTN_DPAD_RIGHT = 11;
    public static final int BTN_START = 12;
    public static final int BTN_BACK = 13;
    public static final int BTN_LT = 14;
    public static final int BTN_RT = 15;
    public static final int TOTAL_BUTTONS = 16;

    // Current & previous button states
    private static final boolean[] buttons = new boolean[TOTAL_BUTTONS];
    private static final boolean[] prevButtons = new boolean[TOTAL_BUTTONS];

    // Axes
    private static float leftStickX = 0.0f;
    private static float leftStickY = 0.0f;
    private static float rightStickX = 0.0f;
    private static float rightStickY = 0.0f;
    private static float leftTrigger = 0.0f;
    private static float rightTrigger = 0.0f;

    private static boolean isConnected = false;
    private static boolean wasConnected = false;
    private static int controllerType = TYPE_NONE;

    // In-menu virtual cursor coordinates
    private static double virtualCursorX = -1;
    private static double virtualCursorY = -1;

    // Cooldown for trigger repeated clicks in game
    private static int useCooldown = 0;

    static {
        loadNativeLibrary();
        if (nativeLoaded) {
            stateBuffer = ByteBuffer.allocateDirect(128).order(ByteOrder.nativeOrder());
        }
    }

    private static void loadNativeLibrary() {
        try {
            // 1. Try system/library path
            System.loadLibrary("gamecontroller");
            nativeLoaded = true;
            return;
        } catch (Throwable t1) {
            // fallback
        }

        try {
            // 2. Try App bundle path
            File appLib = new File("/Applications/Minecraft Beta 1.7.3.app/Contents/Resources/lib/libgamecontroller.dylib");
            if (appLib.exists()) {
                System.load(appLib.getAbsolutePath());
                nativeLoaded = true;
                return;
            }
        } catch (Throwable t2) {
            // fallback
        }

        try {
            // 3. Try build/natives path
            File buildLib = new File("build/natives/libgamecontroller.dylib");
            if (buildLib.exists()) {
                System.load(buildLib.getAbsolutePath());
                nativeLoaded = true;
                return;
            }
        } catch (Throwable t3) {
            // fallback
        }

        try {
            // 4. Extract embedded resource from jar
            InputStream in = ControllerManager.class.getResourceAsStream("/natives/libgamecontroller.dylib");
            if (in != null) {
                File tempFile = new File("/tmp/libgamecontroller.dylib");
                FileOutputStream out = new FileOutputStream(tempFile);
                byte[] b = new byte[8192];
                int len;
                while ((len = in.read(b)) > 0) {
                    out.write(b, 0, len);
                }
                in.close();
                out.close();
                System.load(tempFile.getAbsolutePath());
                nativeLoaded = true;
                return;
            }
        } catch (Throwable t4) {
            System.err.println("[ControllerManager] Could not load libgamecontroller: " + t4.getMessage());
        }
    }

    public static native boolean nativePoll(ByteBuffer buffer);

    public static void update(Minecraft mc) {
        if (!nativeLoaded || stateBuffer == null) return;

        System.arraycopy(buttons, 0, prevButtons, 0, TOTAL_BUTTONS);

        try {
            isConnected = nativePoll(stateBuffer);
        } catch (Throwable t) {
            isConnected = false;
        }

        if (!isConnected) {
            if (wasConnected) {
                wasConnected = false;
                controllerType = TYPE_NONE;
                if (mc.thePlayer != null) {
                    mc.ingameGUI.addChatMessage("\u00a7e[Controller] Disconnected");
                }
            }
            return;
        }

        // Connection notification
        if (!wasConnected) {
            wasConnected = true;
            controllerType = (int) stateBuffer.getFloat(20 * 4);
            String name = (controllerType == TYPE_XBOX) ? "Xbox Controller" :
                          (controllerType == TYPE_PLAYSTATION) ? "PlayStation 5 Controller" : "Gamepad";
            if (mc.thePlayer != null) {
                mc.ingameGUI.addChatMessage("\u00a7a[Controller] " + name + " connected!");
            }
        }

        // Read Raw Axes
        float rawLX = stateBuffer.getFloat(0 * 4);
        float rawLY = stateBuffer.getFloat(1 * 4);
        float rawRX = stateBuffer.getFloat(2 * 4);
        float rawRY = stateBuffer.getFloat(3 * 4);
        leftTrigger = stateBuffer.getFloat(4 * 4);
        rightTrigger = stateBuffer.getFloat(5 * 4);

        // Apply Deadzone & Response Curve
        leftStickX = applyDeadzone(rawLX, 0.18f);
        leftStickY = applyDeadzone(rawLY, 0.18f);
        rightStickX = applyResponseCurve(rawRX, 0.14f);
        rightStickY = applyResponseCurve(rawRY, 0.14f);

        // Read Buttons
        buttons[BTN_A] = stateBuffer.getFloat(6 * 4) > 0.5f;
        buttons[BTN_B] = stateBuffer.getFloat(7 * 4) > 0.5f;
        buttons[BTN_X] = stateBuffer.getFloat(8 * 4) > 0.5f;
        buttons[BTN_Y] = stateBuffer.getFloat(9 * 4) > 0.5f;
        buttons[BTN_LB] = stateBuffer.getFloat(10 * 4) > 0.5f;
        buttons[BTN_RB] = stateBuffer.getFloat(11 * 4) > 0.5f;
        buttons[BTN_LS] = stateBuffer.getFloat(12 * 4) > 0.5f;
        buttons[BTN_RS] = stateBuffer.getFloat(13 * 4) > 0.5f;
        buttons[BTN_DPAD_UP] = stateBuffer.getFloat(14 * 4) > 0.5f;
        buttons[BTN_DPAD_DOWN] = stateBuffer.getFloat(15 * 4) > 0.5f;
        buttons[BTN_DPAD_LEFT] = stateBuffer.getFloat(16 * 4) > 0.5f;
        buttons[BTN_DPAD_RIGHT] = stateBuffer.getFloat(17 * 4) > 0.5f;
        buttons[BTN_START] = stateBuffer.getFloat(18 * 4) > 0.5f;
        buttons[BTN_BACK] = stateBuffer.getFloat(19 * 4) > 0.5f;
        buttons[BTN_LT] = leftTrigger > 0.4f;
        buttons[BTN_RT] = rightTrigger > 0.4f;

        // Process controller actions
        if (mc.currentScreen != null) {
            handleMenuNavigation(mc);
        } else if (mc.inGameHasFocus && mc.thePlayer != null) {
            handleGameplayInput(mc);
        }
    }

    private static void handleGameplayInput(Minecraft mc) {
        // Camera Look
        float sensitivity = mc.gameSettings.mouseSensitivity * 2.0F + 0.5F;
        float lookX = rightStickX * 14.0F * sensitivity;
        float lookY = rightStickY * 12.0F * sensitivity;
        if (mc.gameSettings.invertMouse) {
            lookY = -lookY;
        }
        if (Math.abs(lookX) > 0.001F || Math.abs(lookY) > 0.001F) {
            mc.thePlayer.func_346_d(lookX, -lookY);
        }

        // Hotbar cycling via Bumpers (LB / RB)
        if (wasJustPressed(BTN_LB)) {
            mc.thePlayer.inventory.changeCurrentItem(1);
        }
        if (wasJustPressed(BTN_RB)) {
            mc.thePlayer.inventory.changeCurrentItem(-1);
        }

        // D-Pad cycling / Perspective
        if (wasJustPressed(BTN_DPAD_LEFT)) {
            mc.thePlayer.inventory.changeCurrentItem(1);
        }
        if (wasJustPressed(BTN_DPAD_RIGHT)) {
            mc.thePlayer.inventory.changeCurrentItem(-1);
        }
        if (wasJustPressed(BTN_DPAD_UP) || wasJustPressed(BTN_RS)) {
            mc.gameSettings.thirdPersonView = !mc.gameSettings.thirdPersonView;
        }
        if (wasJustPressed(BTN_DPAD_DOWN)) {
            mc.gameSettings.setOptionValue(EnumOptions.RENDER_DISTANCE, 1);
        }

        // Inventory (X / Square)
        if (wasJustPressed(BTN_X)) {
            mc.displayGuiScreen(new GuiInventory(mc.thePlayer));
        }

        // Drop item (Y / Triangle)
        if (wasJustPressed(BTN_Y)) {
            mc.thePlayer.dropCurrentItem();
        }

        // Pause Menu (Start / Options)
        if (wasJustPressed(BTN_START)) {
            mc.displayInGameMenu();
        }

        // F3 Debug Info (Back / Share)
        if (wasJustPressed(BTN_BACK)) {
            mc.gameSettings.showDebugInfo = !mc.gameSettings.showDebugInfo;
        }

        // Attack / Break block (Right Trigger / RT / R2)
        if (isDown(BTN_RT)) {
            if (wasJustPressed(BTN_RT)) {
                mc.clickMouse(0);
            }
            mc.func_6254_a(0, true);
        } else if (wasJustReleased(BTN_RT)) {
            mc.func_6254_a(0, false);
        }

        // Use / Place block (Left Trigger / LT / L2)
        if (useCooldown > 0) {
            --useCooldown;
        }
        if (isDown(BTN_LT)) {
            if (wasJustPressed(BTN_LT) || useCooldown == 0) {
                mc.clickMouse(1);
                useCooldown = 4; // 4 ticks = 200ms repeat rate like right-clicking
            }
        }
    }

    private static void handleMenuNavigation(Minecraft mc) {
        if (virtualCursorX < 0 || virtualCursorY < 0) {
            virtualCursorX = Mouse.getX();
            virtualCursorY = Mouse.getY();
        }

        // Move virtual cursor with Left Stick or D-Pad
        float moveX = leftStickX;
        float moveY = leftStickY;
        if (isDown(BTN_DPAD_LEFT)) moveX -= 0.8f;
        if (isDown(BTN_DPAD_RIGHT)) moveX += 0.8f;
        if (isDown(BTN_DPAD_UP)) moveY += 0.8f;
        if (isDown(BTN_DPAD_DOWN)) moveY -= 0.8f;

        float cursorSpeed = 16.0f;
        if (Math.abs(moveX) > 0.05f || Math.abs(moveY) > 0.05f) {
            virtualCursorX += moveX * cursorSpeed;
            virtualCursorY += moveY * cursorSpeed;

            if (virtualCursorX < 0) virtualCursorX = 0;
            if (virtualCursorX > mc.displayWidth) virtualCursorX = mc.displayWidth;
            if (virtualCursorY < 0) virtualCursorY = 0;
            if (virtualCursorY > mc.displayHeight) virtualCursorY = mc.displayHeight;

            Mouse.setCursorPosition((int) virtualCursorX, (int) virtualCursorY);
        } else {
            // Keep in sync with actual mouse if user moved physical mouse
            virtualCursorX = Mouse.getX();
            virtualCursorY = Mouse.getY();
        }

        // A button = Left Click (Select / Drag stack)
        if (wasJustPressed(BTN_A)) {
            Mouse.addButtonEvent(0, true);
        } else if (wasJustReleased(BTN_A)) {
            Mouse.addButtonEvent(0, false);
        }

        // X button = Right Click (Split stack / Place 1 item)
        if (wasJustPressed(BTN_X)) {
            Mouse.addButtonEvent(1, true);
        } else if (wasJustReleased(BTN_X)) {
            Mouse.addButtonEvent(1, false);
        }

        // B button = Back / Escape / Close menu
        if (wasJustPressed(BTN_B)) {
            mc.displayGuiScreen((GuiScreen) null);
            mc.setIngameFocus();
        }
    }

    public static boolean wasJustPressed(int btn) {
        return buttons[btn] && !prevButtons[btn];
    }

    public static boolean wasJustReleased(int btn) {
        return !buttons[btn] && prevButtons[btn];
    }

    public static boolean isDown(int btn) {
        return buttons[btn];
    }

    public static float getLeftStickX() {
        return leftStickX;
    }

    public static float getLeftStickY() {
        return leftStickY;
    }

    public static boolean isConnected() {
        return isConnected;
    }

    public static int getControllerType() {
        return controllerType;
    }

    private static float applyDeadzone(float val, float deadzone) {
        if (Math.abs(val) < deadzone) return 0.0f;
        float sign = val > 0 ? 1.0f : -1.0f;
        return sign * (Math.abs(val) - deadzone) / (1.0f - deadzone);
    }

    private static float applyResponseCurve(float val, float deadzone) {
        float linear = applyDeadzone(val, deadzone);
        if (linear == 0.0f) return 0.0f;
        float sign = linear > 0 ? 1.0f : -1.0f;
        float mag = Math.abs(linear);
        return sign * (mag * mag);
    }
}

