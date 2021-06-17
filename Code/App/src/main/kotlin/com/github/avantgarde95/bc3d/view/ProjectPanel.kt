package com.github.avantgarde95.bc3d.view

import com.github.avantgarde95.bc3d.PlaceholderTextField
import com.github.avantgarde95.bc3d.common.*
import com.github.avantgarde95.bc3d.modeling.Mesh
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class ProjectPanel : JPanel() {
    private val nameTextField = PlaceholderTextField("Project name")
    private val incentiveTextField = PlaceholderTextField("Commit incentive supply")
    private val keywordTextField = PlaceholderTextField("Keywords (Tags)")

    private val createButton = JButton("Create").apply {
        addActionListener {
            createEvent.fire(Triple(
                    nameTextField.text,
                    keywordTextField.text,
                    incentiveTextField.text.toFloat()
            ))
        }
    }

    private val loadButton = JButton("Load").apply {
        addActionListener {
            loadEvent.fire(nameTextField.text)
        }
    }

    private val commitAddressesPanel = JPanel().apply {
        layout = GridLayout(0, 1)
    }

    private val infoTextArea = PlaceholderTextArea("Commit information").apply {
        isEditable = false
        rows = 7
    }

    private val renderPanel = RenderPanel()

    private val commitButton = JButton("Commit the 3D model").apply {
        addActionListener {
            commitEvent.fire(Unit)
        }
    }

    private val checkoutButton = JButton("Checkout this version").apply {
        addActionListener {
            checkoutEvent.fire(Unit)
        }
    }

    private val comparePathTextField = PlaceholderTextField("Model's path to compare")

    private val compareButton = JButton("Compare the model").apply {
        addActionListener {
            compareEvent.fire(comparePathTextField.text)
        }
    }

    val createEvent = SimpleEvent<Triple<String, String, Float>>()
    val loadEvent = SimpleEvent<String>()
    val selectEvent = SimpleEvent<String>()
    val commitEvent = SimpleEvent<Unit>()
    val checkoutEvent = SimpleEvent<Unit>()
    val compareEvent = SimpleEvent<String>()

    init {
        layout = BorderLayout()

        add(JPanel().apply {
            layout = BorderLayout()

            add(JPanel().apply {
                layout = GridLayout(1, 3)

                add(nameTextField)
                add(incentiveTextField)
                add(keywordTextField)
            }, BorderLayout.CENTER)

            add(JPanel().apply {
                layout = GridLayout(1, 2)

                add(createButton)
                add(loadButton)
            }, BorderLayout.EAST)
        }, BorderLayout.NORTH)

        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply {
            add(JPanel().apply {
                layout = BorderLayout()

                add(JScrollPane(JPanel().apply {
                    layout = BorderLayout()

                    add(commitAddressesPanel, BorderLayout.NORTH)
                }), BorderLayout.CENTER)

                add(commitButton, BorderLayout.SOUTH)
            }, JSplitPane.LEFT)

            add(JPanel().apply {
                layout = BorderLayout()

                add(JScrollPane(infoTextArea), BorderLayout.NORTH)
                add(JScrollPane(renderPanel), BorderLayout.CENTER)

                add(JPanel().apply {
                    layout = GridLayout(2, 1)

                    add(checkoutButton)

                    add(JPanel().apply {
                        layout = GridLayout(1, 2)

                        add(comparePathTextField)
                        add(compareButton)
                    })
                }, BorderLayout.SOUTH)
            }, JSplitPane.RIGHT)

            dividerLocation = 400
        }, BorderLayout.CENTER)
    }

    fun setProject(project: Project) {
        updateCommitAddressesPanel(project.commitAddresses)
    }

    fun showCommitInformation(address: String, commit: Commit) {
        val commitAddressString = when {
            Switch.useStorage -> address
            else -> "I hate blockchain."
        }

        val previousCommitAddressString = when {
            Switch.useStorage -> commit.previousCommitAddress
            else -> "Blockchain = Decentralized useless shit"
        }

        infoTextArea.text = """
            |Commit $commitAddressString
            |- Previous commit: $previousCommitAddressString
            |- Author: ${commit.authorAddress}
            |- Time: ${Util.date(commit.timestamp)}
            |- Added: ${commit.meshDelta.addedFaces.size} triangles
            |- Removed: ${commit.meshDelta.removedFaces.size} triangles
        """.trimMargin()
    }

    fun drawMesh(commonMesh: Mesh, addedMesh: Mesh, removedMesh: Mesh) {
        renderPanel.setMeshes(commonMesh, addedMesh, removedMesh)
    }

    private fun updateCommitAddressesPanel(addresses: List<String>) {
        commitAddressesPanel.removeAll()

        addresses.forEach { address ->
            commitAddressesPanel.add(JButton("Commit $address").apply {
                horizontalAlignment = SwingConstants.LEFT

                addActionListener {
                    selectEvent.fire(address)
                }
            })
        }

        commitAddressesPanel.revalidate()
        commitAddressesPanel.repaint()
    }
}
