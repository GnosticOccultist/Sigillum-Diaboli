package fr.sigillum.diaboli.graphics;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.file.FileUtils;

public class Drawer {

	private static final int INVALID_ID = -1;

	private FloatBuffer data = null;

	private volatile boolean drawing = false;

	private int vao = INVALID_ID;

	private int vbo = INVALID_ID;

	int vertexCount;

	private int program = INVALID_ID;

	public Drawer(int vertexSize) {
		this.data = MemoryUtil.memAllocFloat(3 * vertexSize);
	}

	public void begin() {
		if (drawing) {
			throw new IllegalStateException("The drawer is already in drawing mode!");
		}

		if (vao == INVALID_ID) {
			this.vao = GL30C.glGenVertexArrays();
		}

		GL30C.glBindVertexArray(vao);

		if (vbo == INVALID_ID) {
			this.vbo = GL15C.glGenBuffers();
		}

		GL15C.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		
		if (program == INVALID_ID) {
			createProgram();
		}
		
		GL20C.glUseProgram(program);

		this.drawing = true;
	}

	public void drawVertex(float x, float y, float z) {
		this.data.put(x).put(y).put(z);
		this.vertexCount++;
	}

	public void end() {
		if (!drawing) {
			throw new IllegalStateException("The drawer hasn't been started!");
		}

		this.data.flip();

		assert vao != INVALID_ID;
		GL30C.glBindVertexArray(vao);

		assert vbo != INVALID_ID;
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vbo);
		GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, data, GL15C.GL_DYNAMIC_DRAW);

		GL30C.glEnableVertexAttribArray(0);

		GL30C.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

		GL15C.glDrawArrays(GL11.GL_TRIANGLES, 0, 12);

		this.data.clear();
		this.vertexCount = 0;
		this.drawing = false;
	}

	public void cleanup() {
		MemoryUtil.memFree(data);
		this.data = null;
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
				throw new RuntimeException("An error occured when compiling vertex shader " + GL20C.glGetShaderInfoLog(id, 1024));
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
				throw new RuntimeException("An error occured when compiling fragment shader " + GL20C.glGetShaderInfoLog(id, 1024));
			}

			GL20C.glAttachShader(program, id);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		GL20C.glLinkProgram(program);
	}
}
