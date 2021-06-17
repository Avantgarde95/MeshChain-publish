#version 330 core

struct Vertex {
    vec3 position;
    vec3 normal;
};

struct Eye {
    vec3 position;
};

struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
};

uniform Eye u_eye;
uniform Light u_light;
uniform bool u_enableShading;
uniform int u_showMode;

in vec3 f_vertexPosition;
in vec3 f_vertexNormal;
flat in int f_vertexType;

layout(location = 0) out vec4 glFragColor;

vec3 computeDiffuse(Vertex vertex, Light light) {
    vec3 N = vertex.normal;
    vec3 L = normalize(light.position - vertex.position);
    float NdotL = dot(N, L);

    if (NdotL < 0.0) {
        return vec3(0.0);
    }

    return light.diffuse * NdotL;
}

vec3 computeVertexColor(int vertexType) {
    if (vertexType == 0) {
        return vec3(0.6, 0.7, 1.0);
    } else if (vertexType == 1) {
        return vec3(0.2, 1.0, 0.2);
    } else if (vertexType == 2) {
        return vec3(1.0, 0.2, 0.2);
    } else {
        return vec3(1.0, 1.0, 0.0);
    }
}

void main() {
    if (u_showMode == 0 && f_vertexType == 2) {
        discard;
    } else if (u_showMode == 1 && f_vertexType == 1) {
        discard;
    }

    vec3 color = vec3(1.0, 1.0, 1.0);
    Vertex vertex = Vertex(f_vertexPosition, f_vertexNormal);

    if (u_enableShading) {
        color = u_light.ambient + computeDiffuse(vertex, u_light);
        //color = vec3(0.9, 0.9, 0.9);
    }

    color *= computeVertexColor(f_vertexType);

    if (!u_enableShading) {
        //color += 0.3;
        if (f_vertexType == 0) {
            discard;
        } else {
            color = vec3(1.0, 1.0, 1.0);
        }
    }

    glFragColor = vec4(color, 1.0);
}
