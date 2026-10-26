package io.github.moehreag.legacylwjgl3.mixin;

import net.minecraft.unmapped.C_10350466;
import net.minecraft.unmapped.C_16178213;
import net.minecraft.unmapped.C_82860087;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings({"unused", "UnusedMixin"})
@Mixin({C_82860087.class, C_10350466.class})
public abstract class MultiplayerScreensMixin {

	@Redirect(method = "init", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;focused:Z", opcode = Opcodes.PUTFIELD))
	private void useSetterInsteadOfFieldAccess(C_16178213 instance, boolean value) {
		instance.m_15958311(value);
	}
}
