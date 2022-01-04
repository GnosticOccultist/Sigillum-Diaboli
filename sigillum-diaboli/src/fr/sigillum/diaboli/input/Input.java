package fr.sigillum.diaboli.input;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import fr.sigillum.diaboli.graphics.Window;

public class Input {

	private final Window window;

	private final Vector2d currentPos = new Vector2d();
	private final Vector2d previousPos = new Vector2d(-1, -1);
	private final Vector2f delta = new Vector2f();

	private boolean grabbed = false;
	private boolean leftButtonPressed = false;
	private boolean rightButtonPressed = false;

	public Input(Window window) {
		this.window = window;
		GLFW.glfwSetCursorPosCallback(window.getHandle(), (h, x, y) -> {
			this.currentPos.x = x;
			this.currentPos.y = y;
		});
		GLFW.glfwSetMouseButtonCallback(window.getHandle(), (h, button, action, mode) -> {
			this.leftButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
			this.rightButtonPressed = button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS;
		});
	}

	public void update() {
		this.delta.set(0, 0);
		if ((previousPos.x > 0 && previousPos.y > 0) || grabbed) {
			double dx = currentPos.x - previousPos.x;
			double dy = currentPos.y - previousPos.y;
			boolean rotateX = dx != 0;
			boolean rotateY = dy != 0;
			if (rotateX) {
				delta.y = (float) dx;
			}
			if (rotateY) {
				delta.x = (float) dy;
			}
		}
		this.previousPos.set(currentPos);
	}

	public boolean isKeyDown(int key) {
		return GLFW.glfwGetKey(window.getHandle(), key) == GLFW.GLFW_PRESS;
	}

	public void grab() {
		GLFW.glfwSetInputMode(window.getHandle(), GLFW.GLFW_CURSOR,
				grabbed ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_DISABLED);
		this.grabbed = !grabbed;
	}

	public boolean isGrabbed() {
		return grabbed;
	}

	public boolean isRightButtonPressed() {
		return rightButtonPressed;
	}

	public boolean isLeftButtonPressed() {
		return leftButtonPressed;
	}

	public Vector2f getDelta() {
		return delta;
	}
}
