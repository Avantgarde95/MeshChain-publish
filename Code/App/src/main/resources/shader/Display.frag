#version 330 core

uniform sampler2D u_texture;

in vec2 f_vertexUV;

layout(location = 0) out vec4 glFragColor;

void main() {
    glFragColor = vec4(texture(u_texture, f_vertexUV).rgb, 1.0);
}
