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

		double deltaTicks = (double)passedNano / (1000000000.0D / (double)this.ticksPerSecond) * (double)this.timerSpeed;
		this.elapsedPartialTicksD += deltaTicks;

		this.elapsedTicks = (int)this.elapsedPartialTicksD;
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
