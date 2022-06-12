package fr.sigillum.diaboli.map.generator;

import java.util.Random;
import java.util.UUID;

import org.joml.Vector2f;
import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ArrayCollectors;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.map.World;
import fr.sigillum.diaboli.map.entity.Entity;
import fr.sigillum.diaboli.map.entity.traits.TransformTrait;
import fr.sigillum.diaboli.map.entity.traits.render.LightTrait;
import fr.sigillum.diaboli.map.entity.traits.render.ModelTrait;

public class Village {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.map.generator");

	public static Village generate(int seed, World world) {
		var rand = new Random(seed);

		var village = new Village(10, rand);
		village.build(world);
		return village;
	}

	private Vector2f center = new Vector2f();

	protected Array<Patch> patches = Array.ofType(Patch.class);

	protected final Array<Vector2f> gates = Array.ofType(Vector2f.class);

	protected final Array<Patch> inner = Array.ofType(Patch.class);

	protected final Array<Polygon> streets = Array.ofType(Polygon.class);

	protected final Array<Polygon> roads = Array.ofType(Polygon.class);

	Patch plaza;

	private final int patchCount;

	private boolean plazaNeeded = true;

	Random rand;

	CurtainWall border;

	private Topology topology;

	Array<Polygon> arteries;

	public Village(int patchCount, Random rand) {
		this.patchCount = patchCount;
		this.rand = rand;
	}

	public void build(World world) {
		buildPatches();
		optimizeJunctions();
		buildWalls();
		buildStreets();
		
		var fountain = new Entity(UUID.randomUUID());
		fountain.addTrait(new TransformTrait());
		fountain.addTrait(new LightTrait());
		fountain.requireTrait(LightTrait.class).setColor(1.0f, 0.0f, 1.0f);
		var centroid = plaza.getShape().centroid();
		fountain.requireTrait(TransformTrait.class).translate(centroid.x(), 0, centroid.y())
				.scale(2.0f);
		fountain.addTrait(new ModelTrait("fountain"));
		world.add(fountain);
		
		for (var patch : inner) {
			if (plaza != patch && plaza.getShape().borders(patch.getShape())) {
				// Place the church in an adjacent patch.
				var church = new Entity(UUID.randomUUID());
				church.addTrait(new TransformTrait());
				church.addTrait(new LightTrait());
				church.requireTrait(LightTrait.class).setColor(1.0f, 0.0f, 1.0f);
				centroid = patch.getShape().centroid();
				church.requireTrait(TransformTrait.class).translate(centroid.x(), 0, centroid.y())
						.scale(1f);
				church.addTrait(new ModelTrait("small_medieval_house"));
				world.add(church);
			}
		}
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
				this.center = patch.getShape().stream()
						.min((a, b) -> Float.compare(a.length(), b.length()))
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
					// Replace all occurrences if v1 with v0.
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

		patches = patches.stream().filter(p -> p.getShape().distance(center) < radius * 3)
				.collect(ArrayCollectors.toArray(Patch.class));

		logger.info("Switching from " + oldCount + " patches to " + patches.size() + " patches.");

		this.gates.addAll(border.gates());
	}

	private void buildStreets() {
		topology = new Topology(this);

		for (var gate : gates) {
			// Each gate is connected to the nearest corner of the plaza or to the central
			// junction.
			var end = plaza != null ? plaza.getShape().stream().min((v1, v2) -> {
				float d1 = v1.distance(gate);
				float d2 = v2.distance(gate);
				return Float.compare(d1, d2);
			}).orElse(center) : center;

			var street = topology.buildPath(gate, end, topology.outer);
			if (!street.isEmpty()) {
				streets.add(new Polygon(street));
				logger.info("Adding new street at " + gate + " to " + end + ".");

				if (border.gates().contains(gate)) {
					var dir = gate.normalize(1000);
					Vector2f start = null;
					var dist = Float.MAX_VALUE;
					for (var point : topology.nodeToPt.values()) {
						var d = point.distance(dir);
						if (d < dist) {
							dist = d;
							start = point;
						}
					}

					var road = topology.buildPath(start, gate, topology.inner);
					if (road != null && !road.isEmpty()) {
						logger.info("Adding new road at " + start + " to " + gate + ".");
						roads.add(new Polygon(road));
					}
				}
			} else {
				throw new RuntimeException("Couldn't build a street!");
			}
		}

		tidyUpRoads();

		for (var a : arteries) {
			var smoothed = a.smoothVertexEq(3);
			for (var i = 1; i < a.size() - 1; ++i) {
				a.set(i, smoothed.get(i));
			}
		}
	}

	private void tidyUpRoads() {
		var segments = Array.ofType(Segment.class);

		for (var street : streets) {
			cutSegments(street, segments);
		}

		for (var road : roads) {
			cutSegments(road, segments);
		}

		arteries = Array.ofType(Polygon.class);
		while (!segments.isEmpty()) {
			var seg = segments.pop();

			var attached = false;
			
			for (var a : arteries) {
				var old = a.get(0);
				if (old.equals(seg.end)) {
					for (int i = 1; i < a.size(); ++i) {
						a.set(i, old);
						
						if ((i + 1) < a.size()) {
							old = a.get(i + 1);
						}
					}
					a.set(0, seg.start);
					attached = true;
					break;
				} else if (a.last() == seg.start) {
					a.add(seg.end);
					attached = true;
					break;
				}
			}

			if (!attached) {
				var artery = new Polygon(Array.of(seg.start, seg.end));
				arteries.add(artery);
				logger.info("Added new artery " + artery);
			}
		}
	}

	private void cutSegments(Polygon street, Array<Segment> segments) {
		Vector2f v0 = null;
		var v1 = street.get(0);
		for (var i = 1; i < street.size(); ++i) {
			v0 = v1;
			v1 = street.get(i);

			// Removing segments which go along the plaza.
			if (plaza != null && plaza.getShape().contains(v0) && plaza.getShape().contains(v1)) {
				continue;
			}

			var exists = false;
			for (var seg : segments) {
				if (seg.start.equals(v0) && seg.end.equals(v1)) {
					exists = true;
					break;
				}
			}

			if (!exists) {
				segments.add(new Segment(v0, v1));
			}
		}
	}

	Array<Patch> patchByVertex(Vector2f v) {
		return patches.stream().filter(p -> p.getShape().contains(v)).collect(ArrayCollectors.toArray(Patch.class));
	}

	public int randInt(int bound) {
		return rand.nextInt(bound);
	}

	public void draw(Drawer drawer) {
		drawer.begin();
		
		for (var vert : plaza.getShape()) {
			drawer.drawBox(vert.x(), 0.0f, vert.y());
		}
		
		for (var street : streets) {
			for (var vert : street) {
				drawer.drawBox(vert.x(), 0.0f, vert.y());
			}
		}
		
		for (var street : arteries) {
			for (var vert : street) {
				drawer.drawBox(vert.x(), 0.0f, vert.y());
			}
		}
		
		drawer.end();
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
			logger.info(wards.toString());
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
		return center;
	}
}
