package com.github.avantgarde95.bc3d.common

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import glm_.mat3x3.Mat3
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import java.io.File
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

object Util {
    val klaxon = Klaxon()
            .converter(object : Converter {
                override fun canConvert(cls: Class<*>) = cls == Vec3::class.java

                override fun toJson(value: Any): String {
                    val vector = value as Vec3
                    return """{"x": ${vector.x}, "y": ${vector.y}, "z": ${vector.z}}"""
                }

                @Suppress("UNCHECKED_CAST")
                override fun fromJson(jv: JsonValue) = Vec3(
                        (jv.obj!!.map["x"] as Double).toFloat(),
                        (jv.obj!!.map["y"] as Double).toFloat(),
                        (jv.obj!!.map["z"] as Double).toFloat()
                )
            })
            .converter(object : Converter {
                override fun canConvert(cls: Class<*>) = cls == Vec3i::class.java

                override fun toJson(value: Any): String {
                    val vector = value as Vec3i
                    return """{"x": ${vector.x}, "y": ${vector.y}, "z": ${vector.z}}"""
                }

                @Suppress("UNCHECKED_CAST")
                override fun fromJson(jv: JsonValue) = Vec3i(
                        jv.obj!!.map["x"] as Int,
                        jv.obj!!.map["y"] as Int,
                        jv.obj!!.map["z"] as Int
                )
            })
            .converter(object : Converter {
                override fun canConvert(cls: Class<*>) = cls == Mat3::class.java

                override fun toJson(value: Any): String {
                    val matrix = value as Mat3

                    val vectorJSONs = (0..2).map {
                        """{"x": ${matrix[it].x}, "y": ${matrix[it].y}, "z": ${matrix[it].z}}"""
                    }

                    return "[${vectorJSONs.joinToString(", ")}]"
                }

                @Suppress("UNCHECKED_CAST")
                override fun fromJson(jv: JsonValue): Mat3 {
                    val vectors = (0..2)
                            .map { jv.array!![it] as JsonObject }
                            .map {
                                Vec3(
                                        (it.map["x"] as Double).toFloat(),
                                        (it.map["y"] as Double).toFloat(),
                                        (it.map["z"] as Double).toFloat()
                                )
                            }

                    return Mat3(vectors[0], vectors[1], vectors[2])
                }
            })

    fun toJSON(value: Any) = klaxon.toJsonString(value)

    inline fun <reified T> fromJSON(json: String) = klaxon.parse<T>(json)!!

    fun getResourceAsStream(path: String) =
            Thread.currentThread().contextClassLoader.getResourceAsStream(path)!!

    fun getResourceAsString(path: String) =
            getResourceAsStream(path).bufferedReader().use { it.readText() }

    fun timestamp() = System.currentTimeMillis()

    fun date(timestamp: Long) = Date(Timestamp(timestamp).time)

    fun isWindows() = System.getProperty("os.name").startsWith("Windows")

    fun <T> measureAndLog(
            getMessage: (T) -> String = { "" },
            performTask: () -> T
    ): T {
        var result: T? = null

        val elapsedTime = measureTimeMillis {
            result = performTask()
        } / 1000.0f

        Logger.addString("${getMessage(result!!)} [${elapsedTime}s]")

        return result!!
    }

    fun runCommand(command: String, workingPath: String = ".") {
        ProcessBuilder(command.split("""\s""".toRegex()))
                .directory(File(workingPath))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.MINUTES)
    }
}
