package fr.sigillum.diaboli.graphics.obj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.joml.Vector2f;
import org.joml.Vector3f;

import fr.alchemy.utilities.collections.array.Array;
import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.asset.IAsset;
import fr.sigillum.diaboli.graphics.gl.Mesh;

public class OBJModel implements IAsset {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.graphics.obj");

	public static OBJModel load(Path path) {

		Array<Vector3f> positions = Array.ofType(Vector3f.class);
		Array<Vector2f> textureCoords = Array.ofType(Vector2f.class);
		Array<Vector3f> normals = Array.ofType(Vector3f.class);
		Array<IndexGroup> indices = Array.ofType(IndexGroup.class);

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
				}
			}
		} catch (IOException ex) {
			logger.error("Unable to load OBJ model from file '" + path + "'!", ex);
			return null;
		}

		logger.info("Successfully loaded OBJ model from file '" + path + "'.");
		var model = new OBJModel(positions, textureCoords, normals, indices);
		return model;
	}

	private final Array<Vector3f> positions;
	private final Array<Vector2f> textureCoords;
	private final Array<Vector3f> normals;
	private final Array<IndexGroup> indices;

	private Mesh mesh = null;

	private OBJModel(Array<Vector3f> positions, Array<Vector2f> textureCoords, Array<Vector3f> normals,
			Array<IndexGroup> indices) {
		this.positions = positions;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
	}

	public void render() {
		if (mesh == null) {
			computeMesh();
		}

		mesh.render();
	}

	void computeMesh() {
		var vertices = new Vertex[indices.size()];
		var indexArray = new int[indices.size()];

		for (var i = 0; i < vertices.length; ++i) {
			var index = indices.get(i);
			vertices[i] = new Vertex(positions.get(index.vIndex - 1), new Vector2f(), new Vector3f());

			if (index.vtIndex > IndexGroup.NO_VALUE) {
				var texCoords = textureCoords.get(index.vtIndex - 1);
				// OpenGL needs the Y-axis to go down, so Y = 1 - V.
				texCoords.set(texCoords.x, 1F - texCoords.y);
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

		mesh = null;
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
