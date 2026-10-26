package net.minecraft.src;

public class ClippingHelper {
	public float[][] frustum = new float[16][16];
	public float[] projectionMatrix = new float[16];
	public float[] modelviewMatrix = new float[16];
	public float[] clippingMatrix = new float[16];

	public boolean isBoxInFrustum(double var1, double var3, double var5, double var7, double var9, double var11) {
		for(int var13 = 0; var13 < 6; ++var13) {
			float[] plane = this.frustum[var13];
			double f0 = (double)plane[0];
			double f1 = (double)plane[1];
			double f2 = (double)plane[2];
			double f3 = (double)plane[3];
			double x1 = f0 * var1;
			double x2 = f0 * var7;
			double y1 = f1 * var3;
			double y2 = f1 * var9;
			double z1 = f2 * var5 + f3;
			double z2 = f2 * var11 + f3;
			if(x1 + y1 + z1 <= 0.0D && x2 + y1 + z1 <= 0.0D && x1 + y2 + z1 <= 0.0D && x2 + y2 + z1 <= 0.0D && x1 + y1 + z2 <= 0.0D && x2 + y1 + z2 <= 0.0D && x1 + y2 + z2 <= 0.0D && x2 + y2 + z2 <= 0.0D) {
				return false;
			}
		}

		return true;
	}
}
