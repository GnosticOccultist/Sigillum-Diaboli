package fr.sigillum.diaboli.map.generator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;

public class Voronoi {

	private final Array<Vector2f> points = Array.ofType(Vector2f.class);

	private final Array<Vector2f> frame = Array.ofType(Vector2f.class);

	private final Array<Triangle> triangles = Array.ofType(Triangle.class);

	private final Map<Vector2f, Region> regions = new HashMap<>();

	private boolean regionsDirty;

	public static Voronoi build(Array<Vector2f> vertices) {
		return build(vertices.toArray());
	}

	public static Voronoi build(Vector2f[] vertices) {
		float minX = (float) 1e+10;
		float minY = (float) 1e+10;
		float maxX = (float) -1e9;
		float maxY = (float) -1e9;
		for (var vertex : vertices) {
			if (vertex.x < minX)
				minX = vertex.x;
			if (vertex.y < minY)
				minY = vertex.y;
			if (vertex.x > maxX)
				maxX = vertex.x;
			if (vertex.y > maxY)
				maxY = vertex.y;
		}

		var dx = (maxX - minX) * 0.5f;
		var dy = (maxY - minY) * 0.5f;

		dx /= 2;
		dy /= 2;

		var voronoi = new Voronoi(minX - dx, minY - dy, maxX + dx, maxY + dy);
		for (var vertex : vertices) {
			voronoi.addPoint(vertex);
		}

		return voronoi;
	}

	public static Voronoi relax(Voronoi voronoi, Array<Vector2f> toRelax) {
		var regions = voronoi.partioning();

		var points = Array.ofType(Vector2f.class);
		for (var point : voronoi.points) {
			points.add(new Vector2f(point));
		}
		for (var point : voronoi.frame) {
			points.remove(point);
		}

		if (toRelax == null) {
			toRelax = Array.of(voronoi.points);
		}
		for (var region : regions) {
			if (toRelax.contains(region.seed)) {
				points.remove(region.seed);
				points.add(region.center());
			}
		}

		return build(points);
	}

	private void addPoint(Vector2f point) {
		var toSplit = Array.ofType(Triangle.class);
		for (var triangle : triangles) {
			// The point is inside a triangle cell, so we need to split it.
			if (point.distance(triangle.center) < triangle.radius) {
				toSplit.add(triangle);
			}
		}

		if (!toSplit.isEmpty()) {
			this.points.add(point);

			var a = Array.ofType(Vector2f.class);
			var b = Array.ofType(Vector2f.class);
			for (var t1 : toSplit) {
				boolean e1 = true, e2 = true, e3 = true;
				for (var t2 : toSplit) {
					if (t2 != t1) {
						// If triangles have a common edge, it goes in opposite directions.
						if (e1 && t2.hasEdge(t1.b, t1.a))
							e1 = false;
						if (e2 && t2.hasEdge(t1.c, t1.b))
							e2 = false;
						if (e3 && t2.hasEdge(t1.a, t1.c))
							e3 = false;
						if (!(e1 || e2 || e3))
							break;
					}
				}
				if (e1) {
					a.add(t1.a);
					b.add(t1.b);
				}
				if (e2) {
					a.add(t1.b);
					b.add(t1.c);
				}
				if (e3) {
					a.add(t1.c);
					b.add(t1.a);
				}
			}

			var index = 0;
			do {
				triangles.add(new Triangle(point, a.get(index), b.get(index)));
				index = a.indexOf(b.get(index));
			} while (index != 0);

			for (var t : toSplit) {
				triangles.remove(t);
			}

			// Request a region rebuild.
			this.regionsDirty = true;
		}
	}

	public Array<Region> partioning() {
		var result = Array.ofType(Region.class);
		for (var point : points) {
			var r = getRegion(point);
			var real = true;
			for (var vertex : r.vertices) {
				if (!isReal(vertex)) {
					real = false;
					break;
				}
			}
			
			if (real) {
				result.add(r);
			}
		}
		return result;
	}
	
