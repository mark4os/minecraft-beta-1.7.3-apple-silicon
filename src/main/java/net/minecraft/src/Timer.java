package net.minecraft.src;

public class Timer {
	public float ticksPerSecond;
	private long lastTimeNano;
	public int elapsedTicks;
	public float renderPartialTicks;
	public float timerSpeed = 1.0F;
	public float elapsedPartialTicks = 0.0F;

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
		this.elapsedPartialTicks += (float)deltaTicks;

		this.elapsedTicks = (int)this.elapsedPartialTicks;

		if (this.elapsedTicks > 10) {
			this.elapsedTicks = 10;
			this.elapsedPartialTicks = 0.0F;
		} else {
			this.elapsedPartialTicks -= (float)this.elapsedTicks;
		}

		this.renderPartialTicks = this.elapsedPartialTicks;
	}
}
