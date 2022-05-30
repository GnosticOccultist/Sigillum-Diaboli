package fr.sigillum.diaboli.map.generator;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;

public class Polygon implements Iterable<Vector2f> {

	private final Array<Vector2f> vertices = Array.ofType(Vector2f.class);
	
	public Polygon(Array<Vector2f> vertices) {
		this.vertices.addAll(vertices);
	}
	
	public void apply(Function<Vector2f, Vector2f> function) {
		for (var i = 0; i < size(); ++i) {
			set(i, function.apply(get(i)));
		}
	}
	
	public Vector2f get(int index) {
		return vertices.get(index);
	}
	
	public int size() {
		return vertices.size();
	}
	
	public int indexOf(Vector2f vertex) {
		return vertices.indexOf(vertex);
	}
	
	public Stream<Vector2f> stream() {
		return vertices.stream();
	}

	public boolean contains(Vector2f vertex) {
		return vertices.contains(vertex);
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

	public Vector2f smoothVertex(Vector2f vertex, int factor) {
		var prev = prev(vertex);
		var next = next(vertex);
		return new Vector2f(prev.x + vertex.x * factor + next.x,
				prev.y + vertex.y * factor + next.y).mul(1 / (2 + factor));
	}
	
	public Vector2f next(Vector2f a) {
		return get((indexOf(a) + 1) % size());
	}
	
	public Vector2f prev(Vector2f a) {
		return get((indexOf(a) + size() - 1) % size());
	}
}
