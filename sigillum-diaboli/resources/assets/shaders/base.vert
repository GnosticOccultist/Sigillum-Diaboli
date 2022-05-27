#version 430 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoords;
layout (location = 2) in vec3 normal;

out vec2 fragTexCoords;

out vec3 worldPos;
out vec4 viewPos;
out vec3 fragNormal;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 model;
uniform mat3 normalMatrix;

void main() {

    worldPos = (model * vec4(position, 1.0)).xyz;
    viewPos = viewMatrix * vec4(worldPos, 1.0);
	
	fragTexCoords = texCoords;
	fragNormal = normalize(normalMatrix * normal);
	
	gl_Position = projectionMatrix * viewPos;
}