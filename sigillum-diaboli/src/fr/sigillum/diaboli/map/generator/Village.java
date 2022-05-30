package fr.sigillum.diaboli.map.generator;

import java.util.Random;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ArrayCollectors;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;

public class Village {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.map.generator");

	public static Village generate(long seed) {
		var rand = new Random(seed);

		var village = new Village(10, rand);
		village.build();
		return village;
	}

	private Vector2f center = new Vector2f();

	private final Array<Patch> patches = Array.ofType(Patch.class);

	private final Array<Patch> inner = Array.ofType(Patch.class);

	private Patch plaza;

	private final int patchCount;

	private boolean plazaNeeded = true;

	Random rand;

	public Village(int patchCount, Random rand) {
		this.patchCount = patchCount;
		this.rand = rand;
	}

	public void build() {
		buildPatches();
	}

	private void buildPatches() {
		var radius = rand.nextFloat() * Math.PI * 2;
		var points = new Vector2f[patchCount * 8];

		logger.info("Generating " + points.length + " starting points.");
		for (var i = 0; i < patchCount * 8; ++i) {
			var a = radius + Math.sqrt(i) * 5;
			var r = (i == 0 ? 0 : 10 + i * (2 + rand.nextFloat()));
			points[i] = new Vector2f((float) Math.cos(a) * r, (float) Math.sin(a) * r);
		}

		var voronoi = Voronoi.build(points);

		// Relaxing the central wards.
		for (var i = 0; i < 3; ++i) {
			var toRelax = new Vector2f[4];
			for (var j = 0; j < 3; ++j) {
				toRelax[j] = voronoi.getPoint(j);
			}
			toRelax[3] = voronoi.getPoint(patchCount);
			voronoi = Voronoi.relax(voronoi, Array.of(toRelax));
		}

		voronoi.sortPoints((p1, p2) -> (int) Math.signum(p1.length() - p2.length()));
		var regions = voronoi.partioning();

		var count = 0;
		for (var region : regions) {
			var patch = Patch.from(region);
			patches.add(patch);

			if (count == 0) {
				this.center = patch.getShape().stream().min((a, b) -> Float.compare(a.length(), b.length()))
						.orElseThrow();
				if (plazaNeeded) {
					this.plaza = patch;
				}
			}

			if (count < patchCount) {
				patch.withinCity = true;
				inner.add(patch);
			}

			count++;
		}
	}

	Array<Patch> patchByVertex(Vector2f v) {
		return patches.stream().filter(p -> p.getShape().contains(v)).collect(ArrayCollectors.toArray(Patch.class));
	}
}
