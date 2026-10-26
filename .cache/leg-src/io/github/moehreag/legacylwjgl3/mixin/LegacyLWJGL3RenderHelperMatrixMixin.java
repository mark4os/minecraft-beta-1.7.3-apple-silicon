package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.unmapped.C_37925375;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperMatrixMixin {

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void pushMatrix() {
		C_37925375.m_55305366();
	}

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void translate(float x, float y, float z) {
		C_37925375.m_36201038(x, y, z);
	}

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void popMatrix() {
		C_37925375.m_23590488();
	}
}
