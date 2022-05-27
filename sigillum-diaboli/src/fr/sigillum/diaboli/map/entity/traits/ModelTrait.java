package fr.sigillum.diaboli.map.entity.traits;

import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.entity.Player;

public class ModelTrait extends Trait {

	private final AssetKey key;

	public ModelTrait(String name) {
		super(TransformTrait.class);
		this.key = AssetKey.of("model", name);
	}

	@Override
	public void tick() {

	}

	public void render(Drawer drawer, Player player) {
		var transform = getEntity().getTrait(TransformTrait.class).get();
		drawer.modelMatrix(m -> m.set(transform.asMatrix()));

		var model = Assets.get().getModel(key);
		model.render(drawer);

		drawer.modelMatrix();
	}
}
