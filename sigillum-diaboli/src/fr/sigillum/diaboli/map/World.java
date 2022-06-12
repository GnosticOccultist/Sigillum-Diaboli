package fr.sigillum.diaboli.map;

import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ArrayCollectors;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.Light;
import fr.sigillum.diaboli.map.entity.Entity;
import fr.sigillum.diaboli.map.entity.Player;
import fr.sigillum.diaboli.map.entity.traits.TransformTrait;
import fr.sigillum.diaboli.map.entity.traits.render.LightTrait;
import fr.sigillum.diaboli.map.entity.traits.render.RenderTrait;
import fr.sigillum.diaboli.map.entity.traits.render.ShaderTrait;

public class World {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.world");

	private final Array<Region> regions = Array.ofType(Region.class);

	private final Map<Long, Region> regionCache = new HashMap<>();

	public void add(Entity entity) {
		var translation = entity.requireTrait(TransformTrait.class).getTranslation();
		var region = getRegion(translation.x(), translation.z(), true);
		region.add(entity);
	}

	public void remove(Entity entity) {
		var translation = entity.requireTrait(TransformTrait.class).getTranslation();
		var region = getRegion(translation.x(), translation.z());
		assert region != null;
		region.remove(entity);
	}

	public void tick() {
		for (var i = 0; i < regions.size(); ++i) {
			var region = regions.get(i);
			region.tick();

			if (region.decreaseTimer()) {
				unloadRegion(region);
			}
		}
	}

	public void render(Drawer drawer, Player player) {
		var frustum = drawer.getFrustum();

		drawer.useDefaultTexture();

		for (var region : regions) {
			if (!region.shouldRender(frustum)) {
				continue;
			}

			var lights = region.getEntities(e -> e.getTrait(LightTrait.class).isPresent()).stream()
					.map(e -> e.requireTrait(LightTrait.class)).collect(ArrayCollectors.toArray(LightTrait.class));
			for (var i = 0; i < Math.min(lights.size(), Light.MAX_LIGHTS); ++i) {
				var light = lights.get(i);
				if (light.getIndex() != i) {
					light.setIndex(i);
				}
			}

			var program = drawer.defaultShader();
			region.getEntities(e -> e.getTrait(ShaderTrait.class).isPresent()).stream()
					.map(e -> e.requireTrait(ShaderTrait.class)).forEach(t -> t.upload(program));

			region.getEntities(e -> e.getTrait(RenderTrait.class).isPresent()).stream()
					.map(e -> e.getTrait(RenderTrait.class).get()).forEach(t -> t.render(drawer, player));
		}
	}

	public void unloadRegion(Region region) {
		var hash = ((long) region.getZ()) << 32 | region.getX() & 0xFFFFFFFFL;
		var old = regionCache.remove(hash);
		assert old != null && old == region;
		var removed = regions.remove(region);
		assert removed;
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
