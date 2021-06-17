package com.github.avantgarde95.bc3d.common

import com.github.avantgarde95.bc3d.modeling.Mesh
import com.github.avantgarde95.bc3d.modeling.MeshDelta

class Commit(
        val previousCommitAddress: String,
        val authorAddress: String,
        val timestamp: Long,
        val meshDelta: MeshDelta
) {
    fun compress() = CompressedCommit(
            previousCommitAddress,
            authorAddress,
            timestamp,
            Mesh.fromFaces(meshDelta.addedFaces).toCTM(),
            Mesh.fromFaces(meshDelta.removedFaces).toCTM()
    )
}

class CompressedCommit(
        val previousCommitAddress: String,
        val author: String,
        val timestamp: Long,
        val addedMeshCTM: String,
        val removedMeshCTM: String
) {
    fun decompress() = Commit(
            previousCommitAddress,
            author,
            timestamp,
            MeshDelta(
                    addedFaces = Mesh.fromCTM(addedMeshCTM).toFaces(),
                    removedFaces = Mesh.fromCTM(removedMeshCTM).toFaces()
            )
    )
}
