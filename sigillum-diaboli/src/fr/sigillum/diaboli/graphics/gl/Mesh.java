package fr.sigillum.diaboli.graphics.gl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.MemoryUtil;

import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.graphics.obj.Vertex;

public class Mesh {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.graphics.gl");

	private static final int INVALID_ID = -1;

	private static final int DATA = 0;
	private static final int INDICES = 1;

	private int vao = INVALID_ID;

	private int[] vbo = { INVALID_ID, INVALID_ID };

	private FloatBuffer data = null;

	private Buffer indices = null;

	public Mesh(Vertex[] vertices, int[] indices) {
		createBuffers(vertices, indices);

		if (vao == INVALID_ID) {
			this.vao = GL30C.glGenVertexArrays();
		}

		GL30C.glBindVertexArray(vao);

		if (vbo[INDICES] == INVALID_ID) {
			this.vbo[INDICES] = GL15C.glGenBuffers();
		}

		GL15C.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo[INDICES]);
		bufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.indices, GL15C.GL_STATIC_DRAW);

		if (vbo[DATA] == INVALID_ID) {
			this.vbo[DATA] = GL15C.glGenBuffers();
		}

		GL15C.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo[DATA]);
		bufferData(GL15C.GL_ARRAY_BUFFER, data, GL15C.GL_STATIC_DRAW);

		GL30C.glEnableVertexAttribArray(0);
		GL30C.glEnableVertexAttribArray(1);
		GL30C.glEnableVertexAttribArray(2);

		GL30C.glVertexAttribPointer(0, 3, GL11C.GL_FLOAT, false, Vertex.BUFFER_SIZE * 4, 0);
		GL30C.glVertexAttribPointer(1, 1, GL11C.GL_FLOAT, false, Vertex.BUFFER_SIZE * 4, 12);
		GL30C.glVertexAttribPointer(2, 2, GL11C.GL_FLOAT, false, Vertex.BUFFER_SIZE * 4, 16);

		GL30C.glBindVertexArray(0);
	}

	public void render() {
		GL30C.glBindVertexArray(vao);
		GL15C.glDrawElements(GL11C.GL_TRIANGLES, indices.remaining(), getType(indices), 0);
	}

	private int getType(Buffer indices) {
		if (indices instanceof ByteBuffer) {
			return GL11C.GL_UNSIGNED_BYTE;
		} else if (indices instanceof ShortBuffer) {
			return GL11C.GL_UNSIGNED_SHORT;
		} else if (indices instanceof IntBuffer) {
			return GL11C.GL_UNSIGNED_INT;
		}

		logger.warning("Unrecognized buffer type: " + indices.getClass().getName());
		return GL11C.GL_UNSIGNED_INT;
	}

	public void createBuffers(Vertex[] vertices, int[] indices) {
		this.data = MemoryUtil.memAllocFloat(vertices.length * Vertex.BUFFER_SIZE);

		for (int i = 0; i < vertices.length; ++i) {
			data.put(vertices[i].position.x);
			data.put(vertices[i].position.y);
			data.put(vertices[i].position.z);

			data.put(1.0f);

			data.put(vertices[i].textureCoords.x);
			data.put(vertices[i].textureCoords.y);
		}

		data.flip();

		this.indices = createIndexBuffer(indices.length, indices);
	}

	private void bufferData(int type, Buffer buffer, int usage) {
		if (buffer instanceof ByteBuffer) {
			GL15C.glBufferData(type, (ByteBuffer) buffer, usage);
		} else if (buffer instanceof ShortBuffer) {
			GL15C.glBufferData(type, (ShortBuffer) buffer, usage);
		} else if (buffer instanceof IntBuffer) {
			GL15C.glBufferData(type, (IntBuffer) buffer, usage);
		} else if (buffer instanceof FloatBuffer) {
			GL15C.glBufferData(type, (FloatBuffer) buffer, usage);
		} else {
			logger.warning("Unrecognized buffer type: " + buffer.getClass().getName());
		}
	}

	private Buffer createIndexBuffer(int size, int[] indices) {
		var maxIndex = indices.length;

		if (maxIndex < 256) {
			var buffer = MemoryUtil.memAlloc(size);
			for (var i = 0; i < indices.length; ++i) {
				buffer.put((byte) indices[i]);
			}
			buffer.flip();
			return buffer;

		} else if (maxIndex < 65536) {
			var buffer = MemoryUtil.memAllocShort(size);
			for (var i = 0; i < indices.length; ++i) {
				buffer.put((short) indices[i]);
			}
			buffer.flip();
			return buffer;
		}

		var buffer = MemoryUtil.memAllocInt(size);
		for (var i = 0; i < indices.length; ++i) {
			buffer.put(indices[i]);
		}

		buffer.flip();
		return buffer;
	}

	public void dispose() {
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
	}
	
	@Override
	public String toString() {
		return "Mesh [ id= " + vao + " ]"; 
	}
}
