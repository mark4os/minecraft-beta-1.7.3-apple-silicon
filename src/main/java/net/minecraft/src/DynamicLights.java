package net.minecraft.src;

import net.minecraft.client.Minecraft;

public class DynamicLights {
	private static boolean active = false;
	private static double lightX;
	private static double lightY;
	private static double lightZ;
	private static int blockLightX;
	private static int blockLightY;
	private static int blockLightZ;
	private static int currentLightLevel = 0;

	private static double lastUpdatedX;
	private static double lastUpdatedY;
	private static double lastUpdatedZ;
	private static int lastUpdatedLightLevel = 0;
	private static long lastUpdateMs = 0L;

	public static int getItemLightLevel(ItemStack stack) {
		if (stack == null) {
			return 0;
		}
		int id = stack.itemID;
		if (id == Block.torchWood.blockID) {
			return 14;
		}
		if (id == Block.glowStone.blockID || id == Block.pumpkinLantern.blockID || id == Item.bucketLava.shiftedIndex || id == Block.fire.blockID || id == Block.lavaMoving.blockID || id == Block.lavaStill.blockID) {
			return 15;
		}
		if (id == Block.torchRedstoneActive.blockID || id == Block.oreRedstoneGlowing.blockID || id == Block.oreRedstone.blockID) {
			return 9;
		}
		if (id > 0 && id < 256 && Block.lightValue[id] > 0) {
			return Block.lightValue[id];
		}
		return 0;
	}

	public static int getPlayerLightLevel(EntityPlayer player) {
		if (player == null) {
			return 0;
		}
		if (player.fire > 0) {
			return 15;
		}
		int mainLight = getItemLightLevel(player.inventory.getCurrentItem());
		int offLight = getItemLightLevel(player.inventory.getOffHandItem());
		return mainLight >= offLight ? mainLight : offLight;
	}

	public static boolean isActive() {
		return active;
	}

	public static void update(Minecraft mc) {
		if (mc == null || mc.theWorld == null || mc.thePlayer == null || mc.gameSettings == null || !mc.gameSettings.dynamicLights) {
			if (active || lastUpdatedLightLevel > 0) {
				active = false;
				currentLightLevel = 0;
				if (mc != null && mc.theWorld != null && lastUpdatedLightLevel > 0) {
					markRadiusDirty(mc.theWorld, lastUpdatedX, lastUpdatedY, lastUpdatedZ, lastUpdatedLightLevel);
				}
				lastUpdatedLightLevel = 0;
			}
			return;
		}

		int level = getPlayerLightLevel(mc.thePlayer);
		double px = mc.thePlayer.posX;
		double py = mc.thePlayer.posY;
		double pz = mc.thePlayer.posZ;

		lightX = px;
		lightY = py;
		lightZ = pz;
		blockLightX = MathHelper.floor_double(px);
		blockLightY = MathHelper.floor_double(py);
		blockLightZ = MathHelper.floor_double(pz);
		currentLightLevel = level;
		active = (level > 0);

		if (level != lastUpdatedLightLevel) {
			int r = Math.min(Math.max(level, lastUpdatedLightLevel), 9);
			if (lastUpdatedLightLevel > 0) {
				markRadiusDirty(mc.theWorld, lastUpdatedX, lastUpdatedY, lastUpdatedZ, r);
			}
			if (level > 0) {
				markRadiusDirty(mc.theWorld, px, py, pz, r);
			}
			lastUpdatedX = px;
			lastUpdatedY = py;
			lastUpdatedZ = pz;
			lastUpdatedLightLevel = level;
			lastUpdateMs = System.currentTimeMillis();
		} else if (level > 0) {
			double dx = px - lastUpdatedX;
			double dy = py - lastUpdatedY;
			double dz = pz - lastUpdatedZ;
			double distSq = dx * dx + dy * dy + dz * dz;
			long now = System.currentTimeMillis();
			if (distSq >= 0.36D && (now - lastUpdateMs) >= 80L) {
				int r = Math.min(level, 9);
				int minX = MathHelper.floor_double(Math.min(px, lastUpdatedX)) - r;
				int minY = MathHelper.floor_double(Math.min(py, lastUpdatedY)) - r;
				int minZ = MathHelper.floor_double(Math.min(pz, lastUpdatedZ)) - r;
				int maxX = MathHelper.floor_double(Math.max(px, lastUpdatedX)) + r;
				int maxY = MathHelper.floor_double(Math.max(py, lastUpdatedY)) + r;
				int maxZ = MathHelper.floor_double(Math.max(pz, lastUpdatedZ)) + r;
				mc.theWorld.markBlocksDirty(minX, minY, minZ, maxX, maxY, maxZ);
				lastUpdatedX = px;
				lastUpdatedY = py;
				lastUpdatedZ = pz;
				lastUpdateMs = now;
			}
		}
	}

