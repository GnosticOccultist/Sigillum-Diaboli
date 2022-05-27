package fr.sigillum.diaboli.map.entity.traits.render;

import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.entity.Player;
import fr.sigillum.diaboli.map.entity.traits.TransformTrait;

public class ModelTrait extends RenderTrait {

	public ModelTrait(String name) {
		super(AssetKey.of("model", name));
	}

	@Override
	public void render(Drawer drawer, Player player) {
		var transform = getEntity().getTrait(TransformTrait.class).get();
		drawer.modelMatrix(m -> m.set(transform.asMatrix()));

		var model = Assets.get().getModel(getKey());
		model.render(drawer);

		drawer.modelMatrix();
	}
}
