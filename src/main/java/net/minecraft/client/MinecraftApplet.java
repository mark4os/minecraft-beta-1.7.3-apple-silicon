package net.minecraft.client;

/**
 * Legacy stub for MinecraftApplet to support modern Java (17/21/27+)
 * where java.applet.Applet has been permanently removed from the JDK.
 */
public class MinecraftApplet {
	public void start() {}
	public void stop() {}
	public void destroy() {}
	public void clearApplet() {}
	public boolean isActive() {
		return false;
	}
	public String getParameter(String name) {
		return null;
	}
}
