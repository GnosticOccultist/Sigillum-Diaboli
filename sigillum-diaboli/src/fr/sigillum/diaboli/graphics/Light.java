package fr.sigillum.diaboli.graphics;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Light {

	private final Vector3f position;

	private final Vector3f color;

	private final Vector3f attenuation;

	public Light() {
		this(0, 0, 0);
	}

	public Light(float x, float y, float z) {
		this.position = new Vector3f(x, y, z);
		this.color = new Vector3f();
		this.attenuation = new Vector3f(1, 0, 0);
	}

	public Vector3fc getPosition() {
		return position;
	}

	public Light setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
		return this;
	}

	public Vector3fc getColor() {
		return color;
	}

	public Light setColor(float r, float g, float b) {
		this.color.set(r, g, b);
		return this;
	}

	public Vector3fc getAttenuation() {
		return attenuation;
	}

	public Light setAttenuation(float constant, float linear, float quadratic) {
		this.attenuation.set(constant, linear, quadratic);
		return this;
	}
}
