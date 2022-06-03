package fr.sigillum.diaboli.map.generator;

import java.util.function.Predicate;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.collections.array.ArrayCollectors;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;

public class CurtainWall {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.map.generator");

	private final Polygon shape;

	private final Array<Patch> patches = Array.ofType(Patch.class);

	private final Array<Vector2f> gates = Array.ofType(Vector2f.class);

	public CurtainWall(Village village, Array<Patch> patches, Array<Vector2f> reserved) {
		this.patches.addAll(patches);

		if (patches.size() == 1) {
			this.shape = patches.first().getShape();
		} else {
			this.shape = Village.findCircumference(patches);
		}

		buildGate(village, reserved);
	}

	private void buildGate(Village village, Array<Vector2f> reserved) {
		// Entrances are vertices of the walls with more than 1 adjacent inner ward
		// so that a street could connect it to the city center.
		Array<Vector2f> entrances = null;
		if (patches.size() > 1) {
			Predicate<Vector2f> first = v -> patches.stream().filter(p -> p.getShape().contains(v)).count() > 1;
			Predicate<Vector2f> second = v -> !reserved.contains(v) && first.test(v);
			entrances = shape.stream().filter(second).collect(ArrayCollectors.toArray(Vector2f.class));
		} else {
			entrances = shape.stream().filter(v -> !reserved.contains(v))
					.collect(ArrayCollectors.toArray(Vector2f.class));
		}

		if (entrances.isEmpty()) {
			throw new RuntimeException("Bad walled area shape!");
		}

		logger.info("Found " + entrances.size() + " viable entrances!");

		do {
			var index = village.randInt(entrances.size());
			var gate = entrances.get(index);
			gates.add(gate);

			// Removing neighbouring entrances to ensure
			// that no gates are too close.
			if (index == 0) {
				entrances.fastRemove(0);
				if (entrances.size() > 2) {
					entrances.remove(1);
				}
				entrances.pop();
			} else if (index == entrances.size() - 1) {
				entrances.fastRemove(index - 1);
				if (entrances.size() > index) {
					entrances.remove(index);
				}
				entrances.poll();
			} else {
				entrances.fastRemove(index - 1);
				entrances.fastRemove(index);
				if (entrances.size() > index + 1) {
					entrances.fastRemove(index + 1);
				}
			}

		} while (entrances.size() >= 3);

		if (gates.isEmpty()) {
			throw new RuntimeException("Bad walled area shape!");
		}

		logger.info("Successfully generated " + gates.size() + " gates!");
	}

	public Polygon getShape() {
		return shape;
	}

	public float getRadius() {
		var radius = 0.0f;
		for (var vertex : shape) {
			radius = Math.max(radius, vertex.length());
		}
		return radius;
	}

	public Array<Vector2f> gates() {
		return gates;
	}
}
