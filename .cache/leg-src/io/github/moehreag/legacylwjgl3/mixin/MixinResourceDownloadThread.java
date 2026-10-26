package io.github.moehreag.legacylwjgl3.mixin;

import java.io.File;
import net.minecraft.unmapped.C_31574624;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(C_31574624.class)
public abstract class MixinResourceDownloadThread {

	@Shadow
	protected abstract void loadResources(File dir, String prefix);

	@Shadow
	public File resourcesDir;

	@Inject(method = "run", at = @At("HEAD"), cancellable = true)
	private void noResourceLoading(CallbackInfo ci) {
		ci.cancel();
		this.loadResources(resourcesDir, "");
	}
}