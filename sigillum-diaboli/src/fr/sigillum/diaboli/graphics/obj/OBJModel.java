package fr.sigillum.diaboli.graphics.obj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.asset.IAsset;
import fr.sigillum.diaboli.graphics.Drawer;
import fr.sigillum.diaboli.graphics.gl.Mesh;

public class OBJModel implements IAsset {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.graphics.obj");

	public static OBJModel load(Path path) {

		Array<Vector3f> positions = Array.ofType(Vector3f.class);
		Array<Vector2f> textureCoords = Array.ofType(Vector2f.class);
		Array<Vector3f> normals = Array.ofType(Vector3f.class);
		Array<IndexGroup> indices = Array.ofType(IndexGroup.class);

		Map<String, OBJMaterial> materials = new HashMap<>();

		String modelName = null;
		String materialName = null;
		OBJModel root = null;

		try (var reader = FileUtils.readBuffered(Files.newInputStream(path))) {

			String line = null;
			long currentSmoothGroup = -1;
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				// Ignore any commented lines.
				if (line.length() > 0 && line.charAt(0) == '#') {
					continue;
				}

				// Tokenize the line.
				var tokens = line.split("\\s+");

				if (tokens.length == 0) {
					continue;
				}

				var prefix = tokens[0];
				switch (prefix) {
					case "v":
						var vertex = new Vector3f(Float.valueOf(tokens[1]), Float.valueOf(tokens[2]),
								Float.valueOf(tokens[3]));
						positions.add(vertex);
						break;
					case "vt":
						var coords = new Vector2f(Float.valueOf(tokens[1]), Float.valueOf(tokens[2]));
						textureCoords.add(coords);
						break;
					case "vn":
						var normal = new Vector3f(Float.valueOf(tokens[1]), Float.valueOf(tokens[2]),
								Float.valueOf(tokens[3]));
						normals.add(normal);
						break;
					case "f":
						int size = tokens.length - 1;
						// Support for triangle fans.
						if (tokens.length == 5) {
							size = 6;
						}
						var indexGroup = new IndexGroup[size];
						for (int i = 0; i < tokens.length - 1; i++) {
							indexGroup[i] = new IndexGroup(tokens[i + 1], currentSmoothGroup);
						}
						// If we have 4 elements per face, build a triangle fan.
						if (tokens.length == 5) {
							indexGroup[3] = new IndexGroup(tokens[0 + 1], currentSmoothGroup);
							indexGroup[4] = new IndexGroup(tokens[2 + 1], currentSmoothGroup);
							indexGroup[5] = new IndexGroup(tokens[3 + 1], currentSmoothGroup);
						}
						indices.addAll(indexGroup);
						break;
					case "o":
						modelName = tokens[1];
						break;
					case "mtllib":
						var materialLibPath = path.getParent().resolve(tokens[1]);
						loadMaterialLibrary(materialLibPath, materials);
						break;
					case "usemtl":
						if (!indices.isEmpty()) {
							var material = materials.get(materialName);
							var child = new OBJModel(modelName, material, positions, textureCoords, normals, indices);
							if (root == null) {
								root = child;
							} else {
								root.children.add(child);
							}
							indices.clear();
						}
						materialName = tokens[1];
						break;
				}
			}
		} catch (IOException ex) {
			logger.error("Unable to load OBJ model from file '" + path + "'!", ex);
			return null;
		}

		if (modelName == null || modelName.isEmpty()) {
			modelName = FileUtils.getFileName(path);
		}

		// Push the final OBJ model.
		var material = materials.get(materialName);
		var child = new OBJModel(modelName, material, positions, textureCoords, normals, indices);
		if (root == null) {
			root = child;
		} else {
			root.children.add(child);
		}

