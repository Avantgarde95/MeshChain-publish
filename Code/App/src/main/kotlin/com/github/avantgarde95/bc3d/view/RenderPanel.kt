package com.github.avantgarde95.bc3d.view

import com.github.avantgarde95.bc3d.modeling.Mesh
import com.github.avantgarde95.bc3d.render.DrawMode
import com.github.avantgarde95.bc3d.render.MeshRenderer
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*

class RenderPanel : JPanel() {
    private val renderer = MeshRenderer()

    /*
    private val alphaSlider = JSlider(0, 100, 100).apply {
        addChangeListener {
        }
    }
    */

    private val showRadioButtons = listOf(
            JRadioButton("Show surface").apply {
                addActionListener {
                    renderer.drawMode = DrawMode.Surface
                }
            },
            JRadioButton("Show wireframe").apply {
                addActionListener {
                    renderer.drawMode = DrawMode.Wireframe
                }
            },
            JRadioButton("Show all").apply {
                addActionListener {
                    renderer.drawMode = DrawMode.All
                }

                isSelected = true
            }
    )

    init {
        layout = BorderLayout()

        add(JPanel().apply {
            layout = GridLayout(1, 2)

            add(JLabel("Removed", JLabel.CENTER))
            add(JLabel("Added", JLabel.CENTER))
        }, BorderLayout.NORTH)

        add(JPanel().apply {
            alignmentX = 0.0f
            alignmentY = 0.0f
            layout = BorderLayout()

            add(renderer, BorderLayout.CENTER)
        }, BorderLayout.CENTER)

        /*
        add(JPanel().apply {
            layout = GridLayout(2, 1)

            add(alphaSlider)

            add(JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                val group = ButtonGroup()

                showRadioButtons.forEach {
                    add(it)
                    group.add(it)
                }
            })
        }, BorderLayout.SOUTH)
        */

        renderer.start()
    }

    fun setMeshes(commonMesh: Mesh, addedMesh: Mesh, removedMesh: Mesh) {
        renderer.setMeshes(commonMesh, addedMesh, removedMesh)
    }
}
