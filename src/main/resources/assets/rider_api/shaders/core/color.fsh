#version 150

uniform sampler2D Sampler0;
uniform float Time;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }

    // 创建颜色动画效果
    float hue = mod(Time * 0.5, 1.0);
    vec3 rgb = hsv2rgb(vec3(hue, 1.0, 1.0));

    fragColor = vec4(rgb, color.a) * vertexColor * ColorModulator;
}

// HSV到RGB的转换函数
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}