package fr.sigillum.diaboli.map.entity;

import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import fr.sigillum.diaboli.input.Input;
import fr.sigillum.diaboli.map.entity.traits.PhysicsTrait;

public class Player extends Entity {

	private final Input input;
	
	public Player(Input input, float x, float y, float z) {
		super(UUID.randomUUID(), x, y, z);
		this.input = input;
		
		addTrait(new PhysicsTrait());
	}

	public Player(UUID id, Input input, float x, float y, float z) {
		super(id, x, y, z);
		this.input = input;
		
		addTrait(new PhysicsTrait());
	}

	@Override
	public void tick() {
		rotation.add(input.getDelta().x() * 0.1f, input.getDelta().y() * 0.1f);
		
		var physics = getTrait(PhysicsTrait.class).get();

		if (input.isKeyDown(GLFW.GLFW_KEY_W)) {
			physics.applyVelocity((float) (0.2f * Math.sin(Math.toRadians(rotation.y))),
					(float) (-0.2f * Math.cos(Math.toRadians(rotation.y))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_S)) {
			physics.applyVelocity((float) (-0.2f * Math.sin(Math.toRadians(rotation.y))),
					(float) (0.2f * Math.cos(Math.toRadians(rotation.y))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_A)) {
			physics.applyVelocity((float) (0.2f * Math.sin(Math.toRadians(rotation.y - 90))),
					(float) (-0.2f * Math.cos(Math.toRadians(rotation.y - 90))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_D)) {
			physics.applyVelocity((float) (-0.2f * Math.sin(Math.toRadians(rotation.y - 90))),
					(float) (0.2f * Math.cos(Math.toRadians(rotation.y - 90))));
		}
		if (input.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
			physics.jump(0.2F);
		}
		
		super.tick();
	}
}
