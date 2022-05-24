package fr.sigillum.diaboli.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import fr.sigillum.diaboli.asset.Assets;
import fr.sigillum.diaboli.asset.Assets.AssetKey;
import fr.sigillum.diaboli.graphics.gl.ShaderProgram;
import fr.sigillum.diaboli.graphics.gl.Texture;

public class Drawer {

	private static final int INVALID_ID = -1;

	private static final int DATA = 0;
	private static final int INDICES = 1;

	public static final AssetKey GRASS = AssetKey.of("texture", "grass");
	
	public static final AssetKey DEFAULT_SHADER = AssetKey.of("shader", "base");

	private FloatBuffer data = null;

	private IntBuffer indices = null;

	private volatile boolean drawing = false;

	private int vao = INVALID_ID;

	private int[] vbo = { INVALID_ID, INVALID_ID };

	private Matrix4f projectionMatrix, viewMatrix, modelMatrix, projViewMatrix;

	private final FrustumIntersection frustum = new FrustumIntersection();

	private int currentIndex;

	public Drawer(int rectangleSize) {
		this.data = MemoryUtil.memAllocFloat(16 * rectangleSize);
		this.indices = MemoryUtil.memAllocInt(6 * rectangleSize);
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
		this.modelMatrix = new Matrix4f();
		this.projViewMatrix = new Matrix4f();
	}

	public void begin() {
		if (drawing) {
			throw new IllegalStateException("The drawer is already in drawing mode!");
		}

		if (vao == INVALID_ID) {
			this.vao = GL30C.glGenVertexArrays();
		}

		GL30C.glBindVertexArray(vao);

		if (vbo[DATA] == INVALID_ID) {
			this.vbo[DATA] = GL15C.glGenBuffers();
			GL15C.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo[DATA]);

			// Initialize the buffer size.
			GL15C.glBufferData(GL15.GL_ARRAY_BUFFER, data.capacity() * 4, GL15C.GL_DYNAMIC_DRAW);
		}

		if (vbo[INDICES] == INVALID_ID) {
			this.vbo[INDICES] = GL15C.glGenBuffers();
			GL15C.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo[INDICES]);

