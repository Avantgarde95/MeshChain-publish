package com.github.avantgarde95.bc3d.view

import com.github.avantgarde95.bc3d.common.SimpleEvent
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane

class LogPanel : JPanel() {
    private val logTextArea = PlaceholderTextArea("Program log").apply {
        lineWrap = true
        isEditable = false
    }

    private val clearButton = JButton("Clear").apply {
        addActionListener {
            clearEvent.fire(Unit)
        }
    }

    private val saveButton = JButton("Save").apply {
        addActionListener {
            saveEvent.fire(Unit)
        }
    }

    val clearEvent = SimpleEvent<Unit>()
    val saveEvent = SimpleEvent<Unit>()

    fun addLog(string: String) {
        logTextArea.append(string)
    }

    fun clearLog() {
        logTextArea.text = ""
    }

    init {
        layout = BorderLayout()

        add(JScrollPane(logTextArea), BorderLayout.CENTER)

        add(JPanel().apply {
            layout = GridLayout(1, 2)

            add(clearButton)
            add(saveButton)
        }, BorderLayout.SOUTH)
    }
}
