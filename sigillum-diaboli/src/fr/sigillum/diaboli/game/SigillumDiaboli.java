package fr.sigillum.diaboli.game;

import org.lwjgl.opengl.GL11C;

import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.Window;

public class SigillumDiaboli {

	public static void main(String[] args) {
		start(new SigillumDiaboli());
	}

	private Window window;

	private Drawer drawer;
	
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
		this.drawer = new Drawer(6);
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
		
		drawer.drawRectangle(-0.5F, 0.5F, 0.75F, 0);
		
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
