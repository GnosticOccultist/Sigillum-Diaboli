package fr.sigillum.diaboli.map.entity.traits.render;

import org.joml.Vector3fc;

import fr.sigillum.diaboli.graphics.Light;
import fr.sigillum.diaboli.graphics.gl.ShaderProgram;
import fr.sigillum.diaboli.map.entity.traits.TransformTrait;

public class LightTrait extends ShaderTrait {

	private final Light light;

	private volatile int index = -1;

	public LightTrait() {
		super(TransformTrait.class);
		this.light = new Light();
	}
	
	public LightTrait(float x, float y, float z) {
		super(TransformTrait.class);
		this.light = new Light(x, y, z);
	}

	@Override
	public void tick() {
		
	}

	@Override
	public void upload(ShaderProgram program) {
		if (index == -1) {
			return;
		}

		program.uniformBool("lights[" + index + "].enabled", true);
		program.uniformVec3("lights[" + index + "].position", getPosition());
		program.uniformVec3("lights[" + index + "].color", getColor());
		program.uniformVec3("lights[" + index + "].attenuation", getAttenuation());
		program.uniformFloat("lights[" + index + "].intensity", 1.0f);
		program.uniformFloat("lights[" + index + "].range", 10.0f);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
		dirty();
	}

	public Vector3fc getPosition() {
		return light.getPosition();
	}

	public LightTrait setPosition(float x, float y, float z) {
		this.light.setPosition(x, y, z);
		dirty();
		return this;
	}

	private LightTrait updatePosition(float x, float y, float z) {
		setPosition(light.getPosition().x() + x, light.getPosition().y() + y, light.getPosition().z() + z);
		return this;
	}

	public Vector3fc getColor() {
		return light.getColor();
	}

	public LightTrait setColor(float r, float g, float b) {
		this.light.setColor(r, g, b);
		dirty();
		return this;
	}

	public Vector3fc getAttenuation() {
		return light.getAttenuation();
	}

	public LightTrait setAttenuation(float constant, float linear, float quadratic) {
		this.light.setAttenuation(constant, linear, quadratic);
		dirty();
		return this;
	}
}
