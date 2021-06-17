package com.github.avantgarde95.bc3d.render

import glm_.glm
import glm_.vec3.Vec3

class AABB(vertices: List<Vec3>) {
    val min = vertices.reduce { result, v -> glm.min(result, v) }
    val max = vertices.reduce { result, v -> glm.max(result, v) }
    val center = (min + max) * 0.5f
    val extent = max - min
    val maxExtent = maxOf(extent.x, extent.y, extent.z)
    val minExtent = minOf(extent.x, extent.y, extent.z)
}
