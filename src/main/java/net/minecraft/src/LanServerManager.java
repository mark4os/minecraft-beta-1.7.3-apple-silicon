package net.minecraft.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import net.minecraft.client.Minecraft;

public class LanServerManager {
	private static Process serverProcess = null;
	private static PrintWriter serverStdin = null;
	private static String lanIp = "127.0.0.1";
	private static int lanPort = 25565;
	private static File activeWorldDir = null;
	private static String hostUsername = null;
	private static ItemStack savedHostOffHand = null;
	private static boolean pendingWelcomeChat = false;

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			stopLanServer();
		}, "LAN-Server-Shutdown-Hook"));
	}

	public static boolean isLanServerRunning() {
		return serverProcess != null && serverProcess.isAlive();
	}

	public static String getLanAddress() {
		return lanIp + ":" + lanPort;
	}

	public static boolean openToLan(Minecraft mc) {
		if (mc == null || mc.theWorld == null || mc.thePlayer == null || mc.isMultiplayerWorld()) {
			return false;
		}
		stopLanServer();

		ISaveHandler handler = mc.theWorld.getSaveHandler();
		if (!(handler instanceof SaveHandler)) {
			return false;
		}
		SaveHandler saveHandler = (SaveHandler) handler;
		File worldDir = saveHandler.getSaveDirectory();
		File savesDir = worldDir.getParentFile();
		String worldFolderName = worldDir.getName();
		String worldDisplayName = mc.theWorld.getWorldInfo() != null ? mc.theWorld.getWorldInfo().getWorldName() : worldFolderName;

		if (mc.session == null || mc.session.username == null || mc.session.username.trim().isEmpty()) {
			mc.session = new Session("Player" + (System.currentTimeMillis() % 1000L), "");
		}
		hostUsername = mc.session.username;
		activeWorldDir = worldDir;
		savedHostOffHand = mc.thePlayer.inventory.getOffHandItem() != null ? mc.thePlayer.inventory.getOffHandItem().copy() : null;

		try {
			// Save host player state into world save
			File playersDir = new File(worldDir, "players");
			playersDir.mkdirs();
			NBTTagCompound playerNbt = new NBTTagCompound();
			mc.thePlayer.writeToNBT(playerNbt);
			File playerDat = new File(playersDir, hostUsername + ".dat");
			try (FileOutputStream fos = new FileOutputStream(playerDat)) {
				CompressedStreamTools.writeGzippedCompoundToOutputStream(playerNbt, fos);
			}

			// Flush singleplayer world and release region file handles before starting server
			mc.theWorld.saveWorld(true, (IProgressUpdate) null);
			SaveHandler.flushAsyncSaves();
			RegionFileCache.func_22192_a();
			mc.changeWorld1((World) null);

			// Extract embedded server jar if needed
			File serverJar = new File(savesDir, ".lan_server.jar");
			try (InputStream in = LanServerManager.class.getResourceAsStream("/server/minecraft_server.jar")) {
				if (in == null) {
					System.err.println("Missing /server/minecraft_server.jar resource!");
					return false;
				}
				try (FileOutputStream out = new FileOutputStream(serverJar)) {
					byte[] buf = new byte[8192];
					int read;
					while ((read = in.read(buf)) != -1) {
						out.write(buf, 0, read);
					}
				}
			}

			lanPort = findFreePort(25565);
			lanIp = detectLanIpv4();

			File propsFile = new File(savesDir, "server.properties");
			try (PrintWriter pw = new PrintWriter(new FileOutputStream(propsFile))) {
				pw.println("#Minecraft Beta 1.7.3 LAN Server Properties");
				pw.println("level-name=" + worldFolderName);
				pw.println("online-mode=false");
				pw.println("server-ip=");
				pw.println("server-port=" + lanPort);
				pw.println("spawn-monsters=" + (mc.gameSettings.difficulty > 0));
				pw.println("spawn-animals=true");
				pw.println("pvp=true");
				pw.println("allow-flight=true");
				pw.println("allow-nether=true");
				pw.println("max-players=20");
			}

			File opsFile = new File(savesDir, "ops.txt");
			try (PrintWriter pw = new PrintWriter(new FileOutputStream(opsFile))) {
				pw.println(hostUsername.toLowerCase());
			}

			String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			ProcessBuilder pb = new ProcessBuilder(
				javaBin,
				"-Djava.awt.headless=true",
				"-Djava.net.preferIPv4Stack=true",
				"-Xmx1024M",
				"-Xms256M",
				"-jar",
				serverJar.getAbsolutePath(),
				"nogui"
			);
			pb.directory(savesDir);
			pb.redirectErrorStream(true);
			serverProcess = pb.start();
			serverStdin = new PrintWriter(new OutputStreamWriter(serverProcess.getOutputStream()), true);

			BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
			long deadline = System.currentTimeMillis() + 15000L;
			boolean ready = false;
			while (System.currentTimeMillis() < deadline && serverProcess.isAlive()) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				System.out.println("[LAN Server] " + line);
				if (line.contains("Done (") || line.contains("For help, type")) {
					ready = true;
					break;
				}
			}

			if (!ready && !serverProcess.isAlive()) {
				return false;
			}

			// Keep draining stdout in background
			Thread logThread = new Thread(() -> {
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						System.out.println("[LAN Server] " + line);
					}
				} catch (Exception ignored) {
				}
			}, "LAN-Server-Log");
			logThread.setDaemon(true);
			logThread.start();

			// Broadcast LAN ping over UDP 224.0.2.60:4445
			final String motd = hostUsername + " - " + worldDisplayName;
			final int broadcastPort = lanPort;
			Thread broadcastThread = new Thread(() -> {
				try (DatagramSocket socket = new DatagramSocket()) {
					InetAddress group = InetAddress.getByName("224.0.2.60");
					while (isLanServerRunning()) {
						String msg = "[MOTD]" + motd + "[/MOTD][AD]" + broadcastPort + "[/AD]";
						byte[] bytes = msg.getBytes("UTF-8");
						DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, 4445);
						socket.send(packet);
						Thread.sleep(1500L);
					}
				} catch (Exception ignored) {
				}
			}, "LAN-UDP-Broadcast");
			broadcastThread.setDaemon(true);
			broadcastThread.start();

			copyToClipboard(getLanAddress());
			pendingWelcomeChat = true;

			mc.displayGuiScreen(new GuiConnecting(mc, "127.0.0.1", lanPort));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			stopLanServer();
			return false;
		}
	}

	public static void onClientTick(Minecraft mc) {
		if (pendingWelcomeChat && mc != null && mc.theWorld != null && mc.thePlayer != null && mc.isMultiplayerWorld()) {
			pendingWelcomeChat = false;
			if (savedHostOffHand != null) {
				mc.thePlayer.inventory.setOffHandItem(savedHostOffHand);
				savedHostOffHand = null;
			}
			String addr = getLanAddress();
			String vpnIp = detectVpnIpv4();
			mc.ingameGUI.addChatMessage("\u00a7a[LAN] World opened to LAN & Multiplayer!");
			mc.ingameGUI.addChatMessage("\u00a7eWi-Fi / LAN IP (copied): \u00a7b" + addr);
			if (vpnIp != null && !vpnIp.equals(lanIp)) {
				mc.ingameGUI.addChatMessage("\u00a7eVPN / Tunnel IP: \u00a7b" + vpnIp + ":" + lanPort);
			}
			mc.ingameGUI.addChatMessage("\u00a77Any official Beta 1.7.3 client (Win/Mac/Linux) can join via IP.");
			mc.ingameGUI.addChatMessage("\u00a7cNote: If friends get 'Connection reset', check macOS Firewall (System Settings -> Network -> Firewall).");
		}
	}

	public static synchronized void stopLanServer() {
		if (serverProcess != null) {
			try {
				if (serverStdin != null) {
					serverStdin.println("save-all");
					serverStdin.println("stop");
					serverStdin.flush();
				}
				serverProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
			} catch (Exception ignored) {
			} finally {
				if (serverProcess.isAlive()) {
					serverProcess.destroyForcibly();
				}
				serverProcess = null;
				serverStdin = null;
			}
		}

		// Sync player.dat back to level.dat for singleplayer
		if (activeWorldDir != null && hostUsername != null) {
			try {
				File playerDat = new File(new File(activeWorldDir, "players"), hostUsername + ".dat");
				File levelDat = new File(activeWorldDir, "level.dat");
				if (playerDat.exists() && levelDat.exists()) {
					NBTTagCompound playerNbt;
					try (FileInputStream fis = new FileInputStream(playerDat)) {
						playerNbt = CompressedStreamTools.func_1138_a(fis);
					}
					NBTTagCompound rootNbt;
					try (FileInputStream fis = new FileInputStream(levelDat)) {
						rootNbt = CompressedStreamTools.func_1138_a(fis);
					}
					NBTTagCompound dataNbt = rootNbt.getCompoundTag("Data");
					if (dataNbt != null && playerNbt != null) {
						dataNbt.setCompoundTag("Player", playerNbt);
						try (FileOutputStream fos = new FileOutputStream(levelDat)) {
							CompressedStreamTools.writeGzippedCompoundToOutputStream(rootNbt, fos);
						}
					}
				}
				RegionFileCache.func_22192_a();
			} catch (Exception e) {
				e.printStackTrace();
			}
			activeWorldDir = null;
		}
	}

	public static void copyToClipboard(String text) {
		try {
			Process p = new ProcessBuilder("pbcopy").start();
			try (OutputStream os = p.getOutputStream()) {
				os.write(text.getBytes("UTF-8"));
			}
			p.waitFor();
		} catch (Exception ignored) {
		}
	}

	private static int findFreePort(int preferred) {
		for (int port = preferred; port < preferred + 20; ++port) {
			try (ServerSocket ss = new ServerSocket(port)) {
				ss.setReuseAddress(true);
				return port;
			} catch (Exception ignored) {
			}
		}
		return preferred;
	}

	public static String detectLanIpv4() {
		String fallback = "127.0.0.1";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface nif = interfaces.nextElement();
				if (nif.isLoopback() || !nif.isUp()) {
					continue;
				}
				String name = nif.getName().toLowerCase();
				if (name.startsWith("utun") || name.startsWith("awdl") || name.startsWith("llw") || name.startsWith("bridge")) {
					continue;
				}
				Enumeration<InetAddress> addrs = nif.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
						String ip = addr.getHostAddress();
						if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
							return ip;
						}
						if (addr.isSiteLocalAddress() || fallback.equals("127.0.0.1")) {
							fallback = ip;
						}
					}
				}
			}
		} catch (Exception ignored) {
		}
		return fallback;
	}

	public static String detectVpnIpv4() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface nif = interfaces.nextElement();
				if (nif.isLoopback() || !nif.isUp()) {
					continue;
				}
				String name = nif.getName().toLowerCase();
				if (name.startsWith("utun") || name.startsWith("tun") || name.startsWith("tap") || name.startsWith("zt") || name.startsWith("tailscale")) {
					Enumeration<InetAddress> addrs = nif.getInetAddresses();
					while (addrs.hasMoreElements()) {
						InetAddress addr = addrs.nextElement();
						if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
							String ip = addr.getHostAddress();
							if (!ip.startsWith("169.254.")) {
								return ip;
							}
						}
					}
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}
}

