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
			passedNano = 1000000000L; // protect against huge time jumps on pause/freeze
		}

		this.elapsedPartialTicks += (float)((double)passedNano / (1000000000.0D / (double)this.ticksPerSecond) * (double)this.timerSpeed);
		this.elapsedTicks = (int)this.elapsedPartialTicks;
		this.elapsedPartialTicks -= (float)this.elapsedTicks;

		if (this.elapsedTicks > 10) {
			this.elapsedTicks = 10;
		}

		this.renderPartialTicks = this.elapsedPartialTicks;
	}
}
