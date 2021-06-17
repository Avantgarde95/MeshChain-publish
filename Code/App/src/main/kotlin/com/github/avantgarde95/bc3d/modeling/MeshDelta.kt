package com.github.avantgarde95.bc3d.modeling

import glm_.mat3x3.Mat3

class MeshDelta(
        val addedFaces: List<Mat3>,
        val removedFaces: List<Mat3>
) {
    companion object {
        fun fromMeshes(previousMesh: Mesh, currentMesh: Mesh): MeshDelta {
            val previousFaces = previousMesh.toFaces()
            val currentFaces = currentMesh.toFaces()

            return MeshDelta(
                    addedFaces = currentFaces - previousFaces,
                    removedFaces = previousFaces - currentFaces
            )
        }

        fun mergeDeltas(meshDeltas: List<MeshDelta>): List<Mat3> {
            val faces = mutableListOf<Mat3>()

            meshDeltas.forEach {
                faces += it.addedFaces
                faces -= it.removedFaces
            }

            return faces
        }

        fun computeIncentive(meshDeltas: List<MeshDelta>): Float {
            val addedFaces = meshDeltas.last().addedFaces

            val ownAddedFaces = meshDeltas.dropLast(1).fold(addedFaces) { acc, meshDelta ->
                acc - meshDelta.addedFaces
            }

            val removedFaces = meshDeltas.last().removedFaces

            val ownRemovedFaces = meshDeltas.dropLast(1).fold(removedFaces) { acc, meshDelta ->
                acc - meshDelta.removedFaces
            }

            val modifiedFacesCount = addedFaces.size + removedFaces.size
            val ownModifiedFacesCount = ownAddedFaces.size + ownRemovedFaces.size

            return when (modifiedFacesCount) {
                0 -> 0.0f
                else -> ownModifiedFacesCount.toFloat() / modifiedFacesCount.toFloat()
            }
        }
    }
}
