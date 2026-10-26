package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import io.github.moehreag.legacylwjgl3.util.IMEPreeditOverlay;
import net.minecraft.unmapped.C_16178213;
import net.minecraft.unmapped.C_23014920;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnusedMixin", "unused", "AddedMixinMembersNamePattern"})
@Mixin(C_16178213.class)
public abstract class IMETextFieldWidgetOldMixin implements PreeditAwareWidget {

	@Shadow
	@Final
	private C_23014920 textRenderer;
	@Shadow
	private boolean focused;

	@Shadow
	public int y;
	@Shadow
	@Final
	private int height;
	@Shadow
	public int x;
	@Shadow
	private String text;
	@Shadow
	private boolean editable;
	@Unique
	private IMEPreeditOverlay overlay;

	@Inject(method = "setFocused", at = @At("HEAD"))
	private void startTextInput(boolean focused, CallbackInfo ci) {
		if (focused != this.focused && this.editable) {
			onFocusUpdate(focused);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void updateRect(CallbackInfo ci) {
		if (overlay != null) {
			overlay.updateInputPosition(this.x + 4 + textRenderer.m_09235706(text), this.y + (this.height - 8) / 2);
		}
	}

	@Override
	public int getCursorX() {
		return 0;
	}

	@Override
	public int getCursorY() {
		return 0;
	}

	@Override
	public int getInputHeight() {
		return 9;
	}
}
