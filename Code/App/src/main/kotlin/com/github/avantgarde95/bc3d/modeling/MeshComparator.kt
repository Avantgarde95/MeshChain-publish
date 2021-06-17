package com.github.avantgarde95.bc3d.modeling

import com.github.avantgarde95.bc3d.common.Util
import java.io.File

class MeshComparator {
    companion object {
        fun compareGeometrically(newMesh: Mesh, targetMesh: Mesh): Float {
            val newFaces = newMesh.toFaces()
            val targetFaces = targetMesh.toFaces()
            val commonFacesSet = newFaces.intersect(targetFaces)

            return when {
                targetFaces.isEmpty() -> 0.0f
                else -> commonFacesSet.size.toFloat() * 2 / (targetFaces.size + newFaces.size).toFloat()
            }
        }

        fun compareVisually(newMesh: Mesh, targetMesh: Mesh): Float {
            File("match/NewMesh.stl").writeText(newMesh.toSTL())
            File("match/TargetMesh.stl").writeText(targetMesh.toSTL())
            Util.runCommand("python VisualComparator.py", workingPath = "match")

            return File("match/Result.txt").readText().trim().toFloat()
        }

        fun test() {
            val newMesh = Mesh.fromOBJ(File("../Plugin/mesh/T19.obj").readText())
            val targetMesh = Mesh.fromOBJ(File("../Plugin/mesh/T20.obj").readText())

            val geometricalSimilarity = MeshComparator.compareGeometrically(newMesh, targetMesh)
            val visualSimilarity = MeshComparator.compareVisually(newMesh, targetMesh)

            println("Geometrical similarity: $geometricalSimilarity")
            println("Visual similarity: $visualSimilarity")
        }
    }
}
