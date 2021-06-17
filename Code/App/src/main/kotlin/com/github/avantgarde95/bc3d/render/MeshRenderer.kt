package com.github.avantgarde95.bc3d.render

import com.github.avantgarde95.bc3d.common.Util
import com.github.avantgarde95.bc3d.modeling.Mesh
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL3
import glm_.ToFloatBuffer
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

enum class DrawMode {
    Surface,
    Wireframe,
    All
}

enum class ShowMode {
    Added,
    Removed
}

enum class VertexType {
    Common,
    Added,
    Removed
}

class MeshRenderer : SimpleRenderer(), KeyListener {
    var drawMode = DrawMode.All

    private lateinit var meshVertexPositions: List<Vec3>
    private lateinit var meshVertexNormals: List<Vec3>
    private lateinit var meshVertexTypes: List<VertexType>
    private lateinit var meshFaceIndices: List<Vec3i>
    private var meshAABB = AABB(listOf(Vec3(0, 0, 0)))
    private var meshEye = Eye(Vec3(0), Vec3(0), Vec3(0))

    private var windowSize = Vec2i(2, 1)
    private var displaySize = Vec2i(1, 1)
    private var isMeshChanged = false
    private var moveMode = Vec3i(0, 0, 0) // ex. x = 0: Stop, 1: Right, -1: Left.
    private var rotateMode = Vec3i(0, 0, 0)
    private var alpha = 1.0f

    private lateinit var meshProgram: Program
    private lateinit var meshVAO: VAO
    private lateinit var meshVBO: VBO
    private lateinit var meshIBO: IBO

    private lateinit var displayProgram: Program
    private lateinit var displayFrameBuffer: FrameBuffer
    private lateinit var displayVAO: VAO
    private lateinit var displayVBO: VBO
    private lateinit var displayIBO: IBO

    init {
        addKeyListener(this)
        isFocusable = true
    }

    fun setMeshes(commonMesh: Mesh, addedMesh: Mesh, removedMesh: Mesh) {
        meshVertexPositions = (
                commonMesh.vertices.asSequence() +
                        addedMesh.vertices.asSequence() +
                        removedMesh.vertices.asSequence()
                ).toList()

        meshVertexNormals = (
                commonMesh.computeNormals().asSequence() +
                        addedMesh.computeNormals().asSequence() +
                        removedMesh.computeNormals().asSequence()
                ).toList()

        meshVertexTypes = (
                commonMesh.vertices.asSequence().map { VertexType.Common } +
                        addedMesh.vertices.asSequence().map { VertexType.Added } +
                        removedMesh.vertices.asSequence().map { VertexType.Removed }
                ).toList()

        meshFaceIndices = (
                commonMesh.faceIndices.asSequence() +
                        addedMesh.faceIndices.asSequence().map {
                            it + commonMesh.vertices.size
                        } +
                        removedMesh.faceIndices.asSequence().map {
                            it + (commonMesh.vertices.size + addedMesh.vertices.size)
                        }
                ).toList()

        isMeshChanged = true
    }

    override fun onStart(gl: GL3) {
        initGL(gl)
        initMeshObjects(gl)
        initDisplayObjects(gl)
    }

    override fun onFrame(gl: GL3) {
        if (isMeshChanged) {
            updateMeshVariables()
            updateMeshObjects(gl)
            isMeshChanged = false
        }

        moveMeshEye()
        rotateMeshEye()
        updateMeshUniforms(gl)
        clearScreen(gl, Vec4(0.9, 0.9, 0.9, 1.0))
        drawMeshOnDisplay(gl, ShowMode.Added)
        drawMeshOnDisplay(gl, ShowMode.Removed)
    }

    override fun onResize(gl: GL3, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        windowSize = Vec2i(width, height)
        displaySize = Vec2i(windowSize.x / 2, windowSize.y)
        //displayFrameBuffer.size = Vec2i(width, height)
    }

    override fun keyTyped(event: KeyEvent?) {
        // Do nothing.
    }

    override fun keyPressed(event: KeyEvent?) {
        if (event != null) {
            when (event.keyChar) {
                'w' -> {
                    moveMode.z = -1
                }
                's' -> {
                    moveMode.z = 1
                }
                'a' -> {
                    moveMode.x = -1
                }
                'd' -> {
                    moveMode.x = 1
                }
                'q' -> {
                    moveMode.y = 1
                }
                'e' -> {
                    moveMode.y = -1
                }
                'i' -> {
                    rotateMode.x = 1
                }
                'k' -> {
                    rotateMode.x = -1
                }
                'j' -> {
                    rotateMode.y = -1
                }
                'l' -> {
                    rotateMode.y = 1
                }
                'u' -> {
                    rotateMode.z = -1
                }
                'o' -> {
                    rotateMode.z = 1
                }
            }
        }
    }

