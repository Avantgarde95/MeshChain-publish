package com.github.avantgarde95.bc3d

import com.github.avantgarde95.bc3d.common.*
import com.github.avantgarde95.bc3d.manager.BlockchainManager
import com.github.avantgarde95.bc3d.manager.ProjectManager
import com.github.avantgarde95.bc3d.manager.StorageManager
import com.github.avantgarde95.bc3d.manager.ToolManager
import com.github.avantgarde95.bc3d.modeling.Mesh
import com.github.avantgarde95.bc3d.modeling.MeshComparator
import com.github.avantgarde95.bc3d.modeling.MeshDelta
import com.github.avantgarde95.bc3d.view.ConfigurePanel
import com.github.avantgarde95.bc3d.view.LogPanel
import com.github.avantgarde95.bc3d.view.ProjectPanel
import com.github.avantgarde95.bc3d.view.SearchPanel
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.locks.ReentrantLock
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime

@ExperimentalTime
class App {
    private val configurePanel = ConfigurePanel().apply {
        applyEvent.addListener { configuration ->
            disableViewsAndRunTask {
                onApplyEvent(configuration)
            }
        }
    }

    private val projectPanel = ProjectPanel().apply {
        createEvent.addListener { (name, keyword, commitIncentiveSupply) ->
            if (name.isNotEmpty()) {
                disableViewsAndRunTask {
                    onCreateEvent(name, keyword, commitIncentiveSupply)
                }
            }
        }

        loadEvent.addListener { name ->
            if (name.isNotEmpty()) {
                disableViewsAndRunTask {
                    onLoadEvent(name)
                }
            }
        }

        selectEvent.addListener { address ->
            disableViewsAndRunTask {
                onSelectEvent(address)
            }
        }

        commitEvent.addListener {
            disableViewsAndRunTask {
                onCommitEvent()
            }
        }

        checkoutEvent.addListener {
            disableViewsAndRunTask {
                onCheckoutEvent()
            }
        }

        compareEvent.addListener { path ->
            disableViewsAndRunTask {
                onCompareEvent(path)
            }
        }
    }

    private val searchPanel = SearchPanel().apply {
        searchEvent.addListener { keyword ->
            disableViewsAndRunTask {
                onSearchEvent(keyword)
            }
        }
    }

