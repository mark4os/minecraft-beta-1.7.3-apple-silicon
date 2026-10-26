package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.net.URI;
import net.minecraft.unmapped.C_85740840;

@SuppressWarnings({"UnusedMixin", "unused"})
@Mixin(C_85740840.class)
public class MixinScreenFixOpenLink {
	/**
	 * @author rdh
	 * @reason fix link opening with GLFW
	 */
	@Overwrite
	public void openLink(URI uri) {
		LegacyLWJGL3.openLink(uri.toString());
	}
}
