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
import fr.sigillum.diaboli.asset.IAsset;

public class ShaderProgram implements IAsset {

	private static ShaderProgram CURRENT = null;
	
	private int id = INVALID_ID;

	public ShaderProgram(Path vertex, Path fragment) {
		createProgram(vertex, fragment);
	}

	public static ShaderProgram load(Path path) {
		try (var reader = FileUtils.readBuffered(Files.newInputStream(path))) {

			String line = null;
			Path vertex = path.getParent();
			Path fragment = path.getParent();
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				var parts = line.split("=");
				if (parts[0].equals("vertexShader")) {
					vertex = vertex.resolve(parts[1]);
				} else if (parts[0].equals("fragmentShader")) {
					fragment = fragment.resolve(parts[1]);
				}

			}

			return new ShaderProgram(vertex, fragment);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return null;
	}

	private void createProgram(Path vertex, Path fragment) {
		this.id = GL20C.glCreateProgram();

		try (var buf = FileUtils.readBuffered(Files.newInputStream(vertex))) {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = buf.readLine()) != null) {
				sb.append(line).append("\n");
			}

			int shaderID = GL20C.glCreateShader(GL20C.GL_VERTEX_SHADER);
			GL20C.glShaderSource(shaderID, sb);
			GL20C.glCompileShader(shaderID);

			if (GL20C.glGetShaderi(shaderID, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
				throw new RuntimeException(
						"An error occured when compiling vertex shader " + GL20C.glGetShaderInfoLog(shaderID, 1024));
			}

			GL20C.glAttachShader(id, shaderID);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		try (var buf = FileUtils.readBuffered(Files.newInputStream(fragment))) {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = buf.readLine()) != null) {
				sb.append(line).append("\n");
			}

			int shaderID = GL20C.glCreateShader(GL20C.GL_FRAGMENT_SHADER);
			GL20C.glShaderSource(shaderID, sb);
			GL20C.glCompileShader(shaderID);

			if (GL20C.glGetShaderi(shaderID, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
				throw new RuntimeException(
						"An error occured when compiling fragment shader " + GL20C.glGetShaderInfoLog(shaderID, 1024));
			}

			GL20C.glAttachShader(id, shaderID);
		} catch (IOException ex) {
			ex.printStackTrace();
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
		try (var stack = MemoryStack.stackPush()) {
			var loc = GL20C.glGetUniformLocation(id, name);
			GL20C.glUniformMatrix4fv(loc, false, matrix.get(stack.mallocFloat(16)));
		}
	}

	public void uniformVec3(String name, Vector3f value) {
		try (var stack = MemoryStack.stackPush()) {
			var loc = GL20C.glGetUniformLocation(id, name);
			GL20C.glUniform3f(loc, value.x(), value.y(), value.z());
		}
	}

	public void uniformInt(String name, int value) {
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
}
