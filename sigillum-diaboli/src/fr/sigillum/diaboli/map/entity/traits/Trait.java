package fr.sigillum.diaboli.map.entity.traits;

import fr.sigillum.diaboli.map.entity.Entity;

public abstract class Trait {

	private Entity entity = null;

	public abstract void tick();

	protected void onAttached() {

	}

	protected void onDetached() {

	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		if (this.entity == entity) {
			return;
		}

		if (this.entity != null) {
			onDetached();
		}

		this.entity = entity;

		if (this.entity != null) {
			onAttached();
		}
	}
}
