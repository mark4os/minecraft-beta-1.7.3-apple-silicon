package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.unmapped.C_96603444;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperBlit13Mixin {

	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void blit(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		GL11.glColor3f(1, 1, 1);
		C_96603444 tesselator = C_96603444.f_99414865;
		tesselator.m_35940071();
		tesselator.m_91558587((float) u / texWidth, (float) (v + height) / texHeight);
		tesselator.m_51994127(x, y + height, 0.0F);
		tesselator.m_91558587((float) (u + width) / texWidth, (float) (v + height) / texHeight);
		tesselator.m_51994127(x + width, y + height, 0.0F);
		tesselator.m_91558587((float) (u + width) / texWidth, (float) v / texHeight);
		tesselator.m_51994127(x + width, y, 0.0F);
		tesselator.m_91558587((float) u / texWidth, (float) v / texHeight);
		tesselator.m_51994127(x, y, 0.0F);
		tesselator.m_70070273();
		GL11.glColor3f(1, 1, 1);
	}
}
