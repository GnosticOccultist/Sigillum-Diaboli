package fr.sigillum.diaboli.asset;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileExtensions;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.graphics.gl.ShaderProgram;
import fr.sigillum.diaboli.graphics.gl.Texture;
import fr.sigillum.diaboli.graphics.obj.OBJModel;

public class Assets {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.assets");

	private static final Array<String> TEXTURE_EXTENSIONS = Array.of(FileExtensions.PNG_FORMAT,
			FileExtensions.JPG_FORMAT, FileExtensions.JPEG_FORMAT);

	private static final AssetKey MISSING = AssetKey.of("texture", "missing");

	private final Map<AssetKey, IAsset> assets = new HashMap<>();

	private static Assets instance = null;

	public static Assets get() {
		if (instance == null) {
			initialize();
		}
		return instance;
	}

	public static void initialize() {
		instance = new Assets();
	}

	private Assets() {
		registerAll("shader", "shader", Paths.get("resources/assets/shaders"), ShaderProgram::load);
		registerAll("texture", TEXTURE_EXTENSIONS, Paths.get("resources/assets/textures"), Texture::new);
		registerAll("sprite", TEXTURE_EXTENSIONS, Paths.get("resources/assets/sprites"), Texture::new);
		registerAll("model", FileExtensions.OBJ_MODEL_FORMAT, Paths.get("resources/assets/models"), OBJModel::load);

		logger.info("Successfully registered " + assets.size() + " assets");
	}

	private void registerAll(String type, String extension, Path path, Function<Path, IAsset> loader) {
		registerAll(type, Array.of(extension), path, loader);
	}

	private void registerAll(String type, Array<String> extensions, Path path, Function<Path, IAsset> loader) {
		var assets = FileUtils.getFiles(path, extensions);
		for (var asset : assets) {
			try {
				var result = loader.apply(asset);
				var key = new AssetKey(type, FileUtils.getFileName(asset));
				this.assets.put(key, result);

			} catch (Exception ex) {
				logger.error("Failed to load asset with path '" + asset + "' of type " + type, ex);
			}
		}
	}

	public Texture getTexture(AssetKey key) {
		return getSafe(Texture.class, key).orElseGet(() -> getMissingTexture());
	}

	public OBJModel getModel(AssetKey key) {
		return getSafe(OBJModel.class, key).orElseThrow();
	}

	public ShaderProgram getShader(AssetKey key) {
		return getSafe(ShaderProgram.class, key).orElseThrow();
	}

	public <A extends IAsset> Optional<A> getSafe(Class<A> type, AssetKey key) {
		return Optional.ofNullable(assets.get(key)).map(type::cast);
	}

	private Texture getMissingTexture() {
		return getTexture(MISSING);
	}

	public void dispose() {
		this.assets.values().forEach(IAsset::dispose);
		this.assets.clear();
	}

	public final static class AssetKey {

		/**
		 * The type of asset.
		 */
		private String type;
		/**
		 * The asset name.
		 */
		private String name;

		/**
		 * Create a new <code>AssetKey</code> of the provided type and name.
		 * 
		 * @param type The type of asset.
		 * @param name The asset name.
		 * @return A new asset key.
		 */
		public static AssetKey of(String type, String name) {
			return new AssetKey(type, name);
		}

		AssetKey(String type, String name) {
			this.type = type;
			this.name = name;
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, name);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}

			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}

			var other = (AssetKey) obj;
			return type.equals(other.type) && name.equals(other.name);
		}

		@Override
		public String toString() {
			return type + ":" + name;
		}
	}
}
