package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.util.IMEManager;
import net.minecraft.client.Minecraft;
import net.minecraft.unmapped.C_85740840;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class IMEMinecraftMixin {
	@Inject(method = "openScreen", at = @At("HEAD"))
	private void unfocusIME(C_85740840 screen, CallbackInfo ci) {
		if (screen == null) {
			IMEManager.getInstance().onWidgetFocusUpdate(null, false);
		}
	}
}
