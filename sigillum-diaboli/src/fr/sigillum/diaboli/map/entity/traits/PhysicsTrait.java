package fr.sigillum.diaboli.map.entity.traits;

import org.joml.Vector3f;

public class PhysicsTrait extends Trait {

	protected final Vector3f velocity;

	public PhysicsTrait() {
		super(TransformTrait.class);
		this.velocity = new Vector3f();
	}

	@Override
	public void tick() {
		if (!isOnGround()) {
			velocity.sub(0, 0.5f, 0);
		}

		getEntity().requireTrait(TransformTrait.class)
				.translate(velocity.x(), velocity.y(), velocity.z());

		velocity.zero();
	}

	private boolean isOnGround() {
		return getEntity().requireTrait(TransformTrait.class).getTranslation().y() <= 1.85f;
	}

	public void applyVelocity(float x, float z) {
		this.velocity.add(x, 0, z);
	}

	public void jump(float power) {
		this.velocity.add(0, power, 0);
	}
}
