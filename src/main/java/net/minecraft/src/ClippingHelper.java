package net.minecraft.src;

public class ClippingHelper {
	public float[][] frustum = new float[16][16];
	public float[] projectionMatrix = new float[16];
	public float[] modelviewMatrix = new float[16];
	public float[] clippingMatrix = new float[16];

	public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		for(int i = 0; i < 6; ++i) {
			float[] plane = this.frustum[i];
			double f0 = (double)plane[0];
			double f1 = (double)plane[1];
			double f2 = (double)plane[2];
			double f3 = (double)plane[3];

			double px = f0 > 0.0D ? maxX : minX;
			double py = f1 > 0.0D ? maxY : minY;
			double pz = f2 > 0.0D ? maxZ : minZ;

			if(f0 * px + f1 * py + f2 * pz + f3 <= 0.0D) {
				return false;
			}
		}

		return true;
	}
}
