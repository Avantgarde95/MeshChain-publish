package com.github.avantgarde95.bc3d

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextField

class PlaceholderTextField(
        var placeholder: String
) : JTextField() {
    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        if (placeholder.isEmpty() || text.isNotEmpty()) {
            return
        }

        (g as Graphics2D).apply {
            setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            )

            color = disabledTextColor

            drawString(
                    placeholder,
                    insets.left,
                    fontMetrics.maxAscent + insets.top
            )
        }
    }
}
