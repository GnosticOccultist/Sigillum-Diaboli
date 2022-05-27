#version 330 core

#define MAX_LIGHTS 4

in vec2 fragTexCoords;

in vec3 worldPos;
in vec4 viewPos;
in vec3 fragNormal;

uniform sampler2D texture_sampler;
uniform vec3 cameraPos;

out vec4 frag_Color;

struct Light {

    bool enabled;

    vec3 position;
    vec3 color;
    float intensity;
    
    // Attenuation factors.
    vec3 attenuation;
    float range;
};

uniform Light lights[MAX_LIGHTS];

struct SurfaceColor {

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

float getFog(float density, float power, float fogValue) {
    return 1.0 - clamp(exp(-pow(density * fogValue, power)), 0.0, 1.0);
}

vec3 calcDiffuse(Light light, vec3 lightDir, vec3 worldNormal) {
    float NdotL = max(dot(worldNormal, lightDir), 0.0);
    return light.color * light.intensity * NdotL; 
}

vec3 calcSpecular(Light light, vec3 viewDir, vec3 lightDir, vec3 worldNormal) {
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float NdotH = max(0.0, dot(worldNormal, halfwayDir));
    
    return light.color * light.intensity * pow(NdotH, 0.1);
}

float getAttenuationFactor(vec3 attenuation, float distance) {
    return 1.0 / (attenuation.x + attenuation.y * distance + attenuation.z * (distance * distance));
}

SurfaceColor calcPointLight(Light light, vec3 viewDir, vec3 worldPos, vec3 worldNormal) {
    SurfaceColor result;
    result.ambient = vec3(0.3);
    result.diffuse = vec3(0.0);
    result.specular = vec3(0.0);
    
    vec3 lightDir = (light.position - worldPos);
    float distance = length(lightDir);
    if (distance > light.range) return result;
    lightDir /= distance;
    
    float attenuation = getAttenuationFactor(light.attenuation, distance);
    
    result.diffuse = calcDiffuse(light, lightDir, worldNormal) * attenuation;
    result.specular = calcSpecular(light, viewDir, lightDir, worldNormal) * attenuation;
    return result;
}

SurfaceColor calcLighting(vec3 viewPos, vec3 viewDir, vec3 worldPos, vec3 worldNormal) {
    Light light;
    SurfaceColor litSurface, result;
    litSurface.ambient = vec3(0.1);
    litSurface.diffuse = vec3(0.0);
    litSurface.specular = vec3(0.0);
    for (int i = 0; i < MAX_LIGHTS; i++) {
        light = lights[i];
        if (!light.enabled) {
            continue;
        }
        
        result = calcPointLight(light, viewDir, worldPos, worldNormal);
        
        litSurface.ambient += result.ambient;
        litSurface.diffuse += result.diffuse;
        litSurface.specular += result.specular;
    }
    
    return litSurface;
}

void main() {

    vec4 baseColor = texture(texture_sampler, fragTexCoords);
    if (baseColor.a < 0.1) {
        // Discard if its a transparent fragment.
        discard;
    }
    
    vec3 viewDir = normalize(cameraPos - worldPos);
    SurfaceColor litSurface = calcLighting(viewPos.xyz/viewPos.w, viewDir, worldPos, fragNormal);
    
    vec3 ambient = litSurface.ambient;
    vec3 diffuse = litSurface.diffuse;
    vec3 specular = litSurface.specular;
    
    vec4 litColor = clamp(baseColor * vec4(ambient + diffuse + specular, baseColor.a), 0.0, 1.0);
    
    float fogAmount = getFog(0.01, 2, abs(viewPos.z/viewPos.w));
    frag_Color = mix(litColor, vec4(0.3, 0.3, 0.3, 1.0), fogAmount); 
}