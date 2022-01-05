package fr.sigillum.diaboli.map.tiles;

public class Tile {

	private int x, z;
	private boolean solid;

	public Tile(int x, int z) {
		this(x, z, false);
	}
	
	public Tile(int x, int z, boolean solid) {
		this.x = x;
		this.z = z;
		this.solid = solid;
	}
	
	public boolean isSolid() {
		return solid;
	}
}
