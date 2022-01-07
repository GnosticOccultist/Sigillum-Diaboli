package fr.sigillum.diaboli.map.entity;

import java.util.UUID;

import org.joml.Vector2f;
import org.joml.Vector3f;

import fr.sigillum.diaboli.map.Region;

public abstract class Entity {

	protected volatile Region region;

	protected final UUID id;

	protected final Vector3f position;
	protected final Vector2f rotation;

	protected final Vector3f velocity;

	public Entity() {
		this(UUID.randomUUID());
	}

	public Entity(UUID id) {
		this(id, 0, 0, 0);
	}

	public Entity(UUID id, float x, float y, float z) {
		this.id = id;
		this.position = new Vector3f(x, y, z);
		this.rotation = new Vector2f();
		this.velocity = new Vector3f();
	}

	public void tick() {

	}

	public void move(float x, float y, float z) {
		this.position.add(x, y, z);
	}

	public boolean shouldRemove() {
		return false;
	}

	public Vector3f getPosition() {
		return position;
	}

	public Vector2f getRotation() {
		return rotation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Entity)) {
			return false;
		}

		var other = (Entity) obj;
		return other.id.equals(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id= " + id + ", position= " + 
				position.toString() + ", rotation= " + rotation + "]";
	}
}
