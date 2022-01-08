package fr.sigillum.diaboli.game;

import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.map.World;

public class AbstractGame {

	protected static final Logger logger = FactoryLogger.getLogger("horror-game.game");

	private static final int INTERVAL = 1000 / 40;

	protected volatile World world = null;

	private volatile boolean running = false;

	private volatile int fps, tps;
	
	private volatile int ticks, frames = Integer.MAX_VALUE - 12;

	public static void start(AbstractGame game) {
		try {
			game.running = true;
			game.initialize();

			// Variables for statistics.
			long lastPollTime = 0;
			int tps = 0;
			int fps = 0;

			int delta = 0;
			long last = System.currentTimeMillis();

			while (game.isRunning()) {
				var now = System.currentTimeMillis();

				delta += (now - last);
				last = now;

				if (delta >= INTERVAL) {
					// Compute the amount of updates we're allowed to perform in the interval.
					long updates = delta / INTERVAL;
					for (var i = 0; i < updates; ++i) {
						game.internalTick();
						tps++;

						delta -= INTERVAL;
					}
				}

				game.update();
				fps++;
				game.frames++;

				// Update statistics every seconds.
				if (now - lastPollTime >= 1000) {
					game.tps = tps;
					game.fps = fps;

					tps = 0;
					fps = 0;
					lastPollTime = now;
				}

				FileUtils.safePerform(1, Thread::sleep);
			}
		} catch (Throwable error) {
			logger.error("A fatal error has occured!", error);
			throw error;
		} finally {
			try {
				game.shutdown();
			} catch (Exception ex) {
				logger.error("An error occured while shutting down the game gracefully!", ex);
			}
		}
	}

	protected void initialize() {

	}

	private void internalTick() {
		this.ticks++;
		
		tick();
	}

	protected void tick() {
		if (world != null) {
			world.tick();
		}
	}

	protected void update() {

	}

	protected void shutdown() {
		logger.info("Shutting down the game...");
	}

	public void exit() {
		this.running = false;
	}

	protected boolean isRunning() {
		return running;
	}
	
	public int getFrames() {
		return frames;
	}
	
	public int getTicks() {
		return ticks;
	}

	public int getFps() {
		return fps;
	}

	public int getTps() {
		return tps;
	}
}
