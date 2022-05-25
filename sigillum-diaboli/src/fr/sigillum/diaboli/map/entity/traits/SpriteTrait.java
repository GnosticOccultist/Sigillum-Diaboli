package fr.sigillum.diaboli.map.entity.traits;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.gl.Texture;
import fr.sigillum.diaboli.map.entity.Player;

public class SpriteTrait extends Trait {

	private final AssetKey key;

	private final Vector3f look = new Vector3f();

	private final Vector3f left = new Vector3f();

	private final Matrix3f orient = new Matrix3f();

	public SpriteTrait(String name) {
		this.key = AssetKey.of("sprite", name);
	}

	@Override
	public void tick() {

	}

	public void render(Drawer drawer, Player player) {
		var pos = getEntity().getPosition();
		drawer.begin();
		GL11C.glDisable(GL11C.GL_CULL_FACE);
		rotateAxial(drawer, player);
		drawer.useTexture(getTexture());
		drawer.drawSprite(pos.x(), 1.25f, pos.y(), 2.25f, pos.z());
		GL11C.glEnable(GL11C.GL_CULL_FACE);
		drawer.end();
		drawer.modelMatrix();
		drawer.useDefaultTexture();
	}

	private void rotateAxial(Drawer drawer, Player player) {
		var entityPos = getEntity().getPosition();
		look.set(player.getPosition()).sub(entityPos);
		var worldMatrix = new Matrix3f();
		look.mul(worldMatrix, left);

		var lengthSquared = left.x() * left.x() + left.z() * left.z();
		if (lengthSquared < 0.5d) {
			// Camera on the billboard axis, rotation not defined.
			return;
		}

		var invLength = 1.0 / Math.sqrt(lengthSquared);
		left.set(left.x() * invLength, 0.0f, left.z() * invLength);
		// Compute the local orientation matrix for the billboard.
		orient.m00(left.z());
		orient.m01(0);
		orient.m02(left.x());
		orient.m10(0);
		orient.m11(1);
		orient.m12(0);
		orient.m20(-left.x());
		orient.m21(0);
		orient.m22(left.z());

		worldMatrix.mul(orient);
		drawer.applyRotMatrix(worldMatrix.invert());
	}

	protected Texture getTexture() {
		var texture = Assets.get().getTexture(key);
		return texture;
	}
}