	public Region getRegion(Vector2f point) {
		if (regionsDirty) {
			rebuildRegions();
		}
		
		return regions.get(point);
	}

	private boolean isReal(Triangle tr) {
		return !(frame.contains(tr.a) || frame.contains(tr.b) || frame.contains(tr.c));
	}

	Voronoi(float minX, float minY, float maxX, float maxY) {
		var c1 = new Vector2f(minX, minY);
		var c2 = new Vector2f(minX, maxY);
		var c3 = new Vector2f(maxX, minY);
		var c4 = new Vector2f(maxX, maxY);

		var points = Array.of(c1, c2, c3, c4);
		this.frame.addAll(points);
		this.points.addAll(points);

		this.triangles.add(new Triangle(c1, c2, c3));
		this.triangles.add(new Triangle(c2, c3, c4));
		
		rebuildRegions();
	}
	
	private void rebuildRegions() {
		regions.clear();
		for (var point : points) {
			this.regions.put(point, buildRegion(point));
		}
		
		this.regionsDirty = false;
	}

	private Region buildRegion(Vector2f point) {
		var region = new Region(point);
		for (var tr : triangles) {
			if (tr.a == point || tr.b == point || tr.c == point) {
				region.vertices.add(tr);
			}
		}
		return region.sort();
	}

	public void sortPoints(Comparator<Vector2f> comparator) {
		this.points.sort(comparator);
	}
	
	public Vector2f getPoint(int index) {
		return points.get(index);
	}

	class Triangle {

		Vector2f a, b, c;

		Vector2f center;
		float radius;

		public Triangle(Vector2f a, Vector2f b, Vector2f c) {
			var s = (b.x - a.x) * (b.y + a.y) + (c.x - b.x) * (c.y + b.y) + (a.x - c.x) * (a.y + c.y);
			this.a = a;
			this.b = s > 0 ? b : c;
			this.c = s > 0 ? c : b;

			var x1 = (a.x + b.x) / 2;
			var y1 = (a.y + b.y) / 2;
			var x2 = (b.x + c.x) / 2;
			var y2 = (b.y + c.y) / 2;

			var dx1 = a.y - b.y;
			var dy1 = b.x - a.x;
			var dx2 = b.y - c.y;
			var dy2 = c.x - b.x;

			var tg1 = dy1 / dx1;
			var t2 = ((y1 - y2) - (x1 - x2) * tg1) / (dy2 - dx2 * tg1);
			this.center = new Vector2f(x2 + dx2 * t2, y2 + dy2 * t2);
			this.radius = center.distance(a);
		}

		public boolean hasEdge(Vector2f a1, Vector2f b1) {
			return (a == a1 && b == b1) || (b == a1 && c == b1) || (c == a1 && a == b1);
		}
		
		@Override
		public String toString() {
			return "a= " + a + ", b= " + b + ", c= " + c;
		}
	}

	class Region {

		Vector2f seed;
		Array<Triangle> vertices = Array.ofType(Triangle.class);

		public final Comparator<Triangle> COMPARATOR = (first, second) -> {
			var x1 = first.c.x - seed.x;
			var y1 = first.c.y - seed.y;
			var x2 = second.c.x - seed.x;
			var y2 = second.c.y - seed.y;

			if (x1 >= 0 && x2 < 0)
				return 1;
			if (x2 >= 0 && x1 < 0)
				return -1;
			if (x1 == 0 && x2 == 0)
				return y2 > y1 ? 1 : -1;

			return (int) Math.signum(x2 * y1 - x1 * y2);
		};

		public Region(Vector2f seed) {
			this.seed = seed;
		}

		public Region sort() {
			this.vertices.sort(COMPARATOR);
			return this;
		}

		public Vector2f center() {
			var c = new Vector2f();
			for (var vertex : vertices) {
				c.add(vertex.center);
				c.mul(1 / vertices.size());
			}
			return c;
		}
	}
}
