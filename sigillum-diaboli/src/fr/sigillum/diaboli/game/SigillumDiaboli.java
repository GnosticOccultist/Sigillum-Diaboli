package fr.sigillum.diaboli.game;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;

import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.Window;
import fr.sigillum.diaboli.input.Input;
import fr.sigillum.diaboli.map.World;
import fr.sigillum.diaboli.map.entity.Entity;
import fr.sigillum.diaboli.map.entity.Player;
import fr.sigillum.diaboli.map.entity.traits.LightTrait;
import fr.sigillum.diaboli.map.entity.traits.ModelTrait;
import fr.sigillum.diaboli.map.entity.traits.SpriteTrait;
import fr.sigillum.diaboli.map.entity.traits.TransformTrait;

public class SigillumDiaboli extends AbstractGame {

	public static void main(String[] args) {
		start(new SigillumDiaboli());
	}

	private Window window;

	private Input input;

	private Drawer drawer;

	private Player player;

	private Entity house;

	@Override
	protected void initialize() {
		this.window = Window.create(this, "Sigillum-Diaboli", 1280, 720);
		this.input = new Input(window);
		this.input.grab();

		Assets.initialize();

		this.drawer = new Drawer(32 * 32 * 3);
		this.drawer.projectionMatrix(window.getWidth(), window.getHeight());

		player = new Player(input, 0, 10, 0);

		this.world = new World();
		this.world.add(player);

		var monkNpc = new Entity(UUID.randomUUID());
		monkNpc.addTrait(new TransformTrait());
		monkNpc.addTrait(new SpriteTrait("monk"));
		world.add(monkNpc);
		
		house = new Entity(UUID.randomUUID());
		house.addTrait(new TransformTrait());
		var light = new LightTrait();
		light.setColor(0.75f, 0.0f, 0.0f);
		monkNpc.addTrait(light);
		house.requireTrait(TransformTrait.class).scale(1.5f);
		house.addTrait(new ModelTrait("small_medieval_house"));
		world.add(house);
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

		if (player != null) {
			var transform = player.requireTrait(TransformTrait.class);
			drawer.viewMatrix(transform.getTranslation(), transform.getRotation());
		}

		world.render(drawer, player);

		house.requireTrait(ModelTrait.class).render(drawer, player);
	}

	public void resize(int width, int height) {
		GL11C.glViewport(0, 0, width, height);
		drawer.projectionMatrix(width, height);
	}

	@Override
	protected void shutdown() {
		logger.info("Disposing active resources...");

		drawer.dispose();

		Assets.get().dispose();

		window.destroy();

		logger.info("Succesfully disposed of resources.");
	}
}
