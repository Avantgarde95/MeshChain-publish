package com.github.avantgarde95.bc3d.render

import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class Eye(
        var position: Vec3,
        var center: Vec3,
        var up: Vec3
) {
    fun moveX(value: Float) {
        val move = glm.normalize(glm.cross(center - position, up)) * value

        position = position + move
        center = center + move
    }

    fun moveY(value: Float) {
        val move = glm.normalize(up) * value

        position = position + move
        center = center + move
    }

    fun moveZ(value: Float) {
        val move = glm.normalize(center - position) * value

        position = position + move
        center = center + move
    }

    fun rotateY(value: Float) {
        val matrix = glm.rotate(Mat4(1.0f), -value, up)

        center = (matrix * Vec4(center - position)).toVec3() + position
    }

    fun rotateX(value: Float) {
        val matrix = glm.rotate(Mat4(1.0f), value, glm.cross(center - position, up))

        center = (matrix * Vec4(center - position)).toVec3() + position
        up = (matrix * Vec4(up)).toVec3()
    }

    fun rotateZ(value: Float) {
        val matrix = glm.rotate(Mat4(1.0f), value, center - position)

        up = (matrix * Vec4(up)).toVec3()
    }
}
