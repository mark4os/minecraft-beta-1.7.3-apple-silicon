package io.github.moehreag.legacylwjgl3.mixin;

import io.github.moehreag.legacylwjgl3.implementation.LegacyLWJGL3RenderHelper;
import net.minecraft.unmapped.C_37925375;
import net.minecraft.unmapped.C_90362304;
import net.minecraft.unmapped.C_93396801;
import net.minecraft.unmapped.C_96603444;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LegacyLWJGL3RenderHelper.class)
public abstract class LegacyLWJGL3RenderHelperBlitMixin {
	/**
	 * @author moehreag
	 * @reason implement cross-version access
	 */
	@Overwrite
	public static void blit(int texWidth, int texHeight, int u, int v, int x, int y, int width, int height) {
		C_37925375.m_38257475(1, 1, 1);
		C_96603444 tesselator = C_96603444.m_23865043();
		C_93396801 bufferBuilder = tesselator.m_74061400();
		bufferBuilder.m_90389466(7, C_90362304.f_42968381);
		bufferBuilder.m_07150154(x, y + height, 0.0F).m_37170459((float) u / texWidth, (float) (v + height) / texHeight).m_27858740();
		bufferBuilder.m_07150154(x + width, y + height, 0.0F).m_37170459((float) (u + width) / texWidth, (float) (v + height) / texHeight).m_27858740();
		bufferBuilder.m_07150154(x + width, y, 0.0F).m_37170459((float) (u + width) / texWidth, (float) v / texHeight).m_27858740();
		bufferBuilder.m_07150154(x, y, 0.0F).m_37170459((float) u / texWidth, (float) v / texHeight).m_27858740();
		tesselator.m_70070273();
		C_37925375.m_38257475(1, 1, 1);
	}
}
