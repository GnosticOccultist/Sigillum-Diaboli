package fr.sigillum.diaboli.util;

import org.joml.Vector3f;

public class BoundingBox {

	private final Vector3f min;
	
	private final Vector3f max;
	
	public BoundingBox() {
		this.min = new Vector3f();
		this.max = new Vector3f();
	}
	
	public BoundingBox(float minX, float minZ, float maxX, float maxZ) {
		this.min = new Vector3f(minX, Float.MIN_VALUE, minZ);
		this.max = new Vector3f(maxX, Float.MAX_VALUE, maxZ);
	}
	
	public BoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.min = new Vector3f(minX, minY, minZ);
		this.max = new Vector3f(maxX, maxY, maxZ);
	}
	
	public Vector3f max() {
		return max;
	}
	
	public Vector3f min() {
		return min;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [min= " + min + ", max=" + max + "]"; 
	}
}
