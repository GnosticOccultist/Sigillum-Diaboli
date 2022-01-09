package fr.sigillum.diaboli.map;

import org.joml.FrustumIntersection;

import fr.alchemy.utilities.collections.array.Array;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.entity.Entity;
import fr.sigillum.diaboli.map.entity.Player;
import fr.sigillum.diaboli.map.tiles.Tile;
import fr.sigillum.diaboli.util.BoundingBox;

public class Region {

	public static final int WALL_SIZE = 3;
	
	public static final int UNLOAD_TICK_TIME = 100;

	public static final int SIZE = 32;

	private final int x, z;

	private final World world;
	
	private final BoundingBox box;

	private final RegionData data;
	
	private int unloadTimer = UNLOAD_TICK_TIME;

	private final Array<Entity> entities = Array.ofType(Entity.class);

	public Region(World world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
		this.box = new BoundingBox(x * SIZE, z * SIZE, x * SIZE + SIZE, z * SIZE + SIZE);
		this.data = RegionData.fromImage(x, z, "map");
	}

	public boolean add(Entity entity) {
		return entities.add(entity);
	}

	public boolean remove(Entity entity) {
		return entities.remove(entity);
	}

	public void tick() {
		var it = entities.iterator();
		while (it.hasNext()) {
			var entity = it.next();
			entity.tick();

			if (entity.shouldRemove()) {
				it.remove();
			} else {
				if (entity instanceof Player) {
					for (int rx = x - 1; rx <= x + 1; ++rx) {
						for (int rz = z - 1; rz <= z + 1; ++rz) {
							var region = world.getRegionLocal(rx, rz, true);
							region.unloadTimer = UNLOAD_TICK_TIME;
						}
					}
					
					var rx = (int) Math.floor(entity.getPosition().x() / (double) Region.SIZE);
					var rz = (int) Math.floor(entity.getPosition().z() / (double) Region.SIZE);
					// Player changed regions.
					if (rx != x || rz != z) {
						var newRegion = world.getRegionLocal(rx, rz, true);
						newRegion.add(entity);
						it.remove();
					}
				}
			}
		}
	}

	public void render(Drawer drawer, FrustumIntersection frustum) {
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
	
	public boolean shouldRender(FrustumIntersection frustum) {
		return frustum.testAab(box.min(), box.max());
	}

	public Tile getTile(int x, int z) {
		if (x < 0 || z < 0 || x >= SIZE || z >= SIZE) {
			return null;
		}
		return data.tiles[x + z * SIZE];
	}
	
	boolean decreaseTimer() {
		return --unloadTimer <= 0;
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[ x= " + x + " z= " + z + "]";
	}
}
