package fr.sigillum.diaboli.map;

import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.collections.array.Array;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.entity.Entity;

public class World {

	private final Array<Region> regions = Array.ofType(Region.class);

	private final Map<Long, Region> regionCache = new HashMap<>();

	public void add(Entity entity) {
		var region = getRegion(entity.getPosition().x(), entity.getPosition().z(), true);
		region.add(entity);
	}

	public void remove(Entity entity) {
		var region = getRegion(entity.getPosition().x(), entity.getPosition().z());
		assert region != null;
		region.remove(entity);
	}

	public void tick() {
		for (var region : regions) {
			region.tick();
		}
	}

	public void render(Drawer drawer) {
		var frustum = drawer.getFrustum();
		
		for (var region : regions) {
			if (!region.shouldRender(frustum)) {
				continue;
			}
			
			drawer.begin();
			region.render(drawer, frustum);
			drawer.end();
		}
	}

	public Region getRegion(float x, float z) {
		return getRegion(x, z, false);
	}

	public Region getRegion(float x, float z, boolean load) {
		var rx = (int) Math.floor(x / (double) Region.SIZE);
		var rz = (int) Math.floor(z / (double) Region.SIZE);
		return getRegionLocal(rx, rz, load);
	}

	public Region getRegionLocal(int rx, int rz) {
		return getRegionLocal(rx, rz, false);
	}

	public Region getRegionLocal(int rx, int rz, boolean load) {
		var hash = ((long) rz) << 32 | rx & 0xFFFFFFFFL;
		var region = regionCache.get(hash);
		if (region == null && load) {
			region = new Region(this, rx, rz);
			regions.add(region);
			regionCache.put(hash, region);
		}
		return region;
	}
}
