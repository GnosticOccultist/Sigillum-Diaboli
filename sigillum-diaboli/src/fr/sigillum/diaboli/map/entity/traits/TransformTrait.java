package fr.sigillum.diaboli.map.entity.traits;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class TransformTrait extends Trait {

	/**
	 * The translation vector of the transform.
	 */
	private final Vector3f translation;
	/**
	 * The rotation quaternion of the transform.
	 */
	private final Quaternionf rotation;
	/**
	 * The scale vector of the transform.
	 */
	private final Vector3f scale;
	
	public TransformTrait() {
		this.translation = new Vector3f();
		this.rotation = new Quaternionf();
		this.scale = new Vector3f();
	}
	
	public TransformTrait(float x, float y, float z) {
		this.translation = new Vector3f(x, y, z);
		this.rotation = new Quaternionf();
		this.scale = new Vector3f();
	}
	
	@Override
	public void tick() {
		
	}
	
	public Matrix4f asMatrix() {
		var matrix = new Matrix4f();
		matrix.translate(translation);
		matrix.rotate(rotation);
		matrix.scale(scale);
		return matrix;
	}
	
	public Vector3fc getTranslation() {
		return translation;
	}
	
	public TransformTrait setTranslation(float x, float y, float z) {
		this.translation.set(x, y, z);
		return this;
	}
	
	public TransformTrait translate(float x, float y, float z) {
		this.translation.add(x, y, z);
		return this;
	}
	
	public Quaternionfc getRotation() {
		return rotation;
	}
	
	public TransformTrait rotate(float x, float y) {
		this.rotation.add(x, y, 0, 0);
		return this;
	}
	
	public Vector3fc getScale() {
		return scale;
	}
	
	public TransformTrait scale(float scale) {
		this.scale.set(scale);
		return this;
	}
	
	@Override
	public String toString() {
		return "TransformTrait [translation= " + translation + 
				", rotation= " + rotation + ", scale= " + scale + "]";  
	}
}
