package com.github.avantgarde95.bc3d.modeling

import darwin.jopenctm.compression.MG1Encoder
import darwin.jopenctm.io.CtmFileReader
import darwin.jopenctm.io.CtmFileWriter
import glm_.glm
import glm_.mat3x3.Mat3
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class Mesh(
        val vertices: List<Vec3>,
        val faceIndices: List<Vec3i>
) {
    companion object {
        fun createEmpty() = Mesh(
                vertices = emptyList(),
                faceIndices = emptyList()
        )

        fun fromOBJ(data: String): Mesh {
            val lines = data.lineSequence().map { it.trim().split("\\s+".toRegex()) }.toList()
            val vertices = mutableListOf<Vec3>()
            val faces = mutableListOf<Vec3i>()

            lines.forEach { line ->
                if (line.first() == "v") {
                    vertices.add(Vec3(line[1].toFloat(), line[2].toFloat(), line[3].toFloat()))
                }
            }

            lines.forEach { line ->
                if (line.first() == "f") {
                    val indices = line.subList(1, line.size).map { it.split('/').first().toInt() - 1 }

                    for (i in 1..(indices.size - 2)) {
                        faces.add(Vec3i(indices[0], indices[i], indices[i + 1]))
                    }
                }
            }

            return Mesh(vertices, faces)
        }

        fun fromCTM(data: String): Mesh {
            if (data.isEmpty()) {
                return Mesh.createEmpty()
            }

            val byteArray = Base64.getDecoder().decode(data)
            val ctmReader = CtmFileReader(ByteArrayInputStream(byteArray))
            val ctmMesh = ctmReader.decode()

            val vertices = mutableListOf<Vec3>()
            val faceIndices = mutableListOf<Vec3i>()

            (ctmMesh.vertices.indices step 3).forEach {
                vertices.add(
                        Vec3(
                                ctmMesh.vertices[it],
                                ctmMesh.vertices[it + 1],
                                ctmMesh.vertices[it + 2]
                        )
                )
            }

            (ctmMesh.indices.indices step 3).forEach {
                faceIndices.add(
                        Vec3i(
                                ctmMesh.indices[it],
                                ctmMesh.indices[it + 1],
                                ctmMesh.indices[it + 2]
                        )
                )
            }

            return Mesh(vertices, faceIndices)
        }

        fun fromFaces(faces: List<Mat3>): Mesh {
            val verticesSet = mutableSetOf<Vec3>()

            faces.forEach {
                verticesSet.add(it[0])
                verticesSet.add(it[1])
                verticesSet.add(it[2])
            }

            val vertices = verticesSet.toList()

            val verticesMap = vertices
                    .asSequence()
                    .mapIndexed { index, vertex -> vertex to index }
                    .toMap()

            val faceIndices = mutableListOf<Vec3i>()

            faces.forEach {
                faceIndices.add(Vec3i(
                        verticesMap.getValue(it[0]),
                        verticesMap.getValue(it[1]),
                        verticesMap.getValue(it[2])
                ))
            }

            return Mesh(vertices, faceIndices)
        }
    }

    fun computeNormals(): List<Vec3> {
        val normals = vertices.map { Vec3(0.0f) }.toMutableList()

        faceIndices.forEach {
            val v0 = vertices[it.x]
            val v1 = vertices[it.y]
            val v2 = vertices[it.z]
            val n = glm.normalize(glm.cross(v1 - v0, v2 - v0))

            normals[it.x] = normals[it.x] + n
            normals[it.y] = normals[it.y] + n
            normals[it.z] = normals[it.z] + n
        }

        for (i in 0..normals.lastIndex) {
            normals[i] = glm.normalize(normals[i])
        }

        return normals
    }

    fun toOBJ(): String {
        val lines = mutableListOf<String>()

        vertices.forEach {
            lines.add("v ${it.x} ${it.y} ${it.z}")
        }

        faceIndices.forEach {
            lines.add("f ${it.x + 1} ${it.y + 1} ${it.z + 1}")
        }

        return lines.joinToString("\n")
    }

    fun toSTL(): String {
        val lines = mutableListOf<String>()
        val faces = toFaces()

        lines.add("solid ")

        faces.forEach {
            val v0 = it[0]
            val v1 = it[1]
            val v2 = it[2]
            val n = glm.normalize(glm.cross(v1 - v0, v2 - v0))

            lines.add("facet normal ${n.x} ${n.y} ${n.z}")
            lines.add("outer loop")
            lines.add("vertex ${v0.x} ${v0.y} ${v0.z}")
            lines.add("vertex ${v1.x} ${v1.y} ${v1.z}")
            lines.add("vertex ${v2.x} ${v2.y} ${v2.z}")
            lines.add("endloop")
            lines.add("endfacet")
        }

        lines.add("endsolid ")

        return lines.joinToString("\n")
    }

    fun toCTM(): String {
        if (vertices.isEmpty()) {
            return ""
        }

        val ctmVertices = mutableListOf<Float>()
        val ctmFaceIndices = mutableListOf<Int>()

        vertices.forEach {
            ctmVertices.add(it.x)
            ctmVertices.add(it.y)
            ctmVertices.add(it.z)
        }

        faceIndices.forEach {
            ctmFaceIndices.add(it.x)
            ctmFaceIndices.add(it.y)
            ctmFaceIndices.add(it.z)
        }

        val ctmMesh = CTMMesh(
                ctmVertices.toFloatArray(),
                null,
                ctmFaceIndices.toIntArray(),
                emptyArray(),
                emptyArray()
        )

        ctmMesh.checkIntegrity()
        ctmMesh.validate()

        val stream = ByteArrayOutputStream()
        val ctmWriter = CtmFileWriter(stream, MG1Encoder())
        ctmWriter.encode(ctmMesh, "")

        val byteArray = stream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun toFaces() = faceIndices.map {
        val face = listOf(
                vertices[Math.floorMod(it.x, vertices.size)],
                vertices[Math.floorMod(it.y, vertices.size)],
                vertices[Math.floorMod(it.z, vertices.size)]
        )

        val maxIndex = face.indices.maxBy { face[it].hashCode() }!!

        Mat3(
                face[maxIndex % 3],
                face[(maxIndex + 1) % 3],
                face[(maxIndex + 2) % 3]
        )
    }
}
