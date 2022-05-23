#version 330 core

in float outColor;
in vec2 fragTexCoords;

in float fogDistance;

uniform sampler2D texture_sampler;

out vec4 frag_Color;

vec4 getFog(in vec4 diffuseColor, in vec4 fogColor, in float fogDensity, in float distance) {

    float fogFactor = 1.0 / exp( (distance * fogDensity) * (distance * fogDensity));
    fogFactor = clamp( fogFactor, 0.0, 1.0 );

    vec4 finalColor = mix(fogColor, diffuseColor, fogFactor);
    return finalColor;
}

void main() {

    vec4 baseColor = texture(texture_sampler, fragTexCoords);
    if (baseColor.a < 0.1) {
        // Discard if its a transparent fragment.
        discard;
    }
    
    vec4 tintedColor = baseColor * outColor;
    frag_Color = getFog(tintedColor, vec4(0.3, 0.3, 0.3, 1.0), 0.07, fogDistance);
}