package fr.sigillum.diaboli.graphics.obj;

import org.joml.Vector2f;
import org.joml.Vector3f;

public final class Vertex {

	public static final int BUFFER_SIZE = 3 + 2 + 3;

	public final Vector3f position;

	public final Vector2f textureCoords;

	public final Vector3f normal;

	public Vertex(Vector3f position, Vector2f textureCoords, Vector3f normal) {
		this.position = new Vector3f(position);
		this.textureCoords = new Vector2f(textureCoords);
		this.normal = new Vector3f(normal);
	}
}
