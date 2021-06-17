package com.github.avantgarde95.bc3d.manager

import com.github.avantgarde95.bc3d.common.*
import com.github.avantgarde95.bc3d.modeling.MeshDelta
import org.web3j.bc3d.BC3D
import org.web3j.protocol.Web3jService
import org.web3j.protocol.admin.Admin
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.ipc.WindowsIpcService
import org.web3j.protocol.websocket.WebSocketService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import java.math.BigInteger

class BlockchainManager(
        blockchainURI: SimpleURI,
        contractAddress: String,
        val userAddress: String,
        userPassword: String?
) {
    private val service: Web3jService = when (blockchainURI.scheme) {
        "ws" ->
            WebSocketService(blockchainURI.toString(), false).apply {
                connect()
            }
        "ipc" ->
            when {
                Util.isWindows() -> WindowsIpcService(blockchainURI.host)
                else -> throw NotImplementedError()
            }
        else ->
            HttpService(blockchainURI.toString())
    }

    private val web3 = Admin.build(service)

    private val contract = BC3D.load(
            contractAddress,
            web3,
            ClientTransactionManager(web3, userAddress),
            DefaultGasProvider()
    )

    init {
        val blockchainVersion = web3.web3ClientVersion().send().web3ClientVersion
        Logger.addString("Connected to the blockchain: $blockchainVersion")

        if (userPassword != null) {
            Logger.addString("Unlocking the account... ", ensureNewline = false)

            val unlockAccount = web3.personalUnlockAccount(
                    userAddress,
                    userPassword,
                    0.toBigInteger()
            ).send()

            if (unlockAccount.accountUnlocked()) {
                Logger.addString("Success")
            } else {
                Logger.addString("Failed")
            }
        }

        Logger.addString("Testing the contract... ", ensureNewline = false)
        Logger.addString(contract.testCall().send())
    }

    // ====================================================

    fun tsClear() {
        contract.tsClear().send()
    }

    fun tsAddCommitAddress(
            commitAddress: String
    ) {
        contract.tsAddCommitAddress(
                commitAddress
        ).send()
    }

    fun tsGetCommitAddress(index: Int): String {
        return contract.tsGetCommitAddress(index.toBigInteger()).send()
    }

    fun tsGetCommitAddressesCount(): Int {
        return contract.tsGetCommitAddressCount().send().toInt()
    }

    // ====================================================

    fun createProject(name: String, keyword: String, commitIncentiveSupply: Float) {
        contract.createProject(name, keyword, ethToWei(commitIncentiveSupply)).send()
    }

    fun addCommitAddressToProject(
            name: String,
            commitAddress: String,
            previousCommits: List<Commit>,
            commit: Commit
    ) {
        contract.addAuthorAddressToProject(name, userAddress).send()

        val commitIncentiveCoefficient = MeshDelta.computeIncentive(
                previousCommits.map { it.meshDelta } + commit.meshDelta
        )

        val commitIncentiveSupply = weiToEth(contract.getCommitIncentiveSupply(name).send()).toFloat()
        val authorCount = contract.getAuthorAddressesOfProject(name).send().size
        val commitIncentiveUnit = commitIncentiveSupply / (authorCount + 1.0f)
        val commitIncentive = commitIncentiveCoefficient * commitIncentiveUnit

        Logger.addString("Commit incentive (in ETH): $commitIncentive = Unit(= $commitIncentiveUnit) X Coefficient(= $commitIncentiveCoefficient)")

        contract.addCommitAddressToProject(
                name,
                commitAddress,
                ethToWei(commitIncentive)
        ).send()
    }

    fun getProject(name: String): Project {
        val keyword = contract.getKeywordOfProject(name).send()

        val authorAddresses = contract.getAuthorAddressesOfProject(name).send()
                .map { it as String }

        val commitAddressCount = contract.getCommitAddressCountOfProject(name).send().toInt()

        val commitAddresses = (0 until commitAddressCount).map {
            contract.getCommitAddressOfProject(name, it.toBigInteger()).send()
        }

        return Project(
                name = name,
                keyword = keyword,
                authorAddresses = authorAddresses,
                commitAddresses = commitAddresses
        )
    }

    fun searchProjects(keyword: String): List<Pair<String, String>> {
        val projectCount = contract.projectCount.send().toInt()

        val projectNames = (0 until projectCount).map {
            contract.getNameOfProject(it.toBigInteger()).send()
        }

        val projectKeywords = projectNames.map {
            contract.getKeywordOfProject(it).send()
        }

        val keywordForSearch = keyword.trim().toLowerCase()

        return when {
            keywordForSearch.isBlank() ->
                (0 until projectCount)
                        .map { Pair(projectNames[it], projectKeywords[it]) }
            else ->
                (0 until projectCount)
                        .filter { keywordForSearch in projectKeywords[it].toLowerCase() }
                        .map { Pair(projectNames[it], projectKeywords[it]) }
        }
    }

    private fun ethToWei(ethPrice: Float) = Convert.toWei(
            ethPrice.toBigDecimal(),
            Convert.Unit.ETHER
    ).toBigInteger()

    private fun weiToEth(weiPrice: BigInteger) = Convert.fromWei(
            weiPrice.toBigDecimal(),
            Convert.Unit.ETHER
    )

    private fun getBalance(address: String) = web3.ethGetBalance(
            address,
            DefaultBlockParameterName.LATEST
    ).send().balance
}
