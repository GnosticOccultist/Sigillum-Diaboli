package fr.sigillum.diaboli.map.generator;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;

public class Polygon implements Iterable<Vector2f> {

	private final Array<Vector2f> vertices = Array.ofType(Vector2f.class);

	public Polygon() {

	}

	public Polygon(Polygon polygon) {
		this(polygon.vertices());
	}

	public Polygon(Array<Vector2f> vertices) {
		this.vertices.addAll(vertices);
	}

	public void apply(Function<Vector2f, Vector2f> function) {
		for (var i = 0; i < size(); ++i) {
			set(i, function.apply(get(i)));
		}
	}

	public Vector2f center() {
		var center = new Vector2f();
		for (var vert : vertices) {
			center.add(vert);
		}

		return center.mul(1.0f / size());
	}

	public void add(Vector2f vertex) {
		this.vertices.add(vertex);
	}

	public Vector2f get(int index) {
		return vertices.get(index);
	}

	public void forEdge(BiConsumer<Vector2f, Vector2f> func) {
		for (var i = 0; i < size(); ++i) {
			func.accept(get(i), get((i + 1) % size()));
		}
	}

	public Array<Polygon> split(Vector2f p1, Vector2f p2) {
		return split(indexOf(p1), indexOf(p2));
	}

	public Array<Polygon> split(int index1, int index2) {
		if (index1 > index2) {
			var temp = index1;
			index1 = index2;
			index2 = temp;
		}

		var vertsFirst = Array.ofType(Vector2f.class);
		for (var i = index1; i <= index2; ++i) {
			vertsFirst.add(get(i));
		}

		var first = new Polygon(vertsFirst);

		var vertsSecond = Array.ofType(Vector2f.class);
		for (var i = index2; i < size(); ++i) {
			vertsSecond.add(get(i));
		}
		for (var i = 0; i <= index1; ++i) {
			vertsSecond.add(get(i));
		}
		
		var second = new Polygon(vertsSecond);

		return Array.of(first, second);
	}

	public float distance(Vector2f point) {
		var v0 = get(0);
		var d = v0.distance(point);
		for (var i = 1; i < size(); ++i) {
			var v1 = get(i);
			var d1 = v1.distance(point);
			if (d1 < d)
				v0 = v1;
		}
		return d;
	}

	public Array<Vector2f> vertices() {
		return vertices;
	}

	public int size() {
		return vertices.size();
	}

	public int indexOf(Vector2f vertex) {
		return vertices.indexOf(vertex);
	}

	public int indexOf(Vector2f vertex, int from) {
		return vertices.indexOf(vertex, from);
	}

	public Stream<Vector2f> stream() {
		return vertices.stream();
	}

	public boolean contains(Vector2f vertex) {
		return vertices.contains(vertex);
	}

	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	public void set(int index, Vector2f vertex) {
		this.vertices.set(index, vertex);
	}

	public void remove(Vector2f vertex) {
		this.vertices.remove(vertex);
	}

	public void remove(int index) {
		this.vertices.remove(index);
	}

	@Override
	public Iterator<Vector2f> iterator() {
		return vertices.iterator();
	}

	public Vector2f smoothVertex(Vector2f vertex, float factor) {
		var prev = prev(vertex);
		var next = next(vertex);
		return new Vector2f(prev.x + vertex.x * factor + next.x, prev.y + vertex.y * factor + next.y)
				.mul(1 / (2 + factor));
	}

	public Polygon smoothVertexEq(float factor) {
		var v1 = get(size() - 1);
		var v2 = get(0);
		var result = Array.ofType(Vector2f.class);
		for (var i = 0; i < size(); ++i) {
			var v0 = v1;
			v1 = v2;
			v2 = get((i + 1) % size());
			result.add(new Vector2f(
					(v0.x + v1.x * factor + v2.x) / (2 + factor),
					(v0.y + v1.y * factor + v2.y) / (2 + factor)
			));
		}
		return new Polygon(result);
	}

	public Vector2f next(Vector2f a) {
		return get((indexOf(a) + 1) % size());
	}

	public Vector2f prev(Vector2f a) {
		return get((indexOf(a) + size() - 1) % size());
	}

	public int findEdge(Vector2f a, Vector2f b) {
		var index = indexOf(a);
		return (index != -1 && get((index + 1) % size()) == b ? index : -1);
	}

	public Vector2f last() {
		return vertices.last();
	}

	@Override
	public String toString() {
		return vertices.toString();
	}
}
