package net.minecraft.src;

public class GuiControllerSettings extends GuiScreen {
	private GuiScreen parentScreen;
	protected String screenTitle = "Controller Settings";
	private GameSettings options;

	private GuiControllerSlider sensitivitySlider;
	private GuiControllerSlider deadzoneSlider;
	private GuiSmallButton invertYButton;
	private GuiSmallButton swapTriggersButton;

	public GuiControllerSettings(GuiScreen parent, GameSettings settings) {
		this.parentScreen = parent;
		this.options = settings;
	}

	public void initGui() {
		StringTranslate tr = StringTranslate.getInstance();
		this.screenTitle = tr.translateKey("options.controllerTitle");

		int leftCol = this.width / 2 - 155;
		int rightCol = this.width / 2 + 5;
		int row1Y = this.height / 6 + 8;
		int row2Y = this.height / 6 + 32;

		float sensVal = (this.options.controllerSensitivity - 0.2F) / 1.8F;
		if (sensVal < 0.0F) sensVal = 0.0F;
		if (sensVal > 1.0F) sensVal = 1.0F;
		this.sensitivitySlider = new GuiControllerSlider(1001, leftCol, row1Y, 0, getSensitivityString(), sensVal, this);
		this.controlList.add(this.sensitivitySlider);

		this.invertYButton = new GuiSmallButton(1002, rightCol, row1Y, getInvertYString());
		this.controlList.add(this.invertYButton);

		float deadzoneVal = (this.options.controllerDeadzone - 0.05F) / 0.30F;
		if (deadzoneVal < 0.0F) deadzoneVal = 0.0F;
		if (deadzoneVal > 1.0F) deadzoneVal = 1.0F;
		this.deadzoneSlider = new GuiControllerSlider(1003, leftCol, row2Y, 1, getDeadzoneString(), deadzoneVal, this);
		this.controlList.add(this.deadzoneSlider);

		this.swapTriggersButton = new GuiSmallButton(1004, rightCol, row2Y, getSwapTriggersString());
		this.controlList.add(this.swapTriggersButton);

		this.controlList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, tr.translateKey("gui.done")));
	}

	public void updateSliderValue(int type, float value) {
		if (type == 0) {
			this.options.controllerSensitivity = 0.2F + value * 1.8F;
			this.sensitivitySlider.displayString = getSensitivityString();
		} else if (type == 1) {
			this.options.controllerDeadzone = 0.05F + value * 0.30F;
			this.deadzoneSlider.displayString = getDeadzoneString();
		}
	}

	private String getSensitivityString() {
		StringTranslate tr = StringTranslate.getInstance();
		int pct = Math.round(this.options.controllerSensitivity * 100.0F);
		return tr.translateKey("options.controller.sensitivity") + ": " + pct + "%";
	}

	private String getDeadzoneString() {
		StringTranslate tr = StringTranslate.getInstance();
		int pct = Math.round(this.options.controllerDeadzone * 100.0F);
		return tr.translateKey("options.controller.deadzone") + ": " + pct + "%";
	}

	private String getInvertYString() {
		StringTranslate tr = StringTranslate.getInstance();
		return tr.translateKey("options.controller.invertY") + ": " + (this.options.controllerInvertY ? tr.translateKey("options.on") : tr.translateKey("options.off"));
	}

	private String getSwapTriggersString() {
		StringTranslate tr = StringTranslate.getInstance();
		return tr.translateKey("options.controller.swapTriggers") + ": " + (this.options.controllerSwapTriggers ? tr.translateKey("options.on") : tr.translateKey("options.off"));
	}

	protected void actionPerformed(GuiButton btn) {
		if (btn.enabled) {
			if (btn.id == 1002) {
				this.options.controllerInvertY = !this.options.controllerInvertY;
				btn.displayString = getInvertYString();
			} else if (btn.id == 1004) {
				this.options.controllerSwapTriggers = !this.options.controllerSwapTriggers;
				btn.displayString = getSwapTriggersString();
			} else if (btn.id == 200) {
				this.options.saveOptions();
				this.mc.displayGuiScreen(this.parentScreen);
			}
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 14, 16777215);

		StringTranslate tr = StringTranslate.getInstance();
		String status;
		int type = ControllerManager.getControllerType();
		if (!ControllerManager.isConnected()) {
			status = "\u00a7c" + tr.translateKey("options.controller.disconnected");
		} else if (type == ControllerManager.TYPE_XBOX) {
			status = "\u00a7a" + tr.translateKey("options.controller.connected.xbox");
		} else if (type == ControllerManager.TYPE_PLAYSTATION) {
			status = "\u00a7a" + tr.translateKey("options.controller.connected.ps");
		} else {
			status = "\u00a7a" + tr.translateKey("options.controller.connected.generic");
		}
		this.drawCenteredString(this.fontRenderer, status, this.width / 2, 26, 16777215);

		int guideY = this.height / 6 + 60;
		this.drawCenteredString(this.fontRenderer, "\u00a7e--- Controller Layout ---", this.width / 2, guideY, 16777215);

		int leftCol = this.width / 2 - 155;
		int rightCol = this.width / 2 + 15;
		int lineSpacing = 12;
		int textY = guideY + 14;

		String attackKey = this.options.controllerSwapTriggers ? "LT / L2" : "RT / R2";
		String useKey = this.options.controllerSwapTriggers ? "RT / R2" : "LT / L2";

		this.drawString(this.fontRenderer, "\u00a7f" + attackKey + ": \u00a77Attack / Mine", leftCol, textY, 16777215);
		this.drawString(this.fontRenderer, "\u00a7f" + useKey + ": \u00a77Place / Use Item", leftCol, textY + lineSpacing, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fLB / RB: \u00a77Cycle Hotbar", leftCol, textY + lineSpacing * 2, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fLeft Stick: \u00a77Move Player", leftCol, textY + lineSpacing * 3, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fRight Stick: \u00a77Look Around", leftCol, textY + lineSpacing * 4, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fD-Pad: \u00a77Hands / F5 / Fog", leftCol, textY + lineSpacing * 5, 16777215);

		this.drawString(this.fontRenderer, "\u00a7fA / Cross: \u00a77Jump", rightCol, textY, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fB / Circle: \u00a77Sneak / Crouch", rightCol, textY + lineSpacing, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fX / Square: \u00a77Open Inventory", rightCol, textY + lineSpacing * 2, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fY / Triangle: \u00a77Drop Item", rightCol, textY + lineSpacing * 3, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fStart: \u00a77Pause Game", rightCol, textY + lineSpacing * 4, 16777215);
		this.drawString(this.fontRenderer, "\u00a7fBack: \u00a77F3 Debug Screen", rightCol, textY + lineSpacing * 5, 16777215);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	public GuiScreen getParentScreen() {
		return this.parentScreen;
	}
}

