package fr.sigillum.diaboli.graphics;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.file.FileUtils;

public class Drawer {

	private static final int INVALID_ID = -1;

	private static final int DATA = 0;
	private static final int INDICES = 1;

	private FloatBuffer data = null;

	private IntBuffer indices = null;

	private volatile boolean drawing = false;

	private int vao = INVALID_ID;

	private int[] vbo = { INVALID_ID, INVALID_ID };

	private Matrix4f projectionMatrix, viewMatrix;

	private int program = INVALID_ID;

	private int currentIndex;

	public Drawer(int rectangleSize) {
		this.data = MemoryUtil.memAllocFloat(16 * rectangleSize);
		this.indices = MemoryUtil.memAllocInt(6 * rectangleSize);
		this.projectionMatrix = new Matrix4f();
		this.viewMatrix = new Matrix4f();
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
		}

		GL15C.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo[DATA]);

		if (vbo[INDICES] == INVALID_ID) {
			this.vbo[INDICES] = GL15C.glGenBuffers();
		}

		GL15C.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo[INDICES]);

		if (program == INVALID_ID) {
			createProgram();
		}

		GL20C.glUseProgram(program);

		this.drawing = true;
	}

	public void drawRectangle(float x, float y0, float y1, float z) {
		var i = currentIndex;
		this.indices.put(i).put(i + 1).put(i + 2)
				.put(i).put(i + 2).put(i + 3);
		currentIndex += 4;

		drawVertex(x, y0, z, 0.8f);
		drawVertex(x, y0, z + 1, 0.8F);
		drawVertex(x + 1, y1, z + 1, 0.5F);
		drawVertex(x + 1, y1, z, 0.5F);
	}

	public void drawVertex(float x, float y, float z, float brightness) {
		this.data.put(x).put(y).put(z);
		this.data.put(brightness);
	}

	public void projectionMatrix(int width, int height) {
		var ratio = (float) width / (float) height;
		this.projectionMatrix.perspective(70.0F, ratio, 0.1f, 1000.0f);

		matrix4f("projectionMatrix", projectionMatrix);
	}

	public void viewMatrix(Vector3f position, Vector2f rotation) {
		this.viewMatrix.identity().rotate((float) Math.toRadians(rotation.x()), new Vector3f(1, 0, 0))
				.rotate((float) Math.toRadians(rotation.y()), new Vector3f(0, 1, 0))
				.translate(-position.x(), -position.y(), -position.z());

		matrix4f("viewMatrix", viewMatrix);
	}

	private void matrix4f(String name, Matrix4f matrix) {
		try (var stack = MemoryStack.stackPush()) {
			var loc = GL20C.glGetUniformLocation(program, name);
			GL20C.glUniformMatrix4fv(loc, false, matrix.get(stack.mallocFloat(16)));
		}
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
		GL15C.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indices, GL15C.GL_DYNAMIC_DRAW);

		assert vbo[DATA] != INVALID_ID;
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vbo[DATA]);
		GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, data, GL15C.GL_DYNAMIC_DRAW);

		GL30C.glEnableVertexAttribArray(0);
		GL30C.glEnableVertexAttribArray(1);

		GL30C.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 16, 0);
		GL30C.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 16, 12);

		GL15C.glDrawElements(GL11C.GL_TRIANGLES, indices.remaining(), GL11.GL_UNSIGNED_INT, 0);

		this.data.clear();
		this.indices.clear();

		this.currentIndex = 0;
		this.drawing = false;
	}

	public void cleanup() {
		MemoryUtil.memFree(data);
		this.data = null;
		MemoryUtil.memFree(indices);
		this.indices = null;

		this.currentIndex = 0;
		this.drawing = false;
	}

	private void createProgram() {
		program = GL20C.glCreateProgram();

		try (var buf = FileUtils.readBuffered(Drawer.class.getResourceAsStream("/shaders/base.vert"))) {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = buf.readLine()) != null) {
				sb.append(line).append("\n");
			}

			int id = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER);
			GL20C.glShaderSource(id, sb);
			GL20C.glCompileShader(id);

			if (GL20C.glGetShaderi(id, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
				throw new RuntimeException(
						"An error occured when compiling vertex shader " + GL20C.glGetShaderInfoLog(id, 1024));
			}

			GL20C.glAttachShader(program, id);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		try (var buf = FileUtils.readBuffered(Drawer.class.getResourceAsStream("/shaders/base.frag"))) {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = buf.readLine()) != null) {
				sb.append(line).append("\n");
			}

			int id = GL20C.glCreateShader(GL33.GL_FRAGMENT_SHADER);
			GL20C.glShaderSource(id, sb);
			GL20C.glCompileShader(id);

			if (GL20C.glGetShaderi(id, GL30.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
				throw new RuntimeException(
						"An error occured when compiling fragment shader " + GL20C.glGetShaderInfoLog(id, 1024));
			}

			GL20C.glAttachShader(program, id);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		GL20C.glLinkProgram(program);
	}
}
