package fr.sigillum.diaboli.map.generator;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.map.generator.Graph.Node;

public class Topology {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.map.generator");

	private Graph graph;

	private final Map<Vector2f, Node> ptToNode = new HashMap<>();
	final Map<Node, Vector2f> nodeToPt = new HashMap<>();

	final Array<Node> inner = Array.ofType(Node.class);

	final Array<Node> outer = Array.ofType(Node.class);

	public Topology(Village village) {

		this.graph = new Graph();

		var border = village.border.getShape();

		for (var patch : village.patches) {
			var withinCity = patch.withinCity;

			var v1 = patch.getShape().last();
			var n1 = processPoint(v1);

			for (var vert : patch.getShape()) {
				var v0 = v1;
				v1 = vert;
				var n0 = n1;
				n1 = processPoint(v1);

				if (n0 != null && !border.contains(v0)) {
					if (withinCity) {
						inner.add(n0);
					} else {
						outer.add(n0);
					}
				}
				if (n1 != null && !border.contains(v1)) {
					if (withinCity) {
						inner.add(n1);
					} else {
						outer.add(n1);
					}
				}
				if (n0 != null && n1 != null) {
					n0.link(n1, v0.distance(v1), true);
				}
			}
		}

		logger.info("Computed " + ptToNode.size() + " nodes from points!");
	}

	public Array<Vector2f> buildPath(Vector2f from, Vector2f to) {
		return buildPath(from, to, Array.empty());
	}

	public Array<Vector2f> buildPath(Vector2f from, Vector2f to, Array<Node> exclude) {
		var startNode = ptToNode.get(from);
		var endNode = ptToNode.get(to);
		if (startNode == null || endNode == null) {
			logger.warning("Missing starting or ending node!");
			return Array.empty();
		}
		
		var path = graph.aStar(startNode, endNode, exclude);
		if (path == null) {
			return Array.empty();
		}

		var result = Array.ofType(Vector2f.class);
		for (var node : path) {
			result.add(nodeToPt.get(node));
		}

		return result;
	}

	private Node processPoint(Vector2f vertex) {
		Node node = null;
		if (ptToNode.containsKey(vertex)) {
			node = ptToNode.get(vertex);
		} else {
			node = graph.add(null);
			ptToNode.put(vertex, node);
			nodeToPt.put(node, vertex);
		}

		return node;
	}
}