	private static void markRadiusDirty(World world, double x, double y, double z, int r) {
		int bx = MathHelper.floor_double(x);
		int by = MathHelper.floor_double(y);
		int bz = MathHelper.floor_double(z);
		world.markBlocksDirty(bx - r, by - r, bz - r, bx + r, by + r, bz + r);
	}

	public static float getDynamicBrightness(int x, int y, int z, float vanillaBrightness, float[] lightBrightnessTable) {
		if (!active) {
			return vanillaBrightness;
		}
		int adx = x - blockLightX;
		if (adx < -currentLightLevel || adx > currentLightLevel) {
			return vanillaBrightness;
		}
		int ady = y - blockLightY;
		if (ady < -currentLightLevel || ady > currentLightLevel) {
			return vanillaBrightness;
		}
		int adz = z - blockLightZ;
		if (adz < -currentLightLevel || adz > currentLightLevel) {
			return vanillaBrightness;
		}

		double dx = (double) x + 0.5D - lightX;
		double dy = (double) y + 0.5D - lightY;
		double dz = (double) z + 0.5D - lightZ;
		double distSq = dx * dx + dy * dy + dz * dz;
		if (distSq >= 169.0D) {
			return vanillaBrightness;
		}
		double dist = Math.sqrt(distSq);
		float dynLevel = (float) currentLightLevel - (float) dist * 1.15F;
		if (dynLevel <= 0.0F) {
			return vanillaBrightness;
		}
		if (dynLevel >= 15.0F) {
			return lightBrightnessTable[15];
		}
		int low = (int) dynLevel;
		float frac = dynLevel - (float) low;
		float bLow = lightBrightnessTable[low];
		float bHigh = lightBrightnessTable[low + 1];
		float dynBrightness = bLow + (bHigh - bLow) * frac;
		return dynBrightness > vanillaBrightness ? dynBrightness : vanillaBrightness;
	}

	public static int getDynamicLightValue(int x, int y, int z, int vanillaLight) {
		if (!active || vanillaLight >= currentLightLevel) {
			return vanillaLight;
		}
		int adx = x - blockLightX;
		if (adx < -currentLightLevel || adx > currentLightLevel) {
			return vanillaLight;
		}
		int ady = y - blockLightY;
		if (ady < -currentLightLevel || ady > currentLightLevel) {
			return vanillaLight;
		}
		int adz = z - blockLightZ;
		if (adz < -currentLightLevel || adz > currentLightLevel) {
			return vanillaLight;
		}

		double dx = (double) x + 0.5D - lightX;
		double dy = (double) y + 0.5D - lightY;
		double dz = (double) z + 0.5D - lightZ;
		double distSq = dx * dx + dy * dy + dz * dz;
		if (distSq >= 169.0D) {
			return vanillaLight;
		}
		double dist = Math.sqrt(distSq);
		int dynInt = (int) ((float) currentLightLevel - (float) dist * 1.15F + 0.5F);
		if (dynInt > 15) {
			dynInt = 15;
		}
		return dynInt > vanillaLight ? dynInt : vanillaLight;
	}
}

