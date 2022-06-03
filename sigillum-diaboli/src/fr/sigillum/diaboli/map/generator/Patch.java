package fr.sigillum.diaboli.map.generator;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;
import fr.sigillum.diaboli.map.generator.Voronoi.Region;

public class Patch {
	
	private final Polygon shape;
	boolean withinCity;

	Patch(Array<Vector2f> vertices) {
		this.shape = new Polygon(vertices);
	}
	
	public Polygon getShape() {
		return shape;
		
	}
	
	public boolean isEmpty() {
		return shape.isEmpty();
	}

	public static Patch from(Region region) {
		var vertices = Array.ofType(Vector2f.class);
		for (var i = 0; i < region.vertices.size(); ++i) {
			vertices.add(region.vertices.get(i).center);
		}
		return new Patch(vertices);
	}

	@Override
	public String toString() {
		return "Patch " + shape.toString();
	}
}
