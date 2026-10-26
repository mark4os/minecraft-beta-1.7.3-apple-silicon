package org.lwjgl.opengl;

public class GLContext {
	private static ContextCapabilities contextCapabilities;

	public static GLContext createFromCurrent() {
		contextCapabilities = new ContextCapabilities();
		return new GLContext();
	}

	public static ContextCapabilities getCapabilities() {
		if (contextCapabilities == null) {
			contextCapabilities = new ContextCapabilities();
		}
		return contextCapabilities;
	}
}
