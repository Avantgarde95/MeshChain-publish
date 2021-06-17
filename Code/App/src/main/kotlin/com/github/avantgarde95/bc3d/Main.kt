package com.github.avantgarde95.bc3d

import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme
import com.github.avantgarde95.bc3d.common.Logger
import com.github.avantgarde95.bc3d.common.Util
import org.apache.log4j.BasicConfigurator
import java.awt.Font
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.UIManager
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    initLog4j()
    initSwing()
    hookException()

    App().show()
}

private fun initLog4j() {
    BasicConfigurator.configure()
}

private fun initSwing() {
    FlatCyanLightIJTheme.install()

    Font.createFont(
            Font.TRUETYPE_FONT,
            Util.getResourceAsStream("font/Roboto/Roboto-Bold.ttf")
    ).deriveFont(16f).run {
        UIManager.put("Button.font", this)
        UIManager.put("TabbedPane.font", this)
        UIManager.put("TitledBorder.font", this)
        UIManager.put("List.font", this)
        UIManager.put("Label.font", this)
        UIManager.put("TextField.font", this)
    }

    Font.createFont(
            Font.TRUETYPE_FONT,
            Util.getResourceAsStream("font/Roboto_Mono/RobotoMono-Bold.ttf")
    ).deriveFont(16f).run {
        UIManager.put("TextArea.font", this)
    }
}

private fun hookException() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        e.printStackTrace()

        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        Logger.addString(sw.toString())
    }
}