			// Initialize the buffer size.
			GL15C.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 4, GL15C.GL_DYNAMIC_DRAW);
		}

		var program = Assets.get().getShader(DEFAULT_SHADER);
		program.use();

		this.drawing = true;
	}

	public void drawRectangle(float x, float y0, float y1, float z) {
		drawRectangle(x, y0, y1, z, 1.0f);
	}

	public void drawSprite(float x, float width, float y, float z, float height) {
		drawSprite(x, width, y, z, height, 1.0f);
	}

	public void drawSprite(float x, float width, float y, float z, float height, float brightness) {
		var i = currentIndex;
		this.indices.put(i).put(i + 1).put(i + 3)
				.put(i + 3).put(i + 1).put(i + 2);
		currentIndex += 4;

		drawVertex(x, y + 2, z, brightness, 0f, 0f);
		drawVertex(x, y, z, brightness, 0f, 1f);
		drawVertex(x + width, y, z + height, brightness, 1f, 1.0f);
		drawVertex(x + width, y + 2, z + height, brightness, 1f, 0.0f);
	}

	public void drawVertPlane(float x0, float z0, float x1, float z1, float y, float height) {
		var i = currentIndex;
		this.indices.put(i).put(i + 1).put(i + 2)
				.put(i).put(i + 2).put(i + 3);
		currentIndex += 4;

		drawVertex(x0, y + height, z0, 0.7F, 0, 0);
		drawVertex(x0, y, z0, 0.7F, 0, 1);
		drawVertex(x1, y, z1, 0.7F, 1, 1);
		drawVertex(x1, y + height, z1, 0.7F, 1, 0);
	}

	public void drawRectangle(float x, float y0, float y1, float z, float brightness) {
		var i = currentIndex;
		this.indices.put(i).put(i + 1).put(i + 2)
				.put(i).put(i + 2).put(i + 3);
		currentIndex += 4;

		drawVertex(x, y0, z, brightness, 0, 0);
		drawVertex(x, y0, z + 1, brightness, 0, 1);
		drawVertex(x + 1, y1, z + 1, brightness, 1, 1);
		drawVertex(x + 1, y1, z, brightness, 1, 0);
	}

	public void drawVertex(float x, float y, float z, float brightness, float u, float v) {
		this.data.put(x).put(y).put(z);
		this.data.put(brightness);
		this.data.put(u).put(v);
	}

	public void drawVertex(float x, float y, float z, float u, float v) {
		drawVertex(x, y, z, 1.0f, u, v);
	}

	public void useTexture() {
		Texture tex = Assets.get().getTexture(GRASS);
		useTexture(tex);
	}

	public void useTexture(Texture texture) {
		var program = Assets.get().getShader(DEFAULT_SHADER);
		program.use();

		texture.bind(0);
		defaultShader().uniformInt("texture_sampler", 0);
	}

	public void unbindTexture() {
		Texture tex = Assets.get().getTexture(GRASS);
		tex.unbind();
		defaultShader().uniformInt("texture_sampler", -1);
	}

	public void projectionMatrix(int width, int height) {
		var program = Assets.get().getShader(DEFAULT_SHADER);
		program.use();

		var ratio = (float) width / (float) height;
		this.projectionMatrix.identity().perspective(70.0F, ratio, 0.1f, 1000.0f);

		defaultShader().matrix4f("projectionMatrix", projectionMatrix);
	}

	public void viewMatrix(Vector3f position, Vector2f rotation) {
		var program = Assets.get().getShader(DEFAULT_SHADER);
		program.use();

		this.viewMatrix.identity().rotate((float) Math.toRadians(rotation.x()), new Vector3f(1, 0, 0))
				.rotate((float) Math.toRadians(rotation.y()), new Vector3f(0, 1, 0))
				.translate(-position.x(), -position.y(), -position.z());

		defaultShader().matrix4f("viewMatrix", viewMatrix);
		defaultShader().uniformVec3("camPos", position);

		projViewMatrix.identity().set(projectionMatrix).mul(viewMatrix);
		frustum.set(projViewMatrix, false);
	}

	public void viewMatrixBillboard(Vector3f position, Vector2f rotation) {
		var program = Assets.get().getShader(DEFAULT_SHADER);
		program.use();

		this.viewMatrix.identity().rotate((float) Math.toRadians(rotation.x()), new Vector3f(1, 0, 0))
				.rotate((float) Math.toRadians(rotation.y()), new Vector3f(0, 1, 0))
				.translate(-position.x(), -position.y(), -position.z());

		this.modelMatrix.lookAt(new Vector3f(0, 0, 0), position, new Vector3f(0, 1, 0));
		this.modelMatrix.setRotationXYZ(0, modelMatrix.getNormalizedRotation(new Quaternionf()).y, 0);

		defaultShader().matrix4f("viewMatrix", viewMatrix);
		defaultShader().matrix4f("model", modelMatrix);

		projViewMatrix.identity().set(projectionMatrix).mul(viewMatrix);
		frustum.set(projViewMatrix, false);
	}

	public void modelMatrix() {
		this.modelMatrix.identity();
		defaultShader().matrix4f("model", modelMatrix);
	}
	
	private ShaderProgram defaultShader() {
		var program = Assets.get().getShader(DEFAULT_SHADER);
		program.use();
		return program;
	}

	public void end() {
		if (!drawing) {
			throw new IllegalStateException("The drawer hasn't been started!");
		}

		this.data.flip();
		this.indices.flip();

		assert vao != INVALID_ID;
		GL30C.glBindVertexArray(vao);

		assert vbo[INDICES] != INVALID_ID;
		GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, vbo[INDICES]);
		GL15C.glBufferSubData(GL15C.GL_ELEMENT_ARRAY_BUFFER, 0, indices);

		assert vbo[DATA] != INVALID_ID;
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vbo[DATA]);
		GL15C.glBufferSubData(GL15C.GL_ARRAY_BUFFER, 0, data);

		GL30C.glEnableVertexAttribArray(0);
		GL30C.glEnableVertexAttribArray(1);
		GL30C.glEnableVertexAttribArray(2);

		GL30C.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
		GL30C.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 24, 12);
		GL30C.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 24, 16);

		GL15C.glDrawElements(GL11C.GL_TRIANGLES, indices.remaining(), GL11.GL_UNSIGNED_INT, 0);

		GL30C.glBindVertexArray(0);

		this.data.clear();
		this.indices.clear();

		this.currentIndex = 0;
		this.drawing = false;
	}

	public void cleanup() {
		GL30C.glDeleteVertexArrays(vao);
		this.vao = INVALID_ID;

		GL15C.glDeleteBuffers(vbo[DATA]);
		this.vbo[DATA] = INVALID_ID;
		MemoryUtil.memFree(data);
		this.data = null;

		GL15C.glDeleteBuffers(vbo[INDICES]);
		this.vbo[INDICES] = INVALID_ID;
		MemoryUtil.memFree(indices);
		this.indices = null;

		this.currentIndex = 0;
		this.drawing = false;
	}

	public FrustumIntersection getFrustum() {
		return frustum;
	}
}
