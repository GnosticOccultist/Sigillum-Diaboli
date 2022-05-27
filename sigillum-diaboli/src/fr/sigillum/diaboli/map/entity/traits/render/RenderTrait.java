package fr.sigillum.diaboli.map.entity.traits.render;

import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.entity.Player;
import fr.sigillum.diaboli.map.entity.traits.Trait;
import fr.sigillum.diaboli.map.entity.traits.TransformTrait;

public abstract class RenderTrait extends Trait {

	private final AssetKey key;

	public RenderTrait(AssetKey key) {
		super(TransformTrait.class);
		this.key = key;
	}

	@Override
	public void tick() {

	}

	public abstract void render(Drawer drawer, Player player);

	public AssetKey getKey() {
		return key;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [ key= " + key + " ]";
	}
}