    private val logPanel = LogPanel().apply {
        Logger.logEvent.addListener { log ->
            addLog(log)
        }

        clearEvent.addListener {
            clearLog()
        }

        saveEvent.addListener {
            val fileChooser = object : JFileChooser() {
                override fun approveSelection() {
                    if (selectedFile.exists()) {
                        when (JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION)) {
                            JOptionPane.YES_OPTION -> super.approveSelection()
                        }
                    } else {
                        super.approveSelection()
                    }
                }
            }.apply {
                dialogTitle = "Save the log"
                addChoosableFileFilter(FileNameExtensionFilter("Text files", "txt"))
                isAcceptAllFileFilterUsed = true
            }

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                val path = fileChooser.selectedFile.absolutePath
                File(path).writeText(Logger.allStrings.joinToString(""))
                Logger.addString("Saved the log to $path")
            }
        }
    }

    private val testButtons = listOf(
            JButton("Test various sizes").apply {
                addActionListener {
                    disableViewsAndRunTask {
                        testVariousSizes(blockchainManager, storageManager)
                    }
                }
            },
            JButton("Test large mesh separation").apply {
                addActionListener {
                    disableViewsAndRunTask {
                        //testLargeMeshSeparation("./mesh/large/Buddha.obj", blockchainManager, storageManager)
                        //testLargeMeshSeparation("./mesh/large/HDragon.obj", blockchainManager, storageManager)
                        testLargeMeshSeparation("./mesh/large/House.obj", blockchainManager, storageManager)
                        //testLargeMeshSeparation("./mesh/large/ManuscriptPart.obj", blockchainManager, storageManager)
                    }
                }
            }
            /*
            ,
            JButton("Test separate & retrieve").apply {
                addActionListener {
                    disableViewsAndRunTask {
                        testSeparateAndRetrieve(blockchainManager, storageManager)
                    }
                }
            }
            */
    )

    private val taskLock = ReentrantLock()

    private lateinit var blockchainManager: BlockchainManager
    private lateinit var toolManager: ToolManager
    private lateinit var storageManager: StorageManager
    private lateinit var projectManager: ProjectManager

    fun show() {
        SwingUtilities.invokeLater {
            JFrame("MeshChain").apply {
                layout = BorderLayout()
                preferredSize = Dimension(950, 720)
                defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                isVisible = true
                isResizable = true

                add(
                        JSplitPane(JSplitPane.VERTICAL_SPLIT).apply {
                            add(JTabbedPane().apply {
                                addTab("Configure", configurePanel)
                                addTab("Project", projectPanel)
                                addTab("Search", searchPanel)
                            }, JSplitPane.TOP)

                            add(JPanel().apply {
                                layout = BorderLayout()

                                if (Switch.useTest) {
                                    add(JPanel().apply {
                                        layout = GridLayout(0, 1)
                                        testButtons.forEach { add(it) }
                                    }, BorderLayout.NORTH)
                                }

                                add(logPanel, BorderLayout.CENTER)
                            }, JSplitPane.BOTTOM)

                            dividerLocation = 500
                        },
                        BorderLayout.CENTER
                )

                pack()
            }
        }
    }

    private fun disableViewsAndRunTask(block: () -> Unit) {
        val containers = listOf(configurePanel, projectPanel, searchPanel)

        thread {
            if (taskLock.tryLock()) {
                try {
                    containers.forEach { enableContainer(it, false) }
                    block()
                } catch (e: Exception) {
                    e.printStackTrace()

                    val sw = StringWriter()
                    e.printStackTrace(PrintWriter(sw))
                    Logger.addString(sw.toString())
                } finally {
                    containers.forEach { enableContainer(it, true) }
                    taskLock.unlock()
                }
            }
        }
    }

    private fun enableContainer(container: Container, enable: Boolean) {
        container.components.forEach {
            it.isEnabled = enable

            if (it is Container) {
                enableContainer(it, enable)
            }
        }
    }

    private fun onApplyEvent(configuration: Configuration) {
        toolManager = ToolManager(
                toolURI = configuration.toolURI
        )

        blockchainManager = BlockchainManager(
                blockchainURI = configuration.blockchainURI,
                contractAddress = configuration.contractAddress,
                userAddress = configuration.userAddress,
                userPassword = configuration.userPassword
        )

        storageManager = StorageManager(
                storageURI = configuration.storageURI
        )
    }

    private fun onCreateEvent(name: String, keyword: String, commitIncentiveSupply: Float) {
        Util.measureAndLog({ """Created the new project "$name"""" }) {
            blockchainManager.createProject(name, keyword, commitIncentiveSupply)
        }
    }

    private fun onLoadEvent(name: String) {
        val project = Util.measureAndLog({
            """Loaded the project "${it.name}" (Has ${it.commitAddresses.size} commits)"""
        }) {
            blockchainManager.getProject(name)
        }

        projectPanel.setProject(project)
        projectManager = ProjectManager(project)
    }

    private fun onSelectEvent(address: String) {
        Util.measureAndLog({ "Done! Showing the information..." }) {
            projectManager.selectCommit(address) { commitAddress ->
                val (commit, _) = Util.measureAndLog({
                    "Got the commit $commitAddress (Data size: ${it.second} bytes)"
                }) {
                    storageManager.downloadCommit(commitAddress)
                }

                return@selectCommit commit
            }
        }

        projectPanel.showCommitInformation(
                projectManager.selectedCommitAddress,
                projectManager.selectedCommit
        )

        projectPanel.drawMesh(
                commonMesh = projectManager.computeCommonMesh(),
                addedMesh = projectManager.computeAddedMesh(),
                removedMesh = projectManager.computeRemovedMesh()
        )
    }

    private fun onCommitEvent() {
        if (!::projectManager.isInitialized) {
            Logger.addString("You should load a project to make a commit")
        } else {
            val mesh = Util.measureAndLog({
                "Got a mesh from the modeling tool (${it.vertices.size} vertices, ${it.faceIndices.size} faces)"
            }) {
                toolManager.getMeshFromTool()
            }

            val commit = when {
                !projectManager.hasCommits() ->
                    Commit(
                            previousCommitAddress = "0x0",
                            authorAddress = blockchainManager.userAddress,
                            timestamp = Util.timestamp(),
                            meshDelta = MeshDelta.fromMeshes(Mesh.createEmpty(), mesh)
                    )
                projectManager.hasSelectedCommit() ->
                    Commit(
                            previousCommitAddress = projectManager.selectedCommitAddress,
                            authorAddress = blockchainManager.userAddress,
                            timestamp = Util.timestamp(),
                            meshDelta = MeshDelta.fromMeshes(
                                    when {
                                        Switch.useDelta -> projectManager.selectedMesh
                                        else -> Mesh.createEmpty()
                                    },
                                    mesh
                            )
                    )
                else -> {
                    Logger.addString("You should select one of the commits")
                    return
                }
            }

            Util.measureAndLog({ "Done!" }) {
                val (commitAddress, _) = Util.measureAndLog({
                    "Uploaded the commit ${it.first} to the storage (Data size: ${it.second} bytes)"
                }) {
                    storageManager.uploadCommitAndGetAddress(commit)
                }

                Util.measureAndLog({ "Uploaded the commit's address to the blockchain" }) {
                    blockchainManager.addCommitAddressToProject(
                            projectManager.project.name,
                            commitAddress,
                            projectManager.commitsUntilSelected,
                            commit
                    )
                }
            }
        }
    }

    @ExperimentalTime
    private fun onCheckoutEvent() {
        if (!projectManager.hasSelectedCommit()) {
            Logger.addString("You should select a commit")
        } else {
            Util.measureAndLog({ "Sent the mesh to the modeling tool" }) {
                toolManager.sendMeshToTool(projectManager.selectedMesh)
            }
        }
    }

    @ExperimentalTime
    private fun onSearchEvent(keyword: String) {
        val infos = Util.measureAndLog({ "Found ${it.size} projects that matches the keyword \"$keyword\"" }) {
            blockchainManager.searchProjects(keyword)
        }

        searchPanel.setProjectInfosPanel(infos)
    }

    @ExperimentalTime
    private fun onCompareEvent(path: String) {
        val localMesh = Util.measureAndLog({ "Read the mesh from \"$path\"" }) {
            Mesh.fromOBJ(File(path).readText())
        }

        Util.measureAndLog({ "Geometrical similarity with this model: %.12f".format(it) }) {
            MeshComparator.compareGeometrically(localMesh, projectManager.selectedMesh)
        }

        Util.measureAndLog({ "Visual similarity with this model: %.12f".format(it) }) {
            MeshComparator.compareVisually(localMesh, projectManager.selectedMesh)
        }
    }
}
