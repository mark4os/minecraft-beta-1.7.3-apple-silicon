package net.minecraft.src;

public class NibbleArray {
	public final byte[] data;

	public NibbleArray(int var1) {
		this.data = new byte[var1 >> 1];
	}

	public NibbleArray(byte[] var1) {
		this.data = var1;
	}

	public int getNibble(int var1, int var2, int var3) {
		int var4 = var1 << 11 | var3 << 7 | var2;
		return (this.data[var4 >> 1] >>> ((var2 & 1) << 2)) & 15;
	}

	public void setNibble(int var1, int var2, int var3, int var4) {
		int var5 = var1 << 11 | var3 << 7 | var2;
		int var6 = var5 >> 1;
		int shift = (var2 & 1) << 2;
		this.data[var6] = (byte)(this.data[var6] & ~(15 << shift) | (var4 & 15) << shift);
	}

	public boolean isValid() {
		return this.data != null;
	}
}