		logger.info("Successfully loaded OBJ model from file '" + path + "'.");
		return root;
	}

	private static void loadMaterialLibrary(Path path, Map<String, OBJMaterial> materialCache) {

		OBJMaterial currentMaterial = null;

		try (var reader = FileUtils.readBuffered(Files.newInputStream(path))) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				// Ignore any commented lines.
				if (line.length() > 0 && line.charAt(0) == '#') {
					continue;
				}

				// Tokenize the line.
				var tokens = line.split("\\s+");

				if (tokens.length == 0) {
					continue;
				}

				var prefix = tokens[0];

				switch (prefix) {
				case "newmtl":
					// Create our new OBJ material.
					currentMaterial = new OBJMaterial(tokens[1]);
					materialCache.put(tokens[1], currentMaterial);
					break;
				case "map_Kd":
					var textureName = line.substring("map_Kd".length()).trim();
					currentMaterial.textureName = textureName;
					break;
				}
			}
		} catch (IOException ex) {
			logger.error("Unable to load OBJ materials from file '" + path + "'!", ex);
		}
	}

	private final String name;

	private final Array<Vector3f> positions;
	private final Array<Vector2f> textureCoords;
	private final Array<Vector3f> normals;
	private final Array<IndexGroup> indices;

	private final OBJMaterial material;

	private Array<OBJModel> children = Array.ofType(OBJModel.class);

	private Mesh mesh = null;

	private OBJModel(String name, OBJMaterial material, Array<Vector3f> positions, Array<Vector2f> textureCoords,
			Array<Vector3f> normals, Array<IndexGroup> indices) {
		this.name = name;
		this.material = material;
		this.positions = Array.of(positions);
		this.textureCoords = Array.of(textureCoords);
		this.normals = Array.of(normals);
		this.indices = Array.of(indices);
	}

	public void render(Drawer drawer) {
		if (mesh == null) {
			computeMesh();
		}

		for (var child : children) {
			child.render(drawer);
		}

		var texture = Assets.get().getTexture(AssetKey.of("texture", FileUtils.getFileName(material.textureName)));
		drawer.useTexture(texture);
		mesh.render();
	}

	void computeMesh() {
		if (indices.isEmpty() || positions.isEmpty()) {
			return;
		}

		var vertices = new Vertex[indices.size()];
		var indexArray = new int[indices.size()];

		for (var i = 0; i < vertices.length; ++i) {
			var index = indices.get(i);
			vertices[i] = new Vertex(positions.get(index.vIndex - 1), new Vector2f(), new Vector3f());

			if (index.vtIndex > IndexGroup.NO_VALUE) {
				var texCoords = textureCoords.get(index.vtIndex - 1);
				// OpenGL needs the Y-axis to go down, so Y = 1 - V.
				texCoords.set(texCoords.x, texCoords.y);
				vertices[i].textureCoords.set(texCoords);
			}

			if (index.vnIndex > IndexGroup.NO_VALUE) {
				var normal = normals.get(index.vnIndex - 1);
				vertices[i].normal.set(normal);
			}

			indexArray[i] = i;
		}

		this.mesh = new Mesh(vertices, indexArray);
	}

	@Override
	public void dispose() {
		if (mesh != null) {
			mesh.dispose();
		}

		this.mesh = null;
	}

	@Override
	public String toString() {
		return "OBJModel [ name= " + name + ", mesh= " + mesh + ", children= " + children + "]";
	}

	protected static class OBJMaterial {

		String name;

		String textureName;

		public OBJMaterial(String name) {
			this.name = name;
		}
	}

	protected static class IndexGroup {

		/**
		 * The no value for an index value &rarr;-1
		 */
		public static final int NO_VALUE = -1;

		/**
		 * The position index for the vertex.
		 */
		public int vIndex;
		/**
		 * The texture coordinate index for the vertex.
		 */
		public int vtIndex;
		/**
		 * The normal index for the vertex.
		 */
		public int vnIndex;
		/**
		 * The smoothing group for the index group.
		 */
		private long smoothingGroup;

		public IndexGroup(String group, long smoothingGroup) {
			var tokens = group.split("/");
			this.vIndex = tokens.length < 1 ? NO_VALUE : Integer.parseInt(tokens[0]);
			// Here we check that the texture coordinate index exist, as an OBJ file may
			// define normals without them.
			this.vtIndex = tokens.length < 2 ? NO_VALUE : tokens[1].isEmpty() ? NO_VALUE : Integer.parseInt(tokens[1]);
			this.vnIndex = tokens.length < 3 ? NO_VALUE : Integer.parseInt(tokens[2]);
			this.smoothingGroup = smoothingGroup;
		}

		/**
		 * Return the smoothing group index of the <code>IndexGroup</code>.
		 * 
		 * @return The smoothing group index, or 0 if it doesn't use smoothing.
		 */
		public long getSmoothingGroup() {
			// A normal index has been defined, so we don't use smoothing group.
			if (vnIndex >= 0) {
				return 0;
			}
			return smoothingGroup;
		}
	}
}
