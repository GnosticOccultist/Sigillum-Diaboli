package fr.sigillum.diaboli.graphics;

import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

public class Texture {

	private int width;
	private int height;
	private int id;

	public Texture(String path) {
		int[] pixels = null;
		try {
			var img = ImageIO.read(Texture.class.getResourceAsStream(path));
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

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
				buffer);
	}

	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	}

	public void bind(int unit) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL33.glActiveTexture(unit);
	}

	public void unbind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
