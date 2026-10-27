package net.minecraft.src;

public class GuiIngameMenu extends GuiScreen {
	private int updateCounter = 0;
	private int saveChunksCounter = 0;
	private int saveDisplayTicks = 25;
	private boolean isSaving = true;

	public void initGui() {
		this.controlList.clear();
		byte var1 = -16;
		this.controlList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + var1, "Save and quit to title"));
		if(this.mc.isMultiplayerWorld()) {
			((GuiButton)this.controlList.get(0)).displayString = "Disconnect";
		}

		this.controlList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + var1, "Back to game"));
		this.controlList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + var1, "Options..."));
		this.controlList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + var1, 98, 20, StatCollector.translateToLocal("gui.achievements")));
		this.controlList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + var1, 98, 20, StatCollector.translateToLocal("gui.stats")));
	}

	protected void actionPerformed(GuiButton var1) {
		if(var1.id == 0) {
			this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
		}

		if(var1.id == 1) {
			this.mc.statFileWriter.readStat(StatList.leaveGameStat, 1);
			if(this.mc.isMultiplayerWorld()) {
				this.mc.theWorld.sendQuittingDisconnectingPacket();
			}

			this.mc.changeWorld1((World)null);
			this.mc.displayGuiScreen(new GuiMainMenu());
		}

		if(var1.id == 4) {
			this.mc.displayGuiScreen((GuiScreen)null);
			this.mc.setIngameFocus();
		}

		if(var1.id == 5) {
			this.mc.displayGuiScreen(new GuiAchievements(this.mc.statFileWriter));
		}

		if(var1.id == 6) {
			this.mc.displayGuiScreen(new GuiStats(this, this.mc.statFileWriter));
		}

	}

	public void updateScreen() {
		super.updateScreen();
		++this.updateCounter;

		if(this.isSaving && this.mc.theWorld != null) {
			boolean moreToSave = !this.mc.theWorld.func_650_a(this.saveChunksCounter++);
			if(!moreToSave) {
				this.isSaving = false;
			}
		}

		if(this.saveDisplayTicks > 0) {
			--this.saveDisplayTicks;
		}
	}

	public void drawScreen(int var1, int var2, float var3) {
		this.drawDefaultBackground();

		if(this.isSaving || this.saveDisplayTicks > 0) {
			float fade = 1.0F;
			if(!this.isSaving && this.saveDisplayTicks < 10) {
				fade = (float)this.saveDisplayTicks / 10.0F;
			}

			float pulse = MathHelper.sin(((float)(this.updateCounter % 10) + var3) / 10.0F * (float)Math.PI * 2.0F) * 0.2F + 0.8F;
			int brightness = (int)(255.0F * pulse * fade);
			if(brightness > 4) {
				int color = brightness << 16 | brightness << 8 | brightness;
				this.drawString(this.fontRenderer, "Saving level..", 8, this.height - 16, color);
			}
		}

		this.drawCenteredString(this.fontRenderer, "Game menu", this.width / 2, 40, 16777215);
		super.drawScreen(var1, var2, var3);
	}
}
