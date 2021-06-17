package com.github.avantgarde95.bc3d.manager

import com.github.avantgarde95.bc3d.common.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.runBlocking

class StorageManager(
        private val storageURI: SimpleURI
) {
    fun uploadCommitAndGetAddress(commit: Commit): Pair<String, Int> {
        val commitJson = when {
            Switch.useCompression ->
                Util.toJSON(commit.compress())
            else ->
                Util.toJSON(commit)
        }

        return Pair(
                when {
                    Switch.useStorage -> uploadData(commitJson)
                    else -> commitJson
                },
                commitJson.toByteArray().size
        )
    }

    fun downloadCommit(address: String): Pair<Commit, Int> {
        val commitJSON = when {
            Switch.useStorage -> downloadData(address)
            else -> address
        }

        return Pair(
                when {
                    Switch.useCompression ->
                        Util.fromJSON<CompressedCommit>(commitJSON).decompress()
                    else ->
                        Util.fromJSON<Commit>(commitJSON)
                },
                commitJSON.toByteArray().size
        )
    }

    fun uploadData(data: String) = runBlocking {
        Fuel.post(storageURI.toString()).body(data).awaitString()
    }

    fun downloadData(address: String) = runBlocking {
        Fuel.get("$storageURI/$address").awaitString()
    }
}
