#version 430 core

layout (location = 0) in vec3 position;
layout (location = 1) in float color;
layout (location = 2) in vec2 texCoords;

out float outColor;
out vec2 fragTexCoords;
out float fogDistance;

uniform vec3 camPos;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 model;

void main() {

    vec4 modelPos = model * vec4(position, 1.0);
    vec4 modelView = viewMatrix * modelPos;
	gl_Position = projectionMatrix * modelView;
	
	fogDistance = distance(camPos, modelPos.xyz);
	
	outColor = color;
	fragTexCoords = texCoords;
}