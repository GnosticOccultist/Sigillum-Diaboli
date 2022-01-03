package fr.sigillum.diaboli.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.Window;
import fr.sigillum.diaboli.map.Region;

public class SigillumDiaboli {

	public static void main(String[] args) {
		start(new SigillumDiaboli());
	}

	private Window window;

	private Drawer drawer;

	private Region region;

	private volatile boolean running = false;

	public static void start(SigillumDiaboli game) {
		try {
			game.running = true;
			game.initialize();

			while (game.running) {
				game.update();

			}
		} finally {
			game.shutdown();
		}
	}

	private void initialize() {
		this.window = Window.create(this, "Sigillum-Diaboli", 1280, 720);
		this.drawer = new Drawer(32 * 32);
		this.drawer.begin();

		GL11C.glViewport(0, 0, window.getWidth(), window.getHeight());
		drawer.projectionMatrix(window.getWidth(), window.getHeight());
		drawer.viewMatrix(new Vector3f(-2, 5, -2), new Vector2f(16, 137));

		this.drawer.end();

		this.region = new Region(0, 0);
	}

	private void update() {
		if (window.shouldClose()) {
			exit();
			return;
		}

		render();

		window.flush();
	}

	private void render() {
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
		drawer.begin();

		region.render(drawer);

		drawer.end();
	}

	public void exit() {
		this.running = false;
	}

	private void shutdown() {
		drawer.cleanup();
		window.destroy();
	}
}
