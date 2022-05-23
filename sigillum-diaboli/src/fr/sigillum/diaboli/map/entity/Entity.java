package fr.sigillum.diaboli.map.entity;

import java.util.Optional;
import java.util.UUID;
import org.joml.Vector2f;
import org.joml.Vector3f;

import fr.alchemy.utilities.collections.array.Array;
import fr.sigillum.diaboli.map.Region;
import fr.sigillum.diaboli.map.entity.traits.Trait;

public class Entity {

	protected volatile Region region;

	protected final UUID id;

	protected final Vector3f position;
	protected final Vector2f rotation;

	private final Array<Trait> traits = Array.ofType(Trait.class);

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
	}

	public Entity addTrait(Trait trait) {
		this.traits.add(trait);
		trait.setEntity(this);
		return this;
	}

	public Entity addTraits(Trait... traits) {
		for (var trait : traits) {
			addTrait(trait);
		}
		return this;
	}

	public Entity removeTrait(Trait trait) {
		this.traits.remove(trait);
		trait.setEntity(null);
		return this;
	}

	public <T extends Trait> Optional<T> getTrait(Class<T> type) {
		return traits.stream().filter(type::isInstance).map(type::cast).findAny();
	}

	public void tick() {
		traits.forEach(Trait::tick);
	}

	public void move(float x, float y, float z) {
		this.position.add(x, y, z);
	}

	public boolean isOnGround() {
		return position.y <= 1.85f;
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
		return getClass().getSimpleName() + " [id= " + id + ", position= " + position.toString() + ", rotation= "
				+ rotation + "]";
	}
}
