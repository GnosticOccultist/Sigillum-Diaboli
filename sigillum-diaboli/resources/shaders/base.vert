#version 430 core

layout (location = 0) in vec3 position;
layout (location = 1) in float color;
layout (location = 2) in vec2 texCoords;

out float outColor;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main() {

    vec4 modelView = viewMatrix * vec4(position, 1.0);
	gl_Position = projectionMatrix * modelView;
	
	outColor = color;
}