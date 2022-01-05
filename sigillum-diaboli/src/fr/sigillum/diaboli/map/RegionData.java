package fr.sigillum.diaboli.map;

import java.io.IOException;

import javax.imageio.ImageIO;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.map.tiles.Tile;

public class RegionData {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.map.region");

	final Tile[] tiles;

	public static RegionData fromImage(int rx, int rz, String path) {
		var data = new RegionData();
		try {
			var image = ImageIO.read(RegionData.class.getResourceAsStream("/regions/" + path + ".png"));
			if (image.getWidth() != Region.SIZE || image.getHeight() != Region.SIZE) {
				throw new RuntimeException("The given image region '" + path + "' isn't 32x32 pixels!");
			}

			var pixels = new int[Region.SIZE * Region.SIZE];
			image.getRGB(0, 0, Region.SIZE, Region.SIZE, pixels, 0, Region.SIZE);

			// Read and decode image pixels to tiles.
			for (var x = 0; x < Region.SIZE; ++x) {
				for (var z = 0; z < Region.SIZE; ++z) {
					var i = x + z * Region.SIZE;
					if (pixels[i] == 0xff000000) {
						data.tiles[i] = new Tile(x, z);
					} else if (pixels[i] == 0xffffffff) {
						data.tiles[i] = new Tile(x, z, true);
					} else {
						throw new IOException("Illegal pixel data to decode: " + pixels[i]);
					}
				}
			}

		} catch (IOException ex) {
			logger.error("An error occured while trying to read image file '" + path + ".png'", ex);
		}

		return data;
	}

	private RegionData() {
		this.tiles = new Tile[Region.SIZE * Region.SIZE];
	}
}
