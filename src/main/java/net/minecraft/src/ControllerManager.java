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
    private static int lastKnownMouseX = -1;
    private static int lastKnownMouseY = -1;

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
            System.loadLibrary("gamecontroller");
            nativeLoaded = true;
            return;
        } catch (Throwable ignored) {
        }

        try {
            File appLib = new File("/Applications/Minecraft Beta 1.7.3.app/Contents/Resources/lib/libgamecontroller.dylib");
            if (appLib.exists()) {
                System.load(appLib.getAbsolutePath());
                nativeLoaded = true;
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
            File buildLib = new File("build/natives/libgamecontroller.dylib");
            if (buildLib.exists()) {
                System.load(buildLib.getAbsolutePath());
                nativeLoaded = true;
                return;
            }
        } catch (Throwable ignored) {
        }

        try {
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

        float deadzone = 0.15f;
        if (mc.gameSettings != null) {
            deadzone = mc.gameSettings.controllerDeadzone;
        }

        // Apply Deadzone & Response Curve
        leftStickX = applyDeadzone(rawLX, deadzone * 1.2f);
        leftStickY = applyDeadzone(rawLY, deadzone * 1.2f);
        rightStickX = applyResponseCurve(rawRX, deadzone);
        rightStickY = applyResponseCurve(rawRY, deadzone);

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
        } else {
            virtualCursorX = -1;
            virtualCursorY = -1;
            lastKnownMouseX = -1;
            lastKnownMouseY = -1;
            if (mc.inGameHasFocus && mc.thePlayer != null) {
                handleGameplayInput(mc);
            }
        }
    }

    private static void handleGameplayInput(Minecraft mc) {
        // Camera Look
        float sens = 1.0F;
        if (mc.gameSettings != null) {
            sens = mc.gameSettings.controllerSensitivity * (mc.gameSettings.mouseSensitivity * 2.0F + 0.5F);
        }
        float lookX = rightStickX * 14.0F * sens;
        float lookY = rightStickY * 12.0F * sens;
        if (mc.gameSettings != null && mc.gameSettings.controllerInvertY) {
            lookY = -lookY;
        }
        if (Math.abs(lookX) > 0.001F || Math.abs(lookY) > 0.001F) {
            mc.thePlayer.func_346_d(lookX, lookY);
        }

        // Hotbar cycling via Bumpers (LB / RB)
        if (wasJustPressed(BTN_LB)) {
            mc.thePlayer.inventory.changeCurrentItem(1);
        }
        if (wasJustPressed(BTN_RB)) {
            mc.thePlayer.inventory.changeCurrentItem(-1);
        }

        // D-Pad actions: Left = Swap Off-Hand, Right = Cycle Hotbar, Up = F5, Down = Fog
        if (wasJustPressed(BTN_DPAD_LEFT)) {
            mc.thePlayer.inventory.swapHands();
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

        // Trigger clicks (Initial press events)
        boolean swapTriggers = mc.gameSettings != null && mc.gameSettings.controllerSwapTriggers;
        int attackBtn = swapTriggers ? BTN_LT : BTN_RT;
        int useBtn = swapTriggers ? BTN_RT : BTN_LT;

        if (wasJustPressed(attackBtn)) {
            mc.clickMouse(0);
            mc.mouseTicksRan = mc.ticksRan;
        }
        if (wasJustPressed(useBtn)) {
            mc.clickMouse(1);
            mc.mouseTicksRan = mc.ticksRan;
        }
    }

    private static void handleMenuNavigation(Minecraft mc) {
        if (virtualCursorX < 0 || virtualCursorY < 0) {
            virtualCursorX = Mouse.getX();
            virtualCursorY = Mouse.getY();
            if (virtualCursorX <= 0 && virtualCursorY <= 0) {
                virtualCursorX = mc.displayWidth / 2.0;
                virtualCursorY = mc.displayHeight / 2.0;
            }
            lastKnownMouseX = (int) virtualCursorX;
            lastKnownMouseY = (int) virtualCursorY;
        }

        // Check if physical mouse / trackpad was moved by the user
        int curMouseX = Mouse.getX();
        int curMouseY = Mouse.getY();
        if (lastKnownMouseX != -1 && (curMouseX != lastKnownMouseX || curMouseY != lastKnownMouseY)) {
            virtualCursorX = curMouseX;
            virtualCursorY = curMouseY;
            lastKnownMouseX = curMouseX;
            lastKnownMouseY = curMouseY;
        }

        // Move virtual cursor with Left Stick, Right Stick, or D-Pad
        float moveX = leftStickX;
        float moveY = leftStickY;
        if (Math.abs(rightStickX) > Math.abs(moveX)) {
            moveX = rightStickX;
        }
        if (Math.abs(rightStickY) > Math.abs(moveY)) {
            moveY = rightStickY;
        }
        if (isDown(BTN_DPAD_LEFT)) moveX -= 0.8f;
        if (isDown(BTN_DPAD_RIGHT)) moveX += 0.8f;
        if (isDown(BTN_DPAD_UP)) moveY += 0.8f;
        if (isDown(BTN_DPAD_DOWN)) moveY -= 0.8f;

        if (Math.abs(moveX) > 0.05f || Math.abs(moveY) > 0.05f) {
            float speed = 14.0f;
            if (mc.gameSettings != null) {
                speed *= mc.gameSettings.controllerSensitivity;
            }
            virtualCursorX += moveX * speed;
            virtualCursorY += moveY * speed;

            if (virtualCursorX < 0) virtualCursorX = 0;
            if (virtualCursorX > mc.displayWidth) virtualCursorX = mc.displayWidth;
            if (virtualCursorY < 0) virtualCursorY = 0;
            if (virtualCursorY > mc.displayHeight) virtualCursorY = mc.displayHeight;

            double winW = Math.max(1, org.lwjgl.opengl.Display.getWindowWidth());
            double winH = Math.max(1, org.lwjgl.opengl.Display.getWindowHeight());
            double dispW = Math.max(1, org.lwjgl.opengl.Display.getWidth());
            double dispH = Math.max(1, org.lwjgl.opengl.Display.getHeight());

            double glfwX = virtualCursorX * winW / dispW;
            double glfwY = (dispH - virtualCursorY) * winH / dispH;

            // Deliver move event into LWJGL queue so GuiScreen and EntityRenderer receive it
            Mouse.addMoveEvent(glfwX, glfwY);
            Mouse.setCursorPosition((int) virtualCursorX, (int) virtualCursorY);

            lastKnownMouseX = (int) virtualCursorX;
            lastKnownMouseY = (int) virtualCursorY;
        }

        // Button Clicks
        // A button or RT = Left Click (Select / Drag)
        boolean clickLeft = wasJustPressed(BTN_A) || wasJustPressed(BTN_RT);
        boolean releaseLeft = wasJustReleased(BTN_A) || wasJustReleased(BTN_RT);
        if (clickLeft) {
            Mouse.addButtonEvent(0, true);
        } else if (releaseLeft) {
            Mouse.addButtonEvent(0, false);
        }

        // X button or LT = Right Click (Split stack / Place 1 item)
        boolean clickRight = wasJustPressed(BTN_X) || wasJustPressed(BTN_LT);
        boolean releaseRight = wasJustReleased(BTN_X) || wasJustReleased(BTN_LT);
        if (clickRight) {
            Mouse.addButtonEvent(1, true);
        } else if (releaseRight) {
            Mouse.addButtonEvent(1, false);
        }

        // Y button in Container = Swap hovered slot with Off-Hand
        if (wasJustPressed(BTN_Y) && mc.currentScreen instanceof GuiContainer) {
            ((GuiContainer) mc.currentScreen).swapHoveredWithOffhand();
        }

        // B button = Back / Escape / Close menu
        if (wasJustPressed(BTN_B)) {
            if (mc.currentScreen instanceof GuiControllerSettings) {
                mc.gameSettings.saveOptions();
                mc.displayGuiScreen(((GuiControllerSettings) mc.currentScreen).getParentScreen());
            } else {
                mc.displayGuiScreen((GuiScreen) null);
                mc.setIngameFocus();
            }
        }

        // Start button = Close menu / Resume
        if (wasJustPressed(BTN_START)) {
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

    public static boolean isAttackDown() {
        if (!isConnected) return false;
        Minecraft mc = Minecraft.getMinecraft();
        boolean swap = (mc != null && mc.gameSettings != null && mc.gameSettings.controllerSwapTriggers);
        return isDown(swap ? BTN_LT : BTN_RT);
    }

    public static boolean isUseItemDown() {
        if (!isConnected) return false;
        Minecraft mc = Minecraft.getMinecraft();
        boolean swap = (mc != null && mc.gameSettings != null && mc.gameSettings.controllerSwapTriggers);
        return isDown(swap ? BTN_RT : BTN_LT);
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

