package fr.sigillum.diaboli.map.tiles;

public class Tile {
	
	private boolean solid;

	public Tile() {
		this(false);
	}
	
	public Tile(boolean solid) {
		this.solid = solid;
	}
	
	public boolean isSolid() {
		return solid;
	}
}
