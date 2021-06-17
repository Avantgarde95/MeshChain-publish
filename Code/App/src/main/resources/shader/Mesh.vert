#version 330 core

struct Vertex {
    vec3 position;
    vec3 normal;
};

struct Transformation {
    mat4 modelMatrix;
    mat4 viewMatrix;
    mat4 projectionMatrix;
};

uniform Transformation u_transformation;
uniform Transformation u_normalTransformation;

layout(location = 0) in vec3 v_vertexPosition;
layout(location = 1) in vec3 v_vertexNormal;
layout(location = 2) in float v_vertexType;// 0: Common, 1: Added, 2: Removed.

out vec3 f_vertexPosition;
out vec3 f_vertexNormal;
flat out int f_vertexType;

void main() {
    Vertex vertex = Vertex(v_vertexPosition, v_vertexNormal);

    Vertex worldVertex = Vertex(
    (u_transformation.modelMatrix * vec4(vertex.position, 1.0)).xyz,
    (u_normalTransformation.modelMatrix * vec4(vertex.normal, 1.0)).xyz
    );

    Vertex eyeVertex = Vertex(
    (u_transformation.viewMatrix * vec4(worldVertex.position, 1.0)).xyz,
    (u_normalTransformation.viewMatrix * vec4(worldVertex.normal, 1.0)).xyz
    );

    // Vertex shader -> GPU.
    gl_Position = u_transformation.projectionMatrix * vec4(eyeVertex.position, 1.0);

    // Vertex shader -> Fragment shader.
    f_vertexPosition = worldVertex.position.xyz;
    f_vertexNormal = worldVertex.normal.xyz;
    f_vertexType = int(v_vertexType);
}
