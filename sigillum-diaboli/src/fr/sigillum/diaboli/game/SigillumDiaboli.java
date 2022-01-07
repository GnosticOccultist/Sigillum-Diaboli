package fr.sigillum.diaboli.game;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.Window;
import fr.sigillum.diaboli.input.Input;
import fr.sigillum.diaboli.map.Region;
import fr.sigillum.diaboli.map.entity.Player;

public class SigillumDiaboli extends AbstractGame {

	public static void main(String[] args) {
		start(new SigillumDiaboli());
	}

	private Window window;

	private Input input;

	private Drawer drawer;

	@Override
	protected void initialize() {
		this.window = Window.create(this, "Sigillum-Diaboli", 1280, 720);
		this.input = new Input(window);
		this.input.grab();
		this.drawer = new Drawer(32 * 32 * 3);

		resize(window.getWidth(), window.getHeight());

		var player = new Player(UUID.randomUUID(), input, 0, 0, 0);

		this.region = new Region(0, 0);
		this.region.addPlayer(player);
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

	@Override
	protected void tick() {
		input.update();

		if (input.isGrabbed()) {
			if (input.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
				input.grab();
			}

			super.tick();

		} else if (input.isLeftButtonPressed()) {
			input.grab();
		}
	}

	protected void render() {
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
		drawer.begin();

		var player = region.getPlayer();
		if (player != null) {
			drawer.viewMatrix(player.getPosition(), player.getRotation());
		}

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
