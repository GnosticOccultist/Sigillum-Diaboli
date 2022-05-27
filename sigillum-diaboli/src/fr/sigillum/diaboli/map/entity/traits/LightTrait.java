package fr.sigillum.diaboli.map.entity.traits;

import org.joml.Vector3fc;

import fr.sigillum.diaboli.graphics.Light;

public class LightTrait extends Trait {

	private final Light light;

	public LightTrait() {
		super(TransformTrait.class);
		this.light = new Light();
	}

	@Override
	public void tick() {
		var translation = getEntity().requireTrait(TransformTrait.class).getTranslation();
		this.light.setPosition(translation.x(), translation.y() + 2, translation.z());
	}

	public Vector3fc getPosition() {
		return light.getPosition();
	}

	public LightTrait setPosition(float x, float y, float z) {
		this.light.setPosition(x, y, z);
		return this;
	}

	public Vector3fc getColor() {
		return light.getColor();
	}

	public LightTrait setColor(float r, float g, float b) {
		this.light.setColor(r, g, b);
		return this;
	}

	public Vector3fc getAttenuation() {
		return light.getAttenuation();
	}

	public LightTrait setAttenuation(float constant, float linear, float quadratic) {
		this.light.setAttenuation(constant, linear, quadratic);
		return this;
	}
}
