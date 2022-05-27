package fr.sigillum.diaboli.map.entity.traits;

import fr.sigillum.diaboli.map.entity.Entity;

public abstract class Trait {

	private Entity entity = null;
	
	private final Class<? extends Trait>[] requirements;
	
	@SafeVarargs
	public Trait(Class<? extends Trait>... requirements) {
		this.requirements = requirements;
	}

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
			for (var require : requirements) {
				
				if (!entity.hasTrait(require)) {
					throw new IllegalStateException("Failed to attach trait " + this + " to " + 
							entity + "! Missing required trait: " + require);
				}
			}
			
			onAttached();
		}
	}
}
