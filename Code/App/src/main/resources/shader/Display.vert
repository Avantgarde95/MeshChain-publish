#version 330 core

uniform int u_showMode;

layout(location = 0) in vec2 v_vertexPosition;
layout(location = 1) in vec2 v_vertexUV;

out vec2 f_vertexUV;

void main() {
    // Vertex shader -> GPU.
    if (u_showMode == 0) {
        gl_Position = vec4(v_vertexPosition + vec2(1.01, 0.0), 0.0, 1.0);
    } else {
        gl_Position = vec4(v_vertexPosition - vec2(0.01, 0.0), 0.0, 1.0);
    }

    // Vertex shader -> Fragment shader.
    f_vertexUV = v_vertexUV;
}
