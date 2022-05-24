package fr.sigillum.diaboli.graphics.gl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import fr.alchemy.utilities.file.FileUtils;
import fr.alchemy.utilities.logging.FactoryLogger;
import fr.alchemy.utilities.logging.Logger;
import fr.sigillum.diaboli.asset.IAsset;

public class ShaderProgram implements IAsset {

	private static final Logger logger = FactoryLogger.getLogger("sigillum-diaboli.graphics.gl");

	/**
	 * The currently used shader program by the OpenGL context, or null.
	 */
	private static ShaderProgram CURRENT = null;

	/**
	 * The name of the shader program.
	 */
	private final String name;
	/**
	 * The shader program identifier.
	 */
	private int id = INVALID_ID;

	private ShaderProgram(String name, Path vertex, Path fragment) {
		this.name = name;
		createProgram(vertex, fragment);
	}

	public static ShaderProgram load(Path path) {
		try (var reader = FileUtils.readBuffered(Files.newInputStream(path))) {

			String line = null;
			var vertex = path.getParent();
			var fragment = path.getParent();
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				var parts = line.split("=");
				if (parts[0].equals("vertexShader")) {
					vertex = vertex.resolve(parts[1]);
				} else if (parts[0].equals("fragmentShader")) {
					fragment = fragment.resolve(parts[1]);
				}

			}

			var name = FileUtils.getFileName(path);
			return new ShaderProgram(name, vertex, fragment);
		} catch (IOException ex) {
			logger.error("Failed to load ShaderProgram from file: '" + path + "'!", ex);
		}

		return null;
	}

	private void createProgram(Path vertexPath, Path fragmentPath) {
		this.id = GL20C.glCreateProgram();

		try (var buf = FileUtils.readBuffered(Files.newInputStream(vertexPath))) {
			var sb = new StringBuilder();
			String line = null;
			while ((line = buf.readLine()) != null) {
				sb.append(line).append("\n");
			}

			var shaderID = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER);
			GL20C.glShaderSource(shaderID, sb);
			GL20C.glCompileShader(shaderID);

			if (GL20C.glGetShaderi(shaderID, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
				throw new RuntimeException(
						"An error occured when compiling vertex shader " + GL20C.glGetShaderInfoLog(shaderID, 1024));
			}

			GL20C.glAttachShader(id, shaderID);
		} catch (IOException ex) {
			logger.error("Failed to load vertex shader source from file: '" + vertexPath + "'!", ex);
		}

		try (var buf = FileUtils.readBuffered(Files.newInputStream(fragmentPath))) {
			var sb = new StringBuilder();
			String line = null;
			while ((line = buf.readLine()) != null) {
				sb.append(line).append("\n");
			}

			var shaderID = GL20C.glCreateShader(GL20C.GL_FRAGMENT_SHADER);
			GL20C.glShaderSource(shaderID, sb);
			GL20C.glCompileShader(shaderID);

			if (GL20C.glGetShaderi(shaderID, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
				throw new RuntimeException(
						"An error occured when compiling fragment shader " + GL20C.glGetShaderInfoLog(shaderID, 1024));
			}

			GL20C.glAttachShader(id, shaderID);
		} catch (IOException ex) {
			logger.error("Failed to load vertex shader source from file: '" + fragmentPath + "'!", ex);
		}

		GL20C.glLinkProgram(id);
		if (GL20C.glGetProgrami(id, GL20C.GL_LINK_STATUS) == GL11C.GL_FALSE) {
			throw new RuntimeException(
					"An error occured when linking shader program " + GL20C.glGetProgramInfoLog(id, 1024));
		}

		GL20C.glValidateProgram(id);
		if (GL20C.glGetProgrami(id, GL20C.GL_VALIDATE_STATUS) == GL11C.GL_FALSE) {
			throw new RuntimeException(
					"An error occured when validating shader program " + GL20C.glGetProgramInfoLog(id, 1024));
		}
	}

	public void use() {
		if (CURRENT != this) {
			GL20C.glUseProgram(id);
			CURRENT = this;
		}
	}

	public void matrix4f(String name, Matrix4f matrix) {
		use();
		try (var stack = MemoryStack.stackPush()) {
			var loc = GL20C.glGetUniformLocation(id, name);
			GL20C.glUniformMatrix4fv(loc, false, matrix.get(stack.mallocFloat(16)));
		}
	}

	public void uniformVec3(String name, Vector3f value) {
		use();
		try (var stack = MemoryStack.stackPush()) {
			var loc = GL20C.glGetUniformLocation(id, name);
			GL20C.glUniform3f(loc, value.x(), value.y(), value.z());
		}
	}

	public void uniformInt(String name, int value) {
		use();
		try (var stack = MemoryStack.stackPush()) {
			var loc = GL20C.glGetUniformLocation(id, name);
			GL20C.glUniform1i(loc, value);
		}
	}

	@Override
	public void dispose() {
		if (CURRENT == this) {
			CURRENT = null;
		}

		GL20C.glDeleteProgram(id);
		this.id = INVALID_ID;
	}
	
	@Override
	public String toString() {
		return "ShaderProgram [name= " + name + ", id= " + id + "]";
	}
}
