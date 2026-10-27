#import <GameController/GameController.h>
#import <Foundation/Foundation.h>
#include <jni.h>

static GCController *activeController = nil;
static BOOL initialized = NO;

static void ensureInitialized() {
    if (initialized) return;
    initialized = YES;
    
    [[NSNotificationCenter defaultCenter] addObserverForName:GCControllerDidConnectNotification
                                                      object:nil
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification *note) {
        if (!activeController) {
            activeController = note.object;
        }
    }];
    
    [[NSNotificationCenter defaultCenter] addObserverForName:GCControllerDidDisconnectNotification
                                                      object:nil
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification *note) {
        if (activeController == note.object) {
            activeController = nil;
            NSArray *all = [GCController controllers];
            if (all.count > 0) activeController = all.firstObject;
        }
    }];
    
    [GCController startWirelessControllerDiscoveryWithCompletionHandler:nil];
}

JNIEXPORT jboolean JNICALL Java_net_minecraft_src_ControllerManager_nativePoll(JNIEnv *env, jclass cls, jobject byteBuf) {
    ensureInitialized();

    // Pump main run loop briefly to dispatch controller notifications & events
    [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate dateWithTimeIntervalSinceNow:0.0005]];

    if (!activeController) {
        NSArray *all = [GCController controllers];
        if (all.count > 0) {
            activeController = all.firstObject;
        }
    }

    if (!activeController || !activeController.extendedGamepad) {
        return JNI_FALSE;
    }

    float *buf = (float *)(*env)->GetDirectBufferAddress(env, byteBuf);
    if (!buf) return JNI_FALSE;

    GCExtendedGamepad *gp = activeController.extendedGamepad;

    // 0..3: Sticks (-1.0 to 1.0)
    buf[0] = (float)gp.leftThumbstick.xAxis.value;
    buf[1] = (float)gp.leftThumbstick.yAxis.value;
    buf[2] = (float)gp.rightThumbstick.xAxis.value;
    buf[3] = (float)gp.rightThumbstick.yAxis.value;

    // 4..5: Triggers (0.0 to 1.0)
    buf[4] = (float)gp.leftTrigger.value;
    buf[5] = (float)gp.rightTrigger.value;

    // 6..9: Face buttons: A, B, X, Y
    buf[6] = gp.buttonA.isPressed ? 1.0f : 0.0f;
    buf[7] = gp.buttonB.isPressed ? 1.0f : 0.0f;
    buf[8] = gp.buttonX.isPressed ? 1.0f : 0.0f;
    buf[9] = gp.buttonY.isPressed ? 1.0f : 0.0f;

    // 10..11: Bumpers: LB/L1, RB/R1
    buf[10] = gp.leftShoulder.isPressed ? 1.0f : 0.0f;
    buf[11] = gp.rightShoulder.isPressed ? 1.0f : 0.0f;

    // 12..13: Stick clicks: L3, R3
    buf[12] = (gp.leftThumbstickButton && gp.leftThumbstickButton.isPressed) ? 1.0f : 0.0f;
    buf[13] = (gp.rightThumbstickButton && gp.rightThumbstickButton.isPressed) ? 1.0f : 0.0f;

    // 14..17: D-Pad: Up, Down, Left, Right
    buf[14] = gp.dpad.up.isPressed ? 1.0f : 0.0f;
    buf[15] = gp.dpad.down.isPressed ? 1.0f : 0.0f;
    buf[16] = gp.dpad.left.isPressed ? 1.0f : 0.0f;
    buf[17] = gp.dpad.right.isPressed ? 1.0f : 0.0f;

    // 18..19: Menu (Start / Options), Options (Back / Share / Create)
    buf[18] = gp.buttonMenu.isPressed ? 1.0f : 0.0f;
    buf[19] = (gp.buttonOptions && gp.buttonOptions.isPressed) ? 1.0f : 0.0f;

    // 20: Controller type: 1 = Xbox, 2 = PlayStation, 3 = Generic
    NSString *name = activeController.vendorName ?: activeController.productCategory ?: @"";
    if ([name containsString:@"Xbox"] || [activeController.productCategory containsString:@"Xbox"]) {
        buf[20] = 1.0f;
    } else if ([name containsString:@"DualSense"] || [name containsString:@"DualShock"] || [name containsString:@"PlayStation"] || [activeController.productCategory containsString:@"DualSense"]) {
        buf[20] = 2.0f;
    } else {
        buf[20] = 3.0f;
    }

    return JNI_TRUE;
}

