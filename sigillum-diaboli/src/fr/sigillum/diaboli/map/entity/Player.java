package fr.sigillum.diaboli.map.entity;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import fr.sigillum.diaboli.input.Input;

public class Player extends Entity {

	private final Input input;

	public Player(UUID id, Input input, float x, float y, float z) {
		super(id, x, y, z);
		this.input = input;
	}

	@Override
	public void tick() {
		rotation.add(input.getDelta().x() * 0.1f, input.getDelta().y() * 0.1f);

		if (input.isKeyDown(GLFW.GLFW_KEY_W)) {
			velocity.add((float) (0.2f * Math.sin(Math.toRadians(rotation.y))), 0,
					(float) (-0.2f * Math.cos(Math.toRadians(rotation.y))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_S)) {
			velocity.add((float) (-0.2f * Math.sin(Math.toRadians(rotation.y))), 0,
					(float) (0.2f * Math.cos(Math.toRadians(rotation.y))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_A)) {
			velocity.add((float) (0.2f * Math.sin(Math.toRadians(rotation.y - 90))), 0,
					(float) (-0.2f * Math.cos(Math.toRadians(rotation.y - 90))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_D)) {
			velocity.add((float) (-0.2f * Math.sin(Math.toRadians(rotation.y - 90))), 0,
					(float) (0.2f * Math.cos(Math.toRadians(rotation.y - 90))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
			velocity.add(0, 0.2F, 0);
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
			velocity.add(0, -0.2F, 0);
		}

		move(velocity.x(), velocity.y(), velocity.z());
		this.velocity.set(0, 0, 0);
	}
}
