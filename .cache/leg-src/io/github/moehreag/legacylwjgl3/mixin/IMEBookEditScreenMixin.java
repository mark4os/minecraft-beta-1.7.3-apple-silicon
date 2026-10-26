package io.github.moehreag.legacylwjgl3.mixin;

import java.util.List;
import net.minecraft.unmapped.C_79370641;
import net.minecraft.unmapped.C_85740840;
import net.minecraft.unmapped.C_88380901;
import io.github.moehreag.legacylwjgl3.api.PreeditAwareWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(C_88380901.class)
public abstract class IMEBookEditScreenMixin extends C_85740840 implements PreeditAwareWidget {

	@Shadow
	@Final
	private boolean unsigned;
	@Shadow
	private C_79370641 pagesNbt;

	@Shadow
	protected abstract String getCurrentPageContent();

	@Shadow
	private int widthOffset;

	@Shadow
	private boolean signing;

	@Shadow
	private String title;

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (this.unsigned) {
			onFocusUpdate(true);
		}
	}

	@Override
	public int getCursorX() {
		if (signing) {
			return (this.f_05230747 - this.widthOffset) / 2 + 36 + (116 + f_11884601.m_09235706(title)) / 2;
		}
		if (this.pagesNbt == null) return 0;
		var currentPageText = getCurrentPageContent() + "_";

		List<String> strings = f_11884601.m_99776868(currentPageText, 116);
		String last = strings.get(strings.size() - 1);
		int w = f_11884601.m_09235706(last) - f_11884601.m_11668766('_');

		return (this.f_05230747 - this.widthOffset) / 2 + 36 + w;
	}

	@Override
	public int getCursorY() {
		if (signing) {
			return 2 + 48;
		}
		if (this.pagesNbt == null) return 0;
		var currentPageText = getCurrentPageContent() + "_";
		return 2 + 16 + 10 + f_11884601.m_71113921(currentPageText, 116);
	}

	@Override
	public int getInputHeight() {
		return f_11884601.f_30706543;
	}
}
