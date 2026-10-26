package io.github.moehreag.legacylwjgl3.mixin;

import java.applet.Applet;

import io.github.moehreag.legacylwjgl3.CrashReportBeta;
import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MinecraftApplet;
import net.minecraft.unmapped.C_87604186;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"removal", "UnusedMixin"})
@Mixin(value = MinecraftApplet.class, priority = 1100)
public abstract class MinecraftAppletMixin extends Applet {

	@Inject(method = "init", remap = false, at = @At("HEAD"), cancellable = true)
	private void onAppletInit(CallbackInfo ci) {
		LegacyLWJGL3.LOGGER.info("Creating window from Applet!");
		ci.cancel();
		Applet launcher = ((Applet) this.getParent());

		boolean var1 = false;
		if (this.getParameter("fullscreen") != null) {
			var1 = this.getParameter("fullscreen").equalsIgnoreCase("true");
		}

		Minecraft minecraft = new Minecraft(null, null, null, 854, 480, var1) {
			public void m_49334482(net.minecraft.unmapped.C_95807626 crashSummary) {
				CrashReportBeta.report(crashSummary);
			}
		};

		if (this.getParameter("username") != null && this.getParameter("sessionid") != null) {
			minecraft.f_69329575 = new C_87604186(this.getParameter("username"), this.getParameter("sessionid"));
			System.out.println("Setting user: " + minecraft.f_69329575.f_18340302 + ", " + minecraft.f_69329575.f_59704460);
			if (this.getParameter("mppass") != null) {
				minecraft.f_69329575.f_97866080 = this.getParameter("mppass");
			}
		} else {
			minecraft.f_69329575 = new C_87604186("Player", "");
		}

		if (this.getParameter("server") != null && this.getParameter("port") != null) {
			minecraft.m_83493923(this.getParameter("server"), Integer.parseInt(this.getParameter("port")));
		}
		minecraft.f_58820764 = !"true".equals(this.getParameter("stand-alone"));
		launcher.setVisible(false);
		launcher.stop();
		launcher.destroy();
		setStub(null);
		launcher.removeAll();
		launcher.setSize(0, 0);

		Thread.currentThread().setName("Minecraft Main Thread");
		@SuppressWarnings("UnnecessaryLocalVariable") Runnable r = minecraft; // ensures that the `run` call doesn't get obfuscated later
		r.run();
		System.exit(0);
	}

}