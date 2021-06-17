package com.github.avantgarde95.bc3d.view

import com.github.avantgarde95.bc3d.common.Configuration
import com.github.avantgarde95.bc3d.common.SimpleEvent
import com.github.avantgarde95.bc3d.common.Util
import java.awt.BorderLayout
import java.io.File
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane

class ConfigurePanel : JPanel() {
    private val inputTextArea = PlaceholderTextArea("Configuration (in JSON)").apply {
        lineWrap = true
        text = File("Configuration.json").readText()
    }

    private val runButton = JButton("Run").apply {
        addActionListener {
            val configuration = Util.fromJSON<Configuration>(inputTextArea.text)
            applyEvent.fire(configuration)

            if (isFirstRun) {
                text = "Re-run"
                isFirstRun = false
            }
        }
    }

    val applyEvent = SimpleEvent<Configuration>()

    private var isFirstRun = true

    init {
        layout = BorderLayout()

        add(JScrollPane(inputTextArea), BorderLayout.CENTER)
        add(runButton, BorderLayout.SOUTH)
    }
}
