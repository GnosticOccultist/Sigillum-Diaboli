package fr.sigillum.diaboli.game;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.Window;
import fr.sigillum.diaboli.map.Region;

public class SigillumDiaboli extends AbstractGame {

	public static void main(String[] args) {
		start(new SigillumDiaboli());
	}

	private Window window;

	private Drawer drawer;

	@Override
	protected void initialize() {
		this.window = Window.create(this, "Sigillum-Diaboli", 1280, 720);
		this.drawer = new Drawer(32 * 32);

		resize(window.getWidth(), window.getHeight());
		drawer.viewMatrix(new Vector3f(-2, 5, -2), new Vector2f(16, 137));

		this.region = new Region(0, 0);
	}

	@Override
	protected void update() {
		if (window.shouldClose()) {
			exit();
			return;
		}

		super.update();

		render();

		window.flush();
	}

	protected void render() {
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
		drawer.begin();

		region.render(drawer);

		drawer.end();
	}

	public void resize(int width, int height) {
		GL11C.glViewport(0, 0, width, height);
		drawer.projectionMatrix(width, height);
	}

	@Override
	protected void shutdown() {
		logger.info("Disposing active resources...");

		drawer.cleanup();

		window.destroy();

		logger.info("Succesfully disposed of resources.");
	}
}
