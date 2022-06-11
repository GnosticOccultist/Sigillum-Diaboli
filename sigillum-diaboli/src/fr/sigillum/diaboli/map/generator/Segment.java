package fr.sigillum.diaboli.map.generator;

import org.joml.Vector2f;

public class Segment {

	final Vector2f start, end;
	
	public Segment(Vector2f start, Vector2f end) {
		this.start = new Vector2f(start);
		this.end = new Vector2f(end);
	}
	
	public Segment(float x0, float y0, float x1, float y1) {
		this.start = new Vector2f(x0, y0);
		this.end = new Vector2f(x1, y1);
	}
	
	public float dx() {
		return end.x - start.x;
	}
	
	public float dy() {
		return end.y - start.y;
	}
	
	public Vector2f vec() {
		return end.sub(start, null);
	}
	
	public float length() {
		return start.distance(end);
	}
}
