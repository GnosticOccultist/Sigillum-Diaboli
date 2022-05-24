package fr.sigillum.diaboli.map.entity.traits;

import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.gl.Texture;
import fr.sigillum.diaboli.map.entity.Player;

public class SpriteTrait extends Trait {

	private final AssetKey key;

	public SpriteTrait(String name) {
		this.key = AssetKey.of("sprite", name);
	}

	@Override
	public void tick() {

	}

	public void render(Drawer drawer, Player player) {
		var pos = getEntity().getPosition();
		drawer.begin();
		drawer.viewMatrixBillboard(player.getPosition(), player.getRotation());
		drawer.useTexture(getTexture());
		drawer.drawSprite(pos.x(), 0.5f, pos.y(), pos.z(), 1f);
		drawer.end();
		drawer.modelMatrix();
		drawer.viewMatrix(player.getPosition(), player.getRotation());
		drawer.useDefaultTexture();
	}

	protected Texture getTexture() {
		var texture = Assets.get().getTexture(key);
		return texture;
	}
}
