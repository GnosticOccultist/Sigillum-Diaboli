package fr.sigillum.diaboli.map;

import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.tiles.Tile;

public class Region {

	public static final int WALL_SIZE = 3;

	public static final int SIZE = 32;

	private final int x, z;

	private final RegionData data;

	public Region(int x, int z) {
		this.x = x;
		this.z = z;
		this.data = RegionData.fromImage(x, z, "map");
	}

	public void tick() {

	}

	public void render(Drawer drawer) {
		var rx = x * SIZE;
		var rz = z * SIZE;

		for (var x = 0; x < SIZE; ++x) {
			for (var z = 0; z < SIZE; ++z) {
				var tile = getTile(x, z);

				// The tile contains a wall.
				if (!tile.isSolid()) {
					drawer.drawRectangle(rx + x, 0, 0, rz + z);
				}

				var left = getTile(x + 1, z);
				var back = getTile(x, z + 1);
				if (left == null || back == null) {
					continue;
				}

				if (tile.isSolid()) {
					// Draw ceiling of the wall.s
					drawer.drawRectangle(rx + x, WALL_SIZE, WALL_SIZE, rz + z, 0.2F);
					if (!left.isSolid()) {
						drawer.drawVertPlane(rx + x + 1, rz + z + 1, rx + x + 1, rz + z, 0, WALL_SIZE);
					}
					if (!back.isSolid()) {
						drawer.drawVertPlane(rx + x, rz + z + 1, rx + x + 1, rz + z + 1, 0, WALL_SIZE);
					}
				} else {
					if (left.isSolid()) {
						drawer.drawVertPlane(rx + x + 1, rz + z, rx + x + 1, rz + z + 1, 0, WALL_SIZE);
					}
					if (back.isSolid()) {
						drawer.drawVertPlane(rx + x + 1, rz + z + 1, rx + x, rz + z + 1, 0, WALL_SIZE);
					}
				}
			}
		}
	}

	public Tile getTile(int x, int z) {
		if (x < 0 || z < 0 || x >= SIZE || z >= SIZE) {
			return null;
		}
		return data.tiles[x + z * SIZE];
	}
}
