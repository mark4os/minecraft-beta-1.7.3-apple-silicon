package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.unmapped.C_29058632;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperFillMixin {
	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void fill(int x1, int y1, int x2, int y2, int color) {
		C_29058632.m_57734177(x1, y1, x2, y2, color);
	}
}
