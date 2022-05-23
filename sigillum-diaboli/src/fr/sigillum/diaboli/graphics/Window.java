package fr.sigillum.diaboli.graphics;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_ANY_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetError;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.game.SigillumDiaboli;

public final class Window {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.graphics");

	public static Window create(SigillumDiaboli game, String title, int width, int height) {
		var window = new Window(game, width, height);
		window.create(title);
		return window;
	}

	private long handle;

	private int width, height;

	private final SigillumDiaboli game;

	private Window(SigillumDiaboli game, int width, int height) {
		this.game = game;
		this.width = width;
		this.height = height;
	}

	private void create(String title) {
		logger.info("Initializing GLFW...");

		GLFWErrorCallback.create((error, desc) -> {
			var message = GLFWErrorCallback.getDescription(desc);
			logger.error("An error occured with GLFW! (code= " + error + "): " + message);
		});

		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW!");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11C.GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL11C.GL_TRUE);
		glfwWindowHint(GLFW_SAMPLES, 4);

		logger.info("Creating window with dimensions " + width + "x" + height);

		this.handle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
		if (handle == MemoryUtil.NULL) {
			int err = glfwGetError(PointerBuffer.allocateDirect(1));
			destroy();
			throw new RuntimeException("Failed to create the GLFW window (code = " + err + ")");
		}

		glfwSetWindowSizeCallback(handle, (handle, w, h) -> {
			this.width = w;
			this.height = h;
		});

		glfwSetFramebufferSizeCallback(handle, (handle, w, h) -> {
			game.resize(w, h);
		});

		// Getting the resolution of the primary monitor.
		var videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		var x = (videoMode.width() - width) / 2;
		var y = (videoMode.height() - height) / 2;
		glfwSetWindowPos(handle, x, y);

		glfwMakeContextCurrent(handle);
		GL.createCapabilities();

		restoreState();

		logger.info("Successfully initialized graphics context!");

		show();
	}

	public void show() {
		glfwShowWindow(handle);
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(handle);
	}

	public void flush() {
		glfwSwapBuffers(handle);
		glfwPollEvents();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public long getHandle() {
		return handle;
	}

	public void restoreState() {
		GL11C.glEnable(GL11C.GL_DEPTH_TEST);
		GL11C.glEnable(GL11C.GL_STENCIL_TEST);
		GL11C.glEnable(GL11C.GL_CULL_FACE);
		GL11C.glCullFace(GL11C.GL_BACK);
	}

	public void destroy() {
		if (handle != MemoryUtil.NULL) {
			glfwDestroyWindow(handle);
			Callbacks.glfwFreeCallbacks(handle);
		}

		glfwTerminate();
	}
}
