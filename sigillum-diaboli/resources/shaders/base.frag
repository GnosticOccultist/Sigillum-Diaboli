#version 330 core

in float outColor;
in vec2 fragTexCoords;

uniform sampler2D texture_sampler;

out vec4 frag_Color;

void main() {

    vec4 baseColor = texture(texture_sampler, fragTexCoords);
    frag_Color = baseColor;
}