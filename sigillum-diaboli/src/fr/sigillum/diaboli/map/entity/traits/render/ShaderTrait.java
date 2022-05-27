package fr.sigillum.diaboli.map.entity.traits.render;

import fr.sigillum.diaboli.graphics.gl.ShaderProgram;
import fr.sigillum.diaboli.map.entity.traits.Trait;

public abstract class ShaderTrait extends Trait {

	private volatile boolean dirty;

	@SafeVarargs
	public ShaderTrait(Class<? extends Trait>... requirements) {
		super(requirements);
		this.dirty = true;
	}

	public abstract void upload(ShaderProgram program);

	public boolean isDirty() {
		return dirty;
	}

	protected void dirty() {
		this.dirty = true;
	}

	protected void clean() {
		this.dirty = false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [ dirty= " + dirty + " ]";
	}
}
