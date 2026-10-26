package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.unmapped.C_85740840;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings({"UnusedMixin", "unused"})
@Mixin(C_85740840.class)
public abstract class MixinScreenFixClipboard {

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 */
	@Overwrite
	public static String getClipboard() {
		return LegacyLWJGL3.getClipboard();
	}

	/**
	 * @author moehreag
	 * @reason Fix clipboard access with GLFW
	 */
	@Overwrite
	public static void setClipboard(String string) {
		LegacyLWJGL3.setClipboard(string);
	}

}
