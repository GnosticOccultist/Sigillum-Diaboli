package fr.sigillum.diaboli.map.generator;

import java.util.HashMap;
import java.util.Map;

import fr.alchemy.utilities.collections.array.Array;

public class Graph {

	private final Array<Node> nodes = Array.ofType(Node.class);

	public Node add(Node node) {
		if (node == null) {
			node = new Node();
		}

		this.nodes.add(node);
		return node;
	}

	public void remove(Node node) {
		node.unlinkAll();
		nodes.remove(node);
	}

	public Array<Node> aStar(Node start, Node goal, Array<Node> exclude) {
		var closedSet = Array.ofType(Node.class);
		if (exclude != null) {
			closedSet.addAll(exclude);
		}

		var openSet = Array.ofType(Node.class);
		openSet.add(start);
		var cameFrom = new HashMap<Node, Node>();

		var gCost = new HashMap<Node, Float>();
		gCost.put(start, 0.0f);

		while (openSet.size() > 0) {
			var current = openSet.poll();
			if (current == goal) {
				return buildPath(cameFrom, current);
			}

			openSet.remove(current);
			closedSet.add(current);

			var currentCost = gCost.get(current);
			for (var neighbour : current.links.keySet()) {
				if (closedSet.contains(neighbour)) {
					continue;
				}

				var cost = currentCost + current.links.get(neighbour);
				if (!openSet.contains(neighbour)) {
					openSet.add(neighbour);
				} else if (cost >= gCost.get(neighbour)) {
					continue;
				}

				cameFrom.put(neighbour, current);
				gCost.put(neighbour, cost);
			}
		}

		return null;
	}

	private Array<Node> buildPath(Map<Node, Node> cameFrom, Node current) {
		var path = Array.ofType(Node.class);

		while (cameFrom.containsKey(current)) {
			path.add(current = cameFrom.get(current));
		}

		return path;
	}
	
	public float computeCost(Array<Node> path) {
		if (path.size() < 2) {
			return 0;
		}
		
		var cost = 0.0f;
		var current = path.first();
		var next = path.get(1);
		for (var i = 0; i < path.size() - 1; ++i) {
			if (current.links.containsKey(next)) {
				cost += current.links.get(next);
			} else {
				return Float.NaN;
			}
			current = next;
			next = path.get(i + 1);
		}
		
		return cost;
	}

	class Node {

		private final Map<Node, Float> links = new HashMap<>();

		public void link(Node node, float cost, boolean sym) {
			this.links.put(node, cost);
			if (sym) {
				node.links.put(this, cost);
			}
		}

		public void unlink(Node node, boolean sym) {
			this.links.remove(node);
			if (sym) {
				node.links.remove(this);
			}
		}

		public void unlinkAll() {
			for (var node : links.keySet()) {
				unlink(node, true);
			}
		}
	}
}
