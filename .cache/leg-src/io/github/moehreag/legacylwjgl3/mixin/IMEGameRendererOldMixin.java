package io.github.moehreag.legacylwjgl3.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3Internal;
import net.minecraft.unmapped.C_03140863;
import net.minecraft.unmapped.C_43718703;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(C_03140863.class)
public abstract class IMEGameRendererOldMixin {
	@Inject(method = "render(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V", shift = At.Shift.AFTER))
	private void renderIME(float tickDelta, CallbackInfo ci, @Local C_43718703 window) {
		LegacyLWJGL3Internal.renderIMEOverlay(window.f_91462382, window.m_39119333(), window.m_16103615());
	}
}
