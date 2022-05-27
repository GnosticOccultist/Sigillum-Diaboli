package fr.sigillum.diaboli.map.entity;

import java.util.Optional;
import java.util.UUID;
import fr.alchemy.utilities.collections.array.Array;
import fr.sigillum.diaboli.map.Region;
import fr.sigillum.diaboli.map.entity.traits.Trait;

public class Entity {

	protected volatile Region region;

	protected final UUID id;

	private volatile boolean destroyed = false;

	private final Array<Trait> traits = Array.ofType(Trait.class);

	public Entity() {
		this(UUID.randomUUID());
	}

	public Entity(UUID id) {
		this.id = id;
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

	public <T extends Trait> boolean hasTrait(Class<T> type) {
		return getTrait(type).isPresent();
	}

	public <T extends Trait> T requireTrait(Class<T> type) {
		return traits.stream().filter(type::isInstance).map(type::cast).findAny().orElseThrow();
	}

	public <T extends Trait> Optional<T> getTrait(Class<T> type) {
		return traits.stream().filter(type::isInstance).map(type::cast).findAny();
	}

	public void tick() {
		if (destroyed) {
			return;
		}

		traits.forEach(Trait::tick);
	}

	public boolean shouldRemove() {
		return destroyed;
	}

	public void destroy() {
		this.destroyed = true;
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
		return getClass().getSimpleName() + " [id= " + id + ", traits= " + traits + "]";
	}
}
