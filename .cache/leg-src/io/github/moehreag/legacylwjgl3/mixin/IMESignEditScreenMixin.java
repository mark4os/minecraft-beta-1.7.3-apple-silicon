package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import net.minecraft.unmapped.C_22460678;
import net.minecraft.unmapped.C_72410450;
import net.minecraft.unmapped.C_85740840;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(C_72410450.class)
public abstract class IMESignEditScreenMixin extends C_85740840 implements PreeditAwareWidget {

	@Shadow
	private C_22460678 sign;
	@Shadow
	private int row;

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		onFocusUpdate(true);
	}

	@Override
	public int getCursorX() {
		var currentRowText = this.sign.f_27758266[this.row].m_30058393();
		return f_05230747/2 + f_11884601.m_09235706(currentRowText) / 2;
	}

	@Override
	public int getCursorY() {
		return 40 + 20 + 12 + this.row * 10;
	}

	@Override
	public int getInputHeight() {
		return 9;
	}
}
