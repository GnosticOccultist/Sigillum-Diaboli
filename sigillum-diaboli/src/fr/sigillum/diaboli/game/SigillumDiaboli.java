package fr.sigillum.diaboli.game;

import fr.sigillum.diaboli.graphics.Window;

public class SigillumDiaboli {

	public static void main(String[] args) {
		start(new SigillumDiaboli());
	}

	private Window window;

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
		this.window = Window.create(this, "HorrorGame", 1280, 720);
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

	}

	public void exit() {
		this.running = false;
	}

	private void shutdown() {
		window.destroy();
	}
}
