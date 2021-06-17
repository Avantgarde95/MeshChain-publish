package com.github.avantgarde95.bc3d.manager

import com.github.avantgarde95.bc3d.common.Commit
import com.github.avantgarde95.bc3d.common.Project
import com.github.avantgarde95.bc3d.modeling.Mesh
import com.github.avantgarde95.bc3d.modeling.MeshDelta

class ProjectManager(
        val project: Project
) {
    lateinit var selectedCommitAddress: String
        private set

    lateinit var selectedCommit: Commit
        private set

    lateinit var selectedMesh: Mesh
        private set

    var commitsUntilSelected = emptyList<Commit>()
        private set

    fun hasCommits() = project.commitAddresses.isNotEmpty()

    fun hasSelectedCommit() = ::selectedCommitAddress.isInitialized

    fun selectCommit(commitAddress: String, getCommit: (address: String) -> Commit) {
        var currentCommit = getCommit(commitAddress)
        val commits = mutableListOf(currentCommit)

        while (true) {
            if (currentCommit.previousCommitAddress != "0x0") {
                val currentAddress = currentCommit.previousCommitAddress
                currentCommit = getCommit(currentAddress)
                commits.add(0, currentCommit)
            } else {
                break
            }
        }

        selectedCommitAddress = commitAddress
        selectedCommit = commits.last()
        selectedMesh = Mesh.fromFaces(MeshDelta.mergeDeltas(commits.map { it.meshDelta }))
        commitsUntilSelected = commits
    }

    fun computeCommonMesh(): Mesh {
        val facesBeforeSelected = MeshDelta.mergeDeltas(
                commitsUntilSelected.dropLast(1).map { it.meshDelta }
        )

        return Mesh.fromFaces(facesBeforeSelected - selectedCommit.meshDelta.removedFaces)
    }

    fun computeAddedMesh() =
            Mesh.fromFaces(selectedCommit.meshDelta.addedFaces)

    fun computeRemovedMesh() =
            Mesh.fromFaces(selectedCommit.meshDelta.removedFaces)
}
