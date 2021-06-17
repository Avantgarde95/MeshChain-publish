@file:Suppress("ConstantConditionIf")

package com.github.avantgarde95.bc3d.manager

import com.github.avantgarde95.bc3d.common.SimpleURI
import com.github.avantgarde95.bc3d.common.Switch
import com.github.avantgarde95.bc3d.common.Util
import com.github.avantgarde95.bc3d.modeling.Mesh
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.io.File
import java.net.URL

class ToolManager(
        toolURI: SimpleURI
) {
    private val client = XmlRpcClient().apply {
        setConfig(XmlRpcClientConfigImpl().apply {
            serverURL = URL(toolURI.toString())
        })
    }

    fun getMeshFromTool(): Mesh {
        if (Switch.useModelingTool) {
            val meshJSON = client.execute("getMesh", emptyList<Any>()) as String
            return Util.fromJSON(meshJSON)
        } else {
            print("Path: ")
            val meshPath = readLine()!!.trim()
            return Mesh.fromOBJ(File(meshPath).readText())
        }
    }

    fun sendMeshToTool(mesh: Mesh) {
        if (Switch.useModelingTool) {
            val meshJSON = Util.toJSON(mesh)
            client.execute("updateMesh", listOf<Any>(meshJSON))
        }
    }
}
