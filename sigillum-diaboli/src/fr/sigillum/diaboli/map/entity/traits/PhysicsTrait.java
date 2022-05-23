package fr.sigillum.diaboli.map.entity.traits;

import org.joml.Vector3f;

public class PhysicsTrait extends Trait {

	protected final Vector3f velocity;

	public PhysicsTrait() {
		this.velocity = new Vector3f();
	}

	@Override
	public void tick() {
		if (!getEntity().isOnGround()) {
			velocity.sub(0, 0.5f, 0);
		}

		getEntity().move(velocity.x(), velocity.y(), velocity.z());

		velocity.zero();
	}

	public void applyVelocity(float x, float z) {
		this.velocity.add(x, 0, z);
	}

	public void jump(float power) {
		this.velocity.add(0, power, 0);
	}
}