    override fun keyReleased(event: KeyEvent?) {
        if (event != null) {
            when (event.keyChar) {
                'w', 's' -> {
                    moveMode.z = 0
                }
                'a', 'd' -> {
                    moveMode.x = 0
                }
                'q', 'e' -> {
                    moveMode.y = 0
                }
                'i', 'k' -> {
                    rotateMode.x = 0
                }
                'j', 'l' -> {
                    rotateMode.y = 0
                }
                'u', 'o' -> {
                    rotateMode.z = 0
                }
            }
        }
    }

    private fun initGL(gl: GL3) {
        gl.glEnable(GL.GL_DEPTH_TEST)
        gl.glDepthFunc(GL.GL_LESS)

        gl.glDisable(GL.GL_CULL_FACE)

        //gl.glEnable(GL.GL_BLEND)
        //gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)

        gl.glEnable(GL.GL_LINE_SMOOTH)
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE)

        gl.glEnable(GL3.GL_POLYGON_SMOOTH)
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST)
    }

    private fun initMeshObjects(gl: GL3) {
        meshProgram = Program(
                gl,
                Shader(gl, GL3.GL_VERTEX_SHADER, Util.getResourceAsString("shader/Mesh.vert")),
                Shader(gl, GL3.GL_FRAGMENT_SHADER, Util.getResourceAsString("shader/Mesh.frag"))
        )

        meshVAO = VAO(gl)
        meshVBO = VBO(gl)
        meshIBO = IBO(gl)

        meshVAO.bind(gl)

        meshVBO.run {
            bind(gl)
            setLayout(gl, listOf(3, 3, 3))
            setData(gl, emptyList())
        }

        meshIBO.run {
            bind(gl)
            setData(gl, emptyList())
        }
    }

    private fun initDisplayObjects(gl: GL3) {
        displayProgram = Program(
                gl,
                Shader(gl, GL3.GL_VERTEX_SHADER, Util.getResourceAsString("shader/Display.vert")),
                Shader(gl, GL3.GL_FRAGMENT_SHADER, Util.getResourceAsString("shader/Display.frag"))
        )

        displayFrameBuffer = FrameBuffer(gl, Vec2i(512, 512))
        displayProgram.use(gl)
        displayProgram.setUniform(gl, "u_texture", displayFrameBuffer.colorTexture.unit)

        displayVAO = VAO(gl)
        displayVBO = VBO(gl)
        displayIBO = IBO(gl)

        displayVAO.bind(gl)

        displayVBO.run {
            bind(gl)
            setLayout(gl, listOf(2, 2))

            setData(gl, listOf(
                    Vec2(-1, -1), Vec2(0, 0),
                    Vec2(0, -1), Vec2(1, 0),
                    Vec2(-1, 1), Vec2(0, 1),
                    Vec2(0, 1), Vec2(1, 1)
            ))
        }

        displayIBO.run {
            bind(gl)
            setData(gl, listOf(Vec3i(0, 1, 2), Vec3i(2, 1, 3)))
        }
    }

    private fun updateMeshVariables() {
        meshAABB = AABB(meshVertexPositions)

        meshEye = Eye(
                position = meshAABB.center + Vec3(0, 0, meshAABB.maxExtent),
                center = meshAABB.center,
                up = Vec3(0, 1, 0)
        )
    }

    private fun updateMeshObjects(gl: GL3) {
        meshVAO.bind(gl)

        meshVBO.run {
            bind(gl)
            val data = mutableListOf<ToFloatBuffer>()

            for (i in meshVertexPositions.indices) {
                data.add(meshVertexPositions[i])
                data.add(meshVertexNormals[i])
                data.add(Vec3(meshVertexTypes[i].ordinal, 0, 0))
            }

            setData(gl, data)
        }

        meshIBO.run {
            bind(gl)
            setData(gl, meshFaceIndices)
        }
    }

    private fun moveMeshEye() {
        val speed = meshAABB.minExtent * 0.01f

        when (moveMode.x) {
            1 -> meshEye.moveX(speed)
            -1 -> meshEye.moveX(-speed)
        }

        when (moveMode.y) {
            1 -> meshEye.moveY(speed)
            -1 -> meshEye.moveY(-speed)
        }

        when (moveMode.z) {
            1 -> meshEye.moveZ(-speed)
            -1 -> meshEye.moveZ(speed)
        }
    }

    private fun rotateMeshEye() {
        val speed = glm.radians(1.0f)

        when (rotateMode.x) {
            1 -> meshEye.rotateX(speed)
            -1 -> meshEye.rotateX(-speed)
        }

        when (rotateMode.y) {
            1 -> meshEye.rotateY(speed)
            -1 -> meshEye.rotateY(-speed)
        }

        when (rotateMode.z) {
            1 -> meshEye.rotateZ(speed)
            -1 -> meshEye.rotateZ(-speed)
        }
    }

    private fun updateMeshUniforms(gl: GL3) {
        val modelMatrix = Mat4(1.0f)
        val viewMatrix = glm.lookAt(meshEye.position, meshEye.center, meshEye.up)
        val normalModelMatrix = glm.inverseTranspose(modelMatrix)
        val normalViewMatrix = glm.inverseTranspose(viewMatrix)

        val projectionMatrix = glm.perspective(
                glm.radians(90.0f),
                //windowSize.x.toFloat() / windowSize.y.toFloat(),
                1.0f,
                meshAABB.minExtent * 0.04f,
                meshAABB.maxExtent * 100.0f
        )

        meshProgram.run {
            use(gl)
            setUniform(gl, "u_transformation.modelMatrix", modelMatrix)
            setUniform(gl, "u_transformation.viewMatrix", viewMatrix)
            setUniform(gl, "u_transformation.projectionMatrix", projectionMatrix)
            setUniform(gl, "u_normalTransformation.modelMatrix", normalModelMatrix)
            setUniform(gl, "u_normalTransformation.viewMatrix", normalViewMatrix)

            setUniform(gl, "u_eye.position", meshEye.position)
            setUniform(gl, "u_light.position", meshEye.position)
            setUniform(gl, "u_light.ambient", Vec3(0.2f, 0.2f, 0.2f))
            setUniform(gl, "u_light.diffuse", Vec3(1.0f, 1.0f, 1.0f))
        }
    }

    private fun drawMeshOnDisplay(gl: GL3, showMode: ShowMode) {
        meshProgram.use(gl)
        meshProgram.setUniform(gl, "u_showMode", showMode.ordinal)
        displayFrameBuffer.bind(gl)
        clearScreen(gl, Vec4(0.2, 0.2, 0.2, 1.0))
        drawMesh(gl)
        displayFrameBuffer.unbind(gl)

        displayProgram.use(gl)
        displayProgram.setUniform(gl, "u_showMode", showMode.ordinal)
        drawDisplay(gl)
    }

    private fun drawMesh(gl: GL3) {
        if (displaySize.y > displaySize.x) {
            gl.glViewport(0, 0, 512 * displaySize.y / displaySize.x, 512)
        } else {
            gl.glViewport(0, 0, 512, 512 * displaySize.x / displaySize.y)
        }

        meshProgram.use(gl)
        meshVAO.bind(gl)

        when (drawMode) {
            DrawMode.Surface -> {
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_FILL)
                meshProgram.setUniform(gl, "u_enableShading", true)
                meshIBO.draw(gl)
            }
            DrawMode.Wireframe -> {
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE)
                meshProgram.setUniform(gl, "u_enableShading", false)
                meshIBO.draw(gl)
            }
            DrawMode.All -> {
                // Let the wireframe be in front of the surface.
                gl.glPolygonOffset(1.0f, 1.0f)

                // Draw the surface.
                gl.glEnable(GL.GL_POLYGON_OFFSET_FILL)
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_FILL)
                meshProgram.setUniform(gl, "u_enableShading", true)
                meshIBO.draw(gl)

                // Draw the wireframe.
                gl.glDisable(GL.GL_POLYGON_OFFSET_FILL)
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_LINE)
                meshProgram.setUniform(gl, "u_enableShading", false)
                meshIBO.draw(gl)
            }
        }
    }

    private fun drawDisplay(gl: GL3) {
        gl.glViewport(0, 0, windowSize.x, windowSize.y)
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL3.GL_FILL)
        displayProgram.use(gl)
        displayVAO.bind(gl)
        displayIBO.draw(gl)
    }

    private fun clearScreen(gl: GL3, color: Vec4) {
        gl.glClearColor(color.r, color.g, color.b, color.a)
        gl.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
    }
}
