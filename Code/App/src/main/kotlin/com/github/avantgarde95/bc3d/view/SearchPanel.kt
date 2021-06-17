package com.github.avantgarde95.bc3d.view

import com.github.avantgarde95.bc3d.PlaceholderTextField
import com.github.avantgarde95.bc3d.common.SimpleEvent
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.EtchedBorder

class SearchPanel : JPanel() {
    private val keywordTextField = PlaceholderTextField("Search by keywords (ex. \"Tower, Simple, Military\")")

    private val searchButton = JButton("Search").apply {
        addActionListener {
            searchEvent.fire(keywordTextField.text)
        }
    }

    private val projectInfosPanel = JPanel().apply {
        layout = GridLayout(0, 1)
    }

    val searchEvent = SimpleEvent<String>()

    init {
        layout = BorderLayout()

        add(JPanel().apply {
            layout = BorderLayout()

            add(keywordTextField, BorderLayout.CENTER)
            add(searchButton, BorderLayout.EAST)
        }, BorderLayout.NORTH)

        add(JScrollPane(JPanel().apply {
            layout = BorderLayout()

            add(projectInfosPanel, BorderLayout.NORTH)
        }), BorderLayout.CENTER)
    }

    fun setProjectInfosPanel(infos: List<Pair<String, String>>) {
        projectInfosPanel.removeAll()

        infos.forEach { (name, keyword) ->
            projectInfosPanel.add(JPanel().apply {
                layout = BorderLayout()
                border = BorderFactory.createEtchedBorder(EtchedBorder.RAISED)

                add(JLabel("Project $name (Keyword: $keyword)").apply {
                    horizontalAlignment = SwingConstants.LEFT
                    border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                }, BorderLayout.CENTER)
            })
        }

        projectInfosPanel.revalidate()
        projectInfosPanel.repaint()
    }
}
