package net.minecraft.src;

public class Timer {
	public float ticksPerSecond;
	private long lastTimeNano;
	public int elapsedTicks;
	public float renderPartialTicks;
	public float timerSpeed = 1.0F;
	public float elapsedPartialTicks = 0.0F;
	private double elapsedPartialTicksD = 0.0D;

	public Timer(float tps) {
		this.ticksPerSecond = tps;
		this.lastTimeNano = System.nanoTime();
	}

	public void updateTimer() {
		long now = System.nanoTime();
		long passedNano = now - this.lastTimeNano;
		this.lastTimeNano = now;

		if (passedNano < 0L) {
			passedNano = 0L;
		}
		if (passedNano > 1000000000L) {
			passedNano = 1000000000L;
		}

		double deltaTicks;
		boolean vsyncLocked = false;
		if (this.timerSpeed == 1.0F && this.ticksPerSecond == 20.0F) {
			if (passedNano >= 12000000L && passedNano <= 22000000L) {
				deltaTicks = 1.0D / 3.0D;
				vsyncLocked = true;
			} else if (passedNano >= 7000000L && passedNano <= 10500000L) {
				deltaTicks = 1.0D / 6.0D;
				vsyncLocked = true;
			} else if (passedNano >= 28000000L && passedNano <= 38500000L) {
				deltaTicks = 2.0D / 3.0D;
				vsyncLocked = true;
			} else if (passedNano >= 45000000L && passedNano <= 55000000L) {
				deltaTicks = 1.0D;
				vsyncLocked = true;
			} else {
				deltaTicks = (double)passedNano / (1000000000.0D / (double)this.ticksPerSecond) * (double)this.timerSpeed;
			}
		} else {
			deltaTicks = (double)passedNano / (1000000000.0D / (double)this.ticksPerSecond) * (double)this.timerSpeed;
		}

		this.elapsedPartialTicksD += deltaTicks;
		if (vsyncLocked) {
			this.elapsedPartialTicksD = (double)Math.round(this.elapsedPartialTicksD * 6.0D) / 6.0D;
		}

		this.elapsedTicks = (int)(this.elapsedPartialTicksD + 1.0E-4D);

		if (this.elapsedTicks > 10) {
			this.elapsedTicks = 10;
			this.elapsedPartialTicksD = 0.0D;
		} else {
			this.elapsedPartialTicksD -= (double)this.elapsedTicks;
			if (this.elapsedPartialTicksD < 0.0D) {
				this.elapsedPartialTicksD = 0.0D;
			}
		}

		this.elapsedPartialTicks = (float)this.elapsedPartialTicksD;
		this.renderPartialTicks = this.elapsedPartialTicks;
	}
}
