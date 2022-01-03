package fr.sigillum.diaboli.map;

import fr.sigillum.diaboli.graphics.Drawer;

public class Region {

	public static final int SIZE = 32;

	private int x, z;

	public Region(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public void render(Drawer drawer) {
		var rx = x * SIZE;
		var rz = z * SIZE;

		for (var x = 0; x < SIZE; ++x) {
			for (var z = 0; z < SIZE; ++z) {
				drawer.drawRectangle(rx + x, 0, 0, rz + z);
			}
		}
	}
}
