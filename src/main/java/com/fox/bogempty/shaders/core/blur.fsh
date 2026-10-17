#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 OutSize;

in vec2 texCoord;
in vec2 oneTexel;
out vec4 fragColor;

void main() {
    vec4 sum = vec4(0.0);
    float radius = 2.0; // Радиус размытия

    // Берем 9 пикселей вокруг текущего и смешиваем их
    for(float x = -radius; x <= radius; x += radius) {
        for(float y = -radius; y <= radius; y += radius) {
            vec2 offset = vec2(x, y) * oneTexel;
            sum += texture(DiffuseSampler, texCoord + offset);
        }
    }
    
    fragColor = sum / 9.0;
}