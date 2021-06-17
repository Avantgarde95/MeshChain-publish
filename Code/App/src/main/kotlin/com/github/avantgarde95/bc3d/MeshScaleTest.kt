package com.github.avantgarde95.bc3d

import com.github.avantgarde95.bc3d.common.Logger
import com.github.avantgarde95.bc3d.common.Util
import com.github.avantgarde95.bc3d.manager.BlockchainManager
import com.github.avantgarde95.bc3d.manager.StorageManager
import com.github.avantgarde95.bc3d.modeling.Mesh
import java.io.File
import kotlin.math.min

fun testVariousSizes(blockchainManager: BlockchainManager, storageManager: StorageManager) {
    blockchainManager.tsClear()
    Logger.addString("Cleared the blockchain")

    (1..10).flatMap { i -> (1..10).map { j -> "./raw/File${i}MB${j}.txt" } }.forEach { path ->
        Logger.addString("Path: $path")

        val data = File(path).readText()

        val address = Util.measureAndLog({ "- Added to the storage" }) {
            storageManager.uploadData(data)
        }

        Util.measureAndLog({ "- Added to the blockchain" }) {
            blockchainManager.tsAddCommitAddress(address)
        }

        val addressCount = blockchainManager.tsGetCommitAddressesCount()
        Logger.addString("- We have $addressCount addresses!")
    }
}

fun testLargeMeshSeparation(path: String, blockchainManager: BlockchainManager, storageManager: StorageManager) {
    val mesh = Mesh.fromOBJ(File(path).readText())
    val faces = mesh.toFaces()
    val maxPartSize = (faces.size / 2.0).toInt() + (faces.size % 2)

    blockchainManager.tsClear()
    Logger.addString("Cleared the blockchain")

    Logger.addString("Test 1: Upload just the whole mesh")

    Util.measureAndLog({ "- Done" }) {
        Logger.addString("- Mesh: ${faces.size} triangles")

        val facesAddress = Util.measureAndLog({ "- Uploaded the triangles to the storage" }) {
            storageManager.uploadData(Util.toJSON(faces))
        }

        Util.measureAndLog({ "- Added the storage address ($facesAddress) to the blockchain" }) {
            blockchainManager.tsAddCommitAddress(facesAddress)
        }
    }

    Logger.addString("Test 2: Separate the mesh into the parts")

    Util.measureAndLog({ "- Done" }) {
        val parts = (0..faces.lastIndex step maxPartSize).map {
            faces.subList(it, min(faces.size, it + maxPartSize))
        }

        Logger.addString("- Parts: ${parts.joinToString { "${it.size} triangles" }}")

        val partsAddresses = parts.map { part ->
            Util.measureAndLog({ "- Uploaded ${part.size} triangles to the storage" }) {
                storageManager.uploadData(Util.toJSON(part))
            }
        }

        val mergedPartsAddress = partsAddresses.joinToString("|")

        Util.measureAndLog({ "- Added the storage address ($mergedPartsAddress) to the blockchain" }) {
            blockchainManager.tsAddCommitAddress(mergedPartsAddress)
        }
    }

    Logger.addString("Test 3: Separate the mesh and compress the parts")

    Util.measureAndLog({ "- Done" }) {
        val parts = (0..faces.lastIndex step maxPartSize).map {
            faces.subList(it, min(faces.size, it + maxPartSize))
        }

        Logger.addString("- Parts: ${parts.joinToString { "${it.size} triangles" }}")

        val partsAddresses = parts.map { part ->
            Util.measureAndLog({ "- Compressed and uploaded ${part.size} triangles to the storage" }) {
                val compressedPart = Mesh.fromFaces(part).toCTM()
                storageManager.uploadData(Util.toJSON(compressedPart))
            }
        }

        val mergedPartsAddress = partsAddresses.joinToString("|")

        Util.measureAndLog({ "- Added the storage address ($mergedPartsAddress) to the blockchain" }) {
            blockchainManager.tsAddCommitAddress(mergedPartsAddress)
        }
    }
}
