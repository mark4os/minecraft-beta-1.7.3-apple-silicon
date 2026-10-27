package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiControllerSlider extends GuiButton {
	public float sliderValue = 1.0F;
	public boolean dragging = false;
	private final int sliderType; // 0 = sensitivity, 1 = deadzone
	private final GuiControllerSettings parentGui;

	public GuiControllerSlider(int id, int x, int y, int type, String text, float value, GuiControllerSettings parent) {
		super(id, x, y, 150, 20, text);
		this.sliderType = type;
		this.sliderValue = value;
		this.parentGui = parent;
	}

	protected int getHoverState(boolean var1) {
		return 0;
	}

	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (this.enabled2) {
			if (this.dragging) {
				this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
				if (this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}
				if (this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}
				this.parentGui.updateSliderValue(this.sliderType, this.sliderValue);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
			this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
		}
	}

	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
			if (this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}
			if (this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}
			this.parentGui.updateSliderValue(this.sliderType, this.sliderValue);
			this.dragging = true;
			return true;
		} else {
			return false;
		}
	}

	public void mouseReleased(int var1, int var2) {
		this.dragging = false;
	}
}
