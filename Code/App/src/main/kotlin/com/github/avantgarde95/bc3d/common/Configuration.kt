package com.github.avantgarde95.bc3d.common

class Configuration(
        val toolURI: SimpleURI,
        val blockchainURI: SimpleURI,
        val storageURI: SimpleURI,
        val contractAddress: String,
        val userAddress: String,
        val userPassword: String?
)
