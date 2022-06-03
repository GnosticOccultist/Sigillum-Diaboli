package fr.sigillum.diaboli.map.generator;

import java.util.Random;

import org.joml.Vector2f;
import org.lwjgl.opengl.GL11C;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ArrayCollectors;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.graphics.Drawer;

public class Village {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.map.generator");

	public static Village generate(int seed) {
		var rand = new Random(seed);

		var village = new Village(10, rand);
		village.build();
		return village;
	}

	private Vector2f center = new Vector2f();

	protected final Array<Patch> patches = Array.ofType(Patch.class);

	protected final Array<Vector2f> gates = Array.ofType(Vector2f.class);

	protected final Array<Patch> inner = Array.ofType(Patch.class);

	private Patch plaza;

	private final int patchCount;

	private boolean plazaNeeded = true;

	Random rand;

	CurtainWall border;

	public Village(int patchCount, Random rand) {
		this.patchCount = patchCount;
		this.rand = rand;
	}

	public void build() {
		buildPatches();
		optimizeJunctions();
		buildWalls();
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

	private void optimizeJunctions() {
		var patchesToOptimize = inner;

		var wards2clean = Array.ofType(Patch.class);
		for (var w : patchesToOptimize) {
			var index = 0;
			while (index < w.getShape().size()) {

				var v0 = w.getShape().get(index);
				var v1 = w.getShape().get((index + 1) % w.getShape().size());

				if (v0 != v1 && v0.distance(v1) < 8) {
					for (var w1 : patchByVertex(v1)) {
						if (w1 != w) {
							w1.getShape().set(w1.getShape().indexOf(v1), v0);
							wards2clean.add(w1);
						}
					}

					v0.add(v1);
					v0.mul(0.5f);

					w.getShape().remove(v1);
				}

				index++;
			}
		}

		// Removing duplicate vertices.
		for (var w : wards2clean) {
			for (var i = 0; i < w.getShape().size(); ++i) {
				var v = w.getShape().get(i);
				var dupIdx = 0;
				while ((dupIdx = w.getShape().indexOf(v, i + 1)) != -1) {
					w.getShape().remove(dupIdx);
				}
			}
		}

		var it = patchesToOptimize.iterator();
		while (it.hasNext()) {
			var patch = it.next();
			if (patch.isEmpty()) {
				it.remove();
			}
		}

		logger.info("Successfully optimized junctions!");
		logger.info("Remaining " + patches.size() + " patches with " + inner.size() + " inner patches.");
	}

	private void buildWalls() {
		Array<Vector2f> reserved = Array.empty();

		border = new CurtainWall(this, inner, reserved);

		var radius = border.getRadius();

		logger.info("Only retaining patches within 3 * radius -> 3 * " + radius + " (radius * 3).");
		if (Float.isNaN(radius)) {
			throw new RuntimeException("An error occured while computing border radius!");
		}

		var oldCount = patches.size();

		var temp = patches.stream().filter(p -> p.getShape().distance(center) < radius * 3)
				.collect(ArrayCollectors.toArray(Patch.class));
		patches.clear();
		patches.addAll(temp);

		logger.info("Switching from " + oldCount + " patches to " + patches.size() + " patches.");

		this.gates.addAll(border.gates());
	}

	Array<Patch> patchByVertex(Vector2f v) {
		return patches.stream().filter(p -> p.getShape().contains(v)).collect(ArrayCollectors.toArray(Patch.class));
	}

	public int randInt(int bound) {
		return rand.nextInt(bound);
	}

	public void draw(Drawer drawer) {
		GL11C.glDisable(GL11C.GL_CULL_FACE);
		drawer.begin();
		
		for (var vert : plaza.getShape()) {
			logger.info(vert.toString());
			drawer.drawVertex(vert.x(), 0.0f, vert.y(), 1.0f, 1.0f);
		}

		logger.info("-------");
		drawer.end();
		GL11C.glEnable(GL11C.GL_CULL_FACE);
	}

	public static Polygon findCircumference(Array<Patch> wards) {
		if (wards.size() == 0) {
			return new Polygon();
		} else if (wards.size() == 1) {
			return new Polygon(wards.get(0).getShape());
		}

		var a = Array.ofType(Vector2f.class);
		var b = Array.ofType(Vector2f.class);

		for (var w1 : wards) {
			w1.getShape().forEdge((pa, pb) -> {
				var outer = true;
				for (var w2 : wards) {
					if (w2.getShape().findEdge(pb, pa) != -1) {
						outer = false;
						break;
					}
				}
				if (outer) {
					a.add(pa);
					b.add(pb);
				}
			});
		}

		var result = new Polygon();
		var index = 0;
		do {
			result.add(a.get(index));
			index = a.indexOf(b.get(index));
		} while (index != 0);

		return result;
	}

	public Vector2f getCenter() {
		return plaza.getShape().center();
	}
}
