package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class SaveHandler implements ISaveHandler {
	private static final Logger logger = Logger.getLogger("Minecraft");
	private final File saveDirectory;
	private final File playersDirectory;
	private final File field_28114_d;
	private final long now = System.currentTimeMillis();
	private long lastLockCheckTime = 0L;

	public SaveHandler(File var1, String var2, boolean var3) {
		this.saveDirectory = new File(var1, var2);
		this.saveDirectory.mkdirs();
		this.playersDirectory = new File(this.saveDirectory, "players");
		this.field_28114_d = new File(this.saveDirectory, "data");
		this.field_28114_d.mkdirs();
		if(var3) {
			this.playersDirectory.mkdirs();
		}

		this.func_22154_d();
	}

	private void func_22154_d() {
		try {
			File var1 = new File(this.saveDirectory, "session.lock");
			DataOutputStream var2 = new DataOutputStream(new FileOutputStream(var1));

			try {
				var2.writeLong(this.now);
			} finally {
				var2.close();
			}

		} catch (IOException var7) {
			var7.printStackTrace();
			throw new RuntimeException("Failed to check session lock, aborting");
		}
	}

	public File getSaveDirectory() {
		return this.saveDirectory;
	}

	public void func_22150_b() {
		long currentTime = System.currentTimeMillis();
		if(currentTime - this.lastLockCheckTime < 5000L) {
			return;
		}
		this.lastLockCheckTime = currentTime;
		try {
			File var1 = new File(this.saveDirectory, "session.lock");
			DataInputStream var2 = new DataInputStream(new FileInputStream(var1));

			try {
				if(var2.readLong() != this.now) {
					throw new MinecraftException("The save is being accessed from another location, aborting");
				}
			} finally {
				var2.close();
			}

		} catch (IOException var7) {
			throw new MinecraftException("Failed to check session lock, aborting");
		}
	}

	public IChunkLoader getChunkLoader(WorldProvider var1) {
		if(var1 instanceof WorldProviderHell) {
			File var2 = new File(this.saveDirectory, "DIM-1");
			var2.mkdirs();
			return new ChunkLoader(var2, true);
		} else {
			return new ChunkLoader(this.saveDirectory, true);
		}
	}

	public WorldInfo loadWorldInfo() {
		File var1 = new File(this.saveDirectory, "level.dat");
		NBTTagCompound var2;
		NBTTagCompound var3;
		if(var1.exists()) {
			try {
				var2 = CompressedStreamTools.func_1138_a(new FileInputStream(var1));
				var3 = var2.getCompoundTag("Data");
				return new WorldInfo(var3);
			} catch (Exception var5) {
				var5.printStackTrace();
			}
		}

		var1 = new File(this.saveDirectory, "level.dat_old");
		if(var1.exists()) {
			try {
				var2 = CompressedStreamTools.func_1138_a(new FileInputStream(var1));
				var3 = var2.getCompoundTag("Data");
				return new WorldInfo(var3);
			} catch (Exception var4) {
				var4.printStackTrace();
			}
		}

		return null;
	}

	private static final java.util.concurrent.ExecutorService saveExecutor = java.util.concurrent.Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "Async-LevelDat-Saver");
			t.setDaemon(true);
			return t;
		}
	});

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				saveExecutor.shutdown();
				try {
					saveExecutor.awaitTermination(3L, java.util.concurrent.TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
			}
		}, "SaveHandler-Flush"));
	}

	public static void flushAsyncSaves() {
		try {
			saveExecutor.submit(new Runnable() {
				public void run() {
				}
			}).get(3L, java.util.concurrent.TimeUnit.SECONDS);
		} catch (Exception ignored) {
		}
	}

	private void writeLevelDatInternal(final NBTTagCompound rootTag) {
		final File dir = this.saveDirectory;
		saveExecutor.submit(new Runnable() {
			public void run() {
				try {
					File var5 = new File(dir, "level.dat_new");
					File var6 = new File(dir, "level.dat_old");
					File var7 = new File(dir, "level.dat");
					CompressedStreamTools.writeGzippedCompoundToOutputStream(rootTag, new FileOutputStream(var5));
					if(var6.exists()) {
						var6.delete();
					}

					var7.renameTo(var6);
					if(var7.exists()) {
						var7.delete();
					}

					var5.renameTo(var7);
					if(var5.exists()) {
						var5.delete();
					}
				} catch (Exception var8) {
					var8.printStackTrace();
				}
			}
		});
	}

	public void saveWorldInfoAndPlayer(WorldInfo var1, List var2) {
		NBTTagCompound var3 = var1.getNBTTagCompoundWithPlayer(var2);
		NBTTagCompound var4 = new NBTTagCompound();
		var4.setTag("Data", var3);
		this.writeLevelDatInternal(var4);
	}

	public void saveWorldInfo(WorldInfo var1) {
		NBTTagCompound var2 = var1.getNBTTagCompound();
		NBTTagCompound var3 = new NBTTagCompound();
		var3.setTag("Data", var2);
		this.writeLevelDatInternal(var3);
	}

	public File func_28113_a(String var1) {
		return new File(this.field_28114_d, var1 + ".dat");
	}
}
