package fr.sigillum.diaboli.graphics.gl;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryUtil;

import fr.sigillum.diaboli.asset.IAsset;

public class Texture implements IAsset {
	
	private int width;
	private int height;
	private int id;

	public Texture(Path path) {
		int[] pixels = null;
		try {
			var img = ImageIO.read(Files.newInputStream(path));
			this.width = img.getWidth();
			this.height = img.getHeight();
			pixels = new int[width * height];
			img.getRGB(0, 0, width, height, pixels, 0, width);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Read pixels from ARGB format.
		int[] data = new int[width * height];
		for (int i = 0; i < pixels.length; ++i) {
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = (pixels[i] & 0xff);

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
		buffer.put(data);
		buffer.flip();

		this.id = GL11.glGenTextures();
		bind();

		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST_MIPMAP_LINEAR);

		GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE,
				buffer);
		
		GL30C.glGenerateMipmap(GL11C.GL_TEXTURE_2D);
	}

	public void bind() {
		assert id > INVALID_ID;
		GL11.glBindTexture(GL11C.GL_TEXTURE_2D, id);
	}

	public void bind(int unit) {
		bind();
		GL33C.glActiveTexture(unit);
	}

	public void unbind() {
		GL11.glBindTexture(GL11C.GL_TEXTURE_2D, 0);
	}
	
	@Override
	public void dispose() {
		GL11C.glDeleteTextures(id);
		this.id = INVALID_ID;
	}
	
	@Override
	public String toString() {
		return "Texture[ id= " + id + "]";
	}
}
