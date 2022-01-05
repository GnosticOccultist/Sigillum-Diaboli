#version 330 core

in float outColor;

out vec4 frag_Color;

void main() {

    frag_Color = vec4(1.0 * outColor, 1.0 * outColor, 1.0 * outColor, 1.0);
}